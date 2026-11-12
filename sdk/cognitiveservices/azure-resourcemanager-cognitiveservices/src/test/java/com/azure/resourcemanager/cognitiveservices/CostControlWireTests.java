// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cognitiveservices;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.resourcemanager.cognitiveservices.models.AccountCostControl;
import com.azure.resourcemanager.cognitiveservices.models.CostControlAction;
import com.azure.resourcemanager.cognitiveservices.models.CostControlActionPatch;
import com.azure.resourcemanager.cognitiveservices.models.CostControlCategory;
import com.azure.resourcemanager.cognitiveservices.models.CostControlNotification;
import com.azure.resourcemanager.cognitiveservices.models.CostControlNotificationPatch;
import com.azure.resourcemanager.cognitiveservices.models.CostControlPatchProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlProviderProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlProviderPropertiesPatch;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThresholdType;
import com.azure.resourcemanager.cognitiveservices.models.CostControlTimeGrain;
import com.azure.resourcemanager.cognitiveservices.models.CostControlTimePeriod;
import com.azure.resourcemanager.cognitiveservices.models.ProjectCostControl;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

public final class CostControlWireTests {
    private static final String SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    private static final String GROUP = "test-resource-group";
    private static final String ACCOUNT = "test-account";
    private static final String PROJECT = "test-project";
    private static final String NAME = "test-budget";
    private static final OffsetDateTime START = OffsetDateTime.parse("2026-11-15T00:00:00Z");
    private static final OffsetDateTime END = OffsetDateTime.parse("2027-11-15T00:00:00Z");

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void createsBudgetWithNotificationsAndBlockingAction(boolean project) {
        QueueHttpClient client = new QueueHttpClient(Collections.singletonList(response(201, resource(project, 25))));
        CognitiveServicesManager manager = manager(client);
        CostControlProperties properties = properties(25)
            .withNotifications(Collections.singletonMap("usageAlert",
                new CostControlNotification().withEventGridConnectionId(accountId() + "/connections/budget-alerts")
                    .withThresholdType(CostControlThresholdType.PERCENTAGE)
                    .withThreshold(80)
                    .withEnabled(true)))
            .withProviderProperties(new CostControlProviderProperties().withActions(Collections.singletonMap(
                "stopRequests",
                new CostControlAction().withThresholdType(CostControlThresholdType.PERCENTAGE).withThreshold(100))));
        CostControlProperties result;
        if (project) {
            result = manager.projectCostControls()
                .define(NAME)
                .withExistingProject(GROUP, ACCOUNT, PROJECT)
                .withProperties(properties)
                .withIfNoneMatch("*")
                .create()
                .properties();
        } else {
            result = manager.accountCostControls()
                .define(NAME)
                .withExistingAccount(GROUP, ACCOUNT)
                .withProperties(properties)
                .withIfNoneMatch("*")
                .create()
                .properties();
        }
        Assertions.assertEquals(25, result.amount());
        assertRequest(client.requests.get(0), "PUT", resourceId(project));
        Assertions.assertEquals("*", client.requests.get(0).getHeaders().getValue("if-none-match"));
        Map<?, ?> body = bodyProperties(client.requests.get(0));
        Assertions.assertEquals("Cost", body.get("category"));
        Assertions.assertEquals(25.0, ((Number) body.get("amount")).doubleValue());
        Assertions.assertEquals("Monthly", body.get("timeGrain"));
        Map<?, ?> period = (Map<?, ?>) body.get("timePeriod");
        Assertions.assertEquals(START, OffsetDateTime.parse((String) period.get("startDate")));
        Assertions.assertEquals(END, OffsetDateTime.parse((String) period.get("endDate")));
        Map<?, ?> notification = (Map<?, ?>) ((Map<?, ?>) body.get("notifications")).get("usageAlert");
        Assertions.assertEquals(accountId() + "/connections/budget-alerts", notification.get("eventGridConnectionId"));
        Assertions.assertEquals("Percentage", notification.get("thresholdType"));
        Assertions.assertEquals(80.0, ((Number) notification.get("threshold")).doubleValue());
        Assertions.assertEquals(true, notification.get("enabled"));
        Map<?, ?> action
            = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) body.get("providerProperties")).get("actions")).get("stopRequests");
        Assertions.assertEquals("Block", action.get("type"));
        Assertions.assertEquals("Percentage", action.get("thresholdType"));
        Assertions.assertEquals("GreaterThanOrEqualTo", action.get("operator"));
        Assertions.assertEquals(100.0, ((Number) action.get("threshold")).doubleValue());
        Assertions.assertFalse(body.containsKey("rules"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void getsBudgetAndFollowsListNextLink(boolean project) {
        QueueHttpClient client = new QueueHttpClient(Arrays.asList(response(200, resource(project, 25)),
            request -> response(200,
                "{\"value\":[" + resource(project, 25) + "],\"nextLink\":\"" + request.getUrl() + "&$skiptoken=next\"}")
                    .apply(request),
            response(200, "{\"value\":[" + resource(project, 50) + "]}")));
        CognitiveServicesManager manager = manager(client);
        CostControlProperties fetched = project
            ? manager.projectCostControls().get(GROUP, ACCOUNT, PROJECT, NAME).properties()
            : manager.accountCostControls().get(GROUP, ACCOUNT, NAME).properties();
        Assertions.assertEquals(CostControlCategory.COST, fetched.category());
        Assertions.assertEquals(CostControlTimeGrain.MONTHLY, fetched.timeGrain());
        Assertions.assertEquals(START, fetched.timePeriod().startDate());
        Assertions.assertEquals(END, fetched.timePeriod().endDate());
        CostControlNotification notification = fetched.notifications().get("usageAlert");
        Assertions.assertEquals(accountId() + "/connections/budget-alerts", notification.eventGridConnectionId());
        Assertions.assertEquals(CostControlThresholdType.PERCENTAGE, notification.thresholdType());
        Assertions.assertEquals(80.0, notification.threshold());
        Assertions.assertTrue(notification.enabled());
        CostControlAction action = fetched.providerProperties().actions().get("stopRequests");
        Assertions.assertEquals("Block", action.type());
        Assertions.assertEquals(CostControlThresholdType.PERCENTAGE, action.thresholdType());
        Assertions.assertEquals(100.0, action.threshold());
        Assertions.assertEquals("GreaterThanOrEqualTo", action.operator());
        List<Double> amounts = project
            ? manager.projectCostControls()
                .list(GROUP, ACCOUNT, PROJECT)
                .stream()
                .map(item -> item.properties().amount())
                .collect(Collectors.toList())
            : manager.accountCostControls()
                .list(GROUP, ACCOUNT)
                .stream()
                .map(item -> item.properties().amount())
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(25.0, 50.0), amounts);
        assertRequest(client.requests.get(0), "GET", resourceId(project));
        assertRequest(client.requests.get(1), "GET", ownerId(project) + "/costControls");
        Assertions.assertEquals(client.requests.get(1).getUrl() + "&$skiptoken=next",
            client.requests.get(2).getUrl().toString());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void patchesNamedSettingsWithIfMatch(boolean project) {
        QueueHttpClient client = new QueueHttpClient(
            Arrays.asList(response(200, resource(project, 25)), response(200, resource(project, 50))));
        CognitiveServicesManager manager = manager(client);
        CostControlPatchProperties patch = new CostControlPatchProperties().withAmount(50.0)
            .withGrouping(Collections.emptyList())
            .withNotifications(
                Collections.singletonMap("usageAlert", new CostControlNotificationPatch().withEnabled(false)))
            .withProviderProperties(new CostControlProviderPropertiesPatch().withActions(
                Collections.singletonMap("stopRequests", new CostControlActionPatch().withThreshold(90.0))));
        CostControlProperties updated;
        if (project) {
            ProjectCostControl fetched = manager.projectCostControls().get(GROUP, ACCOUNT, PROJECT, NAME);
            updated = fetched.update().withProperties(patch).withIfMatch(fetched.etag()).apply().properties();
        } else {
            AccountCostControl fetched = manager.accountCostControls().get(GROUP, ACCOUNT, NAME);
            updated = fetched.update().withProperties(patch).withIfMatch(fetched.etag()).apply().properties();
        }
        Assertions.assertEquals(50, updated.amount());
        HttpRequest request = client.requests.get(1);
        assertRequest(request, "PATCH", resourceId(project));
        Assertions.assertEquals("\"etag-1\"", request.getHeaders().getValue("if-match"));
        Map<?, ?> body = bodyProperties(request);
        Assertions.assertEquals(50.0, ((Number) body.get("amount")).doubleValue());
        Assertions.assertEquals(Collections.emptyList(), body.get("grouping"));
        Assertions.assertFalse(body.containsKey("category"));
        Assertions.assertFalse(body.containsKey("timePeriod"));
        Assertions.assertEquals(Collections.singletonMap("enabled", false),
            ((Map<?, ?>) body.get("notifications")).get("usageAlert"));
        Map<?, ?> action
            = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) body.get("providerProperties")).get("actions")).get("stopRequests");
        Assertions.assertEquals(90.0, ((Number) action.get("threshold")).doubleValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void preservesNullMapEntriesForRemovingNamedSettings(boolean project) {
        QueueHttpClient client = new QueueHttpClient(
            Arrays.asList(response(200, resource(project, 25)), response(200, resource(project, 25))));
        CognitiveServicesManager manager = manager(client);
        CostControlPatchProperties patch = new CostControlPatchProperties()
            .withNotifications(Collections.singletonMap("usageAlert", null))
            .withProviderProperties(
                new CostControlProviderPropertiesPatch().withActions(Collections.singletonMap("stopRequests", null)));
        if (project) {
            manager.projectCostControls().get(GROUP, ACCOUNT, PROJECT, NAME).update().withProperties(patch).apply();
        } else {
            manager.accountCostControls().get(GROUP, ACCOUNT, NAME).update().withProperties(patch).apply();
        }
        Map<?, ?> body = bodyProperties(client.requests.get(1));
        Assertions.assertEquals(Collections.singletonMap("usageAlert", null), body.get("notifications"));
        Assertions.assertEquals(Collections.singletonMap("stopRequests", null),
            ((Map<?, ?>) body.get("providerProperties")).get("actions"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void checksExistenceDeletesConditionallyAndSurfacesNotFound(boolean project) {
        QueueHttpClient client = new QueueHttpClient(Arrays.asList(response(200, ""), response(204, ""),
            response(404, "{\"error\":{\"code\":\"ResourceNotFound\",\"message\":\"Budget not found.\"}}")));
        CognitiveServicesManager manager = manager(client);
        ManagementException error;
        if (project) {
            manager.projectCostControls().checkExistence(GROUP, ACCOUNT, PROJECT, NAME);
            manager.projectCostControls().deleteWithResponse(GROUP, ACCOUNT, PROJECT, NAME, "\"etag-1\"", Context.NONE);
            error = Assertions.assertThrows(ManagementException.class,
                () -> manager.projectCostControls().get(GROUP, ACCOUNT, PROJECT, NAME));
        } else {
            manager.accountCostControls().checkExistence(GROUP, ACCOUNT, NAME);
            manager.accountCostControls().deleteWithResponse(GROUP, ACCOUNT, NAME, "\"etag-1\"", Context.NONE);
            error = Assertions.assertThrows(ManagementException.class,
                () -> manager.accountCostControls().get(GROUP, ACCOUNT, NAME));
        }
        assertRequest(client.requests.get(0), "HEAD", resourceId(project));
        assertRequest(client.requests.get(1), "DELETE", resourceId(project));
        Assertions.assertEquals("\"etag-1\"", client.requests.get(1).getHeaders().getValue("if-match"));
        Assertions.assertEquals(404, error.getResponse().getStatusCode());
    }

    private static CostControlProperties properties(double amount) {
        return new CostControlProperties().withCategory(CostControlCategory.COST)
            .withAmount(amount)
            .withTimeGrain(CostControlTimeGrain.MONTHLY)
            .withTimePeriod(new CostControlTimePeriod().withStartDate(START).withEndDate(END));
    }

    private static String accountId() {
        return "/subscriptions/" + SUBSCRIPTION + "/resourceGroups/" + GROUP
            + "/providers/Microsoft.CognitiveServices/accounts/" + ACCOUNT;
    }

    private static String ownerId(boolean project) {
        return accountId() + (project ? "/projects/" + PROJECT : "");
    }

    private static String resourceId(boolean project) {
        return ownerId(project) + "/costControls/" + NAME;
    }

    private static String resource(boolean project, double amount) {
        return "{\"id\":\"" + resourceId(project) + "\",\"name\":\"" + NAME
            + "\",\"type\":\"Microsoft.CognitiveServices/accounts/" + (project ? "projects/" : "")
            + "costControls\",\"etag\":\"\\\"etag-1\\\"\",\"properties\":{\"provider\":\"Microsoft.CognitiveServices\","
            + "\"category\":\"Cost\",\"amount\":" + amount + ",\"timeGrain\":\"Monthly\","
            + "\"timePeriod\":{\"startDate\":\"" + START + "\",\"endDate\":\"" + END + "\"},"
            + "\"notifications\":{\"usageAlert\":{\"eventGridConnectionId\":\"" + accountId()
            + "/connections/budget-alerts\",\"thresholdType\":\"Percentage\",\"threshold\":80,\"enabled\":true}},"
            + "\"providerProperties\":{\"actions\":{\"stopRequests\":{\"type\":\"Block\","
            + "\"thresholdType\":\"Percentage\",\"threshold\":100,\"operator\":\"GreaterThanOrEqualTo\"}}}}}";
    }

    private static Map<?, ?> bodyProperties(HttpRequest request) {
        return (Map<?, ?>) request.getBodyAsBinaryData().toObject(Map.class).get("properties");
    }

    private static void assertRequest(HttpRequest request, String method, String path) {
        Assertions.assertEquals(method, request.getHttpMethod().toString());
        Assertions.assertEquals(path, request.getUrl().getPath());
        Assertions.assertEquals("api-version=2026-11-15-preview", request.getUrl().getQuery());
    }

    private static CognitiveServicesManager manager(HttpClient client) {
        return CognitiveServicesManager.configure()
            .withHttpClient(client)
            .authenticate(context -> Mono.just(new AccessToken("unit-test-token", OffsetDateTime.MAX)),
                new AzureProfile("", SUBSCRIPTION, AzureCloud.AZURE_PUBLIC_CLOUD));
    }

    private static Function<HttpRequest, HttpResponse> response(int status, String body) {
        return request -> new MockHttpResponse(request, status,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
            body.getBytes(StandardCharsets.UTF_8));
    }

    private static final class QueueHttpClient implements HttpClient {
        private final Deque<Function<HttpRequest, HttpResponse>> responses;
        private final List<HttpRequest> requests = new ArrayList<>();

        private QueueHttpClient(List<Function<HttpRequest, HttpResponse>> responses) {
            this.responses = new ArrayDeque<>(responses);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            requests.add(request);
            Assertions.assertFalse(responses.isEmpty(), "Unexpected HTTP request: " + request.getUrl());
            return Mono.just(responses.removeFirst().apply(request));
        }
    }
}

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
import com.azure.resourcemanager.cognitiveservices.models.CostControl;
import com.azure.resourcemanager.cognitiveservices.models.CostControlDimension;
import com.azure.resourcemanager.cognitiveservices.models.CostControlDimensionType;
import com.azure.resourcemanager.cognitiveservices.models.CostControlPatchProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlPeriod;
import com.azure.resourcemanager.cognitiveservices.models.CostControlProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlRule;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThreshold;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThresholdAction;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThresholdType;
import com.azure.resourcemanager.cognitiveservices.models.CostControlUnit;
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
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class CostControlWireTests {
    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String RESOURCE_GROUP = "test-resource-group";
    private static final String ACCOUNT_NAME = "test-account";
    private static final String COST_CONTROL_NAME = "test-cost-control";
    private static final String RESOURCE_ID = "/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/" + RESOURCE_GROUP
        + "/providers/Microsoft.CognitiveServices/accounts/" + ACCOUNT_NAME + "/costControls/" + COST_CONTROL_NAME;

    @Test
    public void createsWithRuleThresholdAndIfNoneMatch() {
        String created = response("Cost Control", 25, "\"etag-1\"");
        QueueHttpClient httpClient = new QueueHttpClient(Collections.singletonList(response(201, created)));
        CognitiveServicesManager manager = createManager(httpClient);
        CostControl createdCostControl = manager.costControls()
            .define(COST_CONTROL_NAME)
            .withExistingAccount(RESOURCE_GROUP, ACCOUNT_NAME)
            .withProperties(createProperties("Cost Control", 25))
            .withIfNoneMatch("*")
            .create();

        Assertions.assertEquals("etag-1", createdCostControl.etag());
        HttpRequest createRequest = httpClient.requests().get(0);
        Assertions.assertEquals("PUT", createRequest.getHttpMethod().toString());
        Assertions.assertTrue(createRequest.getUrl().getPath().endsWith(RESOURCE_ID));
        assertApiVersion(createRequest);
        Assertions.assertEquals("*", createRequest.getHeaders().getValue("if-none-match"));

        Map<?, ?> createBody = createRequest.getBodyAsBinaryData().toObject(Map.class);
        Map<?, ?> properties = (Map<?, ?>) createBody.get("properties");
        List<?> rules = (List<?>) properties.get("rules");
        Map<?, ?> rule = (Map<?, ?>) rules.get(0);
        Map<?, ?> threshold = (Map<?, ?>) ((List<?>) rule.get("thresholds")).get(0);
        Assertions.assertEquals("account", ((Map<?, ?>) ((List<?>) rule.get("counterKey")).get(0)).get("type"));
        Assertions.assertEquals("usd", rule.get("unit"));
        Assertions.assertEquals(25.0, ((Number) rule.get("amount")).doubleValue());
        Assertions.assertEquals("audit", threshold.get("action"));
        Assertions.assertEquals(0.0, ((Number) threshold.get("value")).doubleValue());
    }

    @Test
    public void getsAndDeserializesThreshold() {
        QueueHttpClient httpClient
            = new QueueHttpClient(Collections.singletonList(response(200, response("Cost Control", 25, "\"etag-1\""))));
        CognitiveServicesManager manager = createManager(httpClient);
        CostControl fetched = manager.costControls().get(RESOURCE_GROUP, ACCOUNT_NAME, COST_CONTROL_NAME);

        Assertions.assertEquals(CostControlThresholdAction.AUDIT,
            fetched.properties().rules().get(0).thresholds().get(0).action());
        Assertions.assertEquals(25.0, fetched.properties().rules().get(0).amount());
        Assertions.assertEquals("GET", httpClient.requests().get(0).getHttpMethod().toString());
        assertApiVersion(httpClient.requests().get(0));
    }

    @Test
    public void followsListNextLink() {
        String created = response("Cost Control", 25, "\"etag-1\"");
        String updated = response("Cost Control updated", 50, "\"etag-2\"");
        QueueHttpClient httpClient = new QueueHttpClient(Arrays.asList(request -> response(200,
            "{\"value\":[" + created + "],\"nextLink\":\"" + request.getUrl() + "&$skiptoken=next\"}").apply(request),
            response(200, "{\"value\":[" + updated + "]}")));
        CognitiveServicesManager manager = createManager(httpClient);
        List<CostControl> listed
            = manager.costControls().list(RESOURCE_GROUP, ACCOUNT_NAME).stream().collect(Collectors.toList());

        Assertions.assertEquals(Arrays.asList("Cost Control", "Cost Control updated"),
            listed.stream().map(item -> item.properties().displayName()).collect(Collectors.toList()));
        Assertions.assertEquals(httpClient.requests().get(0).getUrl() + "&$skiptoken=next",
            httpClient.requests().get(1).getUrl().toString());
        assertApiVersion(httpClient.requests().get(0));
    }

    @Test
    public void updatesWithIfMatchAndReplacementRules() {
        String created = response("Cost Control", 25, "\"etag-1\"");
        String updated = response("Cost Control updated", 50, "\"etag-2\"");
        QueueHttpClient httpClient = new QueueHttpClient(Arrays.asList(response(200, created), response(200, updated)));
        CognitiveServicesManager manager = createManager(httpClient);
        CostControl fetched = manager.costControls().get(RESOURCE_GROUP, ACCOUNT_NAME, COST_CONTROL_NAME);

        CostControl updatedCostControl = fetched.update()
            .withProperties(new CostControlPatchProperties().withDisplayName("Cost Control updated")
                .withRules(Collections.singletonList(createRule(50))))
            .withIfMatch(fetched.etag())
            .apply();

        Assertions.assertEquals(50.0, updatedCostControl.properties().rules().get(0).amount());
        HttpRequest updateRequest = httpClient.requests().get(1);
        Assertions.assertEquals("PATCH", updateRequest.getHttpMethod().toString());
        Assertions.assertEquals("etag-1", updateRequest.getHeaders().getValue("if-match"));
        Map<?, ?> updateBody = updateRequest.getBodyAsBinaryData().toObject(Map.class);
        Map<?, ?> updateProperties = (Map<?, ?>) updateBody.get("properties");
        Map<?, ?> updateRule = (Map<?, ?>) ((List<?>) updateProperties.get("rules")).get(0);
        Assertions.assertEquals(50.0, ((Number) updateRule.get("amount")).doubleValue());
        assertApiVersion(updateRequest);
    }

    @Test
    public void deletesWithIfMatchAndSurfacesNotFound() {
        QueueHttpClient httpClient = new QueueHttpClient(Arrays.asList(response(204, ""),
            response(404, "{\"error\":{\"code\":\"ResourceNotFound\",\"message\":\"Cost Control not found.\"}}")));
        CognitiveServicesManager manager = createManager(httpClient);
        manager.costControls()
            .deleteWithResponse(RESOURCE_GROUP, ACCOUNT_NAME, COST_CONTROL_NAME, "etag-2",
                com.azure.core.util.Context.NONE);

        HttpRequest deleteRequest = httpClient.requests().get(0);
        Assertions.assertEquals("DELETE", deleteRequest.getHttpMethod().toString());
        Assertions.assertEquals("etag-2", deleteRequest.getHeaders().getValue("if-match"));

        ManagementException exception = Assertions.assertThrows(ManagementException.class,
            () -> manager.costControls().get(RESOURCE_GROUP, ACCOUNT_NAME, COST_CONTROL_NAME));
        Assertions.assertEquals(404, exception.getResponse().getStatusCode());
        Assertions.assertEquals("GET", httpClient.requests().get(1).getHttpMethod().toString());
        assertApiVersion(deleteRequest);
    }

    private static void assertApiVersion(HttpRequest request) {
        Assertions.assertNotNull(request.getUrl().getQuery());
        Assertions.assertTrue(request.getUrl().getQuery().matches(".*(?:^|&)api-version=[^&]+.*"));
    }

    private static CognitiveServicesManager createManager(HttpClient httpClient) {
        return CognitiveServicesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("unit-test-token", OffsetDateTime.MAX)),
                new AzureProfile("", SUBSCRIPTION_ID, AzureCloud.AZURE_PUBLIC_CLOUD));
    }

    private static CostControlProperties createProperties(String displayName, double amount) {
        return new CostControlProperties().withDisplayName(displayName)
            .withRules(Collections.singletonList(createRule(amount)));
    }

    private static CostControlRule createRule(double amount) {
        return new CostControlRule().withName("account-budget")
            .withCounterKey(
                Collections.singletonList(new CostControlDimension().withType(CostControlDimensionType.ACCOUNT)))
            .withUnit(CostControlUnit.USD)
            .withAmount(amount)
            .withPeriod(CostControlPeriod.MONTH)
            .withRecurring(true)
            .withThresholds(
                Collections.singletonList(new CostControlThreshold().withType(CostControlThresholdType.PERCENTAGE)
                    .withValue(0)
                    .withAction(CostControlThresholdAction.AUDIT)));
    }

    private static String response(String displayName, double amount, String etag) {
        return "{\"id\":\"" + RESOURCE_ID + "\",\"name\":\"" + COST_CONTROL_NAME
            + "\",\"type\":\"Microsoft.CognitiveServices/accounts/costControls\",\"etag\":" + etag
            + ",\"properties\":{\"displayName\":\"" + displayName + "\",\"rules\":["
            + "{\"name\":\"account-budget\",\"counterKey\":[{\"type\":\"account\"}],\"unit\":\"usd\"," + "\"amount\":"
            + amount + ",\"period\":\"month\",\"recurring\":true,\"thresholds\":["
            + "{\"type\":\"percentage\",\"value\":0,\"action\":\"audit\"}]}]}}";
    }

    private static Function<HttpRequest, HttpResponse> response(int statusCode, String body) {
        return request -> new MockHttpResponse(request, statusCode,
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
            Function<HttpRequest, HttpResponse> response = responses.poll();
            if (response == null) {
                return Mono.error(new IllegalStateException(
                    "Unexpected request: " + request.getHttpMethod() + " " + request.getUrl()));
            }
            return Mono.just(response.apply(request));
        }

        private List<HttpRequest> requests() {
            return requests;
        }
    }
}

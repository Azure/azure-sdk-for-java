// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.edgeactions;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.edgeactions.fluent.EdgeActionVersionsClient;
import com.azure.resourcemanager.edgeactions.fluent.models.VersionCodeInner;
import com.azure.resourcemanager.edgeactions.models.EdgeActionIsDefaultVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersion;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionDeploymentType;
import com.azure.resourcemanager.edgeactions.models.EdgeActionVersionValidationStatus;
import com.azure.resourcemanager.edgeactions.models.ProvisioningState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Timeout(10)
public class EdgeActionVersionLroTests {
    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String RESOURCE_ID = "/subscriptions/" + SUBSCRIPTION_ID
        + "/resourceGroups/test-rg/providers/Microsoft.Cdn/edgeActions/test-action/versions/v1";
    private static final String ENDPOINT = "https://management.azure.com";
    private static final String API_VERSION_QUERY = "?api-version=2026-10-01";
    private static final String VERSION_URL = ENDPOINT + RESOURCE_ID + API_VERSION_QUERY;
    private static final String STATUS_URL
        = ENDPOINT + "/providers/Microsoft.Cdn/locations/global/operationStatuses/test-operation" + API_VERSION_QUERY;
    private static final String VERSION_BODY = "{\"id\":\"" + RESOURCE_ID
        + "\",\"name\":\"v1\",\"type\":\"Microsoft.Cdn/edgeActions/versions\",\"location\":\"global\","
        + "\"properties\":{\"deploymentType\":\"file\",\"provisioningState\":\"Succeeded\","
        + "\"validationStatus\":\"Succeeded\",\"isDefaultVersion\":\"True\"}}";

    @Test
    public void deployVersionCodePollsStatusAndReturnsFullVersion() {
        LroHttpClient httpClient = new LroHttpClient("deployVersionCode", true, 200);
        EdgeActionVersion version = manager(httpClient).edgeActionVersions()
            .deployVersionCode("test-rg", "test-action", "v1", deploymentCode());

        Assertions.assertNotNull(version);
        Assertions.assertEquals(RESOURCE_ID, version.id());
        Assertions.assertEquals("v1", version.name());
        Assertions.assertEquals("global", version.location());
        Assertions.assertNotNull(version.properties());
        Assertions.assertEquals(ProvisioningState.SUCCEEDED, version.properties().provisioningState());
        Assertions.assertEquals(EdgeActionVersionValidationStatus.SUCCEEDED, version.properties().validationStatus());
        Assertions.assertEquals(EdgeActionVersionDeploymentType.FILE, version.properties().deploymentType());
        Assertions.assertEquals(EdgeActionIsDefaultVersion.TRUE, version.properties().isDefaultVersion());
        Assertions.assertEquals(4, httpClient.requests.get());
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 204 })
    public void swapDefaultValidatesStatusWithoutCheckingLocationResponse(int locationStatus) {
        LroHttpClient httpClient = new LroHttpClient("swapDefault", true, locationStatus);
        Void result = manager(httpClient).serviceClient()
            .getEdgeActionVersions()
            .beginSwapDefault("test-rg", "test-action", "v1")
            .getFinalResult();

        Assertions.assertNull(result);
        Assertions.assertEquals(4, httpClient.requests.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "deployVersionCode", "swapDefault" })
    public void failedOperationDoesNotFetchFinalVersion(String action) {
        LroHttpClient httpClient = new LroHttpClient(action, false, 200);
        EdgeActionVersionsClient client = manager(httpClient).serviceClient().getEdgeActionVersions();
        SyncPoller<? extends PollResult<?>, ?> poller;
        if ("deployVersionCode".equals(action)) {
            poller = client.beginDeployVersionCode("test-rg", "test-action", "v1", deploymentCode());
        } else {
            poller = client.beginSwapDefault("test-rg", "test-action", "v1");
        }

        PollResponse<? extends PollResult<?>> response = poller.waitForCompletion();
        Assertions.assertEquals(LongRunningOperationStatus.FAILED, response.getStatus());
        Assertions.assertNotNull(response.getValue().getError());
        Assertions.assertEquals(200, response.getValue().getError().getResponseStatusCode());
        Assertions.assertNull(poller.getFinalResult());
        Assertions.assertEquals(3, httpClient.requests.get());
    }

    private static VersionCodeInner deploymentCode() {
        return new VersionCodeInner().withName("testdeployment").withContent("dGVzdA==");
    }

    private static EdgeActionsManager manager(HttpClient httpClient) {
        return EdgeActionsManager.configure()
            .withHttpClient(httpClient)
            .withPolicy(new EdgeActionsLroResponsePolicy())
            .withDefaultPollInterval(Duration.ofMillis(1))
            .authenticate(context -> Mono.just(new AccessToken("test-token", OffsetDateTime.MAX)),
                new AzureProfile(SUBSCRIPTION_ID, SUBSCRIPTION_ID, AzureCloud.AZURE_PUBLIC_CLOUD));
    }

    private static final class LroHttpClient implements HttpClient {
        private final String action;
        private final boolean succeeds;
        private final int locationStatus;
        private final AtomicInteger requests = new AtomicInteger();

        private LroHttpClient(String action, boolean succeeds, int locationStatus) {
            this.action = action;
            this.succeeds = succeeds;
            this.locationStatus = locationStatus;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromSupplier(() -> {
                int step = requests.getAndIncrement();
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
                if (step == 0) {
                    Assertions.assertEquals(HttpMethod.POST, request.getHttpMethod());
                    Assertions.assertEquals(ENDPOINT + RESOURCE_ID + "/" + action + API_VERSION_QUERY,
                        request.getUrl().toString());
                    headers.set(HttpHeaderName.AZURE_ASYNCOPERATION, STATUS_URL);
                    headers.set(HttpHeaderName.LOCATION, VERSION_URL);
                    return new MockHttpResponse(request, 202, headers, new byte[0]);
                }

                Assertions.assertEquals(HttpMethod.GET, request.getHttpMethod());
                if (step == 1 || step == 2) {
                    Assertions.assertEquals(STATUS_URL, request.getUrl().toString());
                    String status = step == 1 ? "Running" : succeeds ? "Succeeded" : "Failed";
                    return new MockHttpResponse(request, 200, headers,
                        ("{\"status\":\"" + status + "\"}").getBytes(StandardCharsets.UTF_8));
                }
                if (step == 3 && succeeds) {
                    Assertions.assertEquals(VERSION_URL, request.getUrl().toString());
                    return new MockHttpResponse(request, locationStatus, headers,
                        locationStatus == 204 ? new byte[0] : VERSION_BODY.getBytes(StandardCharsets.UTF_8));
                }
                throw new AssertionError("Unexpected LRO request: " + request.getHttpMethod() + " " + request.getUrl());
            });
        }
    }
}

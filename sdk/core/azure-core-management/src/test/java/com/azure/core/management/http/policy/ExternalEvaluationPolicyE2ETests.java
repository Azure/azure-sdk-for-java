// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import com.azure.resourcemanager.storage.models.Kind;
import com.azure.resourcemanager.storage.models.Sku;
import com.azure.resourcemanager.storage.models.SkuName;
import com.azure.resourcemanager.storage.models.StorageAccountCreateParameters;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * End-to-end test for {@link ExternalEvaluationPolicy} that exercises the full external evaluation ("Invoke") flow
 * against a real generated Storage management client, with Azure Resource Manager faked by WireMock.
 */
public class ExternalEvaluationPolicyE2ETests {
    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String RESOURCE_GROUP = "rg";
    private static final String ACCOUNT_NAME = "acct";
    private static final String POLICY_TOKEN_VALUE = "PoP eyJhbGciOiJSUzI1NiJ9.policy-token-value";
    private static final String EE_HEADER = "x-ms-policy-external-evaluations";

    private static final String STORAGE_PATH = "/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/" + RESOURCE_GROUP
        + "/providers/Microsoft.Storage/storageAccounts/" + ACCOUNT_NAME;
    private static final String ACQUIRE_PATH = "/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/" + RESOURCE_GROUP
        + "/providers/Microsoft.Authorization/acquirePolicyToken";

    private static final String DENY_BODY = "{\"error\":{\"code\":\"RequestDisallowedByPolicy\","
        + "\"message\":\"Resource was disallowed by policy.\",\"additionalInfo\":[{\"type\":\"PolicyViolation\","
        + "\"info\":{\"evaluationDetails\":{\"missingPolicyTokenDetails\":{\"shouldDeny\":true,"
        + "\"endpointKind\":\"AzureResourceGraph\",\"isChangeReferenceRequired\":false}}}}]}}";

    private static final String STORAGE_BODY = "{\"id\":\"" + STORAGE_PATH + "\",\"name\":\"" + ACCOUNT_NAME + "\","
        + "\"type\":\"Microsoft.Storage/storageAccounts\",\"location\":\"eastus\","
        + "\"properties\":{\"provisioningState\":\"Succeeded\"}}";

    private static final String ACQUIRE_BODY = "{\"result\":\"Succeeded\",\"token\":\"" + POLICY_TOKEN_VALUE + "\","
        + "\"tokenId\":\"11111111-1111-1111-1111-111111111111\"}";

    // The exact JSON body the Storage create operation serializes for the parameters built below. The External
    // Evaluation flow requires the retried request body to be byte-for-byte identical to the original, so the mock
    // service asserts on this exact string for both the denied and the retried PUT.
    private static final String EXPECTED_REQUEST_BODY
        = "{\"sku\":{\"name\":\"Standard_LRS\"},\"kind\":\"StorageV2\",\"location\":\"eastus\"}";

    private static final String API_VERSION = "2026-04-01";

    @Test
    public void endToEndAcquireAndRetry() {
        String endpoint = "https://management.azure.com";
        String operationUrl = endpoint + STORAGE_PATH + "?api-version=" + API_VERSION;
        String acquireUrl = endpoint + ACQUIRE_PATH + "?api-version=2026-07-01";
        String expectedAcquireBody = "{\"operation\":{\"uri\":\"" + operationUrl
            + "\",\"httpMethod\":\"PUT\",\"content\":" + EXPECTED_REQUEST_BODY + "}}";
        RecordingHttpClient httpClient = new RecordingHttpClient();
        StorageManager storageManager = StorageManager.configure()
            .withHttpClient(httpClient)
            .withExternalEvaluation()
            .authenticate(credential(), profile(endpoint));

        StorageAccountInner result = storageManager.serviceClient()
            .getResourceProviders()
            .create(RESOURCE_GROUP, ACCOUNT_NAME,
                new StorageAccountCreateParameters().withSku(new Sku().withName(SkuName.STANDARD_LRS))
                    .withKind(Kind.STORAGE_V2)
                    .withLocation("eastus"));

        assertNotNull(result);
        assertEquals(ACCOUNT_NAME, result.name());
        assertEquals(3, httpClient.requests.size());
        assertRequest(httpClient.requests.get(0), HttpMethod.PUT, operationUrl, null, EXPECTED_REQUEST_BODY);
        assertRequest(httpClient.requests.get(1), HttpMethod.POST, acquireUrl, null, expectedAcquireBody);
        assertRequest(httpClient.requests.get(2), HttpMethod.PUT, operationUrl, POLICY_TOKEN_VALUE,
            EXPECTED_REQUEST_BODY);
    }

    @Test
    public void externalEvaluationIsDisabledByDefault() {
        String endpoint = "https://management.azure.com";
        RecordingHttpClient httpClient = new RecordingHttpClient();
        StorageManager storageManager
            = StorageManager.configure().withHttpClient(httpClient).authenticate(credential(), profile(endpoint));

        assertThrows(ManagementException.class,
            () -> storageManager.serviceClient()
                .getResourceProviders()
                .create(RESOURCE_GROUP, ACCOUNT_NAME,
                    new StorageAccountCreateParameters().withSku(new Sku().withName(SkuName.STANDARD_LRS))
                        .withKind(Kind.STORAGE_V2)
                        .withLocation("eastus")));

        assertEquals(1, httpClient.requests.size());
        assertRequest(httpClient.requests.get(0), HttpMethod.PUT,
            endpoint + STORAGE_PATH + "?api-version=" + API_VERSION, null, EXPECTED_REQUEST_BODY);
    }

    private static TokenCredential credential() {
        return request -> Mono.just(new AccessToken("dummy-token", OffsetDateTime.now().plusHours(1)));
    }

    private static AzureProfile profile(String endpoint) {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put(AzureEnvironment.Endpoint.MANAGEMENT.identifier(), endpoint);
        endpoints.put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.identifier(), endpoint);
        endpoints.put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.identifier(), endpoint);
        return new AzureProfile("tenant", SUBSCRIPTION_ID, new AzureEnvironment(endpoints));
    }

    private static void assertRequest(CapturedRequest request, HttpMethod method, String url, String token,
        String body) {
        assertEquals(method, request.method);
        assertEquals(url, request.url);
        assertEquals(token, request.token);
        assertEquals(body, request.body);
    }

    private static final class RecordingHttpClient implements HttpClient {
        private final List<CapturedRequest> requests = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String token = request.getHeaders().getValue(HttpHeaderName.fromString(EE_HEADER));
            String body = request.getBodyAsBinaryData() == null ? null : request.getBodyAsBinaryData().toString();
            requests.add(new CapturedRequest(request.getHttpMethod(), request.getUrl().toString(), token, body));

            if (request.getHttpMethod() == HttpMethod.POST && request.getUrl().getPath().equals(ACQUIRE_PATH)) {
                return response(request, 200, ACQUIRE_BODY);
            }
            if (request.getHttpMethod() == HttpMethod.PUT && token == null) {
                return response(request, 403, DENY_BODY);
            }
            if (request.getHttpMethod() == HttpMethod.PUT && POLICY_TOKEN_VALUE.equals(token)) {
                return response(request, 200, STORAGE_BODY);
            }
            return response(request, 500, "{}");
        }

        private static Mono<HttpResponse> response(HttpRequest request, int statusCode, String body) {
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            return Mono.just(new MockHttpResponse(request, statusCode, headers, body.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private static final class CapturedRequest {
        private final HttpMethod method;
        private final String url;
        private final String token;
        private final String body;

        private CapturedRequest(HttpMethod method, String url, String token, String body) {
            this.method = method;
            this.url = url;
            this.token = token;
            this.body = body;
        }
    }
}

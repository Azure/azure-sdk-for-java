// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExternalEvaluationPolicyTests {
    private static final HttpHeaderName POLICY_EXTERNAL_EVALUATIONS
        = HttpHeaderName.fromString("x-ms-policy-external-evaluations");

    private static final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private static final String OPERATION_PATH
        = "/subscriptions/" + SUBSCRIPTION_ID + "/resourceGroups/rg/providers/Microsoft.Storage/storageAccounts/acc";
    private static final String OPERATION_URL
        = "https://management.azure.com" + OPERATION_PATH + "?api-version=2023-01-01";
    private static final String RESOURCE_GROUP_ACQUIRE_URL
        = "https://management.azure.com/subscriptions/" + SUBSCRIPTION_ID
            + "/resourceGroups/rg/providers/Microsoft.Authorization/acquirePolicyToken?api-version=2026-07-01";

    private static final String REQUEST_BODY = "{\"location\":\"eastus\"}";
    private static final String TOKEN_VALUE = "PoP test-policy-token";
    private static final String TOKEN_BODY = "{\"token\":\"" + TOKEN_VALUE + "\"}";

    private static final String MISSING_DETAILS = "\"evaluationDetails\":{\"missingPolicyTokenDetails\":"
        + "{\"shouldDeny\":true,\"endpointKind\":\"AzureResourceGraph\",\"isChangeReferenceRequired\":false}}";
    private static final String MISSING_DETAILS_BODY = denyBody(policyViolation(MISSING_DETAILS));
    private static final String CHANGE_REFERENCE_REQUIRED_BODY
        = denyBody(policyViolation("\"evaluationDetails\":{\"missingPolicyTokenDetails\":{\"shouldDeny\":true,"
            + "\"isChangeReferenceRequired\":true}}"));
    private static final String PLAIN_DENY_BODY
        = "{\"error\":{\"code\":\"RequestDisallowedByPolicy\",\"message\":\"Resource was disallowed by policy.\"}}";

    @Test
    public void acquiresTokenAndRetriesAsync() {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });

        StepVerifier.create(pipeline(client).send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertHappyPath(client);
    }

    @Test
    public void acquiresTokenAndRetriesSync() {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });

        HttpResponse response = pipeline(client).sendSync(newRequest(), Context.NONE);

        Assertions.assertEquals(200, response.getStatusCode());
        assertHappyPath(client);
    }

    @Test
    public void acquisitionUsesDedicatedPipeline() {
        MockHttpClient managementClient
            = new MockHttpClient(new int[] { 403, 200 }, new String[] { MISSING_DETAILS_BODY, "{}" });
        MockHttpClient acquisitionClient = new MockHttpClient(new int[] { 200 }, new String[] { TOKEN_BODY });

        StepVerifier.create(pipeline(managementClient, acquisitionClient).send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(2, managementClient.requests.size());
        Assertions.assertEquals(HttpMethod.PUT, managementClient.requests.get(0).method);
        Assertions.assertEquals(HttpMethod.PUT, managementClient.requests.get(1).method);
        Assertions.assertEquals(1, acquisitionClient.requests.size());
        Assertions.assertEquals(HttpMethod.POST, acquisitionClient.requests.get(0).method);
        Assertions.assertEquals(RESOURCE_GROUP_ACQUIRE_URL, acquisitionClient.requests.get(0).url);
    }

    @ParameterizedTest
    @CsvSource({
        "'/subscriptions/" + SUBSCRIPTION_ID + "/providers/Microsoft.Compute/virtualMachines/vm','/subscriptions/"
            + SUBSCRIPTION_ID + "/providers/Microsoft.Authorization/acquirePolicyToken'",
        "'/subscriptions/" + SUBSCRIPTION_ID
            + "/resourceGroups/my-rg/providers/Microsoft.Compute/virtualMachines/vm','/subscriptions/" + SUBSCRIPTION_ID
            + "/resourceGroups/my-rg/providers/Microsoft.Authorization/acquirePolicyToken'",
        "'/providers/Microsoft.Management/managementGroups/my-mg/providers/Microsoft.Authorization/policyAssignments/a',"
            + "'/providers/Microsoft.Management/managementGroups/my-mg/providers/Microsoft.Authorization/"
            + "acquirePolicyToken'" })
    public void selectsAcquireEndpointFromOperationScope(String operationPath, String expectedAcquirePath) {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });
        HttpRequest request = new HttpRequest(HttpMethod.PUT,
            "https://management.azure.com" + operationPath + "?api-version=2023-01-01")
                .setBody(BinaryData.fromString(REQUEST_BODY));

        StepVerifier.create(pipeline(client).send(request)).expectNextCount(1).verifyComplete();

        Assertions.assertEquals("https://management.azure.com" + expectedAcquirePath + "?api-version=2026-07-01",
            client.requests.get(1).url);
    }

    @Test
    public void onlyAcquiresWhenEveryPolicyViolationRequiresToken() {
        String mixedDeny = denyBody(policyViolation(MISSING_DETAILS),
            policyViolation("\"evaluationDetails\":{\"reason\":\"ordinary deny\"}"));
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { mixedDeny });

        StepVerifier.create(pipeline(client).send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(403, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void shouldDenyMustBeTrue() {
        String shouldNotDeny
            = denyBody(policyViolation("\"evaluationDetails\":{\"missingPolicyTokenDetails\":{\"shouldDeny\":false}}"));
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { shouldNotDeny });

        StepVerifier.create(pipeline(client).send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(403, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void ignoresUnrelatedAdditionalInfoEntries() {
        String deny = denyBody("{\"type\":\"Other\",\"info\":{}}", policyViolation(MISSING_DETAILS));
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { deny, TOKEN_BODY, "{}" });

        StepVerifier.create(pipeline(client).send(newRequest())).expectNextCount(1).verifyComplete();

        Assertions.assertEquals(3, client.requests.size());
    }

    @Test
    public void tokenIsSuccessRegardlessOfOtherResponseFields() {
        MockHttpClient client = new MockHttpClient(new int[] { 403, 200, 200 },
            new String[] { MISSING_DETAILS_BODY, "{\"result\":\"Failed\",\"token\":\"" + TOKEN_VALUE + "\"}", "{}" });

        StepVerifier.create(pipeline(client).send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(TOKEN_VALUE, client.requests.get(2).header);
    }

    @Test
    public void disablesBodyLoggingOnlyForAcquisition() {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });
        BodyLoggingContextPolicy contextPolicy = new BodyLoggingContextPolicy();

        StepVerifier.create(pipeline(client, contextPolicy).send(newRequest())).expectNextCount(1).verifyComplete();

        Assertions.assertEquals(java.util.Arrays.asList(false, true, false), contextPolicy.values);
    }

    @Test
    public void preservesCallerBodyLoggingSuppression() {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });
        BodyLoggingContextPolicy contextPolicy = new BodyLoggingContextPolicy();
        Context context = Context.NONE.addData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT, true);

        StepVerifier.create(pipeline(client, contextPolicy).send(newRequest(), context))
            .expectNextCount(1)
            .verifyComplete();

        Assertions.assertEquals(java.util.Arrays.asList(true, true, true), contextPolicy.values);
    }

    @Test
    public void missingTokenReSurfacesOriginalDeny() {
        MockHttpClient client = new MockHttpClient(new int[] { 403, 200 },
            new String[] { MISSING_DETAILS_BODY, "{\"result\":\"Succeeded\"}" });

        StepVerifier.create(pipeline(client).send(newRequest())).verifyErrorMatches(error -> {
            if (!(error instanceof ManagementException)) {
                return false;
            }
            ManagementException denied = (ManagementException) error;
            return denied.getResponse().getStatusCode() == 403
                && denied.getCause() instanceof IllegalStateException
                && denied.getCause().getMessage().contains("did not contain a valid token");
        });
    }

    @Test
    public void acquisitionHttpErrorReSurfacesOriginalDeny() {
        String acquireError = "{\"error\":{\"code\":\"AcquisitionFailed\",\"message\":\"Policy rejected issuance.\"}}";
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 500 }, new String[] { MISSING_DETAILS_BODY, acquireError });

        StepVerifier.create(pipeline(client).send(newRequest())).verifyErrorMatches(error -> {
            if (!(error instanceof ManagementException) || !(error.getCause() instanceof ManagementException)) {
                return false;
            }
            ManagementException denied = (ManagementException) error;
            ManagementException acquisition = (ManagementException) error.getCause();
            return denied.getResponse().getStatusCode() == 403
                && acquisition.getResponse().getStatusCode() == 500
                && "AcquisitionFailed".equals(acquisition.getValue().getCode());
        });
    }

    @Test
    public void preservesNonReplayableOperationBody() {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });
        BinaryData nonReplayable = BinaryData
            .fromFlux(Flux.just(ByteBuffer.wrap(REQUEST_BODY.getBytes(StandardCharsets.UTF_8))), null, false)
            .block();
        HttpRequest request = new HttpRequest(HttpMethod.PUT, OPERATION_URL).setBody(nonReplayable);

        StepVerifier.create(pipeline(client).send(request)).expectNextCount(1).verifyComplete();

        assertHappyPath(client);
    }

    @Test
    public void acquiresTokenForDeleteWithoutBody() {
        MockHttpClient client
            = new MockHttpClient(new int[] { 403, 200, 200 }, new String[] { MISSING_DETAILS_BODY, TOKEN_BODY, "{}" });
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, OPERATION_URL);

        StepVerifier.create(pipeline(client).send(request)).expectNextCount(1).verifyComplete();

        Assertions.assertEquals("{\"operation\":{\"uri\":\"" + OPERATION_URL + "\",\"httpMethod\":\"DELETE\"}}",
            client.requests.get(1).body);
        Assertions.assertNull(client.requests.get(0).body);
        Assertions.assertNull(client.requests.get(2).body);
    }

    @Test
    public void plainDenyIsPassedThrough() {
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { PLAIN_DENY_BODY });

        StepVerifier.create(pipeline(client).send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(403, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void changeReferenceRequiredIsGuarded() {
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { CHANGE_REFERENCE_REQUIRED_BODY });

        StepVerifier.create(pipeline(client).send(newRequest())).verifyError(IllegalStateException.class);

        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void getAndPreAuthorizedRequestsAreNotIntercepted() {
        MockHttpClient getClient = new MockHttpClient(new int[] { 403 }, new String[] { MISSING_DETAILS_BODY });
        StepVerifier.create(pipeline(getClient).send(new HttpRequest(HttpMethod.GET, OPERATION_URL)))
            .assertNext(response -> Assertions.assertEquals(403, response.getStatusCode()))
            .verifyComplete();

        MockHttpClient authorizedClient = new MockHttpClient(new int[] { 403 }, new String[] { MISSING_DETAILS_BODY });
        HttpRequest authorizedRequest
            = newRequest().setHeader(POLICY_EXTERNAL_EVALUATIONS, "PoP already-present-token");
        StepVerifier.create(pipeline(authorizedClient).send(authorizedRequest))
            .assertNext(response -> Assertions.assertEquals(403, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(1, getClient.requests.size());
        Assertions.assertEquals(1, authorizedClient.requests.size());
    }

    private static void assertHappyPath(MockHttpClient client) {
        Assertions.assertEquals(3, client.requests.size());

        CapturedRequest original = client.requests.get(0);
        Assertions.assertEquals(HttpMethod.PUT, original.method);
        Assertions.assertEquals(OPERATION_URL, original.url);
        Assertions.assertNull(original.header);
        Assertions.assertEquals(REQUEST_BODY, original.body);

        CapturedRequest acquire = client.requests.get(1);
        Assertions.assertEquals(HttpMethod.POST, acquire.method);
        Assertions.assertEquals(RESOURCE_GROUP_ACQUIRE_URL, acquire.url);
        Assertions.assertNull(acquire.header);
        Assertions.assertEquals("{\"operation\":{\"uri\":\"" + OPERATION_URL + "\",\"httpMethod\":\"PUT\",\"content\":"
            + REQUEST_BODY + "}}", acquire.body);

        CapturedRequest replay = client.requests.get(2);
        Assertions.assertEquals(HttpMethod.PUT, replay.method);
        Assertions.assertEquals(OPERATION_URL, replay.url);
        Assertions.assertEquals(TOKEN_VALUE, replay.header);
        Assertions.assertEquals(REQUEST_BODY, replay.body);
    }

    private static String denyBody(String... additionalInfo) {
        return "{\"error\":{\"code\":\"RequestDisallowedByPolicy\",\"message\":\"A policy token is required.\","
            + "\"additionalInfo\":[" + String.join(",", additionalInfo) + "]}}";
    }

    private static String policyViolation(String infoContent) {
        return "{\"type\":\"PolicyViolation\",\"info\":{" + infoContent + "}}";
    }

    private static HttpPipeline pipeline(HttpClient client, HttpPipelinePolicy... additionalPolicies) {
        return pipeline(client, client, additionalPolicies);
    }

    private static HttpPipeline pipeline(HttpClient managementClient, HttpClient acquisitionClient,
        HttpPipelinePolicy... additionalPolicies) {
        HttpPipeline acquisitionPipeline
            = new HttpPipelineBuilder().policies(additionalPolicies).httpClient(acquisitionClient).build();
        HttpPipelinePolicy[] policies = new HttpPipelinePolicy[additionalPolicies.length + 1];
        policies[0] = new ExternalEvaluationPolicy(acquisitionPipeline);
        System.arraycopy(additionalPolicies, 0, policies, 1, additionalPolicies.length);
        return new HttpPipelineBuilder().policies(policies).httpClient(managementClient).build();
    }

    private static HttpRequest newRequest() {
        return new HttpRequest(HttpMethod.PUT, OPERATION_URL).setBody(BinaryData.fromString(REQUEST_BODY));
    }

    private static final class CapturedRequest {
        private final HttpMethod method;
        private final String url;
        private final String header;
        private final String body;

        private CapturedRequest(HttpMethod method, String url, String header, String body) {
            this.method = method;
            this.url = url;
            this.header = header;
            this.body = body;
        }
    }

    private static final class MockHttpClient implements HttpClient {
        private final int[] statusCodes;
        private final String[] bodies;
        private final AtomicInteger index = new AtomicInteger();
        private final List<CapturedRequest> requests = new ArrayList<>();

        private MockHttpClient(int[] statusCodes, String[] bodies) {
            this.statusCodes = statusCodes;
            this.bodies = bodies;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String header = request.getHeaders().getValue(POLICY_EXTERNAL_EVALUATIONS);
            String body = request.getBodyAsBinaryData() == null ? null : request.getBodyAsBinaryData().toString();
            synchronized (requests) {
                requests.add(new CapturedRequest(request.getHttpMethod(), request.getUrl().toString(), header, body));
            }
            int i = index.getAndIncrement();
            return Mono.just(new MockHttpResponse(request, statusCodes[i], bodies[i]));
        }
    }

    private static final class BodyLoggingContextPolicy implements HttpPipelinePolicy {
        private final List<Boolean> values = new ArrayList<>();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            values.add(
                Boolean.TRUE.equals(context.getData(HttpLoggingPolicy.BODY_LOGGING_DISABLED_CONTEXT).orElse(false)));
            return next.process();
        }
    }

    private static final class MockHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final byte[] body;

        private MockHttpResponse(HttpRequest request, int statusCode, String body) {
            super(request);
            this.statusCode = statusCode;
            this.body = body.getBytes(StandardCharsets.UTF_8);
            this.headers = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(this.body.length));
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        @SuppressWarnings("deprecation")
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.just(ByteBuffer.wrap(body));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(body);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just(new String(body, StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(body, charset));
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.evaluation.PolicyToken;
import com.azure.core.management.evaluation.PolicyTokenCredential;
import com.azure.core.management.evaluation.PolicyTokenRequestContext;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

    private static final String OPERATION_URL
        = "https://management.azure.com/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg"
            + "/providers/Microsoft.Storage/storageAccounts/acc?api-version=2023-01-01";

    private static final String REQUEST_BODY = "{\"location\":\"eastus\"}";

    private static final String TOKEN_VALUE = "PoP test-policy-token";

    private static final String MISSING_DETAILS_BODY = "{\"error\":{\"code\":\"RequestDisallowedByPolicy\","
        + "\"message\":\"A policy token is required.\",\"additionalInfo\":[{\"type\":\"PolicyViolation\","
        + "\"info\":{\"evaluationDetails\":{\"missingPolicyTokenDetails\":{\"shouldDeny\":true,"
        + "\"endpointKind\":\"AzureResourceGraph\",\"endpointResultLifespan\":\"PT1H\","
        + "\"isChangeReferenceRequired\":false}}}}]}}";

    private static final String CHANGE_REFERENCE_REQUIRED_BODY = "{\"error\":{\"code\":\"RequestDisallowedByPolicy\","
        + "\"message\":\"A policy token is required.\",\"additionalInfo\":[{\"type\":\"PolicyViolation\","
        + "\"info\":{\"evaluationDetails\":{\"missingPolicyTokenDetails\":{\"shouldDeny\":true,"
        + "\"endpointKind\":\"AzureResourceGraph\",\"isChangeReferenceRequired\":true}}}}]}}";

    private static final String PLAIN_DENY_BODY
        = "{\"error\":{\"code\":\"RequestDisallowedByPolicy\",\"message\":\"Resource was disallowed by policy.\"}}";

    @Test
    public void acquiresTokenAndRetriesAsync() {
        RecordingPolicyTokenCredential credential
            = new RecordingPolicyTokenCredential(request -> Mono.just(new PolicyToken(TOKEN_VALUE)));
        MockHttpClient client = new MockHttpClient(new int[] { 403, 200 }, new String[] { MISSING_DETAILS_BODY, "{}" });

        HttpPipeline pipeline = pipeline(credential, client);

        StepVerifier.create(pipeline.send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertHappyPath(credential, client);
    }

    @Test
    public void acquiresTokenAndRetriesSync() {
        RecordingPolicyTokenCredential credential
            = new RecordingPolicyTokenCredential(request -> Mono.just(new PolicyToken(TOKEN_VALUE)));
        MockHttpClient client = new MockHttpClient(new int[] { 403, 200 }, new String[] { MISSING_DETAILS_BODY, "{}" });

        HttpPipeline pipeline = pipeline(credential, client);

        HttpResponse response = pipeline.sendSync(newRequest(), Context.NONE);
        Assertions.assertEquals(200, response.getStatusCode());

        assertHappyPath(credential, client);
    }

    @Test
    public void plainDenyIsPassedThrough() {
        RecordingPolicyTokenCredential credential
            = new RecordingPolicyTokenCredential(request -> Mono.just(new PolicyToken(TOKEN_VALUE)));
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { PLAIN_DENY_BODY });

        HttpPipeline pipeline = pipeline(credential, client);

        StepVerifier.create(pipeline.send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(403, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(0, credential.invocations.get());
        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void nonForbiddenIsPassedThrough() {
        RecordingPolicyTokenCredential credential
            = new RecordingPolicyTokenCredential(request -> Mono.just(new PolicyToken(TOKEN_VALUE)));
        MockHttpClient client = new MockHttpClient(new int[] { 200 }, new String[] { "{}" });

        HttpPipeline pipeline = pipeline(credential, client);

        StepVerifier.create(pipeline.send(newRequest()))
            .assertNext(response -> Assertions.assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        Assertions.assertEquals(0, credential.invocations.get());
        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void acquirerErrorIsSurfaced() {
        RecordingPolicyTokenCredential credential
            = new RecordingPolicyTokenCredential(request -> Mono.error(new IllegalStateException("policy denied")));
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { MISSING_DETAILS_BODY });

        HttpPipeline pipeline = pipeline(credential, client);

        StepVerifier.create(pipeline.send(newRequest()))
            .verifyErrorMatches(t -> t instanceof IllegalStateException && "policy denied".equals(t.getMessage()));

        Assertions.assertEquals(1, credential.invocations.get());
        Assertions.assertEquals(1, client.requests.size());
    }

    @Test
    public void changeReferenceRequiredIsGuarded() {
        RecordingPolicyTokenCredential credential
            = new RecordingPolicyTokenCredential(request -> Mono.just(new PolicyToken(TOKEN_VALUE)));
        MockHttpClient client = new MockHttpClient(new int[] { 403 }, new String[] { CHANGE_REFERENCE_REQUIRED_BODY });

        HttpPipeline pipeline = pipeline(credential, client);

        StepVerifier.create(pipeline.send(newRequest())).verifyError(IllegalStateException.class);

        Assertions.assertEquals(0, credential.invocations.get());
        Assertions.assertEquals(1, client.requests.size());
    }

    private void assertHappyPath(RecordingPolicyTokenCredential credential, MockHttpClient client) {
        // The acquirer is invoked exactly once with the operation details.
        Assertions.assertEquals(1, credential.invocations.get());
        PolicyTokenRequestContext acquireContext = credential.lastContext;
        Assertions.assertNotNull(acquireContext);
        Assertions.assertEquals("00000000-0000-0000-0000-000000000000", acquireContext.getSubscriptionId());
        Assertions.assertEquals(HttpMethod.PUT, acquireContext.getHttpMethod());
        Assertions.assertEquals(OPERATION_URL, acquireContext.getUri());
        Assertions.assertEquals(REQUEST_BODY, acquireContext.getContent().toString());

        // Two requests are sent: the original and the retry carrying the token header.
        Assertions.assertEquals(2, client.requests.size());
        Assertions.assertNull(client.requests.get(0).header);
        Assertions.assertEquals(TOKEN_VALUE, client.requests.get(1).header);
        // The retried request body is byte-for-byte identical to the original.
        Assertions.assertEquals(REQUEST_BODY, client.requests.get(0).body);
        Assertions.assertEquals(REQUEST_BODY, client.requests.get(1).body);
    }

    private static HttpPipeline pipeline(PolicyTokenCredential credential, HttpClient client) {
        return new HttpPipelineBuilder().policies(new ExternalEvaluationPolicy(credential)).httpClient(client).build();
    }

    private static HttpRequest newRequest() {
        return new HttpRequest(HttpMethod.PUT, OPERATION_URL).setBody(BinaryData.fromString(REQUEST_BODY));
    }

    private static final class RecordingPolicyTokenCredential implements PolicyTokenCredential {
        private final java.util.function.Function<PolicyTokenRequestContext, Mono<PolicyToken>> handler;
        private final AtomicInteger invocations = new AtomicInteger();
        private volatile PolicyTokenRequestContext lastContext;

        RecordingPolicyTokenCredential(
            java.util.function.Function<PolicyTokenRequestContext, Mono<PolicyToken>> handler) {
            this.handler = handler;
        }

        @Override
        public Mono<PolicyToken> getPolicyToken(PolicyTokenRequestContext request) {
            invocations.incrementAndGet();
            lastContext = request;
            return handler.apply(request);
        }
    }

    private static final class CapturedRequest {
        private final String header;
        private final String body;

        CapturedRequest(String header, String body) {
            this.header = header;
            this.body = body;
        }
    }

    private static final class MockHttpClient implements HttpClient {
        private final int[] statusCodes;
        private final String[] bodies;
        private final AtomicInteger index = new AtomicInteger();
        private final List<CapturedRequest> requests = new ArrayList<>();

        MockHttpClient(int[] statusCodes, String[] bodies) {
            this.statusCodes = statusCodes;
            this.bodies = bodies;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String header = request.getHeaders().getValue(POLICY_EXTERNAL_EVALUATIONS);
            String body = request.getBodyAsBinaryData() == null ? null : request.getBodyAsBinaryData().toString();
            synchronized (requests) {
                requests.add(new CapturedRequest(header, body));
            }
            int i = index.getAndIncrement();
            return Mono.just(new MockHttpResponse(request, statusCodes[i], bodies[i]));
        }
    }

    private static final class MockHttpResponse extends HttpResponse {
        private final int statusCode;
        private final HttpHeaders headers;
        private final byte[] body;

        MockHttpResponse(HttpRequest request, int statusCode, String body) {
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

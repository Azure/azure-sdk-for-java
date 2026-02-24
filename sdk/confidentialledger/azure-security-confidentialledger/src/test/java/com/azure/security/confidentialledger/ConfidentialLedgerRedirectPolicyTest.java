// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ConfidentialLedgerRedirectPolicy}.
 */
public class ConfidentialLedgerRedirectPolicyTest {

    private static final String ORIGINAL_URL = "https://ledger.confidential-ledger.azure.com";
    private static final String REDIRECT_URL = "https://ledger.confidential-ledger.azure.com/primary";
    private static final String CROSS_ORIGIN_URL = "https://other-host.example.com/target";
    private static final String AUTH_TOKEN = "Bearer test-token-12345";

    // ---- Async (Mono) tests ----

    @ParameterizedTest
    @ValueSource(ints = { 301, 302, 307, 308 })
    public void postRedirectFollowedForAllStatusCodes(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, statusCode, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendAsync(pipeline, HttpMethod.POST)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, httpClient.getCount());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 301, 302, 307, 308 })
    public void getRedirectFollowedForAllStatusCodes(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, statusCode, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendAsync(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, httpClient.getCount());
        }
    }

    @Test
    public void authorizationHeaderPreservedOnSameOriginRedirect() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                // Verify the authorization header is present on the redirected request
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            // The authorization header should still be on the final request
            assertEquals(AUTH_TOKEN, response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationHeaderStrippedOnCrossOriginRedirect() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, CROSS_ORIGIN_URL);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            // The authorization header should be stripped for cross-origin redirect
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void noRedirectForNonRedirectStatusCode() throws Exception {
        RecordingHttpClient httpClient
            = new RecordingHttpClient(request -> Mono.just(new MockHttpResponse(request, 401)));

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendAsync(pipeline, HttpMethod.POST)) {
            assertEquals(401, response.getStatusCode());
            assertEquals(1, httpClient.getCount());
        }
    }

    @Test
    public void noRedirectWhenLocationHeaderMissing() throws Exception {
        RecordingHttpClient httpClient
            = new RecordingHttpClient(request -> Mono.just(new MockHttpResponse(request, 307)));

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendAsync(pipeline, HttpMethod.POST)) {
            assertEquals(307, response.getStatusCode());
            assertEquals(1, httpClient.getCount());
        }
    }

    @Test
    public void redirectStopsAfterMaxAttempts() throws Exception {
        AtomicInteger requestIndex = new AtomicInteger(0);
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            int idx = requestIndex.getAndIncrement();
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, ORIGINAL_URL + "/redirect/" + idx);
            return Mono.just(new MockHttpResponse(request, 307, headers));
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendAsync(pipeline, HttpMethod.POST)) {
            // MAX_REDIRECT_ATTEMPTS is 3, so initial + 3 redirects = 4 total requests
            assertEquals(4, httpClient.getCount());
            assertEquals(307, response.getStatusCode());
        }
    }

    @Test
    public void redirectStopsOnRedirectLoop() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                // Redirect back to the same URL (loop)
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendAsync(pipeline, HttpMethod.POST)) {
            // Should stop after detecting the duplicate URL
            assertEquals(2, httpClient.getCount());
            assertEquals(307, response.getStatusCode());
        }
    }

    // ---- Sync tests ----

    @ParameterizedTest
    @ValueSource(ints = { 301, 302, 307, 308 })
    public void postRedirectFollowedForAllStatusCodesSync(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, statusCode, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendSync(pipeline, HttpMethod.POST)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, httpClient.getCount());
        }
    }

    @Test
    public void authorizationHeaderPreservedOnSameOriginRedirectSync() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, REDIRECT_URL);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(AUTH_TOKEN, response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationHeaderStrippedOnCrossOriginRedirectSync() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, CROSS_ORIGIN_URL);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void redirectStopsAfterMaxAttemptsSync() throws Exception {
        AtomicInteger requestIndex = new AtomicInteger(0);
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            int idx = requestIndex.getAndIncrement();
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, ORIGINAL_URL + "/redirect/" + idx);
            return Mono.just(new MockHttpResponse(request, 307, headers));
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendSync(pipeline, HttpMethod.POST)) {
            assertEquals(4, httpClient.getCount());
            assertEquals(307, response.getStatusCode());
        }
    }

    @Test
    public void noRedirectForNonRedirectStatusCodeSync() throws Exception {
        RecordingHttpClient httpClient
            = new RecordingHttpClient(request -> Mono.just(new MockHttpResponse(request, 200)));

        HttpPipeline pipeline = createPipeline(httpClient);

        try (HttpResponse response = sendSync(pipeline, HttpMethod.POST)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(1, httpClient.getCount());
        }
    }

    @Test
    public void authorizationPreservedWhenPortsDiffer() throws Exception {
        // Same host with different port — auth should be preserved because
        // Confidential Ledger nodes use non-standard ports (e.g., 16385).
        String redirectUrlDifferentPort = "https://ledger.confidential-ledger.azure.com:8443/primary";
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, redirectUrlDifferentPort);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            // Same host, different port — trusted redirect, auth preserved.
            assertEquals(AUTH_TOKEN, response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationStrippedWhenSchemeChanges() throws Exception {
        // http vs https = different origin, auth should be stripped
        String httpRedirectUrl = "http://ledger.confidential-ledger.azure.com/primary";
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, httpRedirectUrl);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void multipleRedirectsPreserveAuthWhenSameOrigin() throws Exception {
        String redirectUrl1 = "https://ledger.confidential-ledger.azure.com/step1";
        String redirectUrl2 = "https://ledger.confidential-ledger.azure.com/step2";

        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            String url = request.getUrl().toString();
            if (url.equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, redirectUrl1);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else if (url.equals(redirectUrl1)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, redirectUrl2);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(3, httpClient.getCount());
            // Auth should be preserved through all same-origin redirects
            assertEquals(AUTH_TOKEN, response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationPreservedOnSubdomainRedirect() throws Exception {
        // ACL redirects from the load-balanced endpoint to a specific node subdomain with a different port.
        // e.g., ledger.confidential-ledger.azure.com -> accledger-2.ledger.confidential-ledger.azure.com:16385
        String subdomainRedirectUrl = "https://accledger-2.ledger.confidential-ledger.azure.com:16385/app/transactions";
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, subdomainRedirectUrl);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(2, httpClient.getCount());
            // Subdomain of original host — trusted redirect, auth preserved.
            assertEquals(AUTH_TOKEN, response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationPreservedOnSubdomainRedirectSync() throws Exception {
        String subdomainRedirectUrl = "https://accledger-2.ledger.confidential-ledger.azure.com:16385/app/transactions";
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, subdomainRedirectUrl);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, httpClient.getCount());
            assertEquals(AUTH_TOKEN, response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationStrippedOnUnrelatedHostRedirect() throws Exception {
        // Redirect to a completely different host that is not a subdomain — auth should be stripped.
        String unrelatedUrl = "https://malicious.example.com/steal-token";
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(ORIGINAL_URL)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, unrelatedUrl);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(ORIGINAL_URL));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void authorizationStrippedOnPartialHostMatchRedirect() throws Exception {
        // "evil-ledger.confidential-ledger.azure.com" ends with the original host string
        // but is NOT a subdomain of "ledger.confidential-ledger.azure.com" (no dot separator).
        // This verifies the subdomain check uses "." + originalHost.
        String partialMatchUrl = "https://evil-ledger.confidential-ledger.azure.com/target";
        // Use a shorter original so the partial match is meaningful
        String shortOriginal = "https://myacl.azure.com";
        String evilRedirect = "https://notmyacl.azure.com/steal";
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals(shortOriginal)) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.LOCATION, evilRedirect);
                return Mono.just(new MockHttpResponse(request, 307, headers));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = createPipeline(httpClient);

        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL(shortOriginal));
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, AUTH_TOKEN);

        try (HttpResponse response = pipeline.send(request).block()) {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            // "notmyacl.azure.com" is not a subdomain of "myacl.azure.com" — auth stripped.
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    // ---- Helpers ----

    private static HttpPipeline createPipeline(HttpClient httpClient) {
        return new HttpPipelineBuilder().httpClient(httpClient)
            .policies(new ConfidentialLedgerRedirectPolicy())
            .build();
    }

    private static HttpResponse sendAsync(HttpPipeline pipeline, HttpMethod method) throws MalformedURLException {
        return pipeline.send(new HttpRequest(method, new URL(ORIGINAL_URL))).block();
    }

    private static HttpResponse sendSync(HttpPipeline pipeline, HttpMethod method) throws MalformedURLException {
        return pipeline.sendSync(new HttpRequest(method, new URL(ORIGINAL_URL)), Context.NONE);
    }

    /**
     * A simple recording HTTP client that counts requests and delegates to a handler function.
     */
    static class RecordingHttpClient implements HttpClient {
        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, Mono<HttpResponse>> handler;

        RecordingHttpClient(Function<HttpRequest, Mono<HttpResponse>> handler) {
            this.handler = handler;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            count.getAndIncrement();
            return handler.apply(httpRequest);
        }

        int getCount() {
            return count.get();
        }
    }
}

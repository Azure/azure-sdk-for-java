// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Asserts that the api-version selected on the builder is the one placed on the wire, on both the
 * synchronous and the asynchronous path.
 *
 * <p>The generated {@code IdentityClientImpl} accepts a typed enum that carries only the two most
 * recent service versions, so the builder pins the api-version with a pipeline policy instead. This
 * test covers that policy. It captures at the transport layer - a custom {@link HttpClient} is the
 * final stage before the network, so what it observes is what would be sent.</p>
 *
 * <p>The two paths are asserted separately and deliberately. {@code HttpPipelinePolicy} supplies a
 * default {@code processSync} implementation, so a policy that only implements the asynchronous
 * method still behaves correctly but blocks a thread; conversely a policy that mishandles the
 * synchronous path would fail here and nowhere else. The recorded tests cannot make this
 * distinction, because a synchronous fault and an asynchronous fault produce identical recording
 * mismatches.</p>
 *
 * <p>Expected values are written as literals rather than read back from
 * {@link CommunicationIdentityServiceVersion}, so that the assertion cannot be satisfied by the
 * enum agreeing with itself.</p>
 */
public class CommunicationIdentityApiVersionTests {

    private static final String FAKE_CONNECTION_STRING
        = "endpoint=https://localhost/;accesskey=" + "cHJvYmVwcm9iZXByb2JlcHJvYmVwcm9iZXByb2JlcHJvYmU=";

    /**
     * Records the request URL as it reaches the transport, for both the asynchronous and the
     * synchronous entry points, and returns a minimal successful response.
     */
    private static final class CapturingHttpClient implements HttpClient {
        private volatile String url;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.url = request.getUrl().toString();
            return Mono.just(new EmptyJsonResponse(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            this.url = request.getUrl().toString();
            return new EmptyJsonResponse(request);
        }

        String capturedApiVersion() {
            assertNotNull(url, "no request reached the transport");
            int start = url.indexOf("api-version=");
            if (start < 0) {
                return "(absent)";
            }
            String remainder = url.substring(start + "api-version=".length());
            int ampersand = remainder.indexOf('&');
            return ampersand < 0 ? remainder : remainder.substring(0, ampersand);
        }
    }

    private static final class EmptyJsonResponse extends HttpResponse {
        private static final byte[] BODY = "{}".getBytes(StandardCharsets.UTF_8);

        EmptyJsonResponse(HttpRequest request) {
            super(request);
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.just(ByteBuffer.wrap(BODY));
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(BODY);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just(new String(BODY, StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just(new String(BODY, charset));
        }
    }

    @ParameterizedTest(name = "sync {0} sends api-version={1}")
    @CsvSource({
        "V2021_03_07, 2021-03-07",
        "V2022_06_01, 2022-06-01",
        "V2022_10_01, 2022-10-01",
        "V2023_10_01, 2023-10-01",
        "V2025_06_30, 2025-06-30",
        "V2026_09_23, 2026-09-23" })
    public void syncClientSendsSelectedApiVersion(CommunicationIdentityServiceVersion version, String expected) {
        CapturingHttpClient transport = new CapturingHttpClient();
        CommunicationIdentityClient client
            = new CommunicationIdentityClientBuilder().connectionString(FAKE_CONNECTION_STRING)
                .serviceVersion(version)
                .httpClient(transport)
                .buildClient();

        try {
            client.createUser();
        } catch (RuntimeException ex) {
            // The stub response is not a valid identity payload. The request has already been
            // captured by the time it is deserialized, which is all this test asserts on.
        }

        assertEquals(expected, transport.capturedApiVersion());
    }

    @ParameterizedTest(name = "async {0} sends api-version={1}")
    @CsvSource({
        "V2021_03_07, 2021-03-07",
        "V2022_06_01, 2022-06-01",
        "V2022_10_01, 2022-10-01",
        "V2023_10_01, 2023-10-01",
        "V2025_06_30, 2025-06-30",
        "V2026_09_23, 2026-09-23" })
    public void asyncClientSendsSelectedApiVersion(CommunicationIdentityServiceVersion version, String expected) {
        CapturingHttpClient transport = new CapturingHttpClient();
        CommunicationIdentityAsyncClient client
            = new CommunicationIdentityClientBuilder().connectionString(FAKE_CONNECTION_STRING)
                .serviceVersion(version)
                .httpClient(transport)
                .buildAsyncClient();

        try {
            client.createUser().block();
        } catch (RuntimeException ex) {
            // As above.
        }

        assertEquals(expected, transport.capturedApiVersion());
    }
}

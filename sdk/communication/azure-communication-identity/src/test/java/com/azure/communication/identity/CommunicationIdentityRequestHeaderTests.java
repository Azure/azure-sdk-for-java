// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Asserts that a caller can still choose the request headers this client sends.
 *
 * <p>The client sets an {@code Accept} header on the operations that return no content, because the
 * generated protocol methods for those two declare none. That default must not cap a caller who
 * wants something else: a policy added with
 * {@link CommunicationIdentityClientBuilder#addPolicy(HttpPipelinePolicy)} is the supported way to
 * customise a request, and its value has to reach the wire.</p>
 *
 * <p>What makes this hold is ordering rather than a conditional. The client sets the header while
 * constructing the request; pipeline policies run afterwards, so a caller's policy overwrites it.
 * Setting the same header from inside the pipeline instead - with {@code AddHeadersPolicy}, whose
 * {@code setAllHttpHeaders} overwrites - would clobber the caller's choice depending on where it
 * sat in the pipeline, and would fail these assertions.</p>
 *
 * <p>Capture is at the transport layer, so the assertions are on the request that would be sent
 * rather than on an intermediate representation.</p>
 */
public class CommunicationIdentityRequestHeaderTests {

    private static final String FAKE_CONNECTION_STRING
        = "endpoint=https://localhost/;accesskey=" + "cHJvYmVwcm9iZXByb2JlcHJvYmVwcm9iZXByb2JlcHJvYmU=";

    private static final CommunicationUserIdentifier USER = new CommunicationUserIdentifier(
        "8:acs:00000000-0000-0000-0000-000000000000_00000000-0000-0000-0000-000000000000");

    private static final class CapturingHttpClient implements HttpClient {
        private volatile String accept;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.accept = request.getHeaders().getValue(HttpHeaderName.ACCEPT);
            return Mono.just(new NoContentResponse(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            this.accept = request.getHeaders().getValue(HttpHeaderName.ACCEPT);
            return new NoContentResponse(request);
        }
    }

    private static final class NoContentResponse extends HttpResponse {
        NoContentResponse(HttpRequest request) {
            super(request);
        }

        @Override
        public int getStatusCode() {
            return 204;
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
            return Flux.empty();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.just(new byte[0]);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.just("");
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.just("");
        }
    }

    /** The supported way for a caller to customise a request. */
    private static HttpPipelinePolicy acceptOverridePolicy() {
        return new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                context.getHttpRequest().getHeaders().set(HttpHeaderName.ACCEPT, "application/custom");
                return next.process();
            }

            @Override
            public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
                context.getHttpRequest().getHeaders().set(HttpHeaderName.ACCEPT, "application/custom");
                return next.processSync();
            }
        };
    }

    private static CommunicationIdentityClient clientWithOverride(CapturingHttpClient transport) {
        return new CommunicationIdentityClientBuilder().connectionString(FAKE_CONNECTION_STRING)
            .httpClient(transport)
            .addPolicy(acceptOverridePolicy())
            .buildClient();
    }

    @Test
    public void callerPolicyChoosesAcceptOnDeleteUser() {
        CapturingHttpClient transport = new CapturingHttpClient();
        clientWithOverride(transport).deleteUserWithResponse(USER, Context.NONE);

        assertEquals("application/custom", transport.accept);
    }

    @Test
    public void callerPolicyChoosesAcceptOnRevokeTokens() {
        CapturingHttpClient transport = new CapturingHttpClient();
        clientWithOverride(transport).revokeTokensWithResponse(USER, Context.NONE);

        assertEquals("application/custom", transport.accept);
    }
}

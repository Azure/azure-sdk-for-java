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
 * Asserts the {@code Accept} header this client sends, and that a caller can still choose their own.
 *
 * <p>The generated protocol methods for the two operations that return no content declare no
 * {@code Accept} header, so azure-core would fall back to the wildcard. The AutoRest-generated
 * client this replaces sent {@code application/json} on every operation regardless of response
 * shape, so the client sets it for those two to keep the bytes on the wire unchanged.</p>
 *
 * <p>That default must not cap a caller who wants something else: a policy added with
 * {@link CommunicationIdentityClientBuilder#addPolicy(HttpPipelinePolicy)} is the supported way to
 * customise a request, and its value has to reach the wire.</p>
 *
 * <p>What makes that hold is ordering rather than a conditional. The client sets the header while
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

    private static CommunicationIdentityClient client(CapturingHttpClient transport) {
        return new CommunicationIdentityClientBuilder().connectionString(FAKE_CONNECTION_STRING)
            .httpClient(transport)
            .buildClient();
    }

    private static CommunicationIdentityAsyncClient asyncClient(CapturingHttpClient transport) {
        return new CommunicationIdentityClientBuilder().connectionString(FAKE_CONNECTION_STRING)
            .httpClient(transport)
            .buildAsyncClient();
    }

    @Test
    public void deleteUserSendsJsonAcceptByDefault() {
        CapturingHttpClient transport = new CapturingHttpClient();
        client(transport).deleteUserWithResponse(USER, Context.NONE);

        assertEquals("application/json", transport.accept);
    }

    @Test
    public void revokeTokensSendsJsonAcceptByDefault() {
        CapturingHttpClient transport = new CapturingHttpClient();
        client(transport).revokeTokensWithResponse(USER, Context.NONE);

        assertEquals("application/json", transport.accept);
    }

    @Test
    public void deleteUserSendsJsonAcceptByDefaultAsync() {
        CapturingHttpClient transport = new CapturingHttpClient();
        asyncClient(transport).deleteUser(USER).block();

        assertEquals("application/json", transport.accept);
    }

    @Test
    public void revokeTokensSendsJsonAcceptByDefaultAsync() {
        CapturingHttpClient transport = new CapturingHttpClient();
        asyncClient(transport).revokeTokens(USER).block();

        assertEquals("application/json", transport.accept);
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

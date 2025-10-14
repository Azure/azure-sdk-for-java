// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.OAuthTokenCredential;
import io.clientcore.core.credentials.oauth.OAuthTokenRequestContext;
import io.clientcore.core.http.models.AuthScheme;
import io.clientcore.core.http.models.AuthMetadata;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.http.HttpPipelineCallState;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OAuthBearerTokenAuthenticationPolicyTest {
    private OAuthTokenCredential mockCredential;
    private OAuthTokenRequestContext baseContext;
    private OAuthBearerTokenAuthenticationPolicy policy;

    @BeforeEach
    public void setup() {
        mockCredential = trc -> new AccessToken("dummy-token", OffsetDateTime.now());
        baseContext = new OAuthTokenRequestContext().setScopes(Collections.singletonList("test.scope"));
        policy = new OAuthBearerTokenAuthenticationPolicy(mockCredential, baseContext);
    }

    private static HttpRequest createHttpsRequestWithContext(RequestContext context) {
        return new HttpRequest().setContext(context)
            .setUri(URI.create("https://example.com"))
            .setMethod(HttpMethod.GET);
    }

    @Test
    public void testEmptyAuthSchemeNoBearerTokenSet() {
        AuthMetadata authMetadata = new AuthMetadata().setAuthSchemes(Collections.emptyList());
        RequestContext ctx = RequestContext.builder().putMetadata("IO_CLIENTCORE_AUTH_METADATA", authMetadata).build();
        HttpRequest request = createHttpsRequestWithContext(ctx);

        HttpPipelineNextPolicy next = new HttpPipelineNextMockPolicy(null);

        policy.process(request, next);
        assertNull(request.getHeaders().get(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void testNoAuthSchemeNoBearerTokenSet() {
        AuthMetadata authMetadata = new AuthMetadata().setAuthSchemes(Collections.emptyList());
        RequestContext ctx = RequestContext.builder().putMetadata("IO_CLIENTCORE_AUTH_METADATA", authMetadata).build();
        HttpRequest request = createHttpsRequestWithContext(ctx);

        HttpPipelineNextPolicy next = new HttpPipelineNextMockPolicy(null);

        policy.process(request, next);
        assertNull(request.getHeaders().get(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void testOAuth2SchemeTokenSetInHeader() {
        AuthMetadata authMetadata = new AuthMetadata().setAuthSchemes(Collections.singletonList(AuthScheme.OAUTH2));
        RequestContext ctx = RequestContext.builder().putMetadata("IO_CLIENTCORE_AUTH_METADATA", authMetadata).build();
        HttpRequest request = createHttpsRequestWithContext(ctx);

        HttpPipelineNextPolicy next = new HttpPipelineNextMockPolicy(null);

        policy.process(request, next);
        assertEquals("Bearer dummy-token", request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    @Test
    public void testNullAuthMetadataTokenSetInHeader() {
        RequestContext ctx = RequestContext.builder().build();
        HttpRequest request = createHttpsRequestWithContext(ctx);

        HttpPipelineNextPolicy next = new HttpPipelineNextMockPolicy(null);

        policy.process(request, next);
        assertEquals("Bearer dummy-token", request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
    }

    static class HttpPipelineNextMockPolicy extends HttpPipelineNextPolicy {
        /**
         * Package-private constructor. Creates an HttpPipelineNextPolicy instance.
         *
         * @param state The pipeline call state.
         */
        HttpPipelineNextMockPolicy(HttpPipelineCallState state) {
            super(state);
        }

        /**
         * Invokes the next {@link HttpPipelinePolicy}.
         *
         * @return The response.
         * @throws CoreException If an error occurs when sending the request or receiving the response.
         */
        public Response<BinaryData> process() {
            return new Response<>(null, 200, new HttpHeaders(), null);
        }

        /**
         * Copies the current state of the {@link HttpPipelineNextPolicy}.
         * <p>
         * This method must be used when a re-request is made in the pipeline.
         *
         * @return A new instance of this next pipeline policy.
         */
        public HttpPipelineNextPolicy copy() {
            return new HttpPipelineNextMockPolicy(null);
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.credentials.oauth.OAuthTokenRequestContext;
import io.clientcore.core.http.models.AuthMetadata;
import io.clientcore.core.http.models.AuthScheme;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class OAuthBearerTokenAuthenticationPolicyTest {
    private final boolean isAsync;
    private final OAuthBearerTokenAuthenticationPolicy policy;

    public OAuthBearerTokenAuthenticationPolicyTest(boolean isAsync) {
        this.isAsync = isAsync;
        policy = new OAuthBearerTokenAuthenticationPolicy(trc -> new AccessToken("dummy-token", OffsetDateTime.now()),
            new OAuthTokenRequestContext().setScopes(Collections.singletonList("test.scope")));
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
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(policy)
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        HttpRequest request = createHttpsRequestWithContext(ctx);
        try (Response<BinaryData> response = sendRequest(pipeline, request, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertNull(request.getHeaders().get(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void testNoAuthSchemeNoBearerTokenSet() {
        AuthMetadata authMetadata = new AuthMetadata().setAuthSchemes(Collections.emptyList());
        RequestContext ctx = RequestContext.builder().putMetadata("IO_CLIENTCORE_AUTH_METADATA", authMetadata).build();
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(policy)
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        HttpRequest request = createHttpsRequestWithContext(ctx);
        try (Response<BinaryData> response = sendRequest(pipeline, request, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertNull(request.getHeaders().get(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void testOAuth2SchemeTokenSetInHeader() {
        AuthMetadata authMetadata = new AuthMetadata().setAuthSchemes(Collections.singletonList(AuthScheme.OAUTH2));
        RequestContext ctx = RequestContext.builder().putMetadata("IO_CLIENTCORE_AUTH_METADATA", authMetadata).build();
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(policy)
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        HttpRequest request = createHttpsRequestWithContext(ctx);
        try (Response<BinaryData> response = sendRequest(pipeline, request, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertEquals("Bearer dummy-token", request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void testNullAuthMetadataTokenSetInHeader() {
        RequestContext ctx = RequestContext.builder().build();
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(policy)
            .httpClient(request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.empty()))
            .build();

        HttpRequest request = createHttpsRequestWithContext(ctx);
        try (Response<BinaryData> response = sendRequest(pipeline, request, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertEquals("Bearer dummy-token", request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }
}

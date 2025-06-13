// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.models.binarydata.BinaryData;

import java.time.OffsetDateTime;

/**
 * Common test utilities.
 */
public final class TestUtils {
    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }

    static class BeforeRedirectPolicy implements HttpPipelinePolicy {
        @Override
        public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
            httpRequest.getHeaders().set(HttpHeaderName.fromString("Custom-Header"), "Some Value");

            return next.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.BEFORE_REDIRECT;
        }
    }

    static class TestCredential implements TokenCredential {
        @Override
        public AccessToken getToken(TokenRequestContext request) {
            return new AccessToken("TestAccessToken", OffsetDateTime.now().plusHours(1));
        }
    }

    /**
     * HTTP Client builder for asserting HTTP operations.
     */
    public static class AssertingHttpClientBuilder {
        private final HttpClient httpClient;
        private boolean assertSync = false;

        public AssertingHttpClientBuilder(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public AssertingHttpClientBuilder assertSync() {
            this.assertSync = true;
            return this;
        }

        public HttpClient build() {
            return new AssertingHttpClient(httpClient, assertSync);
        }
    }

    private static class AssertingHttpClient implements HttpClient {
        private final HttpClient httpClient;
        private final boolean assertSync;

        AssertingHttpClient(HttpClient httpClient, boolean assertSync) {
            this.httpClient = httpClient;
            this.assertSync = assertSync;
        }

        @Override
        public Response<BinaryData> send(HttpRequest request) {
            return httpClient.send(request);
        }
    }
}

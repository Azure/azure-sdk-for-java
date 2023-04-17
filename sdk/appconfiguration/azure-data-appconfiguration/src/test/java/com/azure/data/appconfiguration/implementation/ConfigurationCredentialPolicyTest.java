// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationCredentialPolicyTest {
    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "Date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";

    private static final String CONNECTION_STRING = "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String HMAC_SHA256 = "HMAC-SHA256 Credential=";

    @SyncAsyncTest
    public void addsRequiredHeadersTest() throws NoSuchAlgorithmException, InvalidKeyException {
        ConfigurationCredentialsPolicy configurationCredentialsPolicy
            = new ConfigurationCredentialsPolicy(new ConfigurationClientCredentials(CONNECTION_STRING));

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final HttpHeaders headers = context.getHttpRequest().getHeaders();
            assertEquals("localhost", headers.get(HOST_HEADER).getValue());
            assertNotNull(headers.get(CONTENT_HASH_HEADER).getValue());
            assertNotNull(headers.get(DATE_HEADER).getValue());
            assertTrue(headers.get(AUTHORIZATION_HEADER).getValue().contains(HMAC_SHA256));
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(configurationCredentialsPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        );
    }

    private Mono<HttpResponse> sendRequest(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.send(new HttpRequest(httpMethod, new URL("http://localhost/")));
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.sendSync(new HttpRequest(httpMethod, new URL("http://localhost/")), Context.NONE);
    }

}

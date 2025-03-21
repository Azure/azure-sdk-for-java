// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIClientBuilderTest {

    @Test
    void testBuildClientWithKeyCredential() {
        OpenAIClientBuilder builder = new OpenAIClientBuilder().credential(new AzureKeyCredential("fake-key"))
            .endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithTokenCredential() {
        TokenCredential tokenCredential = new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext request) {
                return Mono.just(new AccessToken("fake-token", OffsetDateTime.now().plusHours(2)));
            }
        };

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().credential(tokenCredential).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithHttpClient() {
        HttpClient httpClient = HttpClient.createDefault();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().httpClient(httpClient).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithHttpPipeline() {
        HttpPipeline pipeline = new HttpPipelineBuilder().build();

        OpenAIClientBuilder builder = new OpenAIClientBuilder().pipeline(pipeline).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithHttpLogOptions() {
        HttpLogOptions logOptions = new HttpLogOptions();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().httpLogOptions(logOptions).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithConfiguration() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().configuration(configuration).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }
}

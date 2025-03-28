// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.*;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIClientBuilderTest {

    @Test
    void testBuildClientWithKeyCredential() {
        OpenAIClientBuilder builder = new OpenAIClientBuilder().credential(new AzureKeyCredential("fake-key"))
            .endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithTokenCredential() throws NoSuchFieldException, IllegalAccessException {
        TokenCredential tokenCredential = (request) -> {
            assertNotNull(request, "The request parameter should not be null");
            assertTrue(request.getScopes().isEmpty(), "The request scopes should be empty");
            return Mono.just(new AccessToken("fake-token", OffsetDateTime.now().plusHours(2)));
        };

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().credential(tokenCredential).endpoint("https://api.openai.com/");

        OpenAIClient client = builder.buildClient();
        assertNotNull(client, "The client should not be null");

        // Access private fields using reflection
        Field endpointField = OpenAIClientBuilder.class.getDeclaredField("endpoint");
        endpointField.setAccessible(true);
        String endpoint = (String) endpointField.get(builder);
        assertEquals("https://api.openai.com/", endpoint, "The endpoint should be correctly set");

        Field tokenCredentialField = OpenAIClientBuilder.class.getDeclaredField("tokenCredential");
        tokenCredentialField.setAccessible(true);
        TokenCredential actualTokenCredential = (TokenCredential) tokenCredentialField.get(builder);
        assertSame(tokenCredential, actualTokenCredential, "The token credential should be correctly set");
    }

    @Test
    void testBuildClientWithHttpClient() {
        HttpClient httpClient = HttpClient.createDefault();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().httpClient(httpClient).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testHttpLogOptionsSetter() throws NoSuchFieldException, IllegalAccessException {
        HttpLogOptions httpLogOptions = new HttpLogOptions();
        OpenAIClientBuilder builder = new OpenAIClientBuilder().httpLogOptions(httpLogOptions);

        Field httpLogOptionsField = OpenAIClientBuilder.class.getDeclaredField("httpLogOptions");
        httpLogOptionsField.setAccessible(true);
        HttpLogOptions actualHttpLogOptions = (HttpLogOptions) httpLogOptionsField.get(builder);

        assertEquals(httpLogOptions, actualHttpLogOptions);
        assertNotNull(actualHttpLogOptions);
    }

    @Test
    void testPipelineSetter() throws NoSuchFieldException, IllegalAccessException {
        HttpPipeline pipeline = new HttpPipelineBuilder().build();
        OpenAIClientBuilder builder = new OpenAIClientBuilder().pipeline(pipeline);

        Field pipelineField = OpenAIClientBuilder.class.getDeclaredField("pipeline");
        pipelineField.setAccessible(true);
        HttpPipeline actualPipeline = (HttpPipeline) pipelineField.get(builder);

        assertEquals(pipeline, actualPipeline);
        assertNotNull(actualPipeline);
    }

    @Test
    void testBuildClientWithHttpPipeline() throws NoSuchFieldException, IllegalAccessException {
        HttpPipeline pipeline = new HttpPipelineBuilder().build();

        OpenAIClientBuilder builder = new OpenAIClientBuilder().pipeline(pipeline).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());

        Field pipelineField = OpenAIClientBuilder.class.getDeclaredField("pipeline");
        pipelineField.setAccessible(true);
        HttpPipeline actualPipeline = (HttpPipeline) pipelineField.get(builder);

        assertEquals(pipeline, actualPipeline);
    }

    @Test
    void testBuildClientWithHttpLogOptions() {
        HttpLogOptions logOptions = new HttpLogOptions();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().httpLogOptions(logOptions).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testBuildClientWithRetryPolicy() {
        RetryPolicy retryPolicy = new RetryPolicy();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().retryPolicy(retryPolicy).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }

    @Test
    void testAddPolicy() {
        HttpPipelinePolicy customPolicy = (context, next) -> {
            assertNotNull(context, "The context parameter should not be null");
            return next.process();
        };

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().addPolicy(customPolicy).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());

        try {
            Field pipelinePoliciesField = OpenAIClientBuilder.class.getDeclaredField("pipelinePolicies");
            pipelinePoliciesField.setAccessible(true);
            List<?> rawList = (List<?>) pipelinePoliciesField.get(builder);
            List<HttpPipelinePolicy> pipelinePolicies = rawList.stream()
                .filter(HttpPipelinePolicy.class::isInstance)
                .map(HttpPipelinePolicy.class::cast)
                .collect(Collectors.toList());

            assertTrue(pipelinePolicies.contains(customPolicy));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    void testBuildClientWithConfiguration() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        OpenAIClientBuilder builder
            = new OpenAIClientBuilder().configuration(configuration).endpoint("https://api.openai.com/");

        assertNotNull(builder.buildClient());
    }
}

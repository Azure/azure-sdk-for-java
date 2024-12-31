// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai;

import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.OPEN_AI_HOST_PATTERN;
import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.isOpenAiEndpoint;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.OPEN_AI_ENDPOINT;

public class OpenAIClientBuilderTest {
    @Test
    public void testHostPattern() {
        assertTrue(OPEN_AI_HOST_PATTERN.matcher("eu.api.openai.com").matches());
        assertTrue(OPEN_AI_HOST_PATTERN.matcher("asdf.api.openai.com").matches());
        assertFalse(OPEN_AI_HOST_PATTERN.matcher("dead.beef.api.openai.com").matches());
        assertFalse(OPEN_AI_HOST_PATTERN.matcher("api.openai.com.org").matches());
    }

    @Test
    public void testEndpointClassificationHelper() {
        assertTrue(isOpenAiEndpoint("https://eu.api.openai.com/v1"));
        assertTrue(isOpenAiEndpoint("https://asdf.api.openai.com/v1"));
        assertFalse(isOpenAiEndpoint("http://api.openai.com/v1"));
        assertFalse(isOpenAiEndpoint("https://api.openai.com/"));
        assertFalse(isOpenAiEndpoint("https://dead.beef.api.openai.com/v1"));
        assertFalse(isOpenAiEndpoint("https://api.openai.com.org/v1"));
        assertFalse(isOpenAiEndpoint("https://api.openai.com/v2"));
        assertTrue(isOpenAiEndpoint("HTTPS://api.openai.com/v1"));
        assertTrue(isOpenAiEndpoint("https://api.OPENAI.com/v1"));
        assertTrue(isOpenAiEndpoint("https://api.openai.com/v1/foo/bar"));
    }

    @Test
    public void testInnerImplBasedOnEndpoint() throws NoSuchFieldException, IllegalAccessException {
        Field azureClient = OpenAIClient.class.getDeclaredField("serviceClient");
        azureClient.setAccessible(true);
        Field nonAzureClient = OpenAIClient.class.getDeclaredField("openAIServiceClient");
        nonAzureClient.setAccessible(true);

        OpenAIClient nullEndpointClient = new OpenAIClientBuilder().buildClient();
        assertNull(azureClient.get(nullEndpointClient));
        assertNotNull(nonAzureClient.get(nullEndpointClient));

        OpenAIClient customEndpointClient
            = new OpenAIClientBuilder().endpoint("https://my.custom.domain/").buildClient();
        assertNotNull(azureClient.get(customEndpointClient));
        assertNull(nonAzureClient.get(customEndpointClient));

        OpenAIClient defaultEndpointClient = new OpenAIClientBuilder().endpoint(OPEN_AI_ENDPOINT).buildClient();
        assertNull(azureClient.get(defaultEndpointClient));
        assertNotNull(nonAzureClient.get(defaultEndpointClient));

        OpenAIClient euEndpointClient
            = new OpenAIClientBuilder().endpoint("https://eu.api.openai.com/v1").buildClient();
        assertNull(azureClient.get(euEndpointClient));
        assertNotNull(nonAzureClient.get(euEndpointClient));
    }
}

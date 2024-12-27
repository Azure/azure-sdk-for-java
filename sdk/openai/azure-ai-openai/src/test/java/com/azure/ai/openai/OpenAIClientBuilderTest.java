// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai;

import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.OPEN_AI_ENDPOINT_PATTERN;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.OPEN_AI_ENDPOINT;

public class OpenAIClientBuilderTest {
    @Test
    public void testEndpointPattern() {
        assertTrue(OPEN_AI_ENDPOINT_PATTERN.matcher(OPEN_AI_ENDPOINT).matches());
        assertTrue(OPEN_AI_ENDPOINT_PATTERN.matcher("https://eu.api.openai.com/v1").matches());
        assertTrue(OPEN_AI_ENDPOINT_PATTERN.matcher("https://asdf.api.openai.com/v1").matches());
        assertFalse(OPEN_AI_ENDPOINT_PATTERN.matcher("http://api.openai.com/v1").matches());
        assertFalse(OPEN_AI_ENDPOINT_PATTERN.matcher("https://api.openai.com/").matches());
        assertFalse(OPEN_AI_ENDPOINT_PATTERN.matcher("https://dead.beef.api.openai.com/v1").matches());
        assertFalse(OPEN_AI_ENDPOINT_PATTERN.matcher("https://api.openai.com.org/v1").matches());
        assertFalse(OPEN_AI_ENDPOINT_PATTERN.matcher("https://api.openai.com/v2").matches());
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

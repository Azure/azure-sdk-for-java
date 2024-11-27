// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.EU_OPEN_AI_ENDPOINT;
import static com.azure.ai.openai.implementation.NonAzureOpenAIClientImpl.OPEN_AI_ENDPOINT;

public class OpenAIClientBuilderTest {
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

        OpenAIClient euEndpointClient = new OpenAIClientBuilder().endpoint(EU_OPEN_AI_ENDPOINT).buildClient();
        assertNull(azureClient.get(euEndpointClient));
        assertNotNull(nonAzureClient.get(euEndpointClient));
    }
}

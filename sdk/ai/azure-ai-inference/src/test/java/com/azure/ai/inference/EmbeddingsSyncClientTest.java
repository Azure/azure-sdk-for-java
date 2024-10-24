// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.inference;

import com.azure.ai.inference.models.*;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.inference.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class EmbeddingsSyncClientTest extends EmbeddingsClientTestBase {
    private EmbeddingsClient client;

    private EmbeddingsClient getEmbeddingsClient(HttpClient httpClient) {
        return getEmbeddingsClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetEmbeddings(HttpClient httpClient) {
        client = getEmbeddingsClient(httpClient);
        getEmbeddingsRunner((promptList) -> {
            EmbeddingsResult result = client.embed(promptList);
            assertEmbeddings(result);
        });
    }

}

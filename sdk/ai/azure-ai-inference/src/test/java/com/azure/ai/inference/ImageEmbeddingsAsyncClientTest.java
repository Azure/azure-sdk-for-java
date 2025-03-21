// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.inference;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.inference.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageEmbeddingsAsyncClientTest extends EmbeddingsClientTestBase {
    private ImageEmbeddingsAsyncClient client;

    private ImageEmbeddingsAsyncClient getImageEmbeddingsAsyncClient(HttpClient httpClient) {
        return getImageEmbeddingsClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
                .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetEmbeddings(HttpClient httpClient) {
        client = getImageEmbeddingsAsyncClient(httpClient);
        getImageEmbeddingsRunner((inputList) -> {
            StepVerifier.create(client.embed(inputList)).assertNext(result -> {
                assertNotNull(result.getUsage());
                assertEmbeddings(result);
            }).verifyComplete();
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.inference;

import com.azure.ai.inference.models.*;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.inference.TestUtils.TEST_IMAGE_PATH;
import static com.azure.ai.inference.TestUtils.TEST_IMAGE_FORMAT;
import static com.azure.ai.inference.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class ImageEmbeddingsSyncClientTest extends EmbeddingsClientTestBase {
    private ImageEmbeddingsClient client;

    private ImageEmbeddingsClient getImageEmbeddingsClient(HttpClient httpClient) {
        return getImageEmbeddingsClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient).buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetEmbeddings(HttpClient httpClient) {
        Path testFilePath = Paths.get(TEST_IMAGE_PATH);
        List<ImageEmbeddingInput> inputList = new ArrayList<>();
        inputList.add(new ImageEmbeddingInput(testFilePath, TEST_IMAGE_FORMAT));
        client = getImageEmbeddingsClient(httpClient);
        EmbeddingsResult result = client.embed(inputList);
        assertEmbeddings(result);
    }

}

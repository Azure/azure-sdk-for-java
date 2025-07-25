// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.openai.models.embeddings.EmbeddingModel;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

public class TestUtils {
    static final String AZURE_OPEN_AI = "AZURE_OPEN_AI";
    static final String OPEN_AI = "OPEN_AI";
    static final String PREVIEW = "PREVIEW";
    static final String GA = "GA";
    static final String V1 = "v1";

    static final String GPT_3_5_TURBO = "gpt-3.5-turbo";
    private static final String GPT_4_0613 = "gpt-4-0613";
    private static final String GPT_4_1106_PREVIEW = "gpt-4-1106-preview";
    private static final String GPT_4_VISION_PREVIEW = "gpt-4-vision-preview";
    private static final String GPT_35_TURBO_1106 = "gpt-35-turbo-1106";
    private static final String GPT_4O = "gpt-4o";
    private static final String WHISPER = "whisper";
    private static final String DALL_E_3 = "dall-e-3";

    static boolean isAzureConfigMissing() {
        return isAzureEndpointMissing() || isAzureApiKeyMissing();
    }

    static boolean isAzureByodConfigMissing() {
        return isAzureSearchConfigMissing() || isAzureEndpointMissing() || isAzureApiKeyMissing();
    }

    static boolean isAzureSearchConfigMissing() {
        return System.getenv("AZURE_SEARCH_API_KEY") == null
            || System.getenv("AZURE_SEARCH_ENDPOINT") == null
            || System.getenv("AZURE_OPENAI_SEARCH_INDEX") == null;
    }

    static boolean isAzureEndpointMissing() {
        return System.getenv("AZURE_OPENAI_ENDPOINT") == null;
    }

    static boolean isAzureApiKeyMissing() {
        return System.getenv("AZURE_OPENAI_KEY") == null;
    }

    static Stream<Arguments> allApiTypeClient() {
        return Stream.of(
            //            Arguments.of(AZURE_OPEN_AI, GA, GPT_4O),
            Arguments.of(AZURE_OPEN_AI, PREVIEW, GPT_4O), Arguments.of(OPEN_AI, V1, GPT_4O));
    }

    static Stream<Arguments> azureOnlyClient() {
        return Stream.of(Arguments.of(AZURE_OPEN_AI, GA, GPT_4O), Arguments.of(AZURE_OPEN_AI, PREVIEW, GPT_4O));
    }

    static Stream<Arguments> audioOnlyClient() {
        return Stream.of(Arguments.of(AZURE_OPEN_AI, GA, WHISPER), Arguments.of(AZURE_OPEN_AI, PREVIEW, WHISPER));
    }

    static Stream<Arguments> allApiImageClient() {
        return Stream.of(Arguments.of(AZURE_OPEN_AI, GA, DALL_E_3), Arguments.of(OPEN_AI, V1, DALL_E_3));
    }

    static Stream<Arguments> azureByodOnlyClient() {
        return Stream.of(
            //            Arguments.of(AZURE_OPEN_AI, GA, GPT_4_0613),
            Arguments.of(AZURE_OPEN_AI, PREVIEW, GPT_4_0613));
    }

    static Stream<Arguments> azureBlockListTermOnlyClient() {
        return Stream.of(Arguments.of(AZURE_OPEN_AI, GA, GPT_4_1106_PREVIEW),
            Arguments.of(AZURE_OPEN_AI, PREVIEW, GPT_4_1106_PREVIEW));
    }

    static Stream<Arguments> openAiOnlyClient() {
        return Stream.of(Arguments.of(OPEN_AI, V1, GPT_3_5_TURBO));
    }

    static Stream<Arguments> visionOnlyClient() {
        return Stream.of(
            //            Arguments.of(AZURE_OPEN_AI, GA, GPT_4_VISION_PREVIEW),
            Arguments.of(AZURE_OPEN_AI, PREVIEW, GPT_4_VISION_PREVIEW), Arguments.of(OPEN_AI, V1, GPT_4O));
    }

    static Stream<Arguments> azureAdTokenOnly() {
        return Stream.of(Arguments.of(AZURE_OPEN_AI, GA, GPT_4O), Arguments.of(AZURE_OPEN_AI, PREVIEW, GPT_4O));
    }

    static Stream<Arguments> azureOnlyClientWithEmbedding() {
        return Stream.of(Arguments.of("AZURE_OPEN_AI", "GA", EmbeddingModel.TEXT_EMBEDDING_ADA_002.toString()),
            Arguments.of("AZURE_OPEN_AI", "PREVIEW", EmbeddingModel.TEXT_EMBEDDING_ADA_002.toString()));
    }
}

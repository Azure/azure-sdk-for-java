package com.azure.openai.tests;

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
    private static final String GPT_35_TURBO_0613 = "gpt-35-turbo-0613";
    private static final String GPT_35_TURBO_1106 = "gpt-35-turbo-1106";

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

    static Stream<String[]> allApiTypeClient() {
        return Stream.of(new String[][] {
            {AZURE_OPEN_AI, GA, GPT_35_TURBO_1106},
            {AZURE_OPEN_AI, PREVIEW, GPT_35_TURBO_1106},
            {OPEN_AI, V1, GPT_3_5_TURBO}
        });
    }

    static Stream<String[]> azureOnlyClient() {
        return Stream.of(new String[][] {
            {AZURE_OPEN_AI, GA, GPT_35_TURBO_1106},
            {AZURE_OPEN_AI, PREVIEW, GPT_35_TURBO_1106}
        });
    }

    static Stream<String[]> azureByodOnlyClient() {
        return Stream.of(new String[][] {
            {AZURE_OPEN_AI, GA, GPT_4_0613},
            {AZURE_OPEN_AI, PREVIEW, GPT_4_0613}
        });
    }

    static Stream<String[]> azureBlockListTermOnlyClient() {
        return Stream.of(new String[][] {
            {AZURE_OPEN_AI, GA, GPT_4_1106_PREVIEW},
            {AZURE_OPEN_AI, PREVIEW, GPT_4_1106_PREVIEW}
        });
    }

    static Stream<String[]> openAiOnlyClient() {
        return Stream.of(new String[][] {{OPEN_AI, V1, GPT_3_5_TURBO}});
    }

    static Stream<String[]> visionOnlyClient() {
        return Stream.of(new String[][] {
            {AZURE_OPEN_AI, GA, GPT_4_VISION_PREVIEW},
            {AZURE_OPEN_AI, PREVIEW, GPT_4_VISION_PREVIEW},
            {OPEN_AI, V1, GPT_4_VISION_PREVIEW}
        });
    }

    static Stream<String[]> azureAdTokenOnly() {
        return Stream.of(new String[][] {
            {AZURE_OPEN_AI, GA, GPT_35_TURBO_0613},
            {AZURE_OPEN_AI, PREVIEW, GPT_35_TURBO_0613}
        });
    }
}

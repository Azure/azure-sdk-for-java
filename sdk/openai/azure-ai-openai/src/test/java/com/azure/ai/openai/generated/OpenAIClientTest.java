// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.generated;

import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.openai.generated.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class OpenAIClientTest extends OpenAIClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(OpenAIClientTest.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        getCompletionsRunner((deploymentId, prompt) -> {
            Completions resultCompletions = openAIClient.getCompletions(deploymentId, new CompletionsOptions(prompt));
            assertCompletions(getExpectedCompletion("dumpId", 1999,
                    Arrays.asList(getExpectedChoice("dumpText", 0, null, null)),
                    null),
                resultCompletions);
        });
    }
}

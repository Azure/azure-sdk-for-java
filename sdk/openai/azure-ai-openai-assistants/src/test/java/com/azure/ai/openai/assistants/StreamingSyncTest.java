package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.core.http.HttpClient;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.assistants.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class StreamingSyncTest extends AssistantsClientTestBase {

    private AssistantsClient client;

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        client = getAssistantsClient(httpClient);

        client = getAssistantsClient(httpClient);
        String mathTutorAssistantId = createMathTutorAssistant(client);
        createThreadAndRunRunner(createAndRunThreadOptions -> {

            IterableStream<StreamUpdate> streamEvents = client.createThreadAndRunStream(createAndRunThreadOptions);

            streamEvents.forEach(streamUpdate -> {
                System.out.println("StreamUpdate: " + streamUpdate);
            });
        }, mathTutorAssistantId);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runThreadWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        // TODO
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runSimpleTest(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        // TODO
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.assistants.TestUtils#getTestParameters")
    public void runWithTools(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        // TODO
    }
}

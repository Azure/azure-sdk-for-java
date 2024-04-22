package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.AssistantsClientTestBase;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIServerSentEventsTest {

    @Test
    public void testOpenAIServerSentEvents() {
        BinaryData testFile = BinaryData.fromFile(AssistantsClientTestBase.openResourceFile("create_thread_run.dump"));
        OpenAIServerSentEvents<String> openAIServerSentEvents = new OpenAIServerSentEvents(testFile.toFluxByteBuffer());

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .assertNext(event -> {
                assertTrue(StringUtils.isNotBlank(event));
            })
            .verifyComplete();
    }
}

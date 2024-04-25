package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.AssistantsClientTestBase;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIServerSentEventsTest {

    @Test
    public void testOpenAIServerSentEvents() {
        BinaryData testFile = BinaryData.fromFile(AssistantsClientTestBase.openResourceFile("create_thread_run.dump"));
        OpenAIServerSentEvents<String> openAIServerSentEvents = new OpenAIServerSentEvents(testFile.toFluxByteBuffer());

        AtomicInteger i = new AtomicInteger();
        System.out.println("Events: ");
        StepVerifier.create(openAIServerSentEvents.getEvents()
                .doOnEach(event -> {
                    System.out.println("Event " + i.getAndIncrement() + ": ");
                    System.out.println(event + "\n");
                })
            )
                            .expectNextCount(30)
            .verifyComplete();
    }
}

package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.ai.openai.assistants.AssistantsClientTestBase;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIServerSentEventsTest {

    @Test
    public void eventsEmissionCountWithCarriageReturn() {
        BinaryData testFile = BinaryData.fromFile(AssistantsClientTestBase.openResourceFile("create_thread_run.dump"));
        OpenAIServerSentEvents<StreamUpdate> openAIServerSentEvents = new OpenAIServerSentEvents<>(testFile.toFluxByteBuffer());

        // data: [DONE] is the last event in the file, but is not emitted by the Flux
        StepVerifier.create(
            openAIServerSentEvents.getEvents()
                .doOnNext(event -> {
                    assertFalse(BinaryData.fromObject(event).toString().isBlank());
                })
            ).expectNextCount(30)
            .verifyComplete();
    }

    @Test
    public void eventChunkCarriageReturnSeparatedOnly() {

        BinaryData testInput = BinaryData.fromString("data: \r\ndata: \r\n");


        OpenAIServerSentEvents<String> openAIServerSentEvents = new OpenAIServerSentEvents<>(testInput.toFluxByteBuffer(), (currentEvent, outputValues) -> {
            outputValues.add(currentEvent);
        });

        StepVerifier.create(
            openAIServerSentEvents.getEvents()
                .doOnNext(event -> {
                    assertTrue(event.isBlank());
                })
            ).expectNextCount(2)
            .verifyComplete();
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.streaming;

import com.azure.ai.openai.OpenAIClientTestBase;
import com.azure.ai.openai.responses.AzureResponsesTestBase;
import com.azure.ai.openai.responses.implementation.OpenAIServerSentEvents;
import com.azure.ai.openai.responses.models.ResponsesStreamEventCompleted;
import com.azure.ai.openai.responses.models.ResponsesStreamEventContentPartAdded;
import com.azure.ai.openai.responses.models.ResponsesStreamEventContentPartDone;
import com.azure.ai.openai.responses.models.ResponsesStreamEventCreated;
import com.azure.ai.openai.responses.models.ResponsesStreamEventError;
import com.azure.ai.openai.responses.models.ResponsesStreamEventFileSearchCallCompleted;
import com.azure.ai.openai.responses.models.ResponsesStreamEventFunctionCallArgumentsDelta;
import com.azure.ai.openai.responses.models.ResponsesStreamEventFunctionCallArgumentsDone;
import com.azure.ai.openai.responses.models.ResponsesStreamEventOutputItemAdded;
import com.azure.ai.openai.responses.models.ResponsesStreamEventOutputItemDone;
import com.azure.ai.openai.responses.models.ResponsesStreamEventOutputTextDelta;
import com.azure.ai.openai.responses.models.ResponsesStreamEventOutputTextDone;
import com.azure.ai.openai.responses.models.ResponsesStreamEventRefusalDelta;
import com.azure.ai.openai.responses.models.ResponsesStreamEventRefusalDone;
import com.azure.ai.openai.responses.models.ResponsesStreamEventType;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class OpenAIServerSentEventsTest {

    @Test
    public void fromFileDump() {
        BinaryData testFile = BinaryData.fromFile(OpenAIClientTestBase.openResourceFile("create_response_stream.dump"));
        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testFile.toFluxByteBuffer());

        StepVerifier.create(openAIServerSentEvents.getEvents().doOnNext(AzureResponsesTestBase::assertStreamUpdate))
            .expectNextCount(17)
            .verifyComplete();
    }

    @Test
    public void dataSplitAcrossByteBuffers() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesStreamEventCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void errorEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: error\n".getBytes()),
            ByteBuffer.wrap("data: {\"message\":\"An error occurred\",\"type\":\"error\"}\n\n".getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesStreamEventError.class, event);
        }).verifyComplete();
    }

    @Test
    public void testResponseCompleteEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.completed\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_2\",\"object\":\"response\",\"created_at\":1710548045,\"status\":\"completed\",\"model\":\"model-2\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventCompleted
                && event.getType() == ResponsesStreamEventType.RESPONSE_COMPLETED
                && ((ResponsesStreamEventCompleted) event).getResponse().getId().equals("resp_2"))
            .verifyComplete();
    }

    @Test
    public void testResponseContentPartAddedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.content_part.added\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.content_part.added\",\"item_id\":\"item_1\",\"output_index\":0,\"content_index\":0,\"part\":{\"text\":\"part_text\"}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventContentPartAdded
                && event.getType() == ResponsesStreamEventType.RESPONSE_CONTENT_PART_ADDED)
            .verifyComplete();
    }

    @Test
    public void testResponseContentPartDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.content_part.done\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.content_part.done\",\"item_id\":\"item_2\",\"output_index\":1,\"content_index\":1,\"part\":{\"text\":\"done_text\"}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventContentPartDone
                && event.getType() == ResponsesStreamEventType.RESPONSE_CONTENT_PART_DONE)
            .verifyComplete();
    }

    @Test
    public void testResponseFileSearchCallCompletedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.file_search_call.completed\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.file_search_call.completed\",\"file_search_call\":{\"id\":\"search_1\",\"status\":\"completed\"}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventFileSearchCallCompleted
                && event.getType() == ResponsesStreamEventType.RESPONSE_FILE_SEARCH_CALL_COMPLETED)
            .verifyComplete();
    }

    @Test
    public void testResponseFunctionCallArgumentsDeltaEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.function_call_arguments.delta\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.function_call_arguments.delta\",\"item_id\":\"item_3\",\"output_index\":3,\"call_id\":\"call_2\",\"delta\":\"{\\\"arg\\\":\\\"value\\\"}\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventFunctionCallArgumentsDelta
                && event.getType() == ResponsesStreamEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA)
            .verifyComplete();
    }

    @Test
    public void testResponseFunctionCallArgumentsDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.function_call_arguments.done\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.function_call_arguments.done\",\"item_id\":\"item_4\",\"output_index\":4,\"call_id\":\"call_3\",\"arguments\":\"{\\\"arg\\\":\\\"final_value\\\"}\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventFunctionCallArgumentsDone
                && event.getType() == ResponsesStreamEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE)
            .verifyComplete();
    }

    @Test
    public void testResponseOutputItemAddedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.output_item.added\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.output_item.added\",\"output_index\":0,\"item\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":[]}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventOutputItemAdded
                && event.getType() == ResponsesStreamEventType.RESPONSE_OUTPUT_ITEM_ADDED)
            .verifyComplete();
    }

    @Test
    public void testResponseOutputTextDeltaEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.output_text.delta\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.output_text.delta\",\"item_id\":\"msg_1\",\"output_index\":0,\"content_index\":0,\"delta\":\"Hi\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventOutputTextDelta
                && event.getType() == ResponsesStreamEventType.RESPONSE_OUTPUT_TEXT_DELTA
                && ((ResponsesStreamEventOutputTextDelta) event).getDelta() != null)
            .verifyComplete();
    }

    @Test
    public void testResponseOutputTextDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.output_text.done\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.output_text.done\",\"item_id\":\"msg_1\",\"output_index\":0,\"content_index\":0,\"text\":\"Hi there!\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventOutputTextDone
                && event.getType() == ResponsesStreamEventType.RESPONSE_OUTPUT_TEXT_DONE)
            .verifyComplete();
    }

    @Test
    public void testFileSearchCallCompletedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.file_search_call.completed\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.file_search_call.completed\",\"output_index\":0,\"file_search_call\":{}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventFileSearchCallCompleted
                && event.getType() == ResponsesStreamEventType.RESPONSE_FILE_SEARCH_CALL_COMPLETED)
            .verifyComplete();
    }

    @Test
    public void testResponseRefusalDeltaEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.refusal.delta\n".getBytes()),
            ByteBuffer
                .wrap("data: {\"type\":\"response.refusal.delta\",\"output_index\":0,\"delta\":\"Not allowed\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventRefusalDelta
                && event.getType() == ResponsesStreamEventType.RESPONSE_REFUSAL_DELTA)
            .verifyComplete();
    }

    @Test
    public void testResponseRefusalDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.refusal.done\n".getBytes()),
            ByteBuffer.wrap("data: {\"type\":\"response.refusal.done\",\"output_index\":0,\"text\":\"Not allowed\"}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventRefusalDone
                && event.getType() == ResponsesStreamEventType.RESPONSE_REFUSAL_DONE)
            .verifyComplete();
    }

    @Test
    public void testResponseOutputItemDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.output_item.done\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.output_item.done\",\"output_index\":0,\"item\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":[]}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesStreamEventOutputItemDone
                && event.getType() == ResponsesStreamEventType.RESPONSE_OUTPUT_ITEM_DONE)
            .verifyComplete();
    }
}

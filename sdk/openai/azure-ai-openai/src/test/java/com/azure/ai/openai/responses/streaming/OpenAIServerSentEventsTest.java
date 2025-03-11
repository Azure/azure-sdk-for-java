// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.streaming;

import com.azure.ai.openai.OpenAIClientTestBase;
import com.azure.ai.openai.responses.AzureResponsesTestBase;
import com.azure.ai.openai.responses.implementation.OpenAIServerSentEvents;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEvent;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseCodeInterpreterCallCodeDelta;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseCodeInterpreterCallCodeDone;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseCodeInterpreterCallCompleted;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseCompleted;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseContentPartAdded;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseContentPartDone;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseCreated;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseFileSearchCallCompleted;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseFunctionCallArgumentsDelta;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseFunctionCallArgumentsDone;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseOutputItemAdded;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseOutputItemDone;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseOutputTextDelta;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseOutputTextDone;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseRefusalDelta;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventResponseRefusalDone;
import com.azure.ai.openai.responses.models.ResponsesResponseStreamEventType;
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
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void dataSplitAcrossByteBuffers() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void errorEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: error\n".getBytes()),
            ByteBuffer.wrap("data: {\"message\":\"An error occurred\"}\n\n".getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEvent.class, event);
        }).verifyComplete();
    }

    @Test
    public void byteBufferCutsOffAfterEventMarker() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void byteBufferCutsOffInBetweenEventMarker() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("eve".getBytes()),
            ByteBuffer.wrap("nt: response.created\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void byteBufferCutsOffAfterDataMarker() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void byteBufferCutsOffInBetweenDataMarker() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()),
            ByteBuffer.wrap("dat".getBytes()),
            ByteBuffer.wrap(
                "a: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void jsonSplitAcrossByteBuffers() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void eventMarkerSplitAcrossByteBuffers() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents()).assertNext(event -> {
            assertInstanceOf(ResponsesResponseStreamEventResponseCreated.class, event);
        }).verifyComplete();
    }

    @Test
    public void testResponseCreatedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.created\n".getBytes()), ByteBuffer.wrap(
            "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"created_at\":1710548044,\"status\":\"in_progress\",\"model\":\"model-1\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseCreated
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CREATED
                && ((ResponsesResponseStreamEventResponseCreated) event).getResponse().getId().equals("resp_1"))
            .verifyComplete();
    }

    @Test
    public void testResponseCompleteEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.completed\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_2\",\"object\":\"response\",\"created_at\":1710548045,\"status\":\"completed\",\"model\":\"model-2\",\"previous_response_id\":null,\"output\":[],\"error\":null,\"tools\":[],\"top_p\":1.0,\"temperature\":1.0,\"reasoning_effort\":null,\"usage\":null,\"metadata\":{}}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseCompleted
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_COMPLETED
                && ((ResponsesResponseStreamEventResponseCompleted) event).getResponse().getId().equals("resp_2"))
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseContentPartAdded
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CONTENT_PART_ADDED)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseContentPartDone
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CONTENT_PART_DONE)
            .verifyComplete();
    }

    @Test
    public void testResponseCodeInterpreterCallCompletedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.code_interpreter_call.completed\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.code_interpreter_call.completed\",\"output_index\":2,\"code_interpreter_call\":{\"id\":\"call_1\",\"status\":\"completed\"}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(
                event -> event instanceof ResponsesResponseStreamEventResponseCodeInterpreterCallCompleted
                    && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CODE_INTERPRETER_CALL_COMPLETED)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseFileSearchCallCompleted
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_FILE_SEARCH_CALL_COMPLETED)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseFunctionCallArgumentsDelta
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseFunctionCallArgumentsDone
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE)
            .verifyComplete();
    }

    //    @Test
    //    public void testCodeInterpreterCallInProgressEvent() {
    //        Flux<ByteBuffer> testInput = Flux.just(
    //            ByteBuffer.wrap("event: response.code_interpreter_call.in_progress\n".getBytes()),
    //            ByteBuffer.wrap(
    //                "data: {\"type\":\"response.code_interpreter_call.in_progress\",\"output_index\":1,\"code_interpreter_call\":{}}\n\n"
    //                    .getBytes()));
    //
    //        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);
    //
    //        StepVerifier.create(openAIServerSentEvents.getEvents())
    //            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventCodeInterpreterCallInProgress
    //                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS)
    //            .verifyComplete();
    //    }
    //
    //    @Test
    //    public void testCodeInterpreterCallInterpretingEvent() {
    //        Flux<ByteBuffer> testInput = Flux.just(
    //            ByteBuffer.wrap("event: response.code_interpreter_call.interpreting\n".getBytes()),
    //            ByteBuffer.wrap(
    //                "data: {\"type\":\"response.code_interpreter_call.interpreting\",\"output_index\":2,\"code_interpreter_call\":{}}\n\n"
    //                    .getBytes()));
    //
    //        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);
    //
    //        StepVerifier.create(openAIServerSentEvents.getEvents())
    //            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventCodeInterpreterCallInterpreting
    //                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CODE_INTERPRETER_CALL_INTERPRETING)
    //            .verifyComplete();
    //    }
    //
    //    @Test
    //    public void testFileSearchCallInProgressEvent() {
    //        Flux<ByteBuffer> testInput = Flux.just(
    //            ByteBuffer.wrap("event: response.file_search_call.in_progress\n".getBytes()),
    //            ByteBuffer.wrap(
    //                "data: {\"type\":\"response.file_search_call.in_progress\",\"output_index\":3,\"file_search_call\":{}}\n\n"
    //                    .getBytes()));
    //
    //        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);
    //
    //        StepVerifier.create(openAIServerSentEvents.getEvents())
    //            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventFileSearchCallInProgress
    //                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_FILE_SEARCH_CALL_IN_PROGRESS)
    //            .verifyComplete();
    //    }
    //
    //    @Test
    //    public void testFileSearchCallSearchingEvent() {
    //        Flux<ByteBuffer> testInput = Flux.just(
    //            ByteBuffer.wrap("event: response.file_search_call.searching\n".getBytes()),
    //            ByteBuffer.wrap(
    //                "data: {\"type\":\"response.file_search_call.searching\",\"output_index\":4,\"file_search_call\":{}}\n\n"
    //                    .getBytes()));
    //
    //        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);
    //
    //        StepVerifier.create(openAIServerSentEvents.getEvents())
    //            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventFileSearchCallSearching
    //                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_FILE_SEARCH_CALL_SEARCHING)
    //            .verifyComplete();
    //    }

    @Test
    public void testCodeInterpreterCallCodeDeltaEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.code_interpreter_call.code.delta\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.code_interpreter_call.code.delta\",\"output_index\":5,\"delta\":\"code_delta\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(
                event -> event instanceof ResponsesResponseStreamEventResponseCodeInterpreterCallCodeDelta
                    && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA)
            .verifyComplete();
    }

    @Test
    public void testCodeInterpreterCallCodeDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.code_interpreter_call.code.done\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.code_interpreter_call.code.done\",\"output_index\":6,\"code\":\"final_code\"}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseCodeInterpreterCallCodeDone
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CODE_INTERPRETER_CALL_CODE_DONE)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseOutputItemAdded
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_OUTPUT_ITEM_ADDED)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseOutputTextDelta
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_OUTPUT_TEXT_DELTA
                && ((ResponsesResponseStreamEventResponseOutputTextDelta) event).getDelta() != null)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseOutputTextDone
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_OUTPUT_TEXT_DONE)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseFileSearchCallCompleted
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_FILE_SEARCH_CALL_COMPLETED)
            .verifyComplete();
    }

    @Test
    public void testCodeInterpreterCallCompletedEvent() {
        Flux<ByteBuffer> testInput = Flux.just(
            ByteBuffer.wrap("event: response.code_interpreter_call.completed\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.code_interpreter_call.completed\",\"output_index\":0,\"code_interpreter_call\":{}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(
                event -> event instanceof ResponsesResponseStreamEventResponseCodeInterpreterCallCompleted
                    && event.getType() == ResponsesResponseStreamEventType.RESPONSE_CODE_INTERPRETER_CALL_COMPLETED)
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
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseRefusalDelta
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_REFUSAL_DELTA)
            .verifyComplete();
    }

    @Test
    public void testResponseRefusalDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.refusal.done\n".getBytes()),
            ByteBuffer.wrap("data: {\"type\":\"response.refusal.done\",\"output_index\":0,\"text\":\"Not allowed\"}\n\n"
                .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseRefusalDone
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_REFUSAL_DONE)
            .verifyComplete();
    }

    //    @Test
    //    public void testResponseTextAnnotationDeltaEvent() {
    //        Flux<ByteBuffer> testInput = Flux.just(
    //            ByteBuffer.wrap("event: response.output_text.annotation.added\n".getBytes()),
    //            ByteBuffer.wrap(
    //                "data: {\"type\":\"response.output_text.annotation.added\",\"output_index\":0,\"content_index\":0,\"annotation\":{\"type\":\"file_path\",\"file_id\":\"file1\",\"index\":1}}\n\n"
    //                    .getBytes()));
    //
    //        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);
    //
    //        StepVerifier.create(openAIServerSentEvents.getEvents())
    //            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseTextAnnotationAdded
    //                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_TEXT_ANNOTATION_DELTA)
    //            .verifyComplete();
    //    }

    @Test
    public void testResponseOutputItemDoneEvent() {
        Flux<ByteBuffer> testInput = Flux.just(ByteBuffer.wrap("event: response.output_item.done\n".getBytes()),
            ByteBuffer.wrap(
                "data: {\"type\":\"response.output_item.done\",\"output_index\":0,\"item\":{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":[]}}\n\n"
                    .getBytes()));

        OpenAIServerSentEvents openAIServerSentEvents = new OpenAIServerSentEvents(testInput);

        StepVerifier.create(openAIServerSentEvents.getEvents())
            .expectNextMatches(event -> event instanceof ResponsesResponseStreamEventResponseOutputItemDone
                && event.getType() == ResponsesResponseStreamEventType.RESPONSE_OUTPUT_ITEM_DONE)
            .verifyComplete();
    }
}

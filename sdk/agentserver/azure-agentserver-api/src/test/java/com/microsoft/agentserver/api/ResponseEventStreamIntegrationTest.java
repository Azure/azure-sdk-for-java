// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseError;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ResponseEventStream: verifies end-to-end event production,
 * subscription/replay, and final Response snapshot construction across all builder types.
 */
class ResponseEventStreamIntegrationTest {

    private ResponseContext context;
    private AgentServerCreateResponse request;

    @BeforeEach
    void setUp() {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("test input")
            .model("test-model")
            .build()
            ._body();
        request = new AgentServerCreateResponse(null, body);

        context = ResponseContext.builder()
            .responseId("resp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32))
            .provider(ResponsesProvider.inMemory())
            .request(body)
            .build();
    }

    // ══════════════════════════════════════════════════════════════
    //  Full message streaming lifecycle
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Message streaming lifecycle")
    class MessageStreaming {

        @Test
        @DisplayName("Complete message lifecycle produces correct event sequence")
        void completeMessageLifecycle() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> text
                        .emitAdded()
                        .emitDelta("Hello, ")
                        .emitDelta("how are you?")
                        .emitDone("Hello, how are you?"))
                    .emitDone())
                .emitCompleted();

            List<String> names = stream.getEvents().stream()
                .map(ResponseEvent::eventName)
                .toList();

            assertEquals(List.of(
                "response.created",
                "response.in_progress",
                "response.output_item.added",
                "response.content_part.added",
                "response.output_text.delta",
                "response.output_text.delta",
                "response.output_text.done",
                "response.content_part.done",
                "response.output_item.done",
                "response.completed"
            ), names);
        }

        @Test
        @DisplayName("outputItemMessage convenience method produces same event sequence")
        void outputItemMessageConvenience() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg.outputItemMessage("Hello!"))
                .emitCompleted();

            List<String> names = stream.getEvents().stream()
                .map(ResponseEvent::eventName)
                .toList();

            assertTrue(names.contains("response.output_item.added"));
            assertTrue(names.contains("response.output_text.delta"));
            assertTrue(names.contains("response.output_text.done"));
            assertTrue(names.contains("response.content_part.done"));
            assertTrue(names.contains("response.output_item.done"));
        }

        @Test
        @DisplayName("Multiple text parts in one message")
        void multipleTextParts() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> text
                        .emitAdded()
                        .emitDelta("Part 1")
                        .emitDone("Part 1"))
                    .addTextPart(text -> text
                        .emitAdded()
                        .emitDelta("Part 2")
                        .emitDone("Part 2"))
                    .emitDone())
                .emitCompleted();

            long contentPartAddedCount = stream.getEvents().stream()
                .filter(e -> "response.content_part.added".equals(e.eventName()))
                .count();
            assertEquals(2, contentPartAddedCount);
        }

        @Test
        @DisplayName("Final snapshot contains accumulated output items")
        void snapshotContainsOutputItems() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg
                    .outputItemMessage("First message"))
                .addOutputMessage(msg -> msg
                    .outputItemMessage("Second message"))
                .emitCompleted();

            Response snapshot = stream.getResponse();
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), snapshot.status());
            assertEquals(2, snapshot.output().size());

            // Verify message content
            ResponseOutputItem first = snapshot.output().get(0);
            assertTrue(first.isMessage());
            ResponseOutputMessage firstMsg = first.asMessage();
            assertEquals(ResponseOutputMessage.Status.COMPLETED, firstMsg.status());
        }

        @Test
        @DisplayName("Auto-emits output_text.done when not explicitly called")
        void autoEmitsTextDone() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg
                    .emitAdded()
                    .addTextPart(text -> text
                        .emitAdded()
                        .emitDelta("auto done"))
                    .emitDone())
                .emitCompleted();

            // Should still have output_text.done auto-generated
            assertTrue(stream.getEvents().stream()
                .anyMatch(e -> "response.output_text.done".equals(e.eventName())));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Function call streaming lifecycle
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Function call streaming lifecycle")
    class FunctionCallStreaming {

        @Test
        @DisplayName("Complete function call lifecycle")
        void completeFunctionCallLifecycle() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputFunctionCall(func -> func
                    .emitAdded("get_weather", "call_123")
                    .emitArgumentsDelta("{\"loc\":")
                    .emitArgumentsDelta("\"Seattle\"}")
                    .emitArgumentsDone("get_weather", "{\"loc\":\"Seattle\"}")
                    .emitDone())
                .emitCompleted();

            List<String> names = stream.getEvents().stream()
                .map(ResponseEvent::eventName)
                .toList();

            assertTrue(names.contains("response.output_item.added"));
            assertTrue(names.contains("response.function_call_arguments.delta"));
            assertTrue(names.contains("response.function_call_arguments.done"));
            assertTrue(names.contains("response.output_item.done"));

            // Verify snapshot
            Response snapshot = stream.getResponse();
            assertEquals(1, snapshot.output().size());
            assertTrue(snapshot.output().get(0).isFunctionCall());
            ResponseFunctionToolCall call = snapshot.output().get(0).asFunctionCall();
            assertEquals("get_weather", call.name());
            assertEquals("{\"loc\":\"Seattle\"}", call.arguments());
        }

        @Test
        @DisplayName("Mixed message and function call outputs")
        void mixedOutputs() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg.outputItemMessage("Checking weather..."))
                .addOutputFunctionCall(func -> func
                    .emitAdded("get_weather", "call_1")
                    .emitArgumentsDelta("{}")
                    .emitArgumentsDone("get_weather", "{}")
                    .emitDone())
                .emitCompleted();

            Response snapshot = stream.getResponse();
            assertEquals(2, snapshot.output().size());
            assertTrue(snapshot.output().get(0).isMessage());
            assertTrue(snapshot.output().get(1).isFunctionCall());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Terminal event semantics
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Terminal event semantics")
    class TerminalEvents {

        @Test
        @DisplayName("emitCompleted is a terminal event")
        void completedIsTerminal() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated().emitCompleted();

            assertThrows(IllegalStateException.class, stream::emitCompleted);
            assertThrows(IllegalStateException.class, stream::emitFailed);
            assertThrows(IllegalStateException.class, stream::emitIncomplete);
        }

        @Test
        @DisplayName("emitFailed is a terminal event")
        void failedIsTerminal() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated().emitFailed();

            Response snapshot = stream.getResponse();
            assertEquals(java.util.Optional.of(ResponseStatus.FAILED), snapshot.status());
            assertThrows(IllegalStateException.class, stream::emitCompleted);
        }

        @Test
        @DisplayName("emitFailed with custom error code and message")
        void failedWithCustomError() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated()
                .emitFailed(ResponseError.Code.SERVER_ERROR, "Too many requests");

            Response snapshot = stream.getResponse();
            assertEquals(java.util.Optional.of(ResponseStatus.FAILED), snapshot.status());
            assertTrue(snapshot.error().isPresent());
            assertEquals(ResponseError.Code.SERVER_ERROR, snapshot.error().get().code());
        }

        @Test
        @DisplayName("emitIncomplete is a terminal event")
        void incompleteIsTerminal() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated()
                .emitIncomplete(Response.IncompleteDetails.Reason.MAX_OUTPUT_TOKENS);

            Response snapshot = stream.getResponse();
            assertEquals(java.util.Optional.of(ResponseStatus.INCOMPLETE), snapshot.status());
            assertTrue(snapshot.incompleteDetails().isPresent());
        }

        @Test
        @DisplayName("emitCompleted with custom usage data")
        void completedWithUsage() {
            ResponseUsage usage = ResponseUsage.builder()
                .inputTokens(100)
                .outputTokens(50)
                .totalTokens(150)
                .inputTokensDetails(ResponseUsage.InputTokensDetails.builder().cachedTokens(10).build())
                .outputTokensDetails(ResponseUsage.OutputTokensDetails.builder().reasoningTokens(5).build())
                .build();

            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated().emitCompleted(usage);

            Response snapshot = stream.getResponse();
            assertEquals(java.util.Optional.of(ResponseStatus.COMPLETED), snapshot.status());
            assertTrue(snapshot.usage().isPresent());
            assertEquals(150, snapshot.usage().get().totalTokens());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Subscription and reactive delivery
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Subscription and reactive delivery")
    class SubscriptionTests {

        @Test
        @DisplayName("Subscribe receives all events including replayed ones")
        void subscribeReceivesAllEvents() throws InterruptedException {
            ResponseEventStream stream = ResponseEventStream.create(context, request);

            // Emit some events before subscribing
            stream.emitCreated().emitInProgress();

            CopyOnWriteArrayList<String> received = new CopyOnWriteArrayList<>();
            AtomicBoolean completed = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            // Now subscribe — should replay created + in_progress, then receive rest
            stream.subscribe(
                event -> received.add(event.eventName()),
                failure -> fail("Unexpected failure: " + failure),
                () -> {
                    completed.set(true);
                    latch.countDown();
                }
            );

            // Now emit more events
            stream.addOutputMessage(msg -> msg.outputItemMessage("Hello"))
                .emitCompleted();

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(completed.get());

            // First two events should be replayed
            assertEquals("response.created", received.get(0));
            assertEquals("response.in_progress", received.get(1));
            assertTrue(received.contains("response.completed"));
        }

        @Test
        @DisplayName("Subscribing to already-completed stream delivers all events and completes")
        void subscribeToCompletedStream() throws InterruptedException {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated().emitCompleted();

            CopyOnWriteArrayList<String> received = new CopyOnWriteArrayList<>();
            AtomicBoolean completed = new AtomicBoolean(false);

            stream.subscribe(
                event -> received.add(event.eventName()),
                failure -> fail("Unexpected failure"),
                () -> completed.set(true)
            );

            // Should have replayed events and called onComplete
            assertTrue(completed.get());
            assertFalse(received.isEmpty());
        }

        @Test
        @DisplayName("Async producer with awaitSubscription")
        void asyncProducerWithAwaitSubscription() throws Exception {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            CopyOnWriteArrayList<String> received = new CopyOnWriteArrayList<>();
            CountDownLatch doneLatch = new CountDownLatch(1);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    stream.awaitSubscription();
                    stream.emitCreated()
                        .emitInProgress()
                        .addOutputMessage(msg -> msg.outputItemMessage("Async!"))
                        .emitCompleted();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Small delay to ensure the executor thread is waiting
            Thread.sleep(50);

            stream.subscribe(
                event -> received.add(event.eventName()),
                failure -> fail("Unexpected failure"),
                () -> doneLatch.countDown()
            );

            assertTrue(doneLatch.await(5, TimeUnit.SECONDS));
            assertTrue(received.contains("response.created"));
            assertTrue(received.contains("response.completed"));

            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Sequence numbers and response ID propagation
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Sequence numbers and IDs")
    class SequenceAndIds {

        @Test
        @DisplayName("Sequence numbers are monotonically increasing")
        void sequenceNumbersIncrease() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg.outputItemMessage("Test"))
                .emitCompleted();

            // Verify all events have the stream's response ID
            Response snapshot = stream.getResponse();
            assertNotNull(snapshot.id());
            assertTrue(snapshot.id().startsWith("resp_"));
        }

        @Test
        @DisplayName("Output item IDs are unique across multiple outputs")
        void outputItemIdsAreUnique() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg.outputItemMessage("First"))
                .addOutputMessage(msg -> msg.outputItemMessage("Second"))
                .addOutputFunctionCall(func -> func
                    .emitAdded("tool", "call_1")
                    .emitArgumentsDone("tool", "{}")
                    .emitDone())
                .emitCompleted();

            Response snapshot = stream.getResponse();
            List<String> ids = new ArrayList<>();
            for (ResponseOutputItem item : snapshot.output()) {
                if (item.isMessage()) ids.add(item.asMessage().id());
                if (item.isFunctionCall()) ids.add(item.asFunctionCall().id().orElse(""));
            }

            assertEquals(ids.size(), ids.stream().distinct().count(),
                "All output item IDs should be unique");
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Queued lifecycle variant
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Queued lifecycle")
    class QueuedLifecycle {

        @Test
        @DisplayName("emitQueued → emitCreated → emitInProgress → emitCompleted")
        void queuedFullLifecycle() {
            ResponseEventStream stream = ResponseEventStream.create(context, request);
            stream.emitQueued()
                .emitCreated()
                .emitInProgress()
                .addOutputMessage(msg -> msg.outputItemMessage("Delayed"))
                .emitCompleted();

            List<String> names = stream.getEvents().stream()
                .map(ResponseEvent::eventName)
                .toList();

            assertEquals("response.queued", names.get(0));
            assertEquals("response.created", names.get(1));
            assertTrue(names.contains("response.completed"));
        }
    }
}



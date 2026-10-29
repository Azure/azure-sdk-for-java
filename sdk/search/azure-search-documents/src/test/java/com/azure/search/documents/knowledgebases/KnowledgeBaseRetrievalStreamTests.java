// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CloseableIterableStream;
import com.azure.core.util.Context;
import com.azure.json.JsonSerializable;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAgenticReasoningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseErrorStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReferencesCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseStreamErrorEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnowledgeBaseRetrievalStreamTests {
    private static final HttpHeaderName QUERY_SOURCE_AUTHORIZATION
        = HttpHeaderName.fromString("x-ms-query-source-authorization");
    private static final HttpHeaderName QUERY_WORK_IQ_SOURCE_AUTHORIZATION
        = HttpHeaderName.fromString("x-ms-query-work-iq-source-authorization");
    private static final String QUERY_SOURCE_TOKEN = "query-source-token";
    private static final String RETRIEVAL_STARTED_JSON
        = "{\"requestId\":\"request\",\"knowledgeBaseName\":\"kb\",\"outputMode\":\"answerSynthesis\","
            + "\"reasoningEffort\":{\"kind\":\"low\"}}";
    private static final String RESPONSE_COMPLETED_JSON = "{\"statusCode\":200,\"response\":{}}";

    @ParameterizedTest
    @ValueSource(ints = { 200, 206, 299 })
    public void completionStatusCodesRemainNumeric(int statusCode) throws NoSuchMethodException {
        KnowledgeBaseResponseCompletedEvent event
            = BinaryData.fromString("{\"statusCode\":" + statusCode + ",\"response\":{}}")
                .toObject(KnowledgeBaseResponseCompletedEvent.class);
        assertEquals(Integer.class,
            KnowledgeBaseResponseCompletedEvent.class.getMethod("getStatusCode").getReturnType());
        assertEquals(Integer.valueOf(statusCode), event.getStatusCode());
        String json = BinaryData.fromObject(event).toString();
        assertTrue(json.contains("\"statusCode\":" + statusCode));
        assertEquals(event.getStatusCode(),
            BinaryData.fromString(json).toObject(KnowledgeBaseResponseCompletedEvent.class).getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"statusCode\":null}" })
    public void missingCompletionStatusRemainsNull(String json) {
        KnowledgeBaseResponseCompletedEvent event
            = BinaryData.fromString(json).toObject(KnowledgeBaseResponseCompletedEvent.class);
        assertNull(event.getStatusCode());
        assertNull(BinaryData.fromString(BinaryData.fromObject(event).toString())
            .toObject(KnowledgeBaseResponseCompletedEvent.class)
            .getStatusCode());
    }

    @Test
    public void authorizationIsClientOnlyMetadata() {
        KnowledgeBaseRetrievalOptions options
            = new KnowledgeBaseRetrievalOptions().setQuerySourceAuthorization(QUERY_SOURCE_TOKEN);
        assertEquals(QUERY_SOURCE_TOKEN, options.getQuerySourceAuthorization());
        assertEquals("{}", BinaryData.fromObject(options).toString());
        KnowledgeBaseRetrievalOptions deserialized
            = BinaryData.fromString("{\"querySourceAuthorization\":\"untrusted-json-token\"}")
                .toObject(KnowledgeBaseRetrievalOptions.class);
        assertNull(deserialized.getQuerySourceAuthorization());
        assertNull(options.setQuerySourceAuthorization(null).getQuerySourceAuthorization());
    }

    private static Stream<Arguments> authorizationCases() {
        return Stream.of(Arguments.of(null, null, false, null),
            Arguments.of(QUERY_SOURCE_TOKEN, null, false, QUERY_SOURCE_TOKEN),
            Arguments.of(null, "explicit-token", true, "explicit-token"),
            Arguments.of(QUERY_SOURCE_TOKEN, "explicit-token", true, "explicit-token"),
            Arguments.of(QUERY_SOURCE_TOKEN, null, true, QUERY_SOURCE_TOKEN), Arguments.of(null, null, true, null),
            Arguments.of(QUERY_SOURCE_TOKEN, "", true, ""));
    }

    @ParameterizedTest
    @MethodSource("authorizationCases")
    public void bothClientsForwardAuthorizationForRegularRetrieval(String option, String explicit,
        boolean useExplicitOverload, String expected) {
        KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions().setQuerySourceAuthorization(option);
        KnowledgeBaseRetrievalClient client = authorizationBuilder("{}", expected, false).buildClient();
        assertNotNull(useExplicitOverload ? client.retrieve(options, explicit) : client.retrieve(options));
        KnowledgeBaseRetrievalAsyncClient asyncClient = authorizationBuilder("{}", expected, false).buildAsyncClient();
        assertNotNull(
            (useExplicitOverload ? asyncClient.retrieve(options, explicit) : asyncClient.retrieve(options)).block());
        assertEquals(option, options.getQuerySourceAuthorization());
    }

    @ParameterizedTest
    @MethodSource("authorizationCases")
    public void bothClientsForwardAuthorizationForStreamingRetrieval(String option, String explicit,
        boolean useExplicitOverload, String expected) throws IOException {
        KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions().setQuerySourceAuthorization(option);
        String body = frame("response.completed", RESPONSE_COMPLETED_JSON);
        KnowledgeBaseRetrievalClient client = authorizationBuilder(body, expected, true).buildClient();
        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> events
            = useExplicitOverload ? client.retrieveStream(options, explicit) : client.retrieveStream(options)) {
            assertEvent(events.iterator().next(), "response.completed", true);
        }
        KnowledgeBaseRetrievalAsyncClient asyncClient = authorizationBuilder(body, expected, true).buildAsyncClient();
        StepVerifier.create(
            useExplicitOverload ? asyncClient.retrieveStream(options, explicit) : asyncClient.retrieveStream(options))
            .assertNext(event -> assertEvent(event, "response.completed", true))
            .verifyComplete();
        assertEquals(option, options.getQuerySourceAuthorization());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void typedResponsesForwardOptionWithNullRequestOptions(boolean async) {
        KnowledgeBaseRetrievalOptions options
            = new KnowledgeBaseRetrievalOptions().setQuerySourceAuthorization(QUERY_SOURCE_TOKEN);
        KnowledgeBaseRetrievalClientBuilder builder = authorizationBuilder("{}", QUERY_SOURCE_TOKEN, false);
        if (async) {
            StepVerifier.create(builder.buildAsyncClient().retrieveWithResponse(options, null))
                .assertNext(response -> assertNotNull(response.getValue()))
                .verifyComplete();
        } else {
            assertNotNull(builder.buildClient().retrieveWithResponse(options, null).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void typedResponsesPreserveRequestOptionsAndExplicitHeaders(boolean async) {
        AtomicInteger callbacks = new AtomicInteger();
        Context context = new Context("authorization-test", "context-value");
        RequestOptions requestOptions = new RequestOptions().setContext(context)
            .setHeader(QUERY_SOURCE_AUTHORIZATION, "request-options-token")
            .addRequestCallback(request -> {
                callbacks.incrementAndGet();
                request.setHeader(HttpHeaderName.fromString("test-header"), "preserved");
            });
        KnowledgeBaseRetrievalOptions options
            = new KnowledgeBaseRetrievalOptions().setQuerySourceAuthorization(QUERY_SOURCE_TOKEN);
        KnowledgeBaseRetrievalClientBuilder builder
            = authorizationBuilder("{}", "request-options-token", false).addPolicy((pipelineContext, next) -> {
                assertEquals("context-value", pipelineContext.getContext().getData("authorization-test").orElse(null));
                assertEquals("preserved", pipelineContext.getHttpRequest().getHeaders().getValue("test-header"));
                return next.process();
            });
        if (async) {
            assertNotNull(builder.buildAsyncClient().retrieveWithResponse(options, requestOptions).block());
        } else {
            assertNotNull(builder.buildClient().retrieveWithResponse(options, requestOptions));
        }
        assertEquals(1, callbacks.get());
        assertEquals(context, requestOptions.getContext());

        KnowledgeBaseRetrievalOptions noAuthorization = new KnowledgeBaseRetrievalOptions();
        KnowledgeBaseRetrievalClientBuilder reused = authorizationBuilder("{}", "request-options-token", false);
        if (async) {
            assertNotNull(reused.buildAsyncClient().retrieveWithResponse(noAuthorization, requestOptions).block());
        } else {
            assertNotNull(reused.buildClient().retrieveWithResponse(noAuthorization, requestOptions));
        }
        assertEquals(2, callbacks.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void typedResponseFallbackDoesNotModifyReusedRequestOptions(boolean async) {
        AtomicInteger callbacks = new AtomicInteger();
        Context context = new Context("authorization-test", "context-value");
        RequestOptions requestOptions
            = new RequestOptions().setContext(context).addRequestCallback(request -> callbacks.incrementAndGet());
        KnowledgeBaseRetrievalOptions options
            = new KnowledgeBaseRetrievalOptions().setQuerySourceAuthorization(QUERY_SOURCE_TOKEN);
        for (String expected : Arrays.asList(QUERY_SOURCE_TOKEN, "second-token", null)) {
            options.setQuerySourceAuthorization(expected);
            KnowledgeBaseRetrievalClientBuilder builder
                = authorizationBuilder("{}", expected, false).addPolicy((pipelineContext, next) -> {
                    assertEquals("context-value",
                        pipelineContext.getContext().getData("authorization-test").orElse(null));
                    return next.process();
                });
            if (async) {
                assertNotNull(builder.buildAsyncClient().retrieveWithResponse(options, requestOptions).block());
            } else {
                assertNotNull(builder.buildClient().retrieveWithResponse(options, requestOptions));
            }
        }
        assertEquals(3, callbacks.get());
        assertEquals(context, requestOptions.getContext());
    }

    private static KnowledgeBaseRetrievalClientBuilder authorizationBuilder(String body, String expected,
        boolean streaming) {
        return new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
            .knowledgeBaseName("kb")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertEquals("api-version=2026-10-01", request.getUrl().getQuery());
                assertEquals(expected, request.getHeaders().getValue(QUERY_SOURCE_AUTHORIZATION));
                String json = request.getBodyAsBinaryData().toString();
                assertFalse(json.contains("querySourceAuthorization"));
                assertFalse(json.contains("token"));
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE,
                    streaming ? "text/event-stream" : "application/json");
                return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
            });
    }

    @Test
    public void baseEventRemainsAbstract() {
        assertTrue(Modifier.isAbstract(KnowledgeBaseRetrievalStreamEvent.class.getModifiers()));
    }

    @Test
    public void convertsAllStreamEventVariants() {
        KnowledgeBaseRetrievalStreamEvent retrievalStarted
            = KnowledgeBaseRetrievalStreamEventConverter.convert("retrieval.started", RETRIEVAL_STARTED_JSON);
        assertInstanceOf(KnowledgeBaseRetrievalStartedStreamEvent.class, retrievalStarted);
        assertInstanceOf(KnowledgeBaseRetrievalStartedEvent.class,
            ((KnowledgeBaseRetrievalStartedStreamEvent) retrievalStarted).getValue());
        assertInstanceOf(KnowledgeRetrievalLowReasoningEffort.class,
            ((KnowledgeBaseRetrievalStartedStreamEvent) retrievalStarted).getValue().getReasoningEffort());
        assertEvent(retrievalStarted, "retrieval.started", false);

        KnowledgeBaseRetrievalStreamEvent activityStarted = KnowledgeBaseRetrievalStreamEventConverter
            .convert("activity.started", "{\"id\":0,\"type\":\"searchIndex\",\"startedAt\":\"2025-01-01T00:00:00Z\"}");
        assertInstanceOf(KnowledgeBaseActivityStartedStreamEvent.class, activityStarted);
        assertInstanceOf(KnowledgeBaseActivityStartedEvent.class,
            ((KnowledgeBaseActivityStartedStreamEvent) activityStarted).getValue());
        assertEvent(activityStarted, "activity.started", false);

        KnowledgeBaseRetrievalStreamEvent activityCompleted = KnowledgeBaseRetrievalStreamEventConverter
            .convert("activity.completed", "{\"type\":\"future\",\"id\":0}");
        assertInstanceOf(KnowledgeBaseActivityCompletedStreamEvent.class, activityCompleted);
        assertInstanceOf(KnowledgeBaseActivityRecord.class,
            ((KnowledgeBaseActivityCompletedStreamEvent) activityCompleted).getValue());
        assertEvent(activityCompleted, "activity.completed", false);

        KnowledgeBaseRetrievalStreamEvent answerCompleted
            = KnowledgeBaseRetrievalStreamEventConverter.convert("answer.completed",
                "{\"messageIndex\":0,\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"answer\"}]}}");
        assertInstanceOf(KnowledgeBaseAnswerCompletedStreamEvent.class, answerCompleted);
        assertInstanceOf(KnowledgeBaseAnswerCompletedEvent.class,
            ((KnowledgeBaseAnswerCompletedStreamEvent) answerCompleted).getValue());
        assertEvent(answerCompleted, "answer.completed", false);

        KnowledgeBaseRetrievalStreamEvent referencesCompleted = KnowledgeBaseRetrievalStreamEventConverter
            .convert("references.completed", "[{\"type\":\"future\",\"id\":\"reference\",\"activitySource\":0}]");
        assertInstanceOf(KnowledgeBaseReferencesCompletedStreamEvent.class, referencesCompleted);
        assertEquals(1, ((KnowledgeBaseReferencesCompletedStreamEvent) referencesCompleted).getValue().size());
        assertEvent(referencesCompleted, "references.completed", false);

        KnowledgeBaseRetrievalStreamEvent error = KnowledgeBaseRetrievalStreamEventConverter.convert("error",
            "{\"error\":{\"code\":\"BadRequest\",\"message\":\"bad request\"}}");
        assertInstanceOf(KnowledgeBaseErrorStreamEvent.class, error);
        assertInstanceOf(KnowledgeBaseStreamErrorEvent.class, ((KnowledgeBaseErrorStreamEvent) error).getValue());
        assertEvent(error, "error", true);

        KnowledgeBaseRetrievalStreamEvent responseCompleted
            = KnowledgeBaseRetrievalStreamEventConverter.convert("response.completed", RESPONSE_COMPLETED_JSON);
        assertInstanceOf(KnowledgeBaseResponseCompletedStreamEvent.class, responseCompleted);
        assertInstanceOf(KnowledgeBaseResponseCompletedEvent.class,
            ((KnowledgeBaseResponseCompletedStreamEvent) responseCompleted).getValue());
        assertEvent(responseCompleted, "response.completed", true);
    }

    @Test
    public void preservesUnknownEventsAndRejectsMalformedKnownEvents() {
        KnowledgeBaseRetrievalStreamEvent event
            = KnowledgeBaseRetrievalStreamEventConverter.convert("future.event", "not json\nsecond line");

        assertUnknownEvent(event, "future.event");
        KnowledgeBaseRetrievalStreamEvent nullPayload
            = KnowledgeBaseRetrievalStreamEventConverter.convert("future.event", null);
        assertUnknownEvent(nullPayload, "future.event");
        assertThrows(RuntimeException.class,
            () -> KnowledgeBaseRetrievalStreamEventConverter.convert("response.completed", "{"));
    }

    private static Stream<Arguments> knownEvents() {
        return Stream.of(Arguments.of("retrieval.started", RETRIEVAL_STARTED_JSON),
            Arguments.of("activity.started",
                "{\"id\":0,\"type\":\"searchIndex\",\"startedAt\":\"2025-01-01T00:00:00Z\"}"),
            Arguments.of("activity.completed",
                "{\"type\":\"searchIndex\",\"id\":0,\"knowledgeSourceName\":\"source\"}"),
            Arguments.of("answer.completed",
                "{\"messageIndex\":0,\"message\":{\"content\":[{\"type\":\"text\",\"text\":\"answer\"}]}}"),
            Arguments.of("references.completed",
                "[{\"type\":\"searchIndex\",\"id\":\"reference\",\"activitySource\":0,\"docKey\":\"document\"}]"),
            Arguments.of("error", "{\"error\":{\"code\":\"BadRequest\",\"message\":\"bad request\"}}"),
            Arguments.of("response.completed", RESPONSE_COMPLETED_JSON));
    }

    @ParameterizedTest
    @MethodSource("knownEvents")
    public void bothClientsDecodeTypedPayloadsForEveryKnownVariant(String eventName, String json) throws IOException {
        String data = " \n" + json.replaceFirst("\\{", "{\"futureField\":true,") + "\n  ";
        String body = frame(eventName, data) + frame("response.completed", RESPONSE_COMPLETED_JSON);
        KnowledgeBaseRetrievalStreamEvent converted
            = KnowledgeBaseRetrievalStreamEventConverter.convert(eventName, data);
        assertEquals("error".equals(eventName) || "response.completed".equals(eventName), converted.isTerminal());
        if ("activity.completed".equals(eventName)) {
            assertInstanceOf(KnowledgeBaseSearchIndexActivityRecord.class, payload(converted));
        }
        if ("references.completed".equals(eventName)) {
            assertInstanceOf(KnowledgeBaseSearchIndexReference.class,
                ((KnowledgeBaseReferencesCompletedStreamEvent) converted).getValue().get(0));
        }

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            Iterator<KnowledgeBaseRetrievalStreamEvent> iterator = stream.iterator();
            KnowledgeBaseRetrievalStreamEvent event = iterator.next();
            assertTypedEvent(converted, event);
            if (!event.isTerminal()) {
                assertEvent(iterator.next(), "response.completed", true);
            }
            assertFalse(iterator.hasNext());
        }

        List<KnowledgeBaseRetrievalStreamEvent> events = createBuilder(body, null).buildAsyncClient()
            .retrieveStream(new KnowledgeBaseRetrievalOptions())
            .collectList()
            .block();
        assertNotNull(events);
        assertEquals(converted.isTerminal() ? 1 : 2, events.size());
        assertTypedEvent(converted, events.get(0));
        assertTrue(events.get(events.size() - 1).isTerminal());
    }

    @Test
    public void bothClientsPreserveAgenticReasoningInProgressAndCompletedResponse() throws IOException {
        String activity = "{\"type\":\"agenticReasoning\",\"id\":4,\"reasoningTokens\":42,\"elapsedMs\":25}";
        String body = frame("activity.completed", activity)
            + frame("response.completed", "{\"statusCode\":200,\"response\":{\"activity\":[" + activity + "]}}");
        List<KnowledgeBaseRetrievalStreamEvent> syncEvents = new ArrayList<>();
        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            stream.forEach(syncEvents::add);
        }
        List<KnowledgeBaseRetrievalStreamEvent> asyncEvents = createBuilder(body, null).buildAsyncClient()
            .retrieveStream(new KnowledgeBaseRetrievalOptions())
            .collectList()
            .block();

        for (List<KnowledgeBaseRetrievalStreamEvent> events : Arrays.asList(syncEvents, asyncEvents)) {
            assertNotNull(events);
            assertEquals(2, events.size());
            KnowledgeBaseActivityCompletedStreamEvent progress
                = assertInstanceOf(KnowledgeBaseActivityCompletedStreamEvent.class, events.get(0));
            KnowledgeBaseResponseCompletedStreamEvent completed
                = assertInstanceOf(KnowledgeBaseResponseCompletedStreamEvent.class, events.get(1));
            assertEquals(Integer.valueOf(200), completed.getValue().getStatusCode());
            assertEquals(1, completed.getValue().getResponse().getActivity().size());
            for (KnowledgeBaseActivityRecord record : Arrays.asList(progress.getValue(),
                completed.getValue().getResponse().getActivity().get(0))) {
                KnowledgeBaseAgenticReasoningActivityRecord reasoning
                    = assertInstanceOf(KnowledgeBaseAgenticReasoningActivityRecord.class, record);
                assertEquals(4, reasoning.getId());
                assertEquals(42, reasoning.getReasoningTokens());
                assertEquals(25, reasoning.getElapsedMs());
            }
            assertEvent(completed, "response.completed", true);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " \t ", "not json", "not json\n second line ", " {\"future\":true}\n " })
    public void unknownPayloadsAreDiscardedWithoutDisplacingLaterTerminalEvents(String data) throws IOException {
        KnowledgeBaseRetrievalStreamEvent converted
            = KnowledgeBaseRetrievalStreamEventConverter.convert("future.event", data);
        assertUnknownEvent(converted, "future.event");
        String body = frame("future.event", data) + frame("response.completed", RESPONSE_COMPLETED_JSON);
        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            Iterator<KnowledgeBaseRetrievalStreamEvent> iterator = stream.iterator();
            KnowledgeBaseRetrievalStreamEvent event = iterator.next();
            assertUnknownEvent(event, "future.event");
            assertEvent(iterator.next(), "response.completed", true);
            assertFalse(iterator.hasNext());
        }
        StepVerifier
            .create(createBuilder(body, null).buildAsyncClient().retrieveStream(new KnowledgeBaseRetrievalOptions()))
            .assertNext(event -> assertUnknownEvent(event, "future.event"))
            .assertNext(event -> assertEvent(event, "response.completed", true))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("knownEvents")
    public void everyKnownVariantRejectsMalformedData(String eventName, String json) {
        assertThrows(RuntimeException.class, () -> KnowledgeBaseRetrievalStreamEventConverter.convert(eventName,
            "references.completed".equals(eventName) ? "[{" : "{"));
    }

    @Test
    public void asyncClientForwardsAuthorizationHeadersAndEmitsTerminalEvent() {
        KnowledgeBaseRetrievalAsyncClient client
            = createBuilder(streamWithUnknownEvent(), QUERY_SOURCE_TOKEN).buildAsyncClient();

        List<KnowledgeBaseRetrievalStreamEvent> events
            = client.retrieveStream(new KnowledgeBaseRetrievalOptions(), QUERY_SOURCE_TOKEN).collectList().block();

        assertStreamEvents(events);
    }

    @Test
    public void syncClientForwardsAuthorizationHeadersAndDeliversTerminalEvent() throws IOException {
        KnowledgeBaseRetrievalClient client = createBuilder(streamWithUnknownEvent(), QUERY_SOURCE_TOKEN).buildClient();
        List<KnowledgeBaseRetrievalStreamEvent> events = new ArrayList<>();

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = client.retrieveStream(new KnowledgeBaseRetrievalOptions(), QUERY_SOURCE_TOKEN)) {
            stream.forEach(events::add);
        }

        assertStreamEvents(events);
    }

    @Test
    public void asyncClientMinimalOverloadOmitsAuthorizationHeaders() {
        KnowledgeBaseRetrievalAsyncClient client = createBuilder(streamWithUnknownEvent(), null).buildAsyncClient();

        List<KnowledgeBaseRetrievalStreamEvent> events
            = client.retrieveStream(new KnowledgeBaseRetrievalOptions()).collectList().block();

        assertStreamEvents(events);
    }

    @Test
    public void syncClientMinimalOverloadOmitsAuthorizationHeaders() throws IOException {
        KnowledgeBaseRetrievalClient client = createBuilder(streamWithUnknownEvent(), null).buildClient();
        List<KnowledgeBaseRetrievalStreamEvent> events = new ArrayList<>();

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = client.retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            stream.forEach(events::add);
        }

        assertStreamEvents(events);
    }

    @Test
    public void syncClientDoesNotReadBodyBeforeIterationAndClosesEarly() throws IOException {
        AtomicInteger emittedBuffers = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(streamWithUnknownEvent().getBytes(StandardCharsets.UTF_8)))
            .concatWith(Flux.never())
            .doOnNext(ignored -> emittedBuffers.incrementAndGet())
            .doOnCancel(cancellations::incrementAndGet);
        KnowledgeBaseRetrievalClient client = createBuilder(body).buildClient();

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = client.retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            assertEquals(0, emittedBuffers.get());
            Iterator<KnowledgeBaseRetrievalStreamEvent> iterator = stream.iterator();
            assertEquals(0, emittedBuffers.get());
            assertInstanceOf(KnowledgeBaseRetrievalStartedStreamEvent.class, iterator.next());
        }

        assertEquals(1, emittedBuffers.get());
        assertEquals(1, cancellations.get());
    }

    @Test
    public void asyncClientDefersRequestUntilSubscriptionAndCancelsBody() {
        AtomicInteger requests = new AtomicInteger();
        AtomicInteger cancellations = new AtomicInteger();
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(streamWithUnknownEvent().getBytes(StandardCharsets.UTF_8)))
            .concatWith(Flux.never())
            .doOnCancel(cancellations::incrementAndGet);
        KnowledgeBaseRetrievalAsyncClient client = createBuilder(body).addPolicy((context, next) -> {
            requests.incrementAndGet();
            return next.process();
        }).buildAsyncClient();
        Flux<KnowledgeBaseRetrievalStreamEvent> events = client.retrieveStream(new KnowledgeBaseRetrievalOptions());

        assertEquals(0, requests.get());
        StepVerifier.create(events, 1)
            .assertNext(event -> assertEvent(event, "retrieval.started", false))
            .thenCancel()
            .verify();
        assertEquals(1, requests.get());
        assertEquals(1, cancellations.get());
    }

    @Test
    public void clientsEmitErrorEventInclusivelyWithoutParsingFollowingMalformedPayload() throws IOException {
        String body = "event: error\ndata: {\"error\":{\"code\":\"BadRequest\",\"message\":\"bad request\"}}\n\n"
            + "event: response.completed\ndata: {\n\n";

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            Iterator<KnowledgeBaseRetrievalStreamEvent> iterator = stream.iterator();
            assertInstanceOf(KnowledgeBaseErrorStreamEvent.class, iterator.next());
            assertFalse(iterator.hasNext());
        }

        StepVerifier
            .create(createBuilder(body, null).buildAsyncClient().retrieveStream(new KnowledgeBaseRetrievalOptions()))
            .assertNext(event -> {
                assertInstanceOf(KnowledgeBaseErrorStreamEvent.class, event);
                assertEvent(event, "error", true);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("knownEvents")
    public void clientsPropagateMalformedKnownPayload(String eventName, String json) throws IOException {
        String body = frame(eventName, "references.completed".equals(eventName) ? "[{" : "{");

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            assertThrows(RuntimeException.class, () -> stream.iterator().hasNext());
        }

        StepVerifier
            .create(createBuilder(body, null).buildAsyncClient().retrieveStream(new KnowledgeBaseRetrievalOptions()))
            .verifyError(RuntimeException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void syncClientRejectsUnexpectedNoContentResponse(boolean authorization) {
        KnowledgeBaseRetrievalClient client = noContentBuilder(authorization).buildClient();
        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            if (authorization) {
                client.retrieveStream(new KnowledgeBaseRetrievalOptions(), QUERY_SOURCE_TOKEN);
            } else {
                client.retrieveStream(new KnowledgeBaseRetrievalOptions());
            }
        });
        assertEquals(204, exception.getResponse().getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void asyncClientRejectsUnexpectedNoContentResponse(boolean authorization) {
        KnowledgeBaseRetrievalAsyncClient client = noContentBuilder(authorization).buildAsyncClient();
        StepVerifier.create(authorization
            ? client.retrieveStream(new KnowledgeBaseRetrievalOptions(), QUERY_SOURCE_TOKEN)
            : client.retrieveStream(new KnowledgeBaseRetrievalOptions())).verifyErrorSatisfies(throwable -> {
                HttpResponseException exception = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(204, exception.getResponse().getStatusCode());
            });
    }

    private static KnowledgeBaseRetrievalClientBuilder noContentBuilder(boolean authorization) {
        return new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
            .knowledgeBaseName("kb")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertEquals(authorization ? QUERY_SOURCE_TOKEN : null,
                    request.getHeaders().getValue(QUERY_SOURCE_AUTHORIZATION));
                assertNull(request.getHeaders().getValue(QUERY_WORK_IQ_SOURCE_AUTHORIZATION));
                return Mono.just(new MockHttpResponse(request, 204));
            });
    }

    private static void assertStreamEvents(List<KnowledgeBaseRetrievalStreamEvent> events) {
        assertNotNull(events);
        assertEquals(3, events.size());
        assertInstanceOf(KnowledgeBaseRetrievalStartedStreamEvent.class, events.get(0));
        assertEvent(events.get(0), "retrieval.started", false);
        assertUnknownEvent(events.get(1), "future.event");
        assertInstanceOf(KnowledgeBaseResponseCompletedStreamEvent.class, events.get(2));
        assertEvent(events.get(2), "response.completed", true);
    }

    private static String frame(String eventName, String data) {
        return "event: " + eventName + "\ndata: " + data.replace("\n", "\ndata: ") + "\n\n";
    }

    private static void assertTypedEvent(KnowledgeBaseRetrievalStreamEvent expected,
        KnowledgeBaseRetrievalStreamEvent actual) throws IOException {
        assertEquals(expected.getClass(), actual.getClass());
        assertEvent(actual, expected.getEventName(), expected.isTerminal());
        Object expectedValue = payload(expected);
        Object actualValue = payload(actual);
        assertEquals(expectedValue.getClass(), actualValue.getClass());
        if (expectedValue instanceof JsonSerializable<?>) {
            assertEquals(((JsonSerializable<?>) expectedValue).toJsonString(),
                ((JsonSerializable<?>) actualValue).toJsonString());
        } else {
            List<KnowledgeBaseReference> expectedReferences
                = ((KnowledgeBaseReferencesCompletedStreamEvent) expected).getValue();
            List<KnowledgeBaseReference> actualReferences
                = ((KnowledgeBaseReferencesCompletedStreamEvent) actual).getValue();
            assertEquals(expectedReferences.size(), actualReferences.size());
            for (int i = 0; i < expectedReferences.size(); i++) {
                assertEquals(expectedReferences.get(i).getClass(), actualReferences.get(i).getClass());
                assertEquals(expectedReferences.get(i).toJsonString(), actualReferences.get(i).toJsonString());
            }
        }
    }

    private static Object payload(KnowledgeBaseRetrievalStreamEvent event) {
        switch (event.getEventName()) {
            case "retrieval.started":
                return ((KnowledgeBaseRetrievalStartedStreamEvent) event).getValue();

            case "activity.started":
                return ((KnowledgeBaseActivityStartedStreamEvent) event).getValue();

            case "activity.completed":
                return ((KnowledgeBaseActivityCompletedStreamEvent) event).getValue();

            case "answer.completed":
                return ((KnowledgeBaseAnswerCompletedStreamEvent) event).getValue();

            case "references.completed":
                return ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue();

            case "error":
                return ((KnowledgeBaseErrorStreamEvent) event).getValue();

            case "response.completed":
                return ((KnowledgeBaseResponseCompletedStreamEvent) event).getValue();

            default:
                throw new IllegalArgumentException("Expected a known event: " + event.getEventName());
        }
    }

    private static KnowledgeBaseRetrievalClientBuilder createBuilder(Flux<ByteBuffer> responseBody) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream");
        return new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
            .knowledgeBaseName("kb")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200, headers, new byte[0]) {
                @Override
                public Flux<ByteBuffer> getBody() {
                    return responseBody;
                }
            }));
    }

    private static KnowledgeBaseRetrievalClientBuilder createBuilder(String responseBody, String querySourceToken) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream");
        return new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
            .knowledgeBaseName("kb")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertEquals("api-version=2026-10-01", request.getUrl().getQuery());
                assertEquals("text/event-stream", request.getHeaders().getValue(HttpHeaderName.ACCEPT));
                assertEquals(querySourceToken, request.getHeaders().getValue(QUERY_SOURCE_AUTHORIZATION));
                assertNull(request.getHeaders().getValue(QUERY_WORK_IQ_SOURCE_AUTHORIZATION));
                return Mono
                    .just(new MockHttpResponse(request, 200, headers, responseBody.getBytes(StandardCharsets.UTF_8)));
            });
    }

    private static String streamWithUnknownEvent() {
        return "id: event-id\n" + "event: retrieval.started\n" + "data: " + RETRIEVAL_STARTED_JSON + "\n\n"
            + "event: future.event\n" + "data: first line\n" + "data: second line\n\n" + "event: response.completed\n"
            + "data: " + RESPONSE_COMPLETED_JSON + "\n\n" + "event: response.completed\ndata: {\n\n";
    }

    private static void assertUnknownEvent(KnowledgeBaseRetrievalStreamEvent event, String eventName) {
        assertEquals(KnowledgeBaseRetrievalStreamEvent.class, event.getClass().getSuperclass());
        assertTrue(event.getClass().isAnonymousClass());
        assertFalse(Modifier.isPublic(event.getClass().getModifiers()));
        assertEvent(event, eventName, false);
    }

    private static void assertEvent(KnowledgeBaseRetrievalStreamEvent event, String eventName, boolean terminal) {
        assertEquals(eventName, event.getEventName());
        assertEquals(terminal, event.isTerminal());
    }
}

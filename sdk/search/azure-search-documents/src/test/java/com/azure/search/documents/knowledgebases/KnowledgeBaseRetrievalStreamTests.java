// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CloseableIterableStream;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAnswerCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseErrorStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReferencesCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseResponseCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStartedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseStreamErrorEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.UnknownKnowledgeBaseRetrievalStreamEvent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        String rawData = "not json\nsecond line";
        KnowledgeBaseRetrievalStreamEvent event
            = KnowledgeBaseRetrievalStreamEventConverter.convert("future.event", rawData);

        assertInstanceOf(UnknownKnowledgeBaseRetrievalStreamEvent.class, event);
        assertEquals(rawData, ((UnknownKnowledgeBaseRetrievalStreamEvent) event).getData());
        assertEvent(event, "future.event", false);
        assertThrows(RuntimeException.class,
            () -> KnowledgeBaseRetrievalStreamEventConverter.convert("response.completed", "{"));
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

    @Test
    public void clientsPropagateMalformedKnownPayload() throws IOException {
        String body = "event: response.completed\ndata: {\n\n";

        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            assertThrows(RuntimeException.class, () -> stream.iterator().hasNext());
        }

        StepVerifier
            .create(createBuilder(body, null).buildAsyncClient().retrieveStream(new KnowledgeBaseRetrievalOptions()))
            .verifyError(RuntimeException.class);
    }

    private static void assertStreamEvents(List<KnowledgeBaseRetrievalStreamEvent> events) {
        assertNotNull(events);
        assertEquals(3, events.size());
        assertInstanceOf(KnowledgeBaseRetrievalStartedStreamEvent.class, events.get(0));
        assertEvent(events.get(0), "retrieval.started", false);
        assertInstanceOf(UnknownKnowledgeBaseRetrievalStreamEvent.class, events.get(1));
        assertEvent(events.get(1), "future.event", false);
        assertEquals("first line\nsecond line", ((UnknownKnowledgeBaseRetrievalStreamEvent) events.get(1)).getData());
        assertInstanceOf(KnowledgeBaseResponseCompletedStreamEvent.class, events.get(2));
        assertEvent(events.get(2), "response.completed", true);
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

    private static void assertEvent(KnowledgeBaseRetrievalStreamEvent event, String eventName, boolean terminal) {
        assertEquals(eventName, event.getEventName());
        assertEquals(terminal, event.isTerminal());
    }
}

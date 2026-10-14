// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CloseableIterableStream;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityCompletedStreamEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedEvent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityStartedStreamEvent;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

        assertEvent(event, "future.event", false);
        assertTrue(Modifier.isPrivate(event.getClass().getModifiers()));
        assertTrue(Arrays.stream(event.getClass().getDeclaredFields()).allMatch(field -> field.isSynthetic()));
        assertEquals(1,
            Arrays.stream(event.getClass().getDeclaredConstructors())
                .filter(constructor -> !constructor.isSynthetic())
                .count());
        assertTrue(Arrays.stream(event.getClass().getDeclaredConstructors())
            .filter(constructor -> !constructor.isSynthetic())
            .allMatch(constructor -> constructor.getParameterCount() == 1
                && constructor.getParameterTypes()[0] == String.class));
        assertEvent(KnowledgeBaseRetrievalStreamEventConverter.convert("future.event", null), "future.event", false);
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

    @ParameterizedTest
    @ValueSource(strings = { "", " \t ", "not json", "not json\n second line ", " {\"future\":true}\n " })
    public void unknownPayloadsAreDiscardedWithoutDisplacingLaterTerminalEvents(String data) throws IOException {
        assertEvent(KnowledgeBaseRetrievalStreamEventConverter.convert("future.event", data), "future.event", false);
        String body = frame("future.event", data) + frame("response.completed", RESPONSE_COMPLETED_JSON);
        try (CloseableIterableStream<KnowledgeBaseRetrievalStreamEvent> stream
            = createBuilder(body, null).buildClient().retrieveStream(new KnowledgeBaseRetrievalOptions())) {
            Iterator<KnowledgeBaseRetrievalStreamEvent> iterator = stream.iterator();
            KnowledgeBaseRetrievalStreamEvent event = iterator.next();
            assertEvent(event, "future.event", false);
            assertEvent(iterator.next(), "response.completed", true);
            assertFalse(iterator.hasNext());
        }
        StepVerifier
            .create(createBuilder(body, null).buildAsyncClient().retrieveStream(new KnowledgeBaseRetrievalOptions()))
            .assertNext(event -> assertEvent(event, "future.event", false))
            .assertNext(event -> assertEvent(event, "response.completed", true))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("knownEvents")
    public void payloadOnlyConstructorsRetainTypedPayloadsWithoutRawOrJsonApis(String eventName, String json)
        throws ReflectiveOperationException {
        KnowledgeBaseRetrievalStreamEvent converted
            = KnowledgeBaseRetrievalStreamEventConverter.convert(eventName, json);
        KnowledgeBaseRetrievalStreamEvent event = withPayload(converted);
        Object value = payload(event);
        assertSame(value, payload(converted));
        assertEvent(event, eventName, converted.isTerminal());
        assertTrue(Modifier.isAbstract(KnowledgeBaseRetrievalStreamEvent.class.getModifiers()));
        assertFalse(JsonSerializable.class.isAssignableFrom(event.getClass()));
        assertFalse(Arrays.stream(event.getClass().getMethods())
            .anyMatch(method -> "fromJson".equals(method.getName()) || method.getName().startsWith("toJson")));
        assertThrows(NoSuchMethodException.class, () -> event.getClass().getMethod("getRawValue"));
        assertThrows(NoSuchMethodException.class,
            () -> KnowledgeBaseRetrievalStreamEvent.class.getMethod("getRawValue"));
        assertEquals(1, event.getClass().getDeclaredConstructors().length);
        assertEquals(1, event.getClass().getDeclaredConstructors()[0].getParameterCount());
        assertEquals(1, KnowledgeBaseRetrievalStreamEvent.class.getDeclaredConstructors().length);
        assertNotNull(KnowledgeBaseRetrievalStreamEvent.class.getDeclaredConstructor(String.class));
        assertThrows(NoSuchMethodException.class,
            () -> KnowledgeBaseRetrievalStreamEvent.class.getDeclaredConstructor(String.class, String.class));
        assertEquals(1,
            Arrays.stream(KnowledgeBaseRetrievalStreamEvent.class.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .count());
        assertNotNull(KnowledgeBaseRetrievalStreamEvent.class.getDeclaredField("eventName"));
        assertThrows(NoSuchFieldException.class,
            () -> KnowledgeBaseRetrievalStreamEvent.class.getDeclaredField("rawValue"));
        if ("activity.completed".equals(eventName)) {
            assertInstanceOf(KnowledgeBaseSearchIndexActivityRecord.class, value);
        }
        if ("references.completed".equals(eventName)) {
            assertInstanceOf(KnowledgeBaseSearchIndexReference.class,
                ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue().get(0));
        }
    }

    @Test
    public void nullPayloadsAndEmptyReferenceListsRemainAccessible() {
        List<KnowledgeBaseRetrievalStreamEvent> absent = Arrays.asList(
            new KnowledgeBaseRetrievalStartedStreamEvent(null), new KnowledgeBaseActivityStartedStreamEvent(null),
            new KnowledgeBaseActivityCompletedStreamEvent(null), new KnowledgeBaseAnswerCompletedStreamEvent(null),
            new KnowledgeBaseReferencesCompletedStreamEvent(null), new KnowledgeBaseErrorStreamEvent(null),
            new KnowledgeBaseResponseCompletedStreamEvent(null));
        absent.forEach(event -> assertNull(payload(event)));
        List<KnowledgeBaseReference> empty = Collections.emptyList();
        assertSame(empty, new KnowledgeBaseReferencesCompletedStreamEvent(empty).getValue());
        assertEvent(new KnowledgeBaseRetrievalStreamEvent("custom") {
        }, "custom", false);
    }

    @Test
    public void constructorsAndPayloadAccessNeverSerialize() {
        AtomicInteger writes = new AtomicInteger();
        KnowledgeBaseActivityRecord activity = new KnowledgeBaseActivityRecord(0) {
            @Override
            public JsonWriter toJson(JsonWriter writer) throws IOException {
                writes.incrementAndGet();
                throw new IOException("The event wrapper must not serialize its payload.");
            }
        };
        KnowledgeBaseActivityCompletedStreamEvent event = new KnowledgeBaseActivityCompletedStreamEvent(activity);
        assertSame(activity, event.getValue());
        assertSame(activity, event.getValue());
        assertEquals(0, writes.get());

        AtomicInteger referenceWrites = new AtomicInteger();
        KnowledgeBaseReference reference = new KnowledgeBaseReference("reference", 0) {
            @Override
            public JsonWriter toJson(JsonWriter writer) throws IOException {
                referenceWrites.incrementAndGet();
                throw new IOException("The event wrapper must not serialize its payload.");
            }
        };
        List<KnowledgeBaseReference> references = Collections.singletonList(reference);
        KnowledgeBaseReferencesCompletedStreamEvent array = new KnowledgeBaseReferencesCompletedStreamEvent(references);
        assertSame(references, array.getValue());
        assertSame(reference, array.getValue().get(0));
        assertEquals(0, referenceWrites.get());
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
        assertEvent(events.get(1), "future.event", false);
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

    private static KnowledgeBaseRetrievalStreamEvent withPayload(KnowledgeBaseRetrievalStreamEvent event) {
        switch (event.getEventName()) {
            case "retrieval.started":
                return new KnowledgeBaseRetrievalStartedStreamEvent(
                    ((KnowledgeBaseRetrievalStartedStreamEvent) event).getValue());

            case "activity.started":
                return new KnowledgeBaseActivityStartedStreamEvent(
                    ((KnowledgeBaseActivityStartedStreamEvent) event).getValue());

            case "activity.completed":
                return new KnowledgeBaseActivityCompletedStreamEvent(
                    ((KnowledgeBaseActivityCompletedStreamEvent) event).getValue());

            case "answer.completed":
                return new KnowledgeBaseAnswerCompletedStreamEvent(
                    ((KnowledgeBaseAnswerCompletedStreamEvent) event).getValue());

            case "references.completed":
                return new KnowledgeBaseReferencesCompletedStreamEvent(
                    ((KnowledgeBaseReferencesCompletedStreamEvent) event).getValue());

            case "error":
                return new KnowledgeBaseErrorStreamEvent(((KnowledgeBaseErrorStreamEvent) event).getValue());

            case "response.completed":
                return new KnowledgeBaseResponseCompletedStreamEvent(
                    ((KnowledgeBaseResponseCompletedStreamEvent) event).getValue());

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

    private static void assertEvent(KnowledgeBaseRetrievalStreamEvent event, String eventName, boolean terminal) {
        assertEquals(eventName, event.getEventName());
        assertEquals(terminal, event.isTerminal());
    }
}

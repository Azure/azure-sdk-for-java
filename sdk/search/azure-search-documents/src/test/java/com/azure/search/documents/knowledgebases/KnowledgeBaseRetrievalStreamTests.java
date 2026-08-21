// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.knowledgebases;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.http.MockHttpResponse;
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
import com.azure.search.documents.knowledgebases.models.UnknownKnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.models.ServerSentEvent;
import com.azure.search.documents.models.ServerSentEventListener;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnowledgeBaseRetrievalStreamTests {
    private static final String RETRIEVAL_STARTED_JSON
        = "{\"requestId\":\"request\",\"knowledgeBaseName\":\"kb\",\"outputMode\":\"answerSynthesis\","
            + "\"reasoningEffort\":{\"kind\":\"auto\"}}";
    private static final String RESPONSE_COMPLETED_JSON = "{\"statusCode\":200,\"response\":{}}";

    @Test
    public void convertsAllStreamEventVariants() {
        KnowledgeBaseRetrievalStreamEvent retrievalStarted
            = KnowledgeBaseRetrievalStreamEventConverter.convert("retrieval.started", RETRIEVAL_STARTED_JSON);
        assertInstanceOf(KnowledgeBaseRetrievalStartedStreamEvent.class, retrievalStarted);
        assertInstanceOf(KnowledgeBaseRetrievalStartedEvent.class,
            ((KnowledgeBaseRetrievalStartedStreamEvent) retrievalStarted).getValue());
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
    public void asyncClientEmitsTerminalEventBeforeCompletion() {
        KnowledgeBaseRetrievalAsyncClient client = createBuilder(streamWithUnknownEvent()).buildAsyncClient();

        List<ServerSentEvent<KnowledgeBaseRetrievalStreamEvent>> events
            = client.retrieveStream(new KnowledgeBaseRetrievalOptions()).collectList().block();

        assertNotNull(events);
        assertEquals(3, events.size());
        assertEquals("event-id", events.get(0).getId());
        assertEquals("retrieval.started", events.get(0).getEvent());
        assertFalse(events.get(0).getData().isTerminal());
        assertEquals("future.event", events.get(1).getEvent());
        assertInstanceOf(UnknownKnowledgeBaseRetrievalStreamEvent.class, events.get(1).getData());
        assertEquals("first line\nsecond line",
            ((UnknownKnowledgeBaseRetrievalStreamEvent) events.get(1).getData()).getData());
        assertFalse(events.get(1).getData().isTerminal());
        assertEquals("response.completed", events.get(2).getEvent());
        assertTrue(events.get(2).getData().isTerminal());
    }

    @Test
    public void syncClientDeliversTerminalEventBeforeClose() {
        KnowledgeBaseRetrievalClient client = createBuilder(streamWithUnknownEvent()).buildClient();
        List<ServerSentEvent<KnowledgeBaseRetrievalStreamEvent>> events = new ArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean closed = new AtomicBoolean();

        client.retrieveStream(new KnowledgeBaseRetrievalOptions(),
            new ServerSentEventListener<KnowledgeBaseRetrievalStreamEvent>() {
                @Override
                public void onEvent(ServerSentEvent<KnowledgeBaseRetrievalStreamEvent> event) {
                    assertFalse(closed.get());
                    events.add(event);
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                }

                @Override
                public void onClose() {
                    assertEquals(3, events.size());
                    assertInstanceOf(UnknownKnowledgeBaseRetrievalStreamEvent.class, events.get(1).getData());
                    assertFalse(events.get(1).getData().isTerminal());
                    assertTrue(events.get(2).getData().isTerminal());
                    closed.set(true);
                }
            });

        assertNull(error.get());
        assertTrue(closed.get());
    }

    @Test
    public void asyncClientRejectsEofBeforeTerminalEvent() {
        KnowledgeBaseRetrievalAsyncClient client
            = createBuilder("event: retrieval.started\ndata: " + RETRIEVAL_STARTED_JSON + "\n\n").buildAsyncClient();

        assertThrows(RuntimeException.class,
            () -> client.retrieveStream(new KnowledgeBaseRetrievalOptions()).collectList().block());
    }

    private static KnowledgeBaseRetrievalClientBuilder createBuilder(String responseBody) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/event-stream");
        return new KnowledgeBaseRetrievalClientBuilder().endpoint("https://example.search.windows.net")
            .knowledgeBaseName("kb")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertEquals("text/event-stream", request.getHeaders().getValue(HttpHeaderName.ACCEPT));
                return Mono
                    .just(new MockHttpResponse(request, 200, headers, responseBody.getBytes(StandardCharsets.UTF_8)));
            });
    }

    private static String streamWithUnknownEvent() {
        return "id: event-id\n" + "event: retrieval.started\n" + "data: " + RETRIEVAL_STARTED_JSON + "\n\n"
            + "event: future.event\n" + "data: first line\n" + "data: second line\n\n" + "event: response.completed\n"
            + "data: " + RESPONSE_COMPLETED_JSON + "\n\n";
    }

    private static void assertEvent(KnowledgeBaseRetrievalStreamEvent event, String eventName, boolean terminal) {
        assertEquals(eventName, event.getEventName());
        assertEquals(terminal, event.isTerminal());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchGaSamplesTests {
    @Test
    @ResourceLock(Resources.SYSTEM_OUT)
    public void streamSampleLogsErrorPayloadBeforeThrowing() throws IOException {
        KnowledgeBaseRetrievalStreamEvent error = KnowledgeBaseRetrievalStreamEventConverter.convert("error",
            "{\"error\":{\"code\":\"BadRequest\",\"message\":\"invalid request\"}}");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8.name())) {
            System.setOut(capture);
            assertThrows(IllegalStateException.class, () -> KnowledgeBaseRetrievalStreamExample.inspectEvent(error,
                new AtomicBoolean(), new AtomicBoolean()));
        } finally {
            System.setOut(original);
        }
        String logged = output.toString(StandardCharsets.UTF_8.name());
        assertTrue(logged.contains("Received event: error"));
        assertTrue(logged.contains("Event data: "));
        assertTrue(logged.contains("\"code\":\"BadRequest\""));
        assertTrue(logged.contains("\"message\":\"invalid request\""));
    }

    @Test
    @ResourceLock(Resources.SYSTEM_OUT)
    public void streamSampleHandlesProgressCompletionAndErrors() {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean completed = new AtomicBoolean();
        String[] names = {
            "retrieval.started",
            "activity.started",
            "activity.completed",
            "answer.completed",
            "references.completed",
            "response.completed" };
        String[] payloads = {
            "{\"requestId\":\"id\",\"knowledgeBaseName\":\"kb\",\"reasoningEffort\":{\"kind\":\"low\"}}",
            "{\"id\":0,\"type\":\"searchIndex\"}",
            "{\"id\":0,\"type\":\"searchIndex\"}",
            "{\"messageIndex\":0,\"message\":{\"content\":[]}}",
            "[]",
            "{\"statusCode\":200,\"response\":{}}" };
        for (int i = 0; i < names.length; i++) {
            KnowledgeBaseRetrievalStreamEvent event
                = KnowledgeBaseRetrievalStreamEventConverter.convert(names[i], payloads[i]);
            KnowledgeBaseRetrievalStreamExample.inspectEvent(event, started, completed);
        }
        assertTrue(started.get());
        assertTrue(completed.get());
        KnowledgeBaseRetrievalStreamEvent error = KnowledgeBaseRetrievalStreamEventConverter.convert("error",
            "{\"error\":{\"code\":\"BadRequest\",\"message\":\"invalid request\"}}");
        assertThrows(IllegalStateException.class,
            () -> KnowledgeBaseRetrievalStreamExample.inspectEvent(error, started, completed));
    }
}

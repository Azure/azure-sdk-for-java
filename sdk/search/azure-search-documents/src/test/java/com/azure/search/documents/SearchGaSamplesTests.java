// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.CreateSkillsetExample;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.knowledgebases.implementation.KnowledgeBaseRetrievalStreamEventConverter;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalStreamEvent;
import com.azure.search.documents.models.ContentUnderstandingSkillChunkingMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchGaSamplesTests {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void contentUnderstandingSampleUsesMatchingChunkingMethodAndUnit(boolean semantic) {
        Map<?, ?> skill = BinaryData.fromObject(CreateSkillsetExample.contentUnderstandingSkill("model", "deployment",
            semantic
                ? ContentUnderstandingSkillChunkingMethod.SEMANTIC
                : ContentUnderstandingSkillChunkingMethod.FIXED_SIZE))
            .toObject(Map.class);
        assertEquals("model", skill.get("modelName"));
        assertEquals("deployment", skill.get("modelDeployment"));
        Map<?, ?> chunking = (Map<?, ?>) skill.get("chunkingProperties");
        assertEquals(semantic ? "semantic" : "fixedSize", chunking.get("method"));
        assertEquals(semantic ? "tokens" : "characters", chunking.get("unit"));
        assertEquals(semantic ? 500 : 2000, chunking.get("maximumLength"));
    }

    @Test
    @ResourceLock(Resources.SYSTEM_OUT)
    public void indexStatisticsSampleFollowsServicePagination() throws IOException {
        AtomicInteger requests = new AtomicInteger();
        SearchIndexClient client = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                boolean last = requests.incrementAndGet() == 2;
                String query = request.getUrl().getQuery();
                if (last) {
                    assertTrue(query.contains("cursor=next"));
                } else {
                    assertTrue(query.contains("search=sample"));
                    assertTrue(query.contains("pageSize=1"));
                    assertTrue(query.contains("searchType=prefix"));
                }
                String nextLink = last
                    ? ""
                    : ",\"@odata.nextLink\":\"https://example.search.windows.net/indexstats"
                        + "?api-version=2026-11-01-preview&cursor=next\"";
                String body = "{\"value\":[{\"name\":\"sample-" + (last ? "second" : "first")
                    + "\",\"documentCount\":1,\"storageSize\":2,\"vectorIndexSize\":3}]" + nextLink + "}";
                return Mono.just(new MockHttpResponse(request, 200, body.getBytes(StandardCharsets.UTF_8)));
            })
            .buildClient();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8.name())) {
            System.setOut(capture);
            IndexAndServiceStatisticsExample.listIndexStatistics(client, "sample");
        } finally {
            System.setOut(original);
        }
        assertEquals(2, requests.get());
        String logged = output.toString(StandardCharsets.UTF_8.name());
        assertTrue(logged.contains("Index sample-first: 1 documents, 2 storage bytes, 3 vector bytes"));
        assertTrue(logged.contains("Index sample-second: 1 documents, 2 storage bytes, 3 vector bytes"));
    }

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.KnowledgeBase;
import com.azure.ai.discovery.models.KnowledgeBaseIndexingOperationResponse;
import com.azure.ai.discovery.models.KnowledgeBaseOperationResponse;
import com.azure.ai.discovery.models.SearchRequest;
import com.azure.ai.discovery.models.StorageAssetReference;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.polling.AsyncPollResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BookshelfAsyncClient}, covering knowledge-base create-or-update, get, list, indexing, search,
 * operation-status retrieval, and delete. All mutating operations are long-running.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class BookshelfAsyncClientTest extends DiscoveryClientTestBase {

    private KnowledgeBase newKnowledgeBase() {
        return new KnowledgeBase().setDescription("Created by SDK tests")
            .setCopilotInstruction("Use this tool to query cooling technology information.")
            .setStorageAssetReferences(Arrays.asList(
                new StorageAssetReference().setId(storageAssetId).setUserAssignedIdentity(userAssignedIdentity)));
    }

    @Test
    @Order(1)
    public void testBeginCreateOrUpdate() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        KnowledgeBase result = client.beginCreateOrUpdate(knowledgeBaseName, newKnowledgeBase())
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)
            .block();

        assertNotNull(result);
    }

    @Test
    @Order(3)
    public void testGet() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        KnowledgeBase knowledgeBase = client.get(knowledgeBaseName).block();

        assertNotNull(knowledgeBase);
        assertNotNull(knowledgeBase.getName());
    }

    @Test
    @Order(2)
    public void testList() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        List<KnowledgeBase> knowledgeBases = client.list().collectList().block();

        assertNotNull(knowledgeBases);
        assertTrue(knowledgeBases.stream().findAny().isPresent());
    }

    @Test
    @Order(4)
    public void testBeginStartIndexing() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        KnowledgeBaseOperationResponse response = client.beginStartIndexing(knowledgeBaseName).blockLast().getValue();

        assertNotNull(response);
    }

    @Test
    @Order(5)
    public void testBeginSearch() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        KnowledgeBaseOperationResponse response
            = client.beginSearch(knowledgeBaseName, new SearchRequest("immersion cooling")).blockLast().getValue();

        assertNotNull(response);
    }

    @Test
    @Order(6)
    public void testGetOperationStatus() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        // The operation id is the indexing run id; read it from the typed operation result rather than the
        // top-level "id" field, which the test proxy sanitizes during playback.
        KnowledgeBaseIndexingOperationResponse operation
            = (KnowledgeBaseIndexingOperationResponse) client.beginStartIndexing(knowledgeBaseName)
                .blockLast()
                .getValue();
        String operationId = operation.getIndexingResult().getRunId();

        KnowledgeBaseOperationResponse status = client.getOperationStatus(knowledgeBaseName, operationId).block();
        assertNotNull(status);
    }

    @Test
    @Order(7)
    public void testBeginDelete() {
        BookshelfAsyncClient client = getBookshelfAsyncClient();
        String throwawayName = testResourceNamer.randomName("sdk-del-kb", 24);
        client.beginCreateOrUpdate(throwawayName, newKnowledgeBase()).blockLast();

        client.beginDelete(throwawayName).blockLast();

        // The knowledge base must no longer be retrievable after deletion.
        assertThrows(HttpResponseException.class, () -> client.get(throwawayName).block());
    }
}

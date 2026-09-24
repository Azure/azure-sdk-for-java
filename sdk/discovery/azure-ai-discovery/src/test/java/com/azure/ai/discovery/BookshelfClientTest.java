// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.discovery;

import com.azure.ai.discovery.models.KnowledgeBase;
import com.azure.ai.discovery.models.KnowledgeBaseIndexingOperationResponse;
import com.azure.ai.discovery.models.KnowledgeBaseOperationResponse;
import com.azure.ai.discovery.models.SearchRequest;
import com.azure.ai.discovery.models.StorageAssetReference;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BookshelfClient}, covering knowledge-base create-or-update, get, list, indexing (start/cancel),
 * search, operation-status retrieval, and delete. All mutating operations are long-running.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class BookshelfClientTest extends DiscoveryClientTestBase {

    private KnowledgeBase newKnowledgeBase() {
        return new KnowledgeBase().setDescription("Created by SDK tests")
            .setCopilotInstruction("Use this tool to query cooling technology information.")
            .setStorageAssetReferences(Arrays.asList(
                new StorageAssetReference().setId(storageAssetId).setUserAssignedIdentity(userAssignedIdentity)));
    }

    @Test
    @Order(1)
    public void testBeginCreateOrUpdate() {
        BookshelfClient client = getBookshelfClient();
        SyncPoller<KnowledgeBaseOperationResponse, KnowledgeBase> poller
            = client.beginCreateOrUpdate(knowledgeBaseName, newKnowledgeBase());
        KnowledgeBase result = poller.getFinalResult();

        assertNotNull(result);
    }

    @Test
    @Order(3)
    public void testGet() {
        BookshelfClient client = getBookshelfClient();
        KnowledgeBase knowledgeBase = client.get(knowledgeBaseName);

        assertNotNull(knowledgeBase);
        assertNotNull(knowledgeBase.getName());
    }

    @Test
    @Order(2)
    public void testList() {
        BookshelfClient client = getBookshelfClient();
        PagedIterable<KnowledgeBase> knowledgeBases = client.list();

        assertNotNull(knowledgeBases);
        assertTrue(knowledgeBases.stream().findAny().isPresent());
    }

    @Test
    @Order(4)
    public void testBeginStartIndexing() {
        BookshelfClient client = getBookshelfClient();
        SyncPoller<KnowledgeBaseOperationResponse, Void> poller = client.beginStartIndexing(knowledgeBaseName);
        poller.waitForCompletion();

        assertNotNull(poller.poll().getValue());
    }

    @Test
    @Order(5)
    public void testBeginSearch() {
        BookshelfClient client = getBookshelfClient();
        SyncPoller<KnowledgeBaseOperationResponse, Void> poller
            = client.beginSearch(knowledgeBaseName, new SearchRequest("immersion cooling"));

        assertNotNull(poller.waitForCompletion().getValue());
    }

    @Test
    @Order(6)
    public void testGetOperationStatus() {
        BookshelfClient client = getBookshelfClient();
        SyncPoller<KnowledgeBaseOperationResponse, Void> poller = client.beginStartIndexing(knowledgeBaseName);
        // The operation id is the indexing run id; read it from the typed operation result rather than the
        // top-level "id" field, which the test proxy sanitizes during playback.
        KnowledgeBaseIndexingOperationResponse operation
            = (KnowledgeBaseIndexingOperationResponse) poller.poll().getValue();
        String operationId = operation.getIndexingResult().getRunId();

        KnowledgeBaseOperationResponse status = client.getOperationStatus(knowledgeBaseName, operationId);
        assertNotNull(status);
    }

    @Test
    @Order(7)
    public void testBeginDelete() {
        BookshelfClient client = getBookshelfClient();
        String throwawayName = testResourceNamer.randomName("sdk-del-kb", 24);
        client.beginCreateOrUpdate(throwawayName, newKnowledgeBase()).waitForCompletion();

        client.beginDelete(throwawayName).waitForCompletion();

        // The knowledge base must no longer be retrievable after deletion.
        assertThrows(HttpResponseException.class, () -> client.get(throwawayName));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.DeleteMemoryStoreResponse;
import com.azure.ai.agents.models.ListAgentsRequestOrder;
import com.azure.ai.agents.models.MemoryOperation;
import com.azure.ai.agents.models.MemorySearchItem;
import com.azure.ai.agents.models.MemorySearchOptions;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDefinition;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.MemoryStoreSearchResponse;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
import com.azure.ai.agents.models.MemoryStoreUpdateResponse;
import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.ai.agents.models.ResponsesAssistantMessageItemParam;
import com.azure.ai.agents.models.ResponsesUserMessageItemParam;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemoryStoresTests extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicMemoryStoresCrud(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        MemoryStoresClient memoryStoreClient = getMemoryStoresSyncClient(httpClient, serviceVersion);

        String memoryStoreName = "my_memory_store_java";
        String initialDescription = "Example memory store for conversations";
        String updatedDescription = "Updated description";

        cleanupBeforeTest(memoryStoreClient, memoryStoreName);

        String deploymentName = System.getenv("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingDeploymentName = System.getenv("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");
        // Create Memory Store
        MemoryStoreDefinition definition = new MemoryStoreDefaultDefinition(deploymentName, embeddingDeploymentName);

        MemoryStoreDetails createdStore
            = memoryStoreClient.createMemoryStore(memoryStoreName, definition, initialDescription, null);

        assertNotNull(createdStore);
        assertNotNull(createdStore.getId());
        assertEquals(memoryStoreName, createdStore.getName());
        assertEquals(initialDescription, createdStore.getDescription());

        // Get Memory Store
        MemoryStoreDetails retrievedStore = memoryStoreClient.getMemoryStore(createdStore.getName());
        assertNotNull(retrievedStore);
        assertEquals(createdStore.getId(), retrievedStore.getId());
        assertEquals(createdStore.getName(), retrievedStore.getName());
        assertEquals(createdStore.getDescription(), retrievedStore.getDescription());

        // Update Memory Store
        MemoryStoreDetails updatedStore
            = memoryStoreClient.updateMemoryStore(createdStore.getName(), updatedDescription, null);
        assertNotNull(updatedStore);
        assertEquals(createdStore.getId(), updatedStore.getId());
        assertEquals(createdStore.getName(), updatedStore.getName());
        assertEquals(updatedDescription, updatedStore.getDescription());

        // List Memory Stores and ensure the updated one is present
        boolean found = false;
        for (MemoryStoreDetails store : memoryStoreClient.listMemoryStores(10, ListAgentsRequestOrder.DESC, null,
            null)) {
            assertNotNull(store.getId());
            assertNotNull(store.getName());
            if (store.getName().equals(updatedStore.getName())) {
                found = true;
                assertEquals(updatedStore.getId(), store.getId());
                assertEquals(updatedDescription, store.getDescription());
            }
        }
        assertTrue(found, "Created memory store not found in list.");

        // Delete Memory Store
        DeleteMemoryStoreResponse deleteResponse = memoryStoreClient.deleteMemoryStore(updatedStore.getName());
        assertNotNull(deleteResponse);
        assertTrue(deleteResponse.isDeleted());

        // Verify it was deleted
        assertThrows(ResourceNotFoundException.class, () -> memoryStoreClient.getMemoryStore(updatedStore.getName()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicMemoryStores(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        MemoryStoresClient memoryStoreClient = getMemoryStoresSyncClient(httpClient, serviceVersion);

        String memoryStoreName = "my_memory_store";
        String description = "Example memory store for conversations";
        String scope = "user_123";
        String userMessageContent = "I prefer dark roast coffee and usually drink it in the morning";
        String queryMessageContent = "What are my coffee preferences?";

        // Ensure clean state: delete if it already exists
        cleanupBeforeTest(memoryStoreClient, memoryStoreName);

        String deploymentName = System.getenv("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingDeploymentName = System.getenv("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");
        // Create Memory Store with options
        MemoryStoreDefaultDefinition definition
            = new MemoryStoreDefaultDefinition(deploymentName, embeddingDeploymentName);
        definition.setOptions(new MemoryStoreDefaultOptions(true, true));

        MemoryStoreDetails memoryStore
            = memoryStoreClient.createMemoryStore(memoryStoreName, definition, description, null);
        assertNotNull(memoryStore);
        assertNotNull(memoryStore.getId());
        assertEquals(memoryStoreName, memoryStore.getName());
        assertEquals(description, memoryStore.getDescription());

        // Add memories to the memory store
        ResponsesUserMessageItemParam userMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(userMessageContent));
        // beginUpdateMemories returns a poller
        SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> updatePoller
            = memoryStoreClient.beginUpdateMemories(memoryStoreName, scope, Arrays.asList(userMessage), null, 1);

        // Poll for the write end status
        LongRunningOperationStatus status = null;
        while (status != LongRunningOperationStatus.fromString(MemoryStoreUpdateStatus.COMPLETED.toString(), true)) {
            sleep(500);
            System.out.println(status);
            status = updatePoller.poll().getStatus();
        }
        MemoryStoreUpdateCompletedResult updateResult = updatePoller.getFinalResult();
        assertNotNull(updateResult);
        assertNotNull(updateResult.getMemoryOperations());
        assertFalse(updateResult.getMemoryOperations().isEmpty());
        for (MemoryOperation operation : updateResult.getMemoryOperations()) {
            assertNotNull(operation.getKind());
            assertNotNull(operation.getMemoryItem().getMemoryId());
            assertNotNull(operation.getMemoryItem().getContent());
        }

        // Retrieve memories from the memory store
        ResponsesUserMessageItemParam queryMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(queryMessageContent));
        MemorySearchOptions searchOptions = new MemorySearchOptions();
        searchOptions.setMaxMemories(5);
        MemoryStoreSearchResponse searchResponse = memoryStoreClient.searchMemories(memoryStoreName, scope,
            Arrays.asList(queryMessage), null, searchOptions);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getMemories());
        assertFalse(searchResponse.getMemories().isEmpty());
        for (MemorySearchItem memory : searchResponse.getMemories()) {
            assertNotNull(memory.getMemoryItem().getMemoryId());
            assertNotNull(memory.getMemoryItem().getContent());
        }

        // Delete memories for a specific scope
        memoryStoreClient.deleteScope(memoryStoreName, scope);
        // No exception means success

        // Delete memory store
        DeleteMemoryStoreResponse deleteResponse = memoryStoreClient.deleteMemoryStore(memoryStoreName);
        assertNotNull(deleteResponse);
        assertTrue(deleteResponse.isDeleted());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void advancedMemoryStores(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        MemoryStoresClient memoryStoreClient = getMemoryStoresSyncClient(httpClient, serviceVersion);

        String memoryStoreName = "my_memory_store";
        String description = "Example memory store for conversations";
        String scope = "user_123";
        String firstMessageContent = "I prefer dark roast coffee and usually drink it in the morning";
        String chainedMessageContent = "I also like cappuccinos in the afternoon";
        String queryMessageContent = "What are my morning coffee preferences?";
        String followupContextContent = "You previously indicated a preference for dark roast coffee in the morning.";
        String followupQuestionContent = "What about afternoon?";

        cleanupBeforeTest(memoryStoreClient, memoryStoreName);

        String deploymentName = System.getenv("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingDeploymentName = System.getenv("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        MemoryStoreDefaultOptions options = new MemoryStoreDefaultOptions(true, true);
        options.setUserProfileDetails("Preferences and interests relevant to coffee expert agent");

        MemoryStoreDefaultDefinition definition
            = new MemoryStoreDefaultDefinition(deploymentName, embeddingDeploymentName);
        definition.setOptions(options);

        MemoryStoreDetails memoryStore
            = memoryStoreClient.createMemoryStore(memoryStoreName, definition, description, null);
        assertNotNull(memoryStore);
        assertEquals(memoryStoreName, memoryStore.getName());

        ResponsesUserMessageItemParam initialMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(firstMessageContent));
        SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> initialPoller
            = memoryStoreClient.beginUpdateMemories(memoryStoreName, scope, Arrays.asList(initialMessage), null, 300);

        MemoryStoreUpdateResponse initialResponse = initialPoller.poll().getValue();
        assertNotNull(initialResponse);
        String initialUpdateId = initialResponse.getUpdateId();
        assertNotNull(initialUpdateId);

        ResponsesUserMessageItemParam chainedMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(chainedMessageContent));
        SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> chainedPoller = memoryStoreClient
            .beginUpdateMemories(memoryStoreName, scope, Arrays.asList(chainedMessage), initialUpdateId, 0);

        LongRunningOperationStatus chainedStatus = null;
        while (chainedStatus
            != LongRunningOperationStatus.fromString(MemoryStoreUpdateStatus.COMPLETED.toString(), true)) {
            sleep(500);
            System.out.println(chainedStatus);
            chainedStatus = chainedPoller.poll().getStatus();
        }
        MemoryStoreUpdateCompletedResult updateResult = chainedPoller.getFinalResult();
        assertNotNull(updateResult);
        assertNotNull(updateResult.getMemoryOperations());
        assertFalse(updateResult.getMemoryOperations().isEmpty());
        for (MemoryOperation operation : updateResult.getMemoryOperations()) {
            assertNotNull(operation.getKind());
            assertNotNull(operation.getMemoryItem().getMemoryId());
            assertNotNull(operation.getMemoryItem().getContent());
        }

        ResponsesUserMessageItemParam searchQuery
            = new ResponsesUserMessageItemParam(BinaryData.fromString(queryMessageContent));
        MemorySearchOptions searchOptions = new MemorySearchOptions();
        searchOptions.setMaxMemories(5);

        MemoryStoreSearchResponse searchResponse
            = memoryStoreClient.searchMemories(memoryStoreName, scope, Arrays.asList(searchQuery), null, searchOptions);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getMemories());
        assertFalse(searchResponse.getMemories().isEmpty());
        for (MemorySearchItem memory : searchResponse.getMemories()) {
            assertNotNull(memory.getMemoryItem().getMemoryId());
            assertNotNull(memory.getMemoryItem().getContent());
        }
        String previousSearchId = searchResponse.getSearchId();
        assertNotNull(previousSearchId);

        ResponsesAssistantMessageItemParam agentMessage
            = new ResponsesAssistantMessageItemParam(BinaryData.fromString(followupContextContent));
        ResponsesUserMessageItemParam followupQuery
            = new ResponsesUserMessageItemParam(BinaryData.fromString(followupQuestionContent));

        MemoryStoreSearchResponse followupSearch = memoryStoreClient.searchMemories(memoryStoreName, scope,
            Arrays.asList(agentMessage, followupQuery), previousSearchId, searchOptions);
        assertNotNull(followupSearch);
        assertNotNull(followupSearch.getMemories());
        assertFalse(followupSearch.getMemories().isEmpty());
        for (MemorySearchItem memory : followupSearch.getMemories()) {
            assertNotNull(memory.getMemoryItem().getMemoryId());
            assertNotNull(memory.getMemoryItem().getContent());
        }

        memoryStoreClient.deleteScope(memoryStoreName, scope);
        DeleteMemoryStoreResponse deleteResponse = memoryStoreClient.deleteMemoryStore(memoryStoreName);
        assertNotNull(deleteResponse);
        assertTrue(deleteResponse.isDeleted());
    }

    private static void cleanupBeforeTest(MemoryStoresClient memoryStoreClient, String memoryStoreName) {
        // Ensure clean state: delete if it already exists
        try {
            DeleteMemoryStoreResponse deleteExisting = memoryStoreClient.deleteMemoryStore(memoryStoreName);
            assertNotNull(deleteExisting);
        } catch (ResourceNotFoundException ex) {
            // ok if it does not exist
        }
    }
}

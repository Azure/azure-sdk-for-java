// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.*;
import com.azure.ai.agents.models.DeleteMemoryStoreResult;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseInputItem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Awaiting service versioning consolidation.")
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
        for (MemoryStoreDetails store : memoryStoreClient.listMemoryStores(10, PageOrder.DESC, null, null)) {
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
        DeleteMemoryStoreResult deleteResponse = memoryStoreClient.deleteMemoryStore(updatedStore.getName());
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
        System.out.println("Created memory store: " + memoryStore.getName() + " (" + memoryStore.getId() + "): "
            + memoryStore.getDescription());
        System.out.println("  - Chat model: " + definition.getChatModel());
        System.out.println("  - Embedding model: " + definition.getEmbeddingModel());

        // Add memories to the memory store
        ResponseInputItem userMessage = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.USER).content(userMessageContent).build());
        // beginUpdateMemories returns a poller - use update_delay=0 to trigger update immediately
        SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> updatePoller
            = memoryStoreClient.beginUpdateMemories(memoryStoreName, scope, Arrays.asList(userMessage), null, 0);

        // Wait for the update operation to complete
        LongRunningOperationStatus status = null;
        while (status != LongRunningOperationStatus.fromString(MemoryStoreUpdateStatus.COMPLETED.toString(), true)) {
            sleep(500);
            System.out.println("Polling status: " + status);
            status = updatePoller.poll().getStatus();
        }
        MemoryStoreUpdateCompletedResult updateResult = updatePoller.getFinalResult();
        assertNotNull(updateResult);
        assertNotNull(updateResult.getMemoryOperations());
        System.out.println("Updated with " + updateResult.getMemoryOperations().size() + " memory operations");
        for (MemoryOperation operation : updateResult.getMemoryOperations()) {
            assertNotNull(operation.getKind());
            assertNotNull(operation.getMemoryItem().getMemoryId());
            assertNotNull(operation.getMemoryItem().getContent());
            System.out.println("  - Operation: " + operation.getKind() + ", Memory ID: "
                + operation.getMemoryItem().getMemoryId() + ", Content: " + operation.getMemoryItem().getContent());
        }

        ResponseInputItem queryMessage = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.USER).content(queryMessageContent).build());
        MemorySearchOptions searchOptions = new MemorySearchOptions();
        searchOptions.setMaxMemories(5);
        MemoryStoreSearchResponse searchResponse = memoryStoreClient.searchMemories(memoryStoreName, scope,
            Arrays.asList(queryMessage), null, searchOptions);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getMemories());
        System.out.println("Found " + searchResponse.getMemories().size() + " memories");
        for (MemorySearchItem memory : searchResponse.getMemories()) {
            assertNotNull(memory.getMemoryItem().getMemoryId());
            assertNotNull(memory.getMemoryItem().getContent());
            System.out.println("  - Memory ID: " + memory.getMemoryItem().getMemoryId() + ", Content: "
                + memory.getMemoryItem().getContent());
        }

        // Delete memories for a specific scope
        memoryStoreClient.deleteScope(memoryStoreName, scope);
        System.out.println("Deleted memories for scope '" + scope + "'");

        // Delete memory store
        DeleteMemoryStoreResult deleteResponse = memoryStoreClient.deleteMemoryStore(memoryStoreName);
        assertNotNull(deleteResponse);
        assertTrue(deleteResponse.isDeleted());
        System.out.println("Deleted memory store `" + memoryStoreName + "`");
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
        System.out.println("Created memory store: " + memoryStore.getName() + " (" + memoryStore.getId() + "): "
            + memoryStore.getDescription());

        ResponseInputItem initialMessage = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.USER).content(firstMessageContent).build());
        SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> initialPoller
            = memoryStoreClient.beginUpdateMemories(memoryStoreName, scope, Arrays.asList(initialMessage), null, 300);

        MemoryStoreUpdateResponse initialResponse = initialPoller.poll().getValue();
        assertNotNull(initialResponse);
        String initialUpdateId = initialResponse.getUpdateId();
        assertNotNull(initialUpdateId);
        System.out.println("Scheduled memory update operation (Update ID: " + initialUpdateId + ", Status: "
            + initialPoller.poll().getStatus() + ")");

        // Extend the previous update with another update and more messages
        ResponseInputItem chainedMessage = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.USER).content(chainedMessageContent).build());
        SyncPoller<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> chainedPoller = memoryStoreClient
            .beginUpdateMemories(memoryStoreName, scope, Arrays.asList(chainedMessage), initialUpdateId, 0);

        MemoryStoreUpdateResponse chainedResponse = chainedPoller.poll().getValue();
        assertNotNull(chainedResponse);
        String chainedUpdateId = chainedResponse.getUpdateId();
        assertNotNull(chainedUpdateId);
        System.out.println("Scheduled memory update operation (Update ID: " + chainedUpdateId + ", Status: "
            + chainedPoller.poll().getStatus() + ")");

        // As first update has not started yet, the new update will cancel the first update and cover both sets of messages
        System.out.println("Superseded first memory update operation (Update ID: " + initialUpdateId + ", Status: "
            + initialPoller.poll().getStatus() + ")");

        LongRunningOperationStatus chainedStatus = null;
        while (chainedStatus
            != LongRunningOperationStatus.fromString(MemoryStoreUpdateStatus.COMPLETED.toString(), true)) {
            sleep(500);
            chainedStatus = chainedPoller.poll().getStatus();
        }
        MemoryStoreUpdateCompletedResult updateResult = chainedPoller.getFinalResult();
        assertNotNull(updateResult);
        assertNotNull(updateResult.getMemoryOperations());
        System.out.println("Second update " + chainedUpdateId + " completed with "
            + updateResult.getMemoryOperations().size() + " memory operations");
        for (MemoryOperation operation : updateResult.getMemoryOperations()) {
            assertNotNull(operation.getKind());
            assertNotNull(operation.getMemoryItem().getMemoryId());
            assertNotNull(operation.getMemoryItem().getContent());
            System.out.println("  - Operation: " + operation.getKind() + ", Memory ID: "
                + operation.getMemoryItem().getMemoryId() + ", Content: " + operation.getMemoryItem().getContent());
        }

        // Retrieve memories from the memory store
        ResponseInputItem searchQuery = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.USER).content(queryMessageContent).build());
        MemorySearchOptions searchOptions = new MemorySearchOptions();
        searchOptions.setMaxMemories(5);

        MemoryStoreSearchResponse searchResponse
            = memoryStoreClient.searchMemories(memoryStoreName, scope, Arrays.asList(searchQuery), null, searchOptions);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getMemories());
        System.out.println("Found " + searchResponse.getMemories().size() + " memories");
        for (MemorySearchItem memory : searchResponse.getMemories()) {
            assertNotNull(memory.getMemoryItem().getMemoryId());
            assertNotNull(memory.getMemoryItem().getContent());
            System.out.println("  - Memory ID: " + memory.getMemoryItem().getMemoryId() + ", Content: "
                + memory.getMemoryItem().getContent());
        }
        String previousSearchId = searchResponse.getSearchId();
        assertNotNull(previousSearchId);

        // Perform another search using the previous search as context
        ResponseInputItem agentMessage = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.ASSISTANT).content(followupContextContent).build());
        ResponseInputItem followupQuery = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder().role(EasyInputMessage.Role.USER).content(followupQuestionContent).build());

        MemoryStoreSearchResponse followupSearch = memoryStoreClient.searchMemories(memoryStoreName, scope,
            Arrays.asList(agentMessage, followupQuery), previousSearchId, searchOptions);
        assertNotNull(followupSearch);
        assertNotNull(followupSearch.getMemories());
        System.out.println("Found " + followupSearch.getMemories().size() + " memories");
        for (MemorySearchItem memory : followupSearch.getMemories()) {
            assertNotNull(memory.getMemoryItem().getMemoryId());
            assertNotNull(memory.getMemoryItem().getContent());
            System.out.println("  - Memory ID: " + memory.getMemoryItem().getMemoryId() + ", Content: "
                + memory.getMemoryItem().getContent());
        }

        // Delete memories for the current scope
        memoryStoreClient.deleteScope(memoryStoreName, scope);
        System.out.println("Deleted memories for scope '" + scope + "'");

        // Delete memory store
        DeleteMemoryStoreResult deleteResponse = memoryStoreClient.deleteMemoryStore(memoryStoreName);
        assertNotNull(deleteResponse);
        assertTrue(deleteResponse.isDeleted());
        System.out.println("Deleted memory store `" + memoryStoreName + "`");
    }

    private static void cleanupBeforeTest(MemoryStoresClient memoryStoreClient, String memoryStoreName) {
        // Ensure clean state: delete if it already exists
        try {
            DeleteMemoryStoreResult deleteExisting = memoryStoreClient.deleteMemoryStore(memoryStoreName);
            assertNotNull(deleteExisting);
        } catch (ResourceNotFoundException ex) {
            // ok if it does not exist
        }
    }
}

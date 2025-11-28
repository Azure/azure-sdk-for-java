// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.ListAgentsRequestOrder;
import com.azure.ai.agents.models.MemoryOperation;
import com.azure.ai.agents.models.MemorySearchItem;
import com.azure.ai.agents.models.MemorySearchOptions;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDefinition;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
import com.azure.ai.agents.models.MemoryStoreUpdateResponse;
import com.azure.ai.agents.models.MemoryStoreUpdateStatus;
import com.azure.ai.agents.models.ResponsesAssistantMessageItemParam;
import com.azure.ai.agents.models.ResponsesUserMessageItemParam;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Objects;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MemoryStoresAsyncTests extends ClientTestBase {

    private static final LongRunningOperationStatus COMPLETED_OPERATION_STATUS
        = LongRunningOperationStatus.fromString(MemoryStoreUpdateStatus.COMPLETED.toString(), true);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicMemoryStoresCrud(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        MemoryStoresAsyncClient memoryStoreClient = getMemoryStoresAsyncClient(httpClient, serviceVersion);

        String memoryStoreName = "my_memory_store_java";
        String initialDescription = "Example memory store for conversations";
        String updatedDescription = "Updated description";

        String deploymentName = System.getenv("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingDeploymentName = System.getenv("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");
        MemoryStoreDefinition definition = new MemoryStoreDefaultDefinition(deploymentName, embeddingDeploymentName);

        Mono<Void> testMono = cleanupBeforeTest(memoryStoreClient, memoryStoreName)
            .then(memoryStoreClient.createMemoryStore(memoryStoreName, definition, initialDescription, null)
                .flatMap(createdStore -> {
                    assertNotNull(createdStore);
                    assertNotNull(createdStore.getId());
                    assertEquals(memoryStoreName, createdStore.getName());
                    assertEquals(initialDescription, createdStore.getDescription());

                    return memoryStoreClient.getMemoryStore(createdStore.getName()).doOnNext(retrievedStore -> {
                        assertNotNull(retrievedStore);
                        assertEquals(createdStore.getId(), retrievedStore.getId());
                        assertEquals(createdStore.getName(), retrievedStore.getName());
                        assertEquals(createdStore.getDescription(), retrievedStore.getDescription());
                    })
                        .then(memoryStoreClient.updateMemoryStore(createdStore.getName(), updatedDescription, null))
                        .flatMap(updatedStore -> {
                            assertNotNull(updatedStore);
                            assertEquals(createdStore.getId(), updatedStore.getId());
                            assertEquals(createdStore.getName(), updatedStore.getName());
                            assertEquals(updatedDescription, updatedStore.getDescription());

                            return memoryStoreClient.listMemoryStores(10, ListAgentsRequestOrder.DESC, null, null)
                                .collectList()
                                .doOnNext(stores -> {
                                    assertNotNull(stores);
                                    boolean found = false;
                                    for (MemoryStoreDetails store : stores) {
                                        assertNotNull(store.getId());
                                        assertNotNull(store.getName());
                                        if (store.getName().equals(updatedStore.getName())) {
                                            found = true;
                                            assertEquals(updatedStore.getId(), store.getId());
                                            assertEquals(updatedDescription, store.getDescription());
                                        }
                                    }
                                    assertTrue(found, "Created memory store not found in list.");
                                })
                                .then(memoryStoreClient.deleteMemoryStore(updatedStore.getName())
                                    .doOnNext(deleteResponse -> {
                                        assertNotNull(deleteResponse);
                                        assertTrue(deleteResponse.isDeleted());
                                    }))
                                .then(memoryStoreClient.getMemoryStore(updatedStore.getName())
                                    .flatMap(ignored -> Mono.<Void>error(new AssertionError(
                                        "Expected ResourceNotFoundException when retrieving deleted store.")))
                                    .onErrorResume(ResourceNotFoundException.class, ex -> Mono.empty()))
                                .then();
                        });
                }));

        StepVerifier.create(testMono).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicMemoryStores(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        MemoryStoresAsyncClient memoryStoreClient = getMemoryStoresAsyncClient(httpClient, serviceVersion);

        String memoryStoreName = "my_memory_store";
        String description = "Example memory store for conversations";
        String scope = "user_123";
        String userMessageContent = "I prefer dark roast coffee and usually drink it in the morning";
        String queryMessageContent = "What are my coffee preferences?";

        String deploymentName = System.getenv("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingDeploymentName = System.getenv("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");
        MemoryStoreDefaultDefinition definition
            = new MemoryStoreDefaultDefinition(deploymentName, embeddingDeploymentName);
        definition.setOptions(new MemoryStoreDefaultOptions(true, true));

        ResponsesUserMessageItemParam userMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(userMessageContent));
        ResponsesUserMessageItemParam queryMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(queryMessageContent));
        MemorySearchOptions searchOptions = new MemorySearchOptions();
        searchOptions.setMaxMemories(5);

        Mono<Void> testMono = cleanupBeforeTest(memoryStoreClient, memoryStoreName).then(
            memoryStoreClient.createMemoryStore(memoryStoreName, definition, description, null).flatMap(memoryStore -> {
                assertNotNull(memoryStore);
                assertNotNull(memoryStore.getId());
                assertEquals(memoryStoreName, memoryStore.getName());
                assertEquals(description, memoryStore.getDescription());

                return waitForUpdateCompletion(
                    memoryStoreClient.beginUpdateMemories(memoryStoreName, scope, Arrays.asList(userMessage), null, 1))
                        .doOnNext(updateResult -> {
                            assertNotNull(updateResult);
                            assertNotNull(updateResult.getMemoryOperations());
                            assertFalse(updateResult.getMemoryOperations().isEmpty());
                            for (MemoryOperation operation : updateResult.getMemoryOperations()) {
                                assertNotNull(operation.getKind());
                                assertNotNull(operation.getMemoryItem().getMemoryId());
                                assertNotNull(operation.getMemoryItem().getContent());
                            }
                        })
                        .then(memoryStoreClient
                            .searchMemories(memoryStoreName, scope, Arrays.asList(queryMessage), null, searchOptions)
                            .doOnNext(searchResponse -> {
                                assertNotNull(searchResponse);
                                assertNotNull(searchResponse.getMemories());
                                assertFalse(searchResponse.getMemories().isEmpty());
                                for (MemorySearchItem memory : searchResponse.getMemories()) {
                                    assertNotNull(memory.getMemoryItem().getMemoryId());
                                    assertNotNull(memory.getMemoryItem().getContent());
                                }
                            }))
                        .then(memoryStoreClient.deleteScope(memoryStoreName, scope)
                            .doOnNext(deleteScopeResponse -> assertNotNull(deleteScopeResponse)))
                        .then(memoryStoreClient.deleteMemoryStore(memoryStoreName).doOnNext(deleteResponse -> {
                            assertNotNull(deleteResponse);
                            assertTrue(deleteResponse.isDeleted());
                        }))
                        .then();
            }));

        StepVerifier.create(testMono).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void advancedMemoryStores(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        MemoryStoresAsyncClient memoryStoreClient = getMemoryStoresAsyncClient(httpClient, serviceVersion);

        String memoryStoreName = "my_memory_store";
        String description = "Example memory store for conversations";
        String scope = "user_123";
        String firstMessageContent = "I prefer dark roast coffee and usually drink it in the morning";
        String chainedMessageContent = "I also like cappuccinos in the afternoon";
        String queryMessageContent = "What are my morning coffee preferences?";
        String followupContextContent = "You previously indicated a preference for dark roast coffee in the morning.";
        String followupQuestionContent = "What about afternoon?";

        String deploymentName = System.getenv("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingDeploymentName = System.getenv("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        MemoryStoreDefaultOptions options = new MemoryStoreDefaultOptions(true, true);
        options.setUserProfileDetails("Preferences and interests relevant to coffee expert agent");

        MemoryStoreDefaultDefinition definition
            = new MemoryStoreDefaultDefinition(deploymentName, embeddingDeploymentName);
        definition.setOptions(options);

        ResponsesUserMessageItemParam initialMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(firstMessageContent));
        ResponsesUserMessageItemParam chainedMessage
            = new ResponsesUserMessageItemParam(BinaryData.fromString(chainedMessageContent));
        ResponsesUserMessageItemParam searchQuery
            = new ResponsesUserMessageItemParam(BinaryData.fromString(queryMessageContent));
        ResponsesAssistantMessageItemParam agentMessage
            = new ResponsesAssistantMessageItemParam(BinaryData.fromString(followupContextContent));
        ResponsesUserMessageItemParam followupQuery
            = new ResponsesUserMessageItemParam(BinaryData.fromString(followupQuestionContent));

        MemorySearchOptions searchOptions = new MemorySearchOptions();
        searchOptions.setMaxMemories(5);

        Mono<Void> testMono = cleanupBeforeTest(memoryStoreClient, memoryStoreName).then(
            memoryStoreClient.createMemoryStore(memoryStoreName, definition, description, null).flatMap(memoryStore -> {
                assertNotNull(memoryStore);
                assertEquals(memoryStoreName, memoryStore.getName());

                PollerFlux<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> initialPoller
                    = memoryStoreClient.beginUpdateMemories(memoryStoreName, scope, Arrays.asList(initialMessage), null,
                        300);

                Mono<String> initialUpdateIdMono
                    = initialPoller.next().map(AsyncPollResponse::getValue).map(response -> {
                        assertNotNull(response);
                        String updateId = response.getUpdateId();
                        assertNotNull(updateId);
                        return updateId;
                    });

                return initialUpdateIdMono.flatMap(
                    initialUpdateId -> waitForUpdateCompletion(memoryStoreClient.beginUpdateMemories(memoryStoreName,
                        scope, Arrays.asList(chainedMessage), initialUpdateId, 0)).doOnNext(updateResult -> {
                            assertNotNull(updateResult);
                            assertNotNull(updateResult.getMemoryOperations());
                            assertFalse(updateResult.getMemoryOperations().isEmpty());
                            for (MemoryOperation operation : updateResult.getMemoryOperations()) {
                                assertNotNull(operation.getKind());
                                assertNotNull(operation.getMemoryItem().getMemoryId());
                                assertNotNull(operation.getMemoryItem().getContent());
                            }
                        })
                            .then(memoryStoreClient
                                .searchMemories(memoryStoreName, scope, Arrays.asList(searchQuery), null, searchOptions)
                                .flatMap(searchResponse -> {
                                    assertNotNull(searchResponse);
                                    assertNotNull(searchResponse.getMemories());
                                    assertFalse(searchResponse.getMemories().isEmpty());
                                    for (MemorySearchItem memory : searchResponse.getMemories()) {
                                        assertNotNull(memory.getMemoryItem().getMemoryId());
                                        assertNotNull(memory.getMemoryItem().getContent());
                                    }
                                    String previousSearchId = searchResponse.getSearchId();
                                    assertNotNull(previousSearchId);

                                    return memoryStoreClient
                                        .searchMemories(memoryStoreName, scope,
                                            Arrays.asList(agentMessage, followupQuery), previousSearchId, searchOptions)
                                        .doOnNext(followupSearch -> {
                                            assertNotNull(followupSearch);
                                            assertNotNull(followupSearch.getMemories());
                                            assertFalse(followupSearch.getMemories().isEmpty());
                                            for (MemorySearchItem memory : followupSearch.getMemories()) {
                                                assertNotNull(memory.getMemoryItem().getMemoryId());
                                                assertNotNull(memory.getMemoryItem().getContent());
                                            }
                                        });
                                }))
                            .then(memoryStoreClient.deleteScope(memoryStoreName, scope)
                                .doOnNext(deleteScope -> assertNotNull(deleteScope)))
                            .then(memoryStoreClient.deleteMemoryStore(memoryStoreName).doOnNext(deleteResponse -> {
                                assertNotNull(deleteResponse);
                                assertTrue(deleteResponse.isDeleted());
                            }))
                            .then());
            }));

        StepVerifier.create(testMono).verifyComplete();
    }

    private static Mono<Void> cleanupBeforeTest(MemoryStoresAsyncClient memoryStoreClient, String memoryStoreName) {
        return memoryStoreClient.deleteMemoryStore(memoryStoreName)
            .onErrorResume(ResourceNotFoundException.class, ex -> Mono.empty())
            .then();
    }

    private static Mono<MemoryStoreUpdateCompletedResult>
        waitForUpdateCompletion(PollerFlux<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> pollerFlux) {
        Objects.requireNonNull(pollerFlux, "pollerFlux cannot be null");
        return pollerFlux.takeUntil(response -> COMPLETED_OPERATION_STATUS.equals(response.getStatus()))
            .last()
            .map(AsyncPollResponse::getValue)
            .map(response -> {
                MemoryStoreUpdateCompletedResult result = response == null ? null : response.getResult();
                if (result == null) {
                    throw new IllegalStateException("Memory store update did not complete successfully.");
                }
                return result;
            });
    }
}

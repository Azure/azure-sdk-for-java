// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.memory;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaMemoryStoresAsyncClient;
import com.azure.ai.agents.models.MemoryOperation;
import com.azure.ai.agents.models.MemorySearchItem;
import com.azure.ai.agents.models.MemorySearchOptions;
import com.azure.ai.agents.models.MemoryStoreDefaultDefinition;
import com.azure.ai.agents.models.MemoryStoreDefaultOptions;
import com.azure.ai.agents.models.MemoryStoreDetails;
import com.azure.ai.agents.models.MemoryStoreSearchResponse;
import com.azure.ai.agents.models.MemoryStoreUpdateCompletedResult;
import com.azure.ai.agents.models.MemoryStoreUpdateResponse;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseInputItem;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

/**
 * Sample demonstrating conversational memory store operations using the asynchronous
 * {@link BetaMemoryStoresAsyncClient}.
 *
 * <p>Memory stores are a preview feature. Before running, set the following environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME} - a chat completion model deployment name.</li>
 *   <li>{@code AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME} - an embedding model deployment name.</li>
 * </ul>
 */
public class MemoryStoreAdvancedAsyncSample {
    private static final String MEMORY_STORE_NAME = "memory_advanced_store_java_async";
    private static final Duration POLL_TIMEOUT = Duration.ofMinutes(3);
    private static final Duration CLEANUP_TIMEOUT = Duration.ofMinutes(1);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String chatModel = configuration.get("AZURE_AI_CHAT_MODEL_DEPLOYMENT_NAME");
        String embeddingModel = configuration.get("AZURE_AI_EMBEDDING_MODEL_DEPLOYMENT_NAME");

        BetaMemoryStoresAsyncClient memoryStoresAsyncClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .beta()
            .buildBetaMemoryStoresAsyncClient();

        MemoryStoreDefaultOptions options = new MemoryStoreDefaultOptions(true, true)
            .setUserProfileDetails("Preferences and interests relevant to a coffee expert agent");
        MemoryStoreDefaultDefinition definition = new MemoryStoreDefaultDefinition(chatModel, embeddingModel)
            .setOptions(options);

        memoryStoresAsyncClient.deleteMemoryStore(MEMORY_STORE_NAME)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .then(memoryStoresAsyncClient.createMemoryStore(
                MEMORY_STORE_NAME, definition, "Example memory store for conversations", null))
            .flatMap(memoryStore -> runAdvancedMemoryOperations(memoryStoresAsyncClient, memoryStore)
                .onErrorResume(error -> cleanupAsync(memoryStoresAsyncClient, memoryStore.getName(), "user_123")
                    .then(Mono.error(error))))
            .block();
    }

    private static Mono<Void> runAdvancedMemoryOperations(BetaMemoryStoresAsyncClient memoryStoresClient,
        MemoryStoreDetails memoryStore) {
        String scope = "user_123";
        ResponseInputItem initialMessage = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .content("I prefer dark roast coffee and usually drink it in the morning")
                .type(EasyInputMessage.Type.MESSAGE)
                .build());

        PollerFlux<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> initialPoller
            = memoryStoresClient.beginUpdateMemories(
                memoryStore.getName(), scope, Arrays.asList(initialMessage), null, 300);

        return initialPoller.next()
            .map(AsyncPollResponse::getValue)
            .flatMap(initialResponse -> {
                System.out.printf("Scheduled memory update (id: %s, status: %s)%n",
                    initialResponse.getUpdateId(), initialResponse.getStatus());

                ResponseInputItem chainedMessage = ResponseInputItem.ofEasyInputMessage(
                    EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .content("I also like cappuccinos in the afternoon")
                        .type(EasyInputMessage.Type.MESSAGE)
                        .build());
                PollerFlux<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> chainedPoller
                    = memoryStoresClient.beginUpdateMemories(
                        memoryStore.getName(), scope, Arrays.asList(chainedMessage),
                        initialResponse.getUpdateId(), 0);

                return waitForUpdateCompletion(chainedPoller)
                    .doOnNext(updateResult -> {
                        System.out.printf("Memory update completed with %d operations%n",
                            updateResult.getMemoryOperations().size());
                        for (MemoryOperation operation : updateResult.getMemoryOperations()) {
                            System.out.printf("  - Operation: %s, memory ID: %s, content: %s%n",
                                operation.getKind(), operation.getMemoryItem().getMemoryId(),
                                operation.getMemoryItem().getContent());
                        }
                    })
                    .flatMap(updateResult -> searchMemories(memoryStoresClient, memoryStore.getName(), scope));
            })
            .then(memoryStoresClient.deleteScope(memoryStore.getName(), scope))
            .doOnSuccess(unused -> System.out.printf("Deleted memories for scope '%s'%n", scope))
            .then(memoryStoresClient.deleteMemoryStore(memoryStore.getName()))
            .doOnSuccess(unused -> System.out.printf("Memory store deleted (name: %s)%n", memoryStore.getName()));
    }

    private static Mono<Void> searchMemories(BetaMemoryStoresAsyncClient memoryStoresClient, String memoryStoreName,
        String scope) {
        MemorySearchOptions searchOptions = new MemorySearchOptions().setMaxMemories(5);
        ResponseInputItem searchQuery = ResponseInputItem.ofEasyInputMessage(
            EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .content("What are my morning coffee preferences?")
                .type(EasyInputMessage.Type.MESSAGE)
                .build());

        return memoryStoresClient.searchMemories(
            memoryStoreName, scope, Arrays.asList(searchQuery), null, searchOptions)
            .doOnNext(MemoryStoreAdvancedAsyncSample::printSearchResults)
            .flatMap(searchResponse -> {
                ResponseInputItem agentMessage = ResponseInputItem.ofEasyInputMessage(
                    EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.ASSISTANT)
                        .content("You previously indicated a preference for dark roast coffee in the morning.")
                        .type(EasyInputMessage.Type.MESSAGE)
                        .build());
                ResponseInputItem followupQuery = ResponseInputItem.ofEasyInputMessage(
                    EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .content("What about afternoon?")
                        .type(EasyInputMessage.Type.MESSAGE)
                        .build());

                return memoryStoresClient.searchMemories(
                    memoryStoreName, scope, Arrays.asList(agentMessage, followupQuery),
                    searchResponse.getSearchId(), searchOptions)
                    .doOnNext(MemoryStoreAdvancedAsyncSample::printSearchResults)
                    .then();
            });
    }

    private static Mono<MemoryStoreUpdateCompletedResult> waitForUpdateCompletion(
        PollerFlux<MemoryStoreUpdateResponse, MemoryStoreUpdateCompletedResult> poller) {
        return poller.takeUntil(response -> response.getStatus().isComplete())
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)
            .timeout(POLL_TIMEOUT);
    }

    private static Mono<Void> cleanupAsync(BetaMemoryStoresAsyncClient memoryStoresClient, String memoryStoreName,
        String scope) {
        return memoryStoresClient.deleteScope(memoryStoreName, scope)
            .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
            .timeout(CLEANUP_TIMEOUT)
            .onErrorResume(error -> {
                System.err.println("Unable to delete the memory scope: " + error.getMessage());
                return Mono.empty();
            })
            .then(memoryStoresClient.deleteMemoryStore(memoryStoreName)
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty())
                .timeout(CLEANUP_TIMEOUT)
                .onErrorResume(error -> {
                    System.err.println("Unable to delete the memory store: " + error.getMessage());
                    return Mono.empty();
                }));
    }

    private static void printSearchResults(MemoryStoreSearchResponse searchResponse) {
        System.out.printf("Found %d memories (search ID: %s)%n",
            searchResponse.getMemories().size(), searchResponse.getSearchId());
        for (MemorySearchItem memory : searchResponse.getMemories()) {
            System.out.printf("  - Memory ID: %s, content: %s%n",
                memory.getMemoryItem().getMemoryId(), memory.getMemoryItem().getContent());
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerCreateDeleteWithSameNameTest extends TestSuiteBase{
    private final static int TIMEOUT = 300000;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public ContainerCreateDeleteWithSameNameTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public <T> void query() throws Exception {
        String query = "SELECT * FROM r";

        Consumer<CosmosAsyncContainer> func = (container) -> {
            InternalObjectNode docDefinition = getDocumentDefinition();
            container.createItem(docDefinition).block();

            CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
            CosmosPagedFlux<InternalObjectNode> queryFlux = container.queryItems(query, requestOptions, InternalObjectNode.class);
            FeedResponseListValidator<InternalObjectNode> queryValidator = new FeedResponseListValidator.Builder<InternalObjectNode>()
                .totalSize(1)
                .numberOfPages(1)
                .build();
            validateQuerySuccess(queryFlux.byPage(10), queryValidator);
        };

        createDeleteContainerWithSameName(func);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public <T> void readItem() throws Exception {

        Consumer<CosmosAsyncContainer> func = (container) -> {
            InternalObjectNode docDefinition = getDocumentDefinition();
            container.createItem(docDefinition).block();

            Mono<CosmosItemResponse<InternalObjectNode>> responseMono = container.readItem(docDefinition.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                new CosmosItemRequestOptions(),
                InternalObjectNode.class);

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withId(docDefinition.getId())
                    .build();

            this.validateItemSuccess(responseMono, validator);
        };

        createDeleteContainerWithSameName(func);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public <T> void deleteItem() throws Exception {

        Consumer<CosmosAsyncContainer> func = (container) -> {
            InternalObjectNode docDefinition = getDocumentDefinition();
            container.createItem(docDefinition).block();

            Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(
                docDefinition.getId(),
                new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(docDefinition, "mypk")),
                new CosmosItemRequestOptions());

            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .nullResource()
                    .build();
            this.validateItemSuccess(deleteObservable, validator);
        };

        createDeleteContainerWithSameName(func);
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public <T> void upsertItem() throws Exception {

        Consumer<CosmosAsyncContainer> func = (container) -> {
            InternalObjectNode docDefinition = getDocumentDefinition();
            docDefinition = container.createItem(docDefinition).block().getItem();

            String newPropValue = UUID.randomUUID().toString();
            BridgeInternal.setProperty(docDefinition, "newProp", newPropValue);

            Mono<CosmosItemResponse<InternalObjectNode>> readObservable = container.upsertItem(docDefinition, new CosmosItemRequestOptions());

            // Validate result
            CosmosItemResponseValidator validator =
                new CosmosItemResponseValidator.Builder<CosmosItemResponse<InternalObjectNode>>()
                    .withProperty("newProp", newPropValue)
                    .build();

            this.validateItemSuccess(readObservable, validator);
        };

        createDeleteContainerWithSameName(func);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public <T> void changeFeed() throws Exception {

        BiConsumer<CosmosAsyncContainer, CosmosAsyncContainer> func = (feedContainer, leaseContainer) -> {
            String hostName = RandomStringUtils.randomAlphabetic(6);
            int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
            final int FEED_COUNT = 10;
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            setupReadFeedDocuments(createdDocuments, feedContainer, FEED_COUNT);

            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((docs) -> {
                    for (JsonNode item : docs) {
                        receivedDocuments.put(item.get("id").asText(), item);
                    }
                })
                .feedContainer(feedContainer)
                .leaseContainer(leaseContainer)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(20))
                    .setLeaseAcquireInterval(Duration.ofSeconds(10))
                    .setLeaseExpirationInterval(Duration.ofSeconds(30))
                    .setFeedPollDelay(Duration.ofSeconds(2))
                    .setLeasePrefix("TEST")
                    .setMaxItemCount(10)
                    .setStartFromBeginning(true)
                    .setMaxScaleCount(0) // unlimited
                )
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.elastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
            } catch (Exception ex) {
                throw ex;
            }

            // Wait for the feed processor to receive and process the documents.
            try {
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

                changeFeedProcessor.stop().subscribeOn(Schedulers.elastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                for (InternalObjectNode item : createdDocuments) {
                    assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
                }

                // Wait for the feed processor to shutdown.
                Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted exception", e);
            }
        };

        changeFeedCreateDeleteContainerWithSameName(func);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_ContainerCreateDeleteWithSameNameTest() throws Exception {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void after_ContainerCreateDeleteWithSameNameTest() throws Exception {
        safeDeleteAllCollections(createdDatabase);
    }

    private <T> void createDeleteContainerWithSameName(Consumer<CosmosAsyncContainer> validateFunc) {
        CosmosAsyncContainer container = null;
        try{
            // step1: create container
            String testContainerId = UUID.randomUUID().toString();
            CosmosContainerProperties containerProperties = getCollectionDefinition(testContainerId);
            container = createCollection(this.createdDatabase, containerProperties, new CosmosContainerRequestOptions());

            // Step2: execute func
            validateFunc.accept(container);

            // step3: delete the container
            safeDeleteCollection(container);

            // step4: recreate the container with same id as step1
            container = createCollection(this.createdDatabase, containerProperties, new CosmosContainerRequestOptions());

            // step5: same as step2.
            // This part will confirm the cache refreshed correctly
            validateFunc.accept(container);
        }
        finally {
            safeDeleteCollection(container);
        }
    }

    private <T> void changeFeedCreateDeleteContainerWithSameName(BiConsumer<CosmosAsyncContainer, CosmosAsyncContainer> validateFunc) {
        CosmosAsyncContainer feedContainer = null;
        CosmosAsyncContainer leaseContainer = null;

        try {
            // step1: create feed container and lease container
            String feedContainerId = UUID.randomUUID().toString();
            CosmosContainerProperties feedContainerProperties = getCollectionDefinition(feedContainerId);
            feedContainer = createCollection(this.createdDatabase,feedContainerProperties, new CosmosContainerRequestOptions());

            String leaseContainerId = UUID.randomUUID().toString();
            CosmosContainerProperties leaseContainerProperties = getCollectionDefinition(leaseContainerId);
            leaseContainer = createLeaseContainer(leaseContainerProperties.getId());

            // Step2: execute func
            validateFunc.accept(feedContainer, leaseContainer);

            // step3: delete the containers
            safeDeleteCollection(feedContainer);
            safeDeleteCollection(leaseContainer);

            // step4: recreate the feed container and lease container with same ids as step1
            feedContainer = createCollection(this.createdDatabase, feedContainerProperties, new CosmosContainerRequestOptions());
            leaseContainer = createLeaseContainer(leaseContainerProperties.getId());

            // step5: same as step2.
            // This part will confirm the cache refreshed correctly
            validateFunc.accept(feedContainer, leaseContainer);
        }
        finally {
            safeDeleteCollection(feedContainer);
            safeDeleteCollection(leaseContainer);
        }
    }

    private static InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "}"
            , uuid, uuid));
        return doc;
    }

    private void setupReadFeedDocuments(List<InternalObjectNode> createdDocuments, CosmosAsyncContainer feedCollection, long count) {
        List<InternalObjectNode> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments.addAll(bulkInsertBlocking(feedCollection, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private CosmosAsyncContainer createLeaseContainer(String conatinerId) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(conatinerId, "/id");
        return createCollection(createdDatabase, collectionDefinition, options);
    }
}

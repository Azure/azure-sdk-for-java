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
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.FeedResponseListValidator;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.routing.LocationCache;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ContainerCreateDeleteWithSameNameTest extends TestSuiteBase {
    private final static int TIMEOUT = 300000;
    // Delete collections in emulator is not instant,
    // so to avoid get 500 back, we are adding delay for creating the collection with same name
    private final static int COLLECTION_RECREATION_TIME_DELAY = 5000;
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase createdDatabase;
    private String testDatabaseId = CosmosDatabaseForTest.generateId();

    private Function<TestObject, String> getId = new Function<TestObject, String>() {
        @Override
        public String apply(TestObject testObject) {
            return testObject.getId();
        }
    };

    private Function<TestObject, String> getMypk = new Function<TestObject, String>() {
        @Override
        public String apply(TestObject testObject) {
            return testObject.getMypk();
        }
    };

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ContainerCreateDeleteWithSameNameTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
        this.subscriberValidationTimeout = TIMEOUT;
    }

    @DataProvider(name = "containerRecreateFeedArgProvider")
    public Object[][] containerRecreateFeedArgProvider() {
        return new Object[][] {
            { 10100, "/id", getId, 10100, "/id", getId },
            { 10100, "/id", getId, 400, "/id", getId },
            { 400, "/id", getId, 10100, "/id", getId },
            { 10100, "/mypk", getMypk, 10100, "/id", getId }
        };
    }

    @DataProvider(name = "containerRecreateArgProvider")
    public Object[][] containerRecreateArgProvider() {
        return new Object[][] {
            { 10100, "/id", getId, 10100, "/id", getId },
            { 10100, "/mypk", getMypk, 400, "/id", getId }
        };
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateFeedArgProvider", timeOut = TIMEOUT)
    public <T> void query(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {
        String query = "SELECT * FROM r";

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {

                // Create data with new client, so cache can be tested in query workload on recreate
                CosmosAsyncClient cosmosClient = getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                List<TestObject> createdItems = new ArrayList<>();
                for (int i = 0; i < 1; i++) {
                    TestObject testObject = TestObject.creatNewTestObject();
                    cosmosAsyncContainer.createItem(testObject).block();
                    createdItems.add(testObject);
                }
                cosmosClient.close();

                CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
                CosmosPagedFlux<TestObject> queryFlux = container.queryItems(query, requestOptions, TestObject.class);
                FeedResponseListValidator.Builder<TestObject> queryValidatorBuilder = new FeedResponseListValidator.Builder<TestObject>()
                    .totalSize(createdItems.size())
                    .numberOfPagesIsGreaterThanOrEqualTo(1);

                if (isContainerRecreated) {
                    queryValidatorBuilder.withValidator(
                        new FeedResponseListValidator<TestObject>() {
                            @Override
                            public void validate(List<FeedResponse<TestObject>> feedList) {
                                // for the first page response, it contains container refresh
                                List<CosmosDiagnostics> feedResponseDiagnostics =
                                    new ArrayList<>(feedList.get(0).getCosmosDiagnostics().getDiagnosticsContext().getDiagnostics());
                                List<CosmosDiagnostics> diagnosticsWithCollectionRefresh =
                                    feedResponseDiagnostics
                                        .stream()
                                        .filter(diagnostics -> containsCollectionRefresh(diagnostics, container))
                                        .collect(Collectors.toList());

                                assertThat(feedResponseDiagnostics.size()).isGreaterThanOrEqualTo(1);
                            }
                        }
                    );
                }

                this.validateQuerySuccess(queryFlux.byPage(10), queryValidatorBuilder.build());
                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void readItem(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
            // Create data with new client, so cache can be tested in read workload on recreate
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                TestObject docDefinition = TestObject.creatNewTestObject();
                cosmosAsyncContainer.createItem(docDefinition).block();
                cosmosClient.close();

                Mono<CosmosItemResponse<TestObject>> responseMono = container.readItem(docDefinition.getId(),
                    new PartitionKey(getPkFunction.apply(docDefinition)),
                    new CosmosItemRequestOptions(),
                    TestObject.class);

                CosmosItemResponseValidator.Builder validatorBuilder =
                    new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                        .withId(docDefinition.getId());
                if (isContainerRecreated) {
                    validatorBuilder.withValidator(new CosmosItemResponseValidator() {
                        @Override
                        public void validate(CosmosItemResponse itemResponse) {
                            assertThat(containsCollectionRefresh(itemResponse.getDiagnostics(), container)).isTrue();
                        }
                    });
                }

                this.validateItemSuccess(responseMono, validatorBuilder.build());

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void deleteItem(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                // Create data with new client, so cache can be tested in delete workload on recreate
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                TestObject docDefinition = TestObject.creatNewTestObject();
                cosmosAsyncContainer.createItem(docDefinition).block();
                cosmosClient.close();

                Mono<CosmosItemResponse<Object>> deleteObservable = container.deleteItem(
                    docDefinition.getId(),
                    new PartitionKey(getPkFunction.apply(docDefinition)),
                    new CosmosItemRequestOptions());

                CosmosItemResponseValidator.Builder validatorBuilder =
                    new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                        .nullResource();
                if (isContainerRecreated) {
                     validatorBuilder.withValidator(new CosmosItemResponseValidator() {
                        @Override
                        public void validate(CosmosItemResponse itemResponse) {
                            assertThat(containsCollectionRefresh(itemResponse.getDiagnostics(), container)).isTrue();
                        }
                    })
                        .build();
                }
                this.validateItemSuccess(deleteObservable, validatorBuilder.build());

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void upsertItem(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                // Create data with new client, so cache can be tested in upsert workload on recreate
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                TestObject docDefinition = TestObject.creatNewTestObject();
                cosmosAsyncContainer.createItem(docDefinition).block();
                cosmosClient.close();

                docDefinition.setProp(UUID.randomUUID().toString());

                Mono<CosmosItemResponse<TestObject>> readObservable = container.upsertItem(docDefinition, new CosmosItemRequestOptions());

                // Validate result
                CosmosItemResponseValidator.Builder validatorBuilder =
                    new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                        .withProperty("prop", docDefinition.getProp());
                if (isContainerRecreated) {
                     validatorBuilder.withValidator(new CosmosItemResponseValidator() {
                        @Override
                        public void validate(CosmosItemResponse itemResponse) {
                            assertThat(containsCollectionRefresh(itemResponse.getDiagnostics(), container)).isTrue();
                        }
                    });
                }

                this.validateItemSuccess(readObservable, validatorBuilder.build());

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void createItem(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                TestObject docDefinition = TestObject.creatNewTestObject();
                Mono<CosmosItemResponse<TestObject>> createObservable = container.createItem(docDefinition);

                // Validate result
                CosmosItemResponseValidator.Builder validatorBuilder =
                    new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                        .withId(docDefinition.getId())
                        .withProperty("prop", docDefinition.getProp());

                if (isContainerRecreated) {
                    validatorBuilder.withValidator(new CosmosItemResponseValidator() {
                        @Override
                        public void validate(CosmosItemResponse itemResponse) {
                            assertThat(containsCollectionRefresh(itemResponse.getDiagnostics(), container)).isTrue();
                        }
                    });
                }

                this.validateItemSuccess(createObservable, validatorBuilder.build());

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateFeedArgProvider", timeOut = TIMEOUT)
    public <T> void changeFeedProcessor(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        ObjectMapper objectMapper = Utils.getSimpleObjectMapper();
        TriConsumer<CosmosAsyncContainer, CosmosAsyncContainer, Boolean> func =
            (feedContainer, leaseContainer, isContainerRecreated) -> {
                String hostName = RandomStringUtils.randomAlphabetic(6);
                int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
                final int FEED_COUNT = 5;
                List<TestObject> createdDocuments = new ArrayList<>();
                Map<String, TestObject> receivedDocuments = new ConcurrentHashMap<>();

                setupReadFeedDocuments(createdDocuments, feedContainer, FEED_COUNT);

                ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                    .hostName(hostName)
                    .handleChanges((docs) -> {
                        for (JsonNode item : docs) {
                            try {
                                TestObject obj = objectMapper.treeToValue(item, TestObject.class);
                                receivedDocuments.put(obj.getId(), obj);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
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
                    changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .subscribe();

                    // Wait for the feed processor to receive and process the documents.
                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                    assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

                    long remainingWork = 2 * CHANGE_FEED_PROCESSOR_TIMEOUT;
                    while (remainingWork > 0 && receivedDocuments.size() < createdDocuments.size()) {
                        remainingWork -= 100;
                        Thread.sleep(100);
                    }

                    assertThat(remainingWork >= 0).as("Failed to receive all the feed documents").isTrue();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted exception", e);
                } finally {
                    changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                    // Wait for the feed processor to shutdown.
                    try {
                        Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
                    } catch (InterruptedException e) {
                    }
                }

                this.assertCollectionCache(feedContainer);
        };

        changeFeedCreateDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void replaceItem(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
            // Create data with new client, so cache can be tested in replace workload on recreate
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                TestObject docDefinition = TestObject.creatNewTestObject();
                cosmosAsyncContainer.createItem(docDefinition).block();
                cosmosClient.close();

                docDefinition.setProp(UUID.randomUUID().toString());

                Mono<CosmosItemResponse<TestObject>> readObservable =
                    container.replaceItem(
                        docDefinition,
                        docDefinition.getId(),
                        new PartitionKey(getPkFunction.apply(docDefinition)),
                        new CosmosItemRequestOptions());

                // Validate result
                CosmosItemResponseValidator.Builder validatorBuilder =
                    new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                        .withProperty("prop", docDefinition.getProp());
                if (isContainerRecreated) {
                    validatorBuilder.withValidator(new CosmosItemResponseValidator() {
                        @Override
                        public void validate(CosmosItemResponse itemResponse) {
                            assertThat(containsCollectionRefresh(itemResponse.getDiagnostics(), container)).isTrue();
                        }
                    });
                }

                this.validateItemSuccess(readObservable, validatorBuilder.build());

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void patchItem(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                // Create data with new client, so cache can be tested in patch workload on recreate
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                TestObject docDefinition = TestObject.creatNewTestObject();
                cosmosAsyncContainer.createItem(docDefinition).block();
                cosmosClient.close();


                CosmosPatchOperations patchOperations = CosmosPatchOperations.create();
                String newPropertyValue = UUID.randomUUID().toString();
                patchOperations.add("/newProperty", newPropertyValue);

                Mono<CosmosItemResponse<JsonNode>> readObservable =
                    container.patchItem(
                        docDefinition.getId(),
                        new PartitionKey(getPkFunction.apply(docDefinition)),
                        patchOperations,
                        JsonNode.class);

                // Validate result
                CosmosItemResponseValidator.Builder validatorBuilder =
                    new CosmosItemResponseValidator.Builder<CosmosItemResponse<TestObject>>()
                        .withProperty("newProperty", newPropertyValue);
                if (isContainerRecreated) {
                       validatorBuilder.withValidator(new CosmosItemResponseValidator() {
                            @Override
                            public void validate(CosmosItemResponse itemResponse) {
                                assertThat(containsCollectionRefresh(itemResponse.getDiagnostics(), container)).isTrue();
                            }
                        });
                }

                this.validateItemSuccess(readObservable, validatorBuilder.build());

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void batch(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunctoin, isContainerRecreated) -> {

                TestObject newTestItem = TestObject.creatNewTestObject();
                CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(getPkFunctoin.apply(newTestItem)));
                cosmosBatch.createItemOperation(newTestItem);

                CosmosBatchResponse batchResponse = container.executeCosmosBatch(cosmosBatch).block();
                assertThat(batchResponse.getResults().size()).isEqualTo(1);

                if (isContainerRecreated) {
                    assertThat(containsCollectionRefresh(batchResponse.getDiagnostics(), container)).isTrue();
                }

                // validate can read item back successfully
                container.readItem(newTestItem.getId(), new PartitionKey(getPkFunctoin.apply(newTestItem)), TestObject.class).block();
                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public <T> void bulk(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                List<CosmosItemOperation> itemOperations = new ArrayList<>();
                List<TestObject> createdItems = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.creatNewTestObject();
                    itemOperations.add(CosmosBulkOperations.getCreateItemOperation(testObject, new PartitionKey(getPkFunction.apply(testObject))));
                    createdItems.add(testObject);
                }

                container.executeBulkOperations(Flux.fromIterable(itemOperations)).blockLast();

                String query = "select * from c";
                CosmosPagedFlux<TestObject> queryFlux = container.queryItems(query, TestObject.class);
                FeedResponseListValidator<TestObject> queryValidator = new FeedResponseListValidator.Builder<TestObject>()
                    .totalSize(createdItems.size())
                    .numberOfPagesIsGreaterThanOrEqualTo(1)
                    .build();

                this.validateQuerySuccess(queryFlux.byPage(), queryValidator);
                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateArgProvider", timeOut = TIMEOUT)
    public void getFeedRanges(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                // Get feed ranges with a different client
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                List<FeedRange> expectedFeedRanges = cosmosAsyncContainer.getFeedRanges().block();
                cosmosClient.close();

                List<FeedRange> feedRanges = container.getFeedRanges().block();
                assertThat(feedRanges.size()).isEqualTo(expectedFeedRanges.size());
                assertThat(expectedFeedRanges.containsAll(feedRanges)).isTrue();

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateFeedArgProvider", timeOut = TIMEOUT)
    public void queryChangeFeed(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                // Create few items with a different client
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                List<String> createdItems = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.creatNewTestObject();
                    cosmosAsyncContainer.createItem(testObject).block();
                    createdItems.add(testObject.getId());
                }
                cosmosClient.close();

                List<String> itemsFromChangeFeed = new ArrayList<>();
                AtomicBoolean isFirstResponse = new AtomicBoolean(true);
                AtomicReference<CosmosDiagnostics> firstResponseCosmosDiagnostics = new AtomicReference<>(null);
                container.queryChangeFeed(CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(FeedRange.forFullRange()), TestObject.class)
                    .byPage()
                    .doOnNext(response -> {
                        itemsFromChangeFeed.addAll(response.getResults().stream().map(TestObject::getId).collect(Collectors.toList()));

                        // validate cosmos diagnostics
                        if (isFirstResponse.compareAndSet(true, false)) {
                            firstResponseCosmosDiagnostics.set(response.getCosmosDiagnostics());
                        }
                    })
                    .blockLast();

                assertThat(createdItems.size()).isEqualTo(itemsFromChangeFeed.size());
                assertThat(createdItems.containsAll(itemsFromChangeFeed)).isTrue();

                // validate collection cache refresh happens, and collectionRid is being included in the diagnostics
                if (isContainerRecreated) {
                    if (isFirstResponse.compareAndSet(true, false)) {
                        List<CosmosDiagnostics> cosmosDiagnostics =
                            firstResponseCosmosDiagnostics
                                .get()
                                .getDiagnosticsContext()
                                .getDiagnostics()
                                .stream()
                                .collect(Collectors.toList());
                        List<CosmosDiagnostics> diagnosticsWithCollectionRefresh =
                            cosmosDiagnostics
                                .stream()
                                .filter(diagnostics -> containsCollectionRefresh(diagnostics, container))
                                .collect(Collectors.toList());
                        assertThat(diagnosticsWithCollectionRefresh.size()).isEqualTo(1);
                    }
                }

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @Test(groups = {"long-emulator"}, dataProvider = "containerRecreateFeedArgProvider", timeOut = TIMEOUT, enabled = false)
    public void readMany(
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws Exception {

        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> func =
            (container, getPkFunction, isContainerRecreated) -> {
                // Create few items with a different client
                CosmosAsyncClient cosmosClient =  getClientBuilder().buildAsyncClient();
                CosmosAsyncContainer cosmosAsyncContainer = cosmosClient.getDatabase(container.getDatabase().getId()).getContainer(container.getId());
                List<TestObject> createdItems = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    TestObject testObject = TestObject.creatNewTestObject();
                    cosmosAsyncContainer.createItem(testObject).block();
                    createdItems.add(testObject);
                }

                cosmosClient.close();

                List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
                List<TestObject> itemsToRead = createdItems.subList(0, 3);
                for (TestObject itemToRead : itemsToRead) {
                    cosmosItemIdentities.add(new CosmosItemIdentity(new PartitionKey(getPkFunction.apply(itemToRead)), itemToRead.getId()));
                }

                FeedResponse<TestObject> readManyResponse = container.readMany(cosmosItemIdentities, TestObject.class).block();

                if (isContainerRecreated) {
                    List<CosmosDiagnostics> feedResponseDiagnostics =
                        new ArrayList<>(readManyResponse.getCosmosDiagnostics().getDiagnosticsContext().getDiagnostics());
                    List<CosmosDiagnostics> diagnosticsWithCollectionRefresh =
                        feedResponseDiagnostics
                            .stream()
                            .filter(diagnostics -> containsCollectionRefresh(diagnostics, container))
                            .collect(Collectors.toList());

                    assertThat(diagnosticsWithCollectionRefresh.isEmpty()).isFalse();
                }

                List<String> readManyItemIds =
                    readManyResponse
                        .getResults()
                        .stream()
                        .map(TestObject::getId)
                        .collect(Collectors.toList());
                assertThat(readManyItemIds.size()).isEqualTo(itemsToRead.size());
                assertThat(readManyItemIds.containsAll(itemsToRead.stream().map(TestObject::getId).collect(Collectors.toList()))).isTrue();

                this.assertCollectionCache(container);
        };

        createDeleteContainerWithSameName(
            func,
            ruBeforeDelete,
            pkPathBeforeDelete,
            getPkBeforeDelete,
            ruAfterRecreate,
            pkPathAfterRecreate,
            getPkAfterRecreate);
    }

    @BeforeClass(groups = {"long-emulator"}, timeOut = SETUP_TIMEOUT)
    public void before_ContainerCreateDeleteWithSameNameTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, testDatabaseId);
    }

    @AfterClass(groups = {"long-emulator"}, timeOut = SETUP_TIMEOUT)
    public void after_ContainerCreateDeleteWithSameNameTest() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }

    private <T> void createDeleteContainerWithSameName(
        TriConsumer<CosmosAsyncContainer, Function<TestObject, String>, Boolean> validateFunc,
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        Function<TestObject, String> getPkBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate,
        Function<TestObject, String> getPkAfterRecreate) throws InterruptedException {
        CosmosAsyncContainer container = null;
        try {
            // step1: create container
            String testContainerId = UUID.randomUUID().toString();

            PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
            ArrayList<String> paths = new ArrayList<>();
            paths.add(pkPathBeforeDelete);
            partitionKeyDef.setPaths(paths);

            CosmosContainerProperties containerProperties = getCollectionDefinition(testContainerId, partitionKeyDef);
            container = createCollection(this.createdDatabase, containerProperties, new CosmosContainerRequestOptions(), ruBeforeDelete);

            // Step2: execute func
            validateFunc.accept(container, getPkBeforeDelete, false);

            // step3: delete the container
            safeDeleteCollection(container);
            Thread.sleep(COLLECTION_RECREATION_TIME_DELAY);

            // step4: recreate the container with same id as step1
            partitionKeyDef.setPaths(Arrays.asList(pkPathAfterRecreate));

            containerProperties = getCollectionDefinition(testContainerId, partitionKeyDef);
            container = createCollection(this.createdDatabase, containerProperties, new CosmosContainerRequestOptions(), ruAfterRecreate);

            // step5: same as step2.
            // This part will confirm the cache refreshed correctly
            validateFunc.accept(container, getPkAfterRecreate, true);
        } finally {
            safeDeleteCollection(container);
        }
    }

    private <T> void changeFeedCreateDeleteContainerWithSameName(
        TriConsumer<CosmosAsyncContainer, CosmosAsyncContainer, Boolean> validateFunc,
        int ruBeforeDelete,
        String pkPathBeforeDelete,
        int ruAfterRecreate,
        String pkPathAfterRecreate) throws InterruptedException {
        CosmosAsyncContainer feedContainer = null;
        CosmosAsyncContainer leaseContainer = null;

        try {
            // step1: create feed container and lease container
            String feedContainerId = UUID.randomUUID().toString();
            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            partitionKeyDefinition.setPaths(Arrays.asList(pkPathBeforeDelete));
            CosmosContainerProperties feedContainerProperties = getCollectionDefinition(feedContainerId, partitionKeyDefinition);
            feedContainer = createCollection(this.createdDatabase, feedContainerProperties, new CosmosContainerRequestOptions(), ruBeforeDelete);

            String leaseContainerId = UUID.randomUUID().toString();
            CosmosContainerProperties leaseContainerProperties = getCollectionDefinition(leaseContainerId);
            leaseContainer = createLeaseContainer(leaseContainerProperties.getId());

            // Step2: execute func
            validateFunc.accept(feedContainer, leaseContainer, false);

            // step3: delete the lease container and feed container
            safeDeleteCollection(leaseContainer);
            safeDeleteCollection(feedContainer);
            Thread.sleep(COLLECTION_RECREATION_TIME_DELAY);

            // step 4: recreate the feed container with same id as step 1
            partitionKeyDefinition.setPaths(Arrays.asList(pkPathAfterRecreate));
            feedContainerProperties = getCollectionDefinition(feedContainerId, partitionKeyDefinition);
            feedContainer = createCollection(this.createdDatabase, feedContainerProperties, new CosmosContainerRequestOptions(), ruAfterRecreate);

            // step5: recreate the lease container and lease container with same ids as step1
            leaseContainer = createLeaseContainer(leaseContainerProperties.getId());

            // step6: same as step2.
            // This part will confirm the cache refreshed correctly
            validateFunc.accept(feedContainer, leaseContainer, true);
        } finally {
            safeDeleteCollection(feedContainer);
            safeDeleteCollection(leaseContainer);
        }
    }

    private void setupReadFeedDocuments(List<TestObject> createdDocuments, CosmosAsyncContainer feedContainer, long count) {
        List<TestObject> docDefList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            docDefList.add(TestObject.creatNewTestObject());
        }

        createdDocuments.addAll(bulkInsertBlocking(feedContainer, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private CosmosAsyncContainer createLeaseContainer(String conatinerId) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(conatinerId, "/id");
        return createCollection(createdDatabase, collectionDefinition, options);
    }

    private void assertCollectionCache(CosmosAsyncContainer container) {
        CosmosContainerProperties containerProperties = container.read().block().getProperties();

        try {
            DocumentCollection cachedDocumentCollection = getDocumentCollectionFromCache(BridgeInternal.extractContainerSelfLink(container).substring(1));
            assertThat(cachedDocumentCollection.getResourceId()).isEqualTo(containerProperties.getResourceId());
        } catch (Exception e) {
            fail("error while fetching documentCollection from cache");
        }
    }

    private boolean containsCollectionRefresh(CosmosDiagnostics cosmosDiagnostics, CosmosAsyncContainer container) {
        assertThat(cosmosDiagnostics).isNotNull();
        assertThat(container).isNotNull();

        String expectedContainerRid = container.read().block().getProperties().getResourceId();
        MetadataDiagnosticsContext metaDataDiagnosticContext = BridgeInternal.getMetaDataDiagnosticContext(cosmosDiagnostics);
        if (metaDataDiagnosticContext != null && !metaDataDiagnosticContext.isEmpty()) {
            return !metaDataDiagnosticContext
                .metadataDiagnosticList
                .stream()
                .filter(metadataDiagnostics -> {
                    if (metadataDiagnostics.metaDataName.equals(MetadataDiagnosticsContext.MetadataType.CONTAINER_LOOK_UP)) {
                        return ((MetadataDiagnosticsContext.ContainerLookupMetadataDiagnostics)metadataDiagnostics).collectionRid.equals(expectedContainerRid);
                    }

                    return false;
                })
                .collect(Collectors.toList())
                .isEmpty();
        }

        return false;
    }

    static class TestObject {
        String id;
        String mypk;
        String prop;

        public TestObject() {
        }

        public TestObject(String id, String mypk, String prop) {
            this.id = id;
            this.mypk = mypk;
            this.prop = prop;
        }

        public static TestObject creatNewTestObject() {
            return new TestObject(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public String getProp() {
            return prop;
        }

        public void setProp(String prop) {
            this.prop = prop;
        }
    }

    private DocumentCollection getDocumentCollectionFromCache(String containerCacheKey) throws Exception {
        RxClientCollectionCache collectionCache =
            ReflectionUtils.getClientCollectionCache((RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client));
        AsyncCache<String, DocumentCollection> collectionInfoByNameCache =
            ReflectionUtils.getCollectionInfoByNameCache(collectionCache);
        ConcurrentHashMap<String, ?> collectionInfoByNameMap = ReflectionUtils.getValueMap(collectionInfoByNameCache);

        Field locationInfoField = LocationCache.class.getDeclaredField("locationInfo");
        locationInfoField.setAccessible(true);
        Object locationInfo = collectionInfoByNameMap.get(containerCacheKey);

        Class<?> AsyncLazyClass = Class.forName("com.azure.cosmos.implementation.caches.AsyncLazy");
        Field valueField = AsyncLazyClass.getDeclaredField("value");
        valueField.setAccessible(true);

        return (DocumentCollection) valueField.get(locationInfo);
    }
}

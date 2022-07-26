// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.FullFidelityChangeFeedProcessorBuilder;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.ServiceItemLease;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FullFidelityChangeFeedProcessorTest extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(FullFidelityChangeFeedProcessorTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosAsyncDatabase createdDatabase;
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private final int FEED_COLLECTION_THROUGHPUT_MAX = 20000;
    private final int FEED_COLLECTION_THROUGHPUT = 10100;
    private final int FEED_COLLECTION_THROUGHPUT_FOR_SPLIT = 400;
    private final int LEASE_COLLECTION_THROUGHPUT = 400;

    private CosmosAsyncClient client;

    private ChangeFeedProcessor changeFeedProcessor;

    @Factory(dataProvider = "clientBuilders")
    public FullFidelityChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    // Using this test to verify basic functionality
    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void fullFidelityChangeFeedProcessorStartFromNow() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
            ChangeFeedProcessor changeFeedProcessor = new FullFidelityChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                                   .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                   .subscribe();
                logger.info("Starting ChangeFeed processor");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                logger.info("Finished starting ChangeFeed processor");

                setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);
                logger.info("Set up read feed documents");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                logger.info("Validating changes now");

                validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }


    // Using this test to verify basic functionality
    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void fullFidelityChangeFeedProcessorStartFromContinuationToken() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
            ChangeFeedProcessor changeFeedProcessor = new FullFidelityChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                                   .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                   .subscribe();
                logger.info("Starting ChangeFeed processor");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                logger.info("Finished starting ChangeFeed processor");

                setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);
                logger.info("Set up read feed documents");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                logger.info("Validating changes now");

                validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getEstimatedLag() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
            ChangeFeedProcessor changeFeedProcessorMain = new FullFidelityChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new FullFidelityChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .options(changeFeedProcessorOptions)
                .handleChanges((List<JsonNode> docs) -> {
                    fail("ERROR - we should not execute this handler");
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessorMain.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessorMain)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessorMain.stop()
                            .subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            Map<String, Integer> estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            assertThat(estimatedLagResult.size()).isNotZero().as("Change Feed Processor number of leases should not be 0.");

            int totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag at start");

            // check the side cart CFP instance
            Map<String, Integer> estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            assertThat(estimatedLagSideCartResult.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag at start");


            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                .map(estimatedLag -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return estimatedLag;
                }).block();

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Side Cart estimated total lag");


        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getEstimatedLagWithInsertedDocuments() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
            ChangeFeedProcessor changeFeedProcessorMain = new FullFidelityChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new FullFidelityChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .options(changeFeedProcessorOptions)
                .handleChanges((List<JsonNode> docs) -> {
                    fail("ERROR - we should not execute this handler");
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessorMain.start().subscribeOn(Schedulers.boundedElastic())
                                       .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                       .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            Map<String, Integer> estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
                                                                             .map(estimatedLag -> {
                                                                                 try {
                                                                                     log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                                                                                 } catch (JsonProcessingException ex) {
                                                                                     log.error("Unexpected", ex);
                                                                                 }
                                                                                 return estimatedLag;
                                                                             }).block();

            assertThat(estimatedLagResult.size()).isNotZero().as("Change Feed Processor number of leases should not be 0.");

            int totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag at start");

            // check the side cart CFP instance
            Map<String, Integer> estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                                                                                         .map(estimatedLag -> {
                                                                                             try {
                                                                                                 log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                                                                                             } catch (JsonProcessingException ex) {
                                                                                                 log.error("Unexpected", ex);
                                                                                             }
                                                                                             return estimatedLag;
                                                                                         }).block();

            assertThat(estimatedLagSideCartResult.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }


            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag at start");


            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            //  Waiting for change feed processor to process documents
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
                                                        .map(estimatedLag -> {
                                                            try {
                                                                log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                                                            } catch (JsonProcessingException ex) {
                                                                log.error("Unexpected", ex);
                                                            }
                                                            return estimatedLag;
                                                        }).block();

            totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                                                                    .map(estimatedLag -> {
                                                                        try {
                                                                            log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                                                                        } catch (JsonProcessingException ex) {
                                                                            log.error("Unexpected", ex);
                                                                        }
                                                                        return estimatedLag;
                                                                    }).block();

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag");

            changeFeedProcessorMain.stop().subscribe();

            //  Waiting for change feed processor to stop
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            estimatedLagResult = changeFeedProcessorMain.getEstimatedLag()
                                                        .map(estimatedLag -> {
                                                            try {
                                                                log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                                                            } catch (JsonProcessingException ex) {
                                                                log.error("Unexpected", ex);
                                                            }
                                                            return estimatedLag;
                                                        }).block();

            totalLag = 0;
            for (int lag : estimatedLagResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            estimatedLagSideCartResult = changeFeedProcessorSideCart.getEstimatedLag()
                                                                    .map(estimatedLag -> {
                                                                        try {
                                                                            log.info(OBJECT_MAPPER.writeValueAsString(estimatedLag));
                                                                        } catch (JsonProcessingException ex) {
                                                                            log.error("Unexpected", ex);
                                                                        }
                                                                        return estimatedLag;
                                                                    }).block();

            totalLag = 0;
            for (int lag : estimatedLagSideCartResult.values()) {
                totalLag += lag;
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Side Cart estimated total lag");

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getCurrentState() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new FullFidelityChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new FullFidelityChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleChanges((List<JsonNode> docs) -> {
                    fail("ERROR - we should not execute this handler");
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessorMain.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .then(Mono.just(changeFeedProcessorMain)
                        .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .flatMap(value -> changeFeedProcessorMain.stop()
                            .subscribeOn(Schedulers.boundedElastic())
                            .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        ))
                    .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            assertThat(cfpCurrentState.size()).isNotZero().as("Change Feed Processor number of leases should not be 0.");

            int totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag at start");

            // check the side cart CFP instance
            List<ChangeFeedProcessorState> cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag at start");


            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                .map(state -> {
                    try {
                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                    } catch (JsonProcessingException ex) {
                        log.error("Unexpected", ex);
                    }
                    return state;
                }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Side Cart estimated total lag");


        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void getCurrentStateWithInsertedDocuments() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new FullFidelityChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new FullFidelityChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleChanges((List<JsonNode> docs) -> {
                    fail("ERROR - we should not execute this handler");
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessorMain.start().subscribeOn(Schedulers.boundedElastic())
                                       .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                       .subscribe();
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "zero" lag
            List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                                                                                    .map(state -> {
                                                                                        try {
                                                                                            log.info(OBJECT_MAPPER.writeValueAsString(state));
                                                                                        } catch (JsonProcessingException ex) {
                                                                                            log.error("Unexpected", ex);
                                                                                        }
                                                                                        return state;
                                                                                    }).block();

            assertThat(cfpCurrentState.size()).isNotZero().as("Change Feed Processor number of leases should not be 0.");

            int totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag at start");

            // check the side cart CFP instance
            List<ChangeFeedProcessorState> cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                                                                                                .map(state -> {
                                                                                                    try {
                                                                                                        log.info(OBJECT_MAPPER.writeValueAsString(state));
                                                                                                    } catch (JsonProcessingException ex) {
                                                                                                        log.error("Unexpected", ex);
                                                                                                    }
                                                                                                    return state;
                                                                                                }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag at start");


            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            //  Waiting for change feed processor to process documents
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                                                     .map(state -> {
                                                         try {
                                                             log.info(OBJECT_MAPPER.writeValueAsString(state));
                                                         } catch (JsonProcessingException ex) {
                                                             log.error("Unexpected", ex);
                                                         }
                                                         return state;
                                                     }).block();

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                                                                 .map(state -> {
                                                                     try {
                                                                         log.info(OBJECT_MAPPER.writeValueAsString(state));
                                                                     } catch (JsonProcessingException ex) {
                                                                         log.error("Unexpected", ex);
                                                                     }
                                                                     return state;
                                                                 }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(0).as("Change Feed Processor Side Cart estimated total lag");

            changeFeedProcessorMain.stop().subscribe();

            //  Waiting for change feed processor to stop
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Test for "FEED_COUNT total lag
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            cfpCurrentState = changeFeedProcessorMain.getCurrentState()
                                                     .map(state -> {
                                                         try {
                                                             log.info(OBJECT_MAPPER.writeValueAsString(state));
                                                         } catch (JsonProcessingException ex) {
                                                             log.error("Unexpected", ex);
                                                         }
                                                         return state;
                                                     }).block();

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentState) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Main estimated total lag");

            // check the side cart CFP instance
            cfpCurrentStateSideCart = changeFeedProcessorSideCart.getCurrentState()
                                                                 .map(state -> {
                                                                     try {
                                                                         log.info(OBJECT_MAPPER.writeValueAsString(state));
                                                                     } catch (JsonProcessingException ex) {
                                                                         log.error("Unexpected", ex);
                                                                     }
                                                                     return state;
                                                                 }).block();

            assertThat(cfpCurrentStateSideCart.size()).isNotZero().as("Change Feed Processor side cart number of leases should not be 0.");

            totalLag = 0;
            for (ChangeFeedProcessorState item : cfpCurrentStateSideCart) {
                totalLag += item.getEstimatedLag();
            }

            assertThat(totalLag).isEqualTo(FEED_COUNT).as("Change Feed Processor Side Cart estimated total lag");


        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void staledLeaseAcquiring() throws InterruptedException {
        final String ownerFirst = "Owner_First";
        final String ownerSecond = "Owner_Second";
        final String leasePrefix = "TEST";
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            ChangeFeedProcessor changeFeedProcessorFirst = new FullFidelityChangeFeedProcessorBuilder()
                .hostName(ownerFirst)
                .handleChanges(docs -> {
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix(leasePrefix)
                )
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSecond = new FullFidelityChangeFeedProcessorBuilder()
                .hostName(ownerSecond)
                .handleChanges((List<JsonNode> docs) -> {
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                    for (JsonNode item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(10))
                    .setLeaseAcquireInterval(Duration.ofSeconds(5))
                    .setLeaseExpirationInterval(Duration.ofSeconds(20))
                    .setFeedPollDelay(Duration.ofSeconds(2))
                    .setLeasePrefix(leasePrefix)
                    .setMaxItemCount(10)
                    .setMaxScaleCount(0) // unlimited
                )
                .buildChangeFeedProcessor();

            changeFeedProcessorFirst
                .start()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .then(Mono.just(changeFeedProcessorFirst)
                          .delayElement(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                          .flatMap(value ->
                                  changeFeedProcessorFirst.stop()
                                                          .subscribeOn(Schedulers.boundedElastic())
                                                          .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                          ))
                .subscribe();

            try {
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted exception", e);
            }
            log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");

            SqlParameter param = new SqlParameter();
            param.setName("@PartitionLeasePrefix");
            param.setValue(leasePrefix);
            SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Collections.singletonList(param));

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            createdLeaseCollection
                .queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                .flatMap(documentFeedResponse -> Flux.fromIterable(documentFeedResponse.getResults()))
                .flatMap(doc -> {
                    ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc);
                    leaseDocument.setOwner("TEMP_OWNER");
                    CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                    return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                                   .map(CosmosItemResponse::getItem);
                })
                .map(leaseDocument -> {
                    log.info("QueryItems after Change feed processor processing; found host {}", leaseDocument.getOwner());
                    return leaseDocument;
                })
                .blockLast();

            changeFeedProcessorSecond
                .start()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                .subscribe();

            // Wait for the feed processor to start.
            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            List<InternalObjectNode> docDefList = new ArrayList<>();
            for (int i = 0; i < FEED_COUNT; i++) {
                docDefList.add(getDocumentDefinition());
            }

            bulkInsert(createdFeedCollection, docDefList, FEED_COUNT).blockLast();

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            long remainingWork = 10 * CHANGE_FEED_PROCESSOR_TIMEOUT;
            while (remainingWork > 0 && changeFeedProcessorFirst.isStarted() && !changeFeedProcessorSecond.isStarted()) {
                remainingWork -= 100;
                Thread.sleep(100);
            }
            assertThat(changeFeedProcessorSecond.isStarted()).as("Change Feed Processor instance is running").isTrue();

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 30 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            changeFeedProcessorSecond.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            // Wait for the feed processor to shut down.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            logger.info("DONE");
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void ownerNullAcquiring() throws InterruptedException {
        final String ownerFirst = "Owner_First";
        final String leasePrefix = "TEST";
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();

            ChangeFeedProcessor changeFeedProcessorFirst = new FullFidelityChangeFeedProcessorBuilder()
                .hostName(ownerFirst)
                .handleChanges(docs -> {
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    for (JsonNode item : docs) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix(leasePrefix)
                    .setLeaseRenewInterval(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setLeaseAcquireInterval(Duration.ofMillis(5 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setLeaseExpirationInterval(Duration.ofMillis(6 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .setFeedPollDelay(Duration.ofSeconds(5))
                )
                .buildChangeFeedProcessor();

            try {
                log.info("Start more creating documents");
                List<InternalObjectNode> docDefList = new ArrayList<>();

                for (int i = 0; i < FEED_COUNT; i++) {
                    docDefList.add(getDocumentDefinition());
                }

                log.info("Start first Change feed processor");
                changeFeedProcessorFirst.start().subscribeOn(Schedulers.boundedElastic())
                                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                        .subscribe();

                // Wait for the feed processor to start.
                Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                    .last()
                    .then(
                        Mono.just(changeFeedProcessorFirst)
                            .flatMap( value -> {
                                log.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");
                                log.info("QueryItems before Change feed processor processing");

                                SqlParameter param1 = new SqlParameter();
                                param1.setName("@PartitionLeasePrefix");
                                param1.setValue(leasePrefix);
                                SqlParameter param2 = new SqlParameter();
                                param2.setName("@Owner");
                                param2.setValue(ownerFirst);

                                SqlQuerySpec querySpec = new SqlQuerySpec(
                                    "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix) AND c.Owner=@Owner", Arrays.asList(param1, param2));

                                CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

                                return createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                                    .flatMap(documentFeedResponse -> Flux.fromIterable(documentFeedResponse.getResults()))
                                    .flatMap(doc -> {
                                        ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc);
                                        leaseDocument.setOwner(null);
                                        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                                        return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                            .map(CosmosItemResponse::getItem);
                                    })
                                    .map(leaseDocument -> {
                                        log.info("QueryItems after Change feed processor processing; current Owner is'{}'", leaseDocument.getOwner());
                                        return leaseDocument;
                                    })
                                    .last()
                                    .flatMap(leaseDocument -> {
                                        log.info("Start creating more documents");
                                        List<InternalObjectNode> docDefList1 = new ArrayList<>();

                                        for (int i = 0; i < FEED_COUNT; i++) {
                                            docDefList1.add(getDocumentDefinition());
                                        }

                                        return bulkInsert(createdFeedCollection, docDefList1, FEED_COUNT)
                                            .last();
                                    });
                            }))
                    .subscribe();
            } catch (Exception ex) {
                log.error("First change feed processor did not start in the expected time", ex);
                throw ex;
            }

            long remainingWork = 20 * CHANGE_FEED_PROCESSOR_TIMEOUT;
            while (remainingWork > 0 && !changeFeedProcessorFirst.isStarted()) {
                remainingWork -= 100;
                Thread.sleep(100);
            }
            assertThat(changeFeedProcessorFirst.isStarted()).as("Change Feed Processor instance is running").isTrue();

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 30 * CHANGE_FEED_PROCESSOR_TIMEOUT, 2 * FEED_COUNT);

            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            changeFeedProcessorFirst.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 20 * TIMEOUT)
    public void inactiveOwnersRecovery() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT_MAX);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, JsonNode> receivedDocuments = new ConcurrentHashMap<>();
            String leasePrefix = "TEST";

            changeFeedProcessor = new FullFidelityChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleChanges(fullFidelityChangeFeedProcessorHandler(receivedDocuments))
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeaseRenewInterval(Duration.ofSeconds(1))
                    .setLeaseAcquireInterval(Duration.ofSeconds(1))
                    .setLeaseExpirationInterval(Duration.ofSeconds(5))
                    .setFeedPollDelay(Duration.ofSeconds(1))
                    .setLeasePrefix(leasePrefix)
                    .setMaxItemCount(100)
                    .setMaxScaleCount(0) // unlimited
                    //.setScheduler(Schedulers.boundedElastic())
                    .setScheduler(Schedulers.newParallel("CFP parallel",
                        10 * Schedulers.DEFAULT_POOL_SIZE,
                        true))
                )
                .buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                                   .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                   .subscribe();

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            } catch (Exception ex) {
                log.error("Change feed processor did not start in the expected time", ex);
                throw ex;
            }

            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments,2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);

            log.info("Update leases with random owners");

            SqlParameter param1 = new SqlParameter();
            param1.setName("@PartitionLeasePrefix");
            param1.setValue(leasePrefix);

            SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE STARTSWITH(c.id, @PartitionLeasePrefix)", Arrays.asList(param1));

            CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

            createdLeaseCollection.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class).byPage()
                .flatMap(documentFeedResponse -> Flux.fromIterable(documentFeedResponse.getResults()))
                .flatMap(doc -> {
                    ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc);
                    leaseDocument.setOwner(RandomStringUtils.randomAlphabetic(10));
                    CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                    return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                        .map(CosmosItemResponse::getItem);
                })
                .flatMap(leaseDocument -> createdLeaseCollection.readItem(leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), InternalObjectNode.class))
                .map(doc -> {
                    ServiceItemLease leaseDocument = ServiceItemLease.fromDocument(doc.getItem());
                    log.info("Change feed processor current Owner is'{}'", leaseDocument.getOwner());
                    return leaseDocument;
                })
                .blockLast();

            createdDocuments.clear();
            receivedDocuments.clear();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    void validateChangeFeedProcessing(ChangeFeedProcessor changeFeedProcessor, List<InternalObjectNode> createdDocuments, Map<String, JsonNode> receivedDocuments, int sleepTime) throws InterruptedException {
        assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

        List<ChangeFeedProcessorState> cfpCurrentState = changeFeedProcessor
            .getCurrentState()
            .map(state -> {
                try {
                    log.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(state));
                } catch (JsonProcessingException ex) {
                    log.error("Unexpected", ex);
                }
                return state;
            })
            .block();

        assertThat(cfpCurrentState).isNotNull().as("Change Feed Processor current state");

        for (ChangeFeedProcessorState item : cfpCurrentState) {
            assertThat(item.getHostName()).isEqualTo(hostName).as("Change Feed Processor ownership");
        }

        // Added this validation for now to verify received list has something - easy way to see size not being 10
        assertThat(receivedDocuments.size()).isEqualTo(FEED_COUNT);

        for (InternalObjectNode item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }
    }

    private Consumer<List<JsonNode>> fullFidelityChangeFeedProcessorHandler(Map<String, JsonNode> receivedDocuments) {
        return docs -> {
            log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                processItem(item, receivedDocuments);
            }
            log.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private void waitToReceiveDocuments(Map<String, JsonNode> receivedDocuments, long timeoutInMillisecond, long count) throws InterruptedException {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            Thread.sleep(200);
        }

        assertThat(remainingWork > 0).as("Failed to receive all the feed documents").isTrue();
    }

    private Consumer<List<JsonNode>> leasesChangeFeedProcessorHandler(LeaseStateMonitor leaseStateMonitor) {
        return docs -> {
            log.info("LEASES processing from thread in test {}", Thread.currentThread().getId());
            for (JsonNode item : docs) {
                try {
                    log
                        .debug("LEASE RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                } catch (JsonProcessingException e) {
                    log.error("Failure in processing json [{}]", e.getMessage(), e);
                }

                JsonNode leaseToken = item.get("LeaseToken");

                if (leaseToken != null) {
                    JsonNode continuationTokenNode = item.get("ContinuationToken");
                    if (continuationTokenNode == null) {
                        // Something catastrophic went wrong and the lease is malformed.
                        log.error("Found invalid lease document");
                        leaseStateMonitor.isContinuationTokenAdvancing = false;
                    }
                    else {
                        log.info("LEASE {} with continuation {}", leaseToken.asText(), continuationTokenNode.asText());
                        if (leaseStateMonitor.isAfterLeaseInitialization) {
                            String value = continuationTokenNode.asText().replaceAll("[^0-9]", "");
                            if (value.isEmpty()) {
                                log.error("Found unexpected continuation token that does not conform to the expected format");
                                leaseStateMonitor.isContinuationTokenAdvancing = false;
                            }
                            long continuationToken = Long.parseLong(value);
                            if (leaseStateMonitor.parentContinuationToken > continuationToken) {
                                log.error("Found unexpected continuation token that did not advance after the split; parent: {}, current: {}");
                                leaseStateMonitor.isContinuationTokenAdvancing = false;
                            }
                        }
                    }
                    leaseStateMonitor.receivedLeases.put(item.get("id").asText(), item);
                }
            }
            log.info("LEASES processing from thread {}", Thread.currentThread().getId());
        };
    }

    @BeforeMethod(groups = { "emulator", "simple" }, timeOut = 2 * SETUP_TIMEOUT, alwaysRun = true)
     public void beforeMethod() {
     }

    @BeforeClass(groups = { "emulator", "simple" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);

        // Following is code that when enabled locally it allows for a predicted database/collection name that can be
        // checked in the Azure Portal
//        try {
//            client.getDatabase(databaseId).read()
//                .map(cosmosDatabaseResponse -> cosmosDatabaseResponse.getDatabase())
//                .flatMap(database -> database.delete())
//                .onErrorResume(throwable -> {
//                    if (throwable instanceof com.azure.cosmos.CosmosClientException) {
//                        com.azure.cosmos.CosmosClientException clientException = (com.azure.cosmos.CosmosClientException) throwable;
//                        if (clientException.getStatusCode() == 404) {
//                            return Mono.empty();
//                        }
//                    }
//                    return Mono.error(throwable);
//                }).block();
//            Thread.sleep(500);
//        } catch (Exception e){
//            log.warn("Database delete", e);
//        }
//        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterMethod(groups = { "emulator", "simple" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterMethod() {
    }

    @AfterClass(groups = { "emulator", "simple" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
//        try {
//            client.readAllDatabases()
//                .flatMap(cosmosDatabaseProperties -> {
//                    CosmosAsyncDatabase cosmosDatabase = client.getDatabase(cosmosDatabaseProperties.getId());
//                    return cosmosDatabase.delete();
//                }).blockLast();
//            Thread.sleep(500);
//        } catch (Exception e){ }

        safeClose(client);
    }

    private void setupReadFeedDocuments(List<InternalObjectNode> createdDocuments, Map<String, JsonNode> receivedDocuments, CosmosAsyncContainer feedCollection, long count) {
        List<InternalObjectNode> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            InternalObjectNode item = getDocumentDefinition();
            docDefList.add(item);
            logger.info("Adding the following item to bulk list: {}", item);
        }

        createdDocuments.addAll(bulkInsertBlocking(feedCollection, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private void createReadFeedDocuments(List<InternalObjectNode> createdDocuments, CosmosAsyncContainer feedCollection, long count) {
        List<InternalObjectNode> docDefList = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            docDefList.add(getDocumentDefinition());
        }

        createdDocuments.addAll(bulkInsertBlocking(feedCollection, docDefList));
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
    }

    private InternalObjectNode getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        InternalObjectNode doc = new InternalObjectNode(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , uuid, uuid));
        return doc;
    }

    private CosmosAsyncContainer createFeedCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions optionsFeedCollection = new CosmosContainerRequestOptions();
        return createCollection(createdDatabase, getCollectionDefinitionWithFullFidelity(), optionsFeedCollection, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "leases_" + UUID.randomUUID(),
            "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseMonitorCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "monitor_" + UUID.randomUUID(),
            "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private static synchronized void processItem(JsonNode item, Map<String, JsonNode> receivedDocuments) {
        log.info("RECEIVED {}", item.toPrettyString());
        receivedDocuments.put(item.get("current").get("id").asText(), item);
    }

    class LeaseStateMonitor {
        public Map<String, JsonNode> receivedLeases = new ConcurrentHashMap<>();
        public volatile boolean isAfterLeaseInitialization = false;
        public volatile boolean isAfterSplits = false;
        public volatile long parentContinuationToken = 0;
        public volatile boolean isContinuationTokenAdvancing = true;
    }
}

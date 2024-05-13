// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.changefeed.epkversion;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.ChangeFeedProcessorContext;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
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
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.cosmos.BridgeInternal.extractContainerSelfLink;
import static com.azure.cosmos.CosmosBridgeInternal.getContextClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.testng.Assert.assertThrows;

public class FullFidelityChangeFeedProcessorTest extends TestSuiteBase {
    private final static Logger log = LoggerFactory.getLogger(FullFidelityChangeFeedProcessorTest.class);
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosAsyncDatabase createdDatabase;
    private final String hostName = RandomStringUtils.randomAlphabetic(6);
    private final int FEED_COUNT = 10;
    private final int CHANGE_FEED_PROCESSOR_TIMEOUT = 5000;
    private final int FEED_COLLECTION_THROUGHPUT = 400;
    private final int FEED_COLLECTION_THROUGHPUT_FOR_SPLIT = 10100;
    private final int LEASE_COLLECTION_THROUGHPUT = 400;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public FullFidelityChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider
    public Object[] contextTestConfigs() {
        return new Object[] {true, false};
    }

    @DataProvider
    public Object[] incrementalChangeFeedModeStartFromSetting() {
        return new Object[] {true, false};
    }

    // Using this test to verify basic functionality
    @Test(groups = { "emulator" }, dataProvider = "contextTestConfigs", timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void fullFidelityChangeFeedProcessorStartFromNow(boolean isContextRequired) throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            Set<String> receivedLeaseTokensFromContext = ConcurrentHashMap.newKeySet();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection);

            if (isContextRequired) {
                changeFeedProcessorBuilder = changeFeedProcessorBuilder
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandlerWithContext(receivedDocuments, receivedLeaseTokensFromContext));
            } else {
                changeFeedProcessorBuilder = changeFeedProcessorBuilder
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandler(receivedDocuments));
            }

            ChangeFeedProcessor changeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

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

                validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                // query for leases from the createdLeaseCollection
                String leaseQuery = "select * from c where not contains(c.id, \"info\")";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                List<String> leaseTokensCollectedFromLeaseCollection =
                    leaseDocuments.stream().map(lease -> lease.get("LeaseToken").asText()).collect(Collectors.toList());

                if (isContextRequired) {
                    assertThat(leaseTokensCollectedFromLeaseCollection).isNotNull();
                    assertThat(receivedLeaseTokensFromContext.size()).isEqualTo(leaseTokensCollectedFromLeaseCollection.size());

                    assertThat(receivedLeaseTokensFromContext.containsAll(leaseTokensCollectedFromLeaseCollection)).isTrue();
                }

                // Wait for the feed processor to shut down.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

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

    // Steps followed in this test
    //  1. Start the AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor with startFromBeginning set to 'false'.
    //  2  Ingest 10 documents into the feed container.
    //  3. Stop the AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor once the 10 documents are processed.
    //  4. Start the LatestVersion / INCREMENTAL ChangeFeedProcessor.
    //  5. Validate IllegalStateException
    //        a) If the AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor left behind a lease with a valid continuation,
    //           then LatestVersion / INCREMENTAL ChangeFeedProcessor should throw the exception.
    //         b) If not, then LatestVersion / INCREMENTAL ChangeFeedProcessor should be able to reuse the lease
    //            left behind and use its own continuation.
    @Test(groups = { "emulator" }, dataProvider = "incrementalChangeFeedModeStartFromSetting")
    public void fullFidelityChangeFeedModeToIncrementalChangeFeedMode(boolean isStartFromBeginning) throws InterruptedException, JsonProcessingException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();

            Map<String, ChangeFeedProcessorItem> receivedDocumentsByFullFidelityCfp = new ConcurrentHashMap<>();
            Map<String, ChangeFeedProcessorItem> receivedDocumentsByIncrementalCfp = new ConcurrentHashMap<>();

            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection);

            changeFeedProcessorBuilder = changeFeedProcessorBuilder
                .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandler(receivedDocumentsByFullFidelityCfp));

            ChangeFeedProcessor fullFidelityChangeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

            try {
                fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .doOnSuccess(ignore -> logger.info("Starting FULL_FIDELITY ChangeFeedProcessor"))
                    .block();

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                setupReadFeedDocuments(createdDocuments, receivedDocumentsByFullFidelityCfp, createdFeedCollection, FEED_COUNT);
                logger.info("Set up read feed documents");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                logger.info("Validating changes now");

                validateChangeFeedProcessing(fullFidelityChangeFeedProcessor, createdDocuments, receivedDocumentsByFullFidelityCfp, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                fullFidelityChangeFeedProcessor
                    .stop()
                    .subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .block();

                // query for documents in the lease container from the createdLeaseCollection
                String leaseQuery = "select * from c";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                logger.info("Lease container documents created by FULL_FIDELITY ChangeFeedProcessor fetched...");
                for (JsonNode lease : leaseDocuments) {

                    JsonNode continuationToken = lease.get("ContinuationToken");

                    if (continuationToken != null && !(continuationToken instanceof NullNode)) {
                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }

                ChangeFeedProcessorBuilder incrementalChangeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                    .options(new ChangeFeedProcessorOptions().setStartFromBeginning(isStartFromBeginning).setMaxItemCount(1))
                    .hostName(hostName)
                    .feedContainer(createdFeedCollection)
                    .leaseContainer(createdLeaseCollection)
                    .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocumentsByIncrementalCfp));

                ChangeFeedProcessor incrementalChangeFeedProcessor = incrementalChangeFeedProcessorBuilder
                    .buildChangeFeedProcessor();

                assertThrows(IllegalStateException.class, () -> incrementalChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .doOnSuccess(ignore -> logger.info("Starting INCREMENTAL ChangeFeedProcessor"))
                    .block());

                leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                logger.info("Lease container documents created by INCREMENTAL ChangeFeedProcessor fetched...");
                for (JsonNode lease : leaseDocuments) {

                    if (lease.get("ContinuationToken") != null) {
                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }
            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(5000);
        }
    }

    // Steps followed in this test
    //  1. Ingest 10 documents into the feed container.
    //  2. Start the LatestVersion / INCREMENTAL ChangeFeedProcessor with either startFromBeginning set to 'true' or 'false'.
    //  3. Stop the LatestVersion / INCREMENTAL ChangeFeedProcessor once the 10 documents are processed or 0 documents are processed (stop after some delay).
    //  4. Start the AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor.
    //  5. Validate IllegalStateException
    //        a) If the LatestVersion / INCREMENTAL ChangeFeedProcessor left behind a lease with a valid continuation,
    //           then AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor should throw the exception.
    //         b) If not, then AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor should be able to reuse the lease
    //            left behind and use its own continuation.
    @Test(groups = { "emulator" }, dataProvider = "incrementalChangeFeedModeStartFromSetting")
    public void incrementalChangeFeedModeToFullFidelityChangeFeedMode(boolean isStartFromBeginning) throws InterruptedException, JsonProcessingException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocumentsByIncrementalCfp = new ConcurrentHashMap<>();
            Map<String, ChangeFeedProcessorItem> receivedDocumentsByFullFidelityCfp = new ConcurrentHashMap<>();

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(new ChangeFeedProcessorOptions().setStartFromBeginning(isStartFromBeginning).setMaxItemCount(1))
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection);

            changeFeedProcessorBuilder = changeFeedProcessorBuilder
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocumentsByIncrementalCfp));

            ChangeFeedProcessor incrementalChangeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

            logger.info("Set up read feed documents");
            setupReadFeedDocuments(createdDocuments, receivedDocumentsByIncrementalCfp, createdFeedCollection, FEED_COUNT);

            try {
                incrementalChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .doOnSuccess(ignore -> logger.info("Started INCREMENTAL ChangeFeedProcessor"))
                    .block();

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                logger.info("Validating changes now");

                if (isStartFromBeginning) {
                    validateChangeFeedProcessing(incrementalChangeFeedProcessor, createdDocuments, receivedDocumentsByIncrementalCfp, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                } else {
                    validateChangeFeedProcessing(incrementalChangeFeedProcessor, new ArrayList<>(), receivedDocumentsByIncrementalCfp, 0, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                }

                incrementalChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).block();

                String leaseQuery = "select * from c";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                int leaseDocumentsWithNonNullContinuationToken = 0;

                logger.info("Leases container documents created by INCREMENTAL ChangeFeedProcessor fetched...");
                for (JsonNode lease : leaseDocuments) {

                    JsonNode continuationToken = lease.get("ContinuationToken");

                    if (continuationToken != null && !(continuationToken instanceof NullNode)) {
                        leaseDocumentsWithNonNullContinuationToken++;
                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }

                ChangeFeedProcessorBuilder fullFidelityChangeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                    .options(new ChangeFeedProcessorOptions().setMaxItemCount(1))
                    .hostName(hostName)
                    .feedContainer(createdFeedCollection)
                    .leaseContainer(createdLeaseCollection)
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandler(receivedDocumentsByFullFidelityCfp));

                ChangeFeedProcessor fullFidelityChangeFeedProcessor = fullFidelityChangeFeedProcessorBuilder
                    .buildChangeFeedProcessor();

                if (leaseDocumentsWithNonNullContinuationToken > 0) {
                    assertThrows(IllegalStateException.class, () -> fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .block());
                } else {

                    fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .doOnSuccess(ignore -> logger.info("Started FULL_FIDELITY ChangeFeedProcessor successfully!"))
                        .block();

                    createdDocuments = new ArrayList<>();

                    // wait for FULL_FIDELITY ChangeFeedProcessor to start and acquire left behind lease
                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                    setupReadFeedDocuments(createdDocuments, receivedDocumentsByFullFidelityCfp, createdFeedCollection, 10);

                    // wait for FULL_FIDELITY ChangeFeedProcessor to process all ingested documents
                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                    fullFidelityChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .block();

                    validateChangeFeedProcessing(createdDocuments, receivedDocumentsByFullFidelityCfp, FEED_COUNT);
                }

                leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                logger.info("Leases container documents fetched after starting FULL_FIDELITY ChangeFeedProcessor...");
                for (JsonNode lease : leaseDocuments) {

                    if (lease.get("ContinuationToken") != null) {
                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }
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

    // Steps followed in this test
    //  1. Ingest 10 documents into the feed container.
    //  2. Start the LatestVersion / INCREMENTAL ChangeFeedProcessor with either startFromBeginning set to 'true' or 'false'.
    //  3. Stop the LatestVersion / INCREMENTAL ChangeFeedProcessor once 5 documents are processed (startFromBeginning set to 'true')
    //  or 0 otherwise (stop after some delay).
    //  4. Start the AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor.
    //  5. Validate IllegalStateException
    //        a) If the LatestVersion / INCREMENTAL ChangeFeedProcessor left behind a lease with a valid continuation,
    //           then AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor should throw the exception.
    //         b) If not, then AllVersionsAndDeletes / FULL_FIDELITY ChangeFeedProcessor should be able to reuse the lease
    //            left behind and use its own continuation.
    @Test(groups = { "emulator" }, dataProvider = "incrementalChangeFeedModeStartFromSetting")
    public void incrementalChangeFeedModeToFullFidelityChangeFeedModeWithProcessingStoppage(boolean isStartFromBeginning) throws InterruptedException, JsonProcessingException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocumentsByIncrementalCfp = new ConcurrentHashMap<>();
            Map<String, ChangeFeedProcessorItem> receivedDocumentsByFullFidelityCfp = new ConcurrentHashMap<>();

            AtomicReference<ChangeFeedProcessor> changeFeedProcessorInAtomicRefHolder = new AtomicReference<>();
            int docLimit = FEED_COUNT / 2;

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(new ChangeFeedProcessorOptions().setStartFromBeginning(isStartFromBeginning).setMaxItemCount(1))
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection);

            changeFeedProcessorBuilder = changeFeedProcessorBuilder
                .handleLatestVersionChanges(changeFeedProcessorHandler(receivedDocumentsByIncrementalCfp, docLimit, changeFeedProcessorInAtomicRefHolder));

            ChangeFeedProcessor incrementalChangeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

            changeFeedProcessorInAtomicRefHolder.set(incrementalChangeFeedProcessor);

            logger.info("Set up read feed documents");
            setupReadFeedDocuments(createdDocuments, receivedDocumentsByIncrementalCfp, createdFeedCollection, FEED_COUNT);

            try {
                incrementalChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .doOnSuccess(ignore -> logger.info("Starting INCREMENTAL ChangeFeedProcessor"))
                    .block();

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                logger.info("Validating changes now");

                if (isStartFromBeginning) {
                    validateChangeFeedProcessing(createdDocuments, receivedDocumentsByIncrementalCfp, docLimit);
                } else {
                    validateChangeFeedProcessing(createdDocuments, receivedDocumentsByIncrementalCfp, 0);
                }

                // query for leases from the createdLeaseCollection
                // String leaseQuery = "select * from c where not contains(c.id, \"info\")";

                String leaseQuery = "select * from c";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                int leaseDocumentsWithNonNullContinuationToken = 0;

                logger.info("Lease container documents created by INCREMENTAL ChangeFeedProcessor fetched...");
                for (JsonNode lease : leaseDocuments) {

                    JsonNode continuationToken = lease.get("ContinuationToken");

                    if (continuationToken != null && !(continuationToken instanceof NullNode)) {
                        leaseDocumentsWithNonNullContinuationToken++;
                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }

                ChangeFeedProcessorBuilder fullFidelityChangeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                    .options(new ChangeFeedProcessorOptions().setMaxItemCount(1))
                    .hostName(hostName)
                    .feedContainer(createdFeedCollection)
                    .leaseContainer(createdLeaseCollection)
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandler(receivedDocumentsByFullFidelityCfp));

                ChangeFeedProcessor fullFidelityChangeFeedProcessor = fullFidelityChangeFeedProcessorBuilder
                    .buildChangeFeedProcessor();

                if (leaseDocumentsWithNonNullContinuationToken > 0) {
                    assertThrows(IllegalStateException.class, () -> fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .doOnSuccess(ignore -> logger.info("Started FULL_FIDELITY ChangeFeedProcessor"))
                        .block());

                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                    validateChangeFeedProcessing(createdDocuments, receivedDocumentsByFullFidelityCfp, 0);
                } else {
                    fullFidelityChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                        .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                        .doOnSuccess(ignore -> logger.info("Started FULL_FIDELITY ChangeFeedProcessor"))
                        .block();
                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                    logger.info("Set up read feed documents");
                    setupReadFeedDocuments(createdDocuments, receivedDocumentsByFullFidelityCfp, createdFeedCollection, FEED_COUNT);

                    Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                    validateChangeFeedProcessing(createdDocuments, receivedDocumentsByFullFidelityCfp, FEED_COUNT);
                }


                leaseDocuments = createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                logger.info("Lease container documents created by FULL_FIDELITY ChangeFeedProcessor fetched...");
                for (JsonNode lease : leaseDocuments) {

                    if (lease.get("ContinuationToken") != null) {
                        logger.info("Lease document : {}",  OBJECT_MAPPER.writeValueAsString(lease));
                        logger.info("ContinuationToken : {}", new String(Base64.getUrlDecoder().decode(lease.get("ContinuationToken").asText())));
                    } else {
                        logger.info("Lease container document : {}", OBJECT_MAPPER.writeValueAsString(lease));
                    }
                }

                fullFidelityChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .block();

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
    @Test(groups = { "emulator" }, dataProvider = "contextTestConfigs", timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void fullFidelityChangeFeedProcessorStartFromContinuationToken(boolean isContextRequired) throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            Set<String> receivedLeaseTokensFromContext = ConcurrentHashMap.newKeySet();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection);

            if (isContextRequired) {
                changeFeedProcessorBuilder = changeFeedProcessorBuilder.handleAllVersionsAndDeletesChanges(
                    changeFeedProcessorHandlerWithContext(receivedDocuments, receivedLeaseTokensFromContext));
            } else {
                changeFeedProcessorBuilder = changeFeedProcessorBuilder.handleAllVersionsAndDeletesChanges(
                    changeFeedProcessorHandler(receivedDocuments));
            }

            ChangeFeedProcessor changeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

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

                validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                // query for leases from the createdLeaseCollection
                String leaseQuery = "select * from c where not contains(c.id, \"info\")";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                List<String> leaseTokensCollectedFromLeaseCollection =
                    leaseDocuments.stream().map(lease -> lease.get("LeaseToken").asText()).collect(Collectors.toList());

                if (isContextRequired) {
                    assertThat(leaseTokensCollectedFromLeaseCollection).isNotNull();
                    assertThat(receivedLeaseTokensFromContext.size()).isEqualTo(leaseTokensCollectedFromLeaseCollection.size());

                    assertThat(receivedLeaseTokensFromContext.containsAll(leaseTokensCollectedFromLeaseCollection)).isTrue();

                }

                // Wait for the feed processor to shut down.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

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

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT, enabled = false)
    public void getCurrentState() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (ChangeFeedProcessorItem item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new ChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> docs) -> {
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

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT, enabled = false)
    public void getCurrentStateWithInsertedDocuments() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessor changeFeedProcessorMain = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (ChangeFeedProcessorItem item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSideCart = new ChangeFeedProcessorBuilder()
                .hostName("side-cart")
                .handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> docs) -> {
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
                                                             log.info("Current state of main after inserting documents is : {}",
                                                                 OBJECT_MAPPER.writeValueAsString(state));
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
                                                                         log.info("Current state of side cart after inserting documents is : {}",
                                                                             OBJECT_MAPPER.writeValueAsString(state));
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
                                                             log.info("Current state of main after stopping is : {}",
                                                                 OBJECT_MAPPER.writeValueAsString(state));
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
                                                                         log.info("Current state of side cart after stopping is : {}",
                                                                             OBJECT_MAPPER.writeValueAsString(state));
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

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT, enabled = false)
    public void staledLeaseAcquiring() throws InterruptedException {
        final String ownerFirst = "Owner_First";
        final String ownerSecond = "Owner_Second";
        final String leasePrefix = "TEST";
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();

            ChangeFeedProcessor changeFeedProcessorFirst = new ChangeFeedProcessorBuilder()
                .hostName(ownerFirst)
                .handleAllVersionsAndDeletesChanges(docs -> {
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    log.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                })
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix(leasePrefix)
                )
                .buildChangeFeedProcessor();

            ChangeFeedProcessor changeFeedProcessorSecond = new ChangeFeedProcessorBuilder()
                .hostName(ownerSecond)
                .handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> docs) -> {
                    log.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerSecond);
                    for (ChangeFeedProcessorItem item : docs) {
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
                // Wait for the feed processor to shut down.
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
                    ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc);
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

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT, enabled = false)
    public void ownerNullAcquiring() throws InterruptedException {
        final String ownerFirst = "Owner_First";
        final String leasePrefix = "TEST";
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();

            ChangeFeedProcessor changeFeedProcessorFirst = new ChangeFeedProcessorBuilder()
                .hostName(ownerFirst)
                .handleAllVersionsAndDeletesChanges(docs -> {
                    logger.info("START processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
                    for (ChangeFeedProcessorItem item : docs) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                        processItem(item, receivedDocuments);
                    }
                    logger.info("END processing from thread {} using host {}", Thread.currentThread().getId(), ownerFirst);
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
                logger.info("Start more creating documents");
                List<InternalObjectNode> docDefList = new ArrayList<>();

                for (int i = 0; i < FEED_COUNT; i++) {
                    docDefList.add(getDocumentDefinition());
                }

                bulkInsert(createdFeedCollection, docDefList, FEED_COUNT)
                    .last()
                    .flatMap(cosmosItemResponse -> {
                        logger.info("Start first Change feed processor");
                        return changeFeedProcessorFirst.start().subscribeOn(Schedulers.boundedElastic())
                                                       .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT));
                    })
                    .then(
                        Mono.just(changeFeedProcessorFirst)
                            .flatMap( value -> {
                                logger.info("Update leases for Change feed processor in thread {} using host {}", Thread.currentThread().getId(), "Owner_first");
                                try {
                                    Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
                                } catch (InterruptedException ignored) {
                                }

                                logger.info("QueryItems before Change feed processor processing");

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
                                                             .flatMap(documentFeedResponse -> reactor.core.publisher.Flux.fromIterable(documentFeedResponse.getResults()))
                                                             .flatMap(doc -> {
                                                                 ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc);
                                                                 leaseDocument.setOwner(null);
                                                                 CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                                                                 return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                                                                              .map(CosmosItemResponse::getItem);
                                                             })
                                                             .map(leaseDocument -> {
                                                                 logger.info("QueryItems after Change feed processor processing; current Owner is'{}'", leaseDocument.getOwner());
                                                                 return leaseDocument;
                                                             })
                                                             .last()
                                                             .flatMap(leaseDocument -> {
                                                                 logger.info("Start creating more documents");
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

            // Wait for the feed processor to receive and process the documents.
            waitToReceiveDocuments(receivedDocuments, 30 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT);

            assertThat(changeFeedProcessorFirst.isStarted()).as("Change Feed Processor instance is running").isTrue();

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

    @Test(groups = { "emulator" }, timeOut = 20 * TIMEOUT, enabled = false)
    public void inactiveOwnersRecovery() throws InterruptedException {
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            String leasePrefix = "TEST";

            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleAllVersionsAndDeletesChanges(fullFidelityChangeFeedProcessorHandler(receivedDocuments))
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

            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, FEED_COUNT, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

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
                                      ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc);
                                      leaseDocument.setOwner(RandomStringUtils.randomAlphabetic(10));
                                      CosmosItemRequestOptions options = new CosmosItemRequestOptions();
                                      return createdLeaseCollection.replaceItem(leaseDocument, leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), options)
                                                                   .map(CosmosItemResponse::getItem);
                                  })
                                  .flatMap(leaseDocument -> createdLeaseCollection.readItem(leaseDocument.getId(), new PartitionKey(leaseDocument.getId()), InternalObjectNode.class))
                                  .map(doc -> {
                                      ServiceItemLeaseV1 leaseDocument = ServiceItemLeaseV1.fromDocument(doc.getItem());
                                      log.info("Change feed processor current Owner is'{}'", leaseDocument.getOwner());
                                      return leaseDocument;
                                  })
                                  .blockLast();

            createdDocuments.clear();
            receivedDocuments.clear();
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, FEED_COUNT);

            // Wait for the feed processor to receive and process the documents.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // Wait for the feed processor to shutdown.
            Thread.sleep(CHANGE_FEED_PROCESSOR_TIMEOUT);
        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void endToEndTimeoutConfigShouldBeSuppressed() throws InterruptedException {
        CosmosAsyncClient clientWithE2ETimeoutConfig = null;
        CosmosAsyncContainer createdFeedCollection = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(LEASE_COLLECTION_THROUGHPUT);

        try {
            clientWithE2ETimeoutConfig = this.getClientBuilder()
                .endToEndOperationLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofMillis(1)).build())
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            CosmosAsyncDatabase testDatabase = clientWithE2ETimeoutConfig.getDatabase(this.createdDatabase.getId());
            CosmosAsyncContainer createdFeedCollectionDuplicate = testDatabase.getContainer(createdFeedCollection.getId());
            CosmosAsyncContainer createdLeaseCollectionDuplicate = testDatabase.getContainer(createdLeaseCollection.getId());

            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
            ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> docs) -> {
                    log.info("START processing from thread {}", Thread.currentThread().getId());
                    for (ChangeFeedProcessorItem item : docs) {
                        processItem(item, receivedDocuments);
                    }
                    log.info("END processing from thread {}", Thread.currentThread().getId());
                })
                .feedContainer(createdFeedCollectionDuplicate)
                .leaseContainer(createdLeaseCollectionDuplicate)
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

                validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                // Wait for the feed processor to shut down.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            // reset the endToEnd config
            this.getClientBuilder().endToEndOperationLatencyPolicyConfig(null);
            safeClose(clientWithE2ETimeoutConfig);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    // todo: this test was run against an FFCF account, worth re-enabling
    //  when we have such an account for the live tests pipeline
    @Test(groups = { "cfp-split" }, dataProvider = "contextTestConfigs", timeOut = 160 * CHANGE_FEED_PROCESSOR_TIMEOUT, enabled = false)
    public void readFeedDocumentsAfterSplit(boolean isContextRequired) throws InterruptedException {
        CosmosAsyncContainer createdFeedCollectionForSplit = createFeedCollection(FEED_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(2 * LEASE_COLLECTION_THROUGHPUT);
        CosmosAsyncContainer createdLeaseMonitorCollection = createLeaseMonitorCollection(LEASE_COLLECTION_THROUGHPUT);

        CosmosAsyncClient clientWithStaleCache = null;

        try {

            clientWithStaleCache = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();

            CosmosAsyncDatabase databaseFromStaleClient =
                clientWithStaleCache.getDatabase(createdFeedCollectionForSplit.getDatabase().getId());
            CosmosAsyncContainer feedCollectionFromStaleClient =
                databaseFromStaleClient.getContainer(createdFeedCollectionForSplit.getId());
            CosmosAsyncContainer leaseCollectionFromStaleClient =
                databaseFromStaleClient.getContainer(createdLeaseCollection.getId());

            ChangeFeedProcessor staleChangeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .feedContainer(feedCollectionFromStaleClient)
                .leaseContainer(leaseCollectionFromStaleClient)
                .handleAllVersionsAndDeletesChanges(changeFeedProcessorItems -> {})
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(false))
                .buildChangeFeedProcessor();

            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            Set<String> receivedLeaseTokensFromContext = ConcurrentHashMap.newKeySet();
            Set<String> queriedLeaseTokensFromLeaseCollection = ConcurrentHashMap.newKeySet();

            LeaseStateMonitor leaseStateMonitor = new LeaseStateMonitor();

            // create a monitoring CFP for ensuring the leases are updating as expected
            ChangeFeedProcessor leaseMonitoringChangeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .handleLatestVersionChanges(leasesChangeFeedProcessorHandler(leaseStateMonitor))
                .feedContainer(createdLeaseCollection)
                .leaseContainer(createdLeaseMonitorCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("MONITOR")
                    .setStartFromBeginning(true)
                    .setMaxItemCount(10)
                    .setLeaseRenewInterval(Duration.ofSeconds(2))
                ).buildChangeFeedProcessor();

            ChangeFeedProcessorBuilder changeFeedProcessorBuilderForFeedMonitoring = new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .feedContainer(createdFeedCollectionForSplit)
                .leaseContainer(createdLeaseCollection)
                .options(new ChangeFeedProcessorOptions()
                    .setLeasePrefix("TEST")
                    .setStartFromBeginning(false));

            if (isContextRequired) {
                changeFeedProcessorBuilderForFeedMonitoring = changeFeedProcessorBuilderForFeedMonitoring
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandlerWithContext(receivedDocuments, receivedLeaseTokensFromContext));
            } else {
                changeFeedProcessorBuilderForFeedMonitoring = changeFeedProcessorBuilderForFeedMonitoring
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandler(receivedDocuments));
            }

            ChangeFeedProcessor changeFeedProcessor = changeFeedProcessorBuilderForFeedMonitoring.buildChangeFeedProcessor();

            leaseMonitoringChangeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                                              .timeout(Duration.ofMillis(200 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                              .onErrorResume(throwable -> {
                                                  logger.error("Change feed processor for lease monitoring did not start in the expected time", throwable);
                                                  return Mono.error(throwable);
                                              })
                                              .then(
                                                  changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                                                                     .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                                                                     .onErrorResume(throwable -> {
                                                                         logger.error("Change feed processor did not start in the expected time", throwable);
                                                                         return Mono.error(throwable);
                                                                     }))
                                              .subscribe();

            // allow time for both the lease monitoring CFP and
            // feed monitoring CFP to start
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // generate a first batch of documents on the feed collection to be split
            setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            // this call populates the pkRangeCache being the first data-plane request
            // this will force using a stale pkRangeCache
            // in the getCurrentState call after the split
            staleChangeFeedProcessor.getCurrentState().block();

            // Wait for the feed processor to receive and process the first batch of documents and apply throughput change.
            Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);
            validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, FEED_COUNT, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            // query for leases from the createdLeaseCollection
            String leaseQuery = "select * from c where not contains(c.id, \"info\")";
            List<JsonNode> leaseDocuments =
                createdLeaseCollection
                    .queryItems(leaseQuery, JsonNode.class)
                    .byPage()
                    .blockFirst()
                    .getResults();

            // collect lease documents before the monitored collection undergoes a split
            queriedLeaseTokensFromLeaseCollection
                .addAll(leaseDocuments.stream().map(lease -> lease.get("LeaseToken").asText()).collect(Collectors.toList()));

            createdFeedCollectionForSplit
                .readThroughput().subscribeOn(Schedulers.boundedElastic())
                .flatMap(currentThroughput ->
                    createdFeedCollectionForSplit
                        .replaceThroughput(ThroughputProperties.createManualThroughput(FEED_COLLECTION_THROUGHPUT_FOR_SPLIT))
                        .subscribeOn(Schedulers.boundedElastic())
                ).subscribe();

            // Retrieve the latest continuation token value.
            long continuationToken = Long.MAX_VALUE;
            for (JsonNode item : leaseStateMonitor.receivedLeases.values()) {
                JsonNode tempToken = item.get("ContinuationToken");

                long continuationTokenValue = 0;
                if (tempToken != null && StringUtils.isNotEmpty(tempToken.asText())) {
                    ChangeFeedState changeFeedState = ChangeFeedState.fromString(tempToken.asText());
                    continuationTokenValue =
                        Long.parseLong(changeFeedState.getContinuation().getCurrentContinuationToken().getToken().replace("\"", ""));
                }
                if (tempToken == null || continuationTokenValue == 0) {
                    logger.error("Found unexpected lease with continuation token value of null or 0");
                    try {
                        logger.info("ERROR LEASE FOUND {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                    } catch (JsonProcessingException e) {
                        logger.error("Failure in processing json [{}]", e.getMessage(), e);
                    }
                    leaseStateMonitor.isContinuationTokenAdvancing = false;
                }
                else {
                    // keep the lowest continuation token value
                    if (continuationToken > continuationTokenValue) {
                        continuationToken = continuationTokenValue;
                    }
                }
            }
            if (continuationToken == Long.MAX_VALUE) {
                // something went wrong; we could not find any valid leases.
                logger.error("Could not find any valid lease documents");
                leaseStateMonitor.isContinuationTokenAdvancing = false;
            }
            else {
                leaseStateMonitor.parentContinuationToken = continuationToken;
            }
            leaseStateMonitor.isAfterLeaseInitialization = true;

            // Loop through reading the current partition count until we get a split
            //   This can take up to two minute or more.
            String partitionKeyRangesPath = extractContainerSelfLink(createdFeedCollectionForSplit);

            AsyncDocumentClient contextClient = getContextClient(createdDatabase);
            Flux.just(1).subscribeOn(Schedulers.boundedElastic())
                .flatMap(value -> {
                    logger.warn("Reading current throughput change.");
                    return contextClient.readPartitionKeyRanges(partitionKeyRangesPath, (CosmosQueryRequestOptions) null);
                })
                .map(partitionKeyRangeFeedResponse -> {
                    int count = partitionKeyRangeFeedResponse.getResults().size();

                    if (count < 2) {
                        logger.warn("Throughput change is pending.");
                        throw new RuntimeException("Throughput change is not done.");
                    }
                    return count;
                })
                // this will timeout approximately after 30 minutes
                .retryWhen(Retry.max(40).filter(throwable -> {
                    try {
                        logger.warn("Retrying...");
                        // Splits are taking longer, so increasing sleep time between retries
                        Thread.sleep(10 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Interrupted exception", e);
                    }
                    return true;
                }))
                .last()
                .doOnSuccess(partitionCount -> {
                    leaseStateMonitor.isAfterSplits = true;
                })
                .block();

            assertThat(changeFeedProcessor.isStarted()).as("Change Feed Processor instance is running").isTrue();

            // generate the second batch of documents
            createReadFeedDocuments(createdDocuments, createdFeedCollectionForSplit, FEED_COUNT);

            // Wait for the feed processor to receive and process the second batch of documents.
            waitToReceiveDocuments(receivedDocuments, 2 * CHANGE_FEED_PROCESSOR_TIMEOUT, FEED_COUNT * 2);

            changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();
            leaseMonitoringChangeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

            int leaseCount = changeFeedProcessor.getCurrentState().map(List::size).block();
            assertThat(leaseCount > 1).as("Found %d leases", leaseCount).isTrue();

            int leaseCountFromStaleCfp = staleChangeFeedProcessor.getCurrentState().map(List::size).block();
            assertThat(leaseCountFromStaleCfp).isEqualTo(leaseCount);

            assertThat(receivedDocuments.size()).isEqualTo(createdDocuments.size());
            for (InternalObjectNode item : createdDocuments) {
                assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
            }

            // check the continuation tokens have advanced after splits
            assertThat(leaseStateMonitor.isContinuationTokenAdvancing && leaseStateMonitor.parentContinuationToken > 0)
                .as("Continuation tokens for the leases after split should advance from parent value; parent: %d", leaseStateMonitor.parentContinuationToken).isTrue();

            // query for leases from the createdLeaseCollection after monitored collection undergoes a split
            leaseDocuments = createdLeaseCollection
                    .queryItems(leaseQuery, JsonNode.class)
                    .byPage()
                    .blockFirst()
                    .getResults();

            queriedLeaseTokensFromLeaseCollection.addAll(
                leaseDocuments.stream().map(lease -> lease.get("LeaseToken").asText()).collect(Collectors.toList()));

            if (isContextRequired) {
                assertThat(receivedLeaseTokensFromContext.size())
                    .isEqualTo(queriedLeaseTokensFromLeaseCollection.size());

                assertThat(receivedLeaseTokensFromContext.containsAll(queriedLeaseTokensFromLeaseCollection)).isTrue();
            }

            // Wait for the feed processor to shutdown.
            Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

        } finally {
            System.out.println("Start to delete FeedCollectionForSplit");
            safeDeleteCollection(createdFeedCollectionForSplit);
            safeDeleteCollection(createdLeaseCollection);
            safeClose(clientWithStaleCache);

            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    @Test(groups = { "emulator" }, dataProvider = "contextTestConfigs", timeOut = 50 * CHANGE_FEED_PROCESSOR_TIMEOUT)
    public void fullFidelityChangeFeedProcessorWithThroughputControl(boolean isContextRequired) throws InterruptedException {
        // Create a separate client as throughput control group will be applied to it
        CosmosAsyncClient clientWithThroughputControl =
            getClientBuilder()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

        CosmosAsyncDatabase database = clientWithThroughputControl.getDatabase(this.createdDatabase.getId());

        CosmosAsyncContainer createdFeedCollection = createFeedCollection(database, 4000); // using a large value as we plan to create more docs
        CosmosAsyncContainer createdLeaseCollection = createLeaseCollection(database, LEASE_COLLECTION_THROUGHPUT);

        ThroughputControlGroupConfig throughputControlGroupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("changeFeedProcessor")
                .targetThroughput(1)
                .build();

        try {
            List<InternalObjectNode> createdDocuments = new ArrayList<>();
            Map<String, ChangeFeedProcessorItem> receivedDocuments = new ConcurrentHashMap<>();
            Set<String> receivedLeaseTokensFromContext = ConcurrentHashMap.newKeySet();
            ChangeFeedProcessorOptions changeFeedProcessorOptions = new ChangeFeedProcessorOptions();
            int maxItemCount = 100; // force the RU usage per requests > 1
            int feedCount = maxItemCount * 2; // force to do two fetches

            ChangeFeedProcessorBuilder changeFeedProcessorBuilder = new ChangeFeedProcessorBuilder()
                .options(changeFeedProcessorOptions)
                .hostName(hostName)
                .feedContainer(createdFeedCollection)
                .leaseContainer(createdLeaseCollection)
                .options(
                    new ChangeFeedProcessorOptions()
                        .setFeedPollThroughputControlConfig(throughputControlGroupConfig)
                        .setMaxItemCount(maxItemCount)
                );

            if (isContextRequired) {
                changeFeedProcessorBuilder = changeFeedProcessorBuilder
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandlerWithContext(receivedDocuments, receivedLeaseTokensFromContext));
            } else {
                changeFeedProcessorBuilder = changeFeedProcessorBuilder
                    .handleAllVersionsAndDeletesChanges(changeFeedProcessorHandler(receivedDocuments));
            }

            ChangeFeedProcessor changeFeedProcessor = changeFeedProcessorBuilder.buildChangeFeedProcessor();

            try {
                changeFeedProcessor.start().subscribeOn(Schedulers.boundedElastic())
                    .timeout(Duration.ofMillis(2 * CHANGE_FEED_PROCESSOR_TIMEOUT))
                    .subscribe();
                logger.info("Starting ChangeFeed processor");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                logger.info("Finished starting ChangeFeed processor");

                setupReadFeedDocuments(createdDocuments, receivedDocuments, createdFeedCollection, feedCount);
                logger.info("Set up read feed documents");

                // Wait for the feed processor to receive and process the documents.
                Thread.sleep(4 * CHANGE_FEED_PROCESSOR_TIMEOUT);
                logger.info("Validating changes now");

                validateChangeFeedProcessing(changeFeedProcessor, createdDocuments, receivedDocuments, feedCount, 10 * CHANGE_FEED_PROCESSOR_TIMEOUT);

                changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).timeout(Duration.ofMillis(CHANGE_FEED_PROCESSOR_TIMEOUT)).subscribe();

                // query for leases from the createdLeaseCollection
                String leaseQuery = "select * from c where not contains(c.id, \"info\")";
                List<JsonNode> leaseDocuments =
                    createdLeaseCollection
                        .queryItems(leaseQuery, JsonNode.class)
                        .byPage()
                        .blockFirst()
                        .getResults();

                List<String> leaseTokensCollectedFromLeaseCollection =
                    leaseDocuments.stream().map(lease -> lease.get("LeaseToken").asText()).collect(Collectors.toList());

                if (isContextRequired) {
                    assertThat(leaseTokensCollectedFromLeaseCollection).isNotNull();
                    assertThat(receivedLeaseTokensFromContext.size()).isEqualTo(leaseTokensCollectedFromLeaseCollection.size());

                    assertThat(receivedLeaseTokensFromContext.containsAll(leaseTokensCollectedFromLeaseCollection)).isTrue();
                }

                // Wait for the feed processor to shut down.
                Thread.sleep(2 * CHANGE_FEED_PROCESSOR_TIMEOUT);

            } catch (Exception ex) {
                log.error("Change feed processor did not start and stopped in the expected time", ex);
                throw ex;
            }

        } finally {
            safeDeleteCollection(createdFeedCollection);
            safeDeleteCollection(createdLeaseCollection);
            safeClose(clientWithThroughputControl);
            // Allow some time for the collections to be deleted before exiting.
            Thread.sleep(500);
        }
    }

    private Consumer<List<ChangeFeedProcessorItem>> changeFeedProcessorHandler(Map<String, ChangeFeedProcessorItem> receivedDocuments) {
        return docs -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private Consumer<List<ChangeFeedProcessorItem>> changeFeedProcessorHandler(
        Map<String, ChangeFeedProcessorItem> receivedDocuments,
        int documentLimit,
        AtomicReference<ChangeFeedProcessor> changeFeedProcessorAtomicReference) {
        return docs -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());

            if (receivedDocuments.size() >= documentLimit) {
                logger.info("Processed {} documents which is the document limit, stopping the change feed processor", receivedDocuments.size());
                stopChangeFeedProcessor(changeFeedProcessorAtomicReference.get());
                return;
            }

            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private BiConsumer<List<ChangeFeedProcessorItem>, ChangeFeedProcessorContext> changeFeedProcessorHandlerWithContext(
        Map<String, ChangeFeedProcessorItem> receivedDocuments, Set<String> receivedLeaseTokensFromContext) {
        return (docs, context) -> {
            logger.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            validateChangeFeedProcessorContext(context);
            processChangeFeedProcessorContext(context, receivedLeaseTokensFromContext);
            logger.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    void validateChangeFeedProcessing(List<InternalObjectNode> createdDocuments,
                                      Map<String, ChangeFeedProcessorItem> receivedDocuments,
                                      int expectedFeedCount) {

        // Added this validation for now to verify received list has something - easy way to see size not being 10
        assertThat(receivedDocuments.size()).isEqualTo(expectedFeedCount);

        Set<String> createdDocIds = createdDocuments.stream().map(createdDocument -> createdDocument.get("id").toString()).collect(Collectors.toSet());

        for (ChangeFeedProcessorItem item : receivedDocuments.values()) {
            assertThat(createdDocIds.contains(item.getCurrent().get("id").asText())).as("Document with getId: " + item.getCurrent().get("id").asText()).isTrue();
        }
    }


    void validateChangeFeedProcessing(
        ChangeFeedProcessor changeFeedProcessor,
        List<InternalObjectNode> createdDocuments,
        Map<String, ChangeFeedProcessorItem> receivedDocuments,
        int expectedFeedCount,
        int sleepTime) throws InterruptedException {
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
        assertThat(receivedDocuments.size()).isEqualTo(expectedFeedCount);

        for (InternalObjectNode item : createdDocuments) {
            assertThat(receivedDocuments.containsKey(item.getId())).as("Document with getId: " + item.getId()).isTrue();
        }
    }

    void validateChangeFeedProcessorContext(ChangeFeedProcessorContext changeFeedProcessorContext) {

        String leaseToken = changeFeedProcessorContext.getLeaseToken();

        assertThat(leaseToken).isNotNull();
    }

    private Consumer<List<ChangeFeedProcessorItem>> fullFidelityChangeFeedProcessorHandler(Map<String, ChangeFeedProcessorItem> receivedDocuments) {
        return docs -> {
            log.info("START processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                processItem(item, receivedDocuments);
            }
            log.info("END processing from thread {}", Thread.currentThread().getId());
        };
    }

    private void waitToReceiveDocuments(Map<String, ChangeFeedProcessorItem> receivedDocuments, long timeoutInMillisecond, long count) throws InterruptedException {
        long remainingWork = timeoutInMillisecond;
        while (remainingWork > 0 && receivedDocuments.size() < count) {
            remainingWork -= 100;
            Thread.sleep(200);
        }

        assertThat(remainingWork > 0).as("Failed to receive all the feed documents").isTrue();
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_ChangeFeedProcessorTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = getSharedCosmosDatabase(client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = 2 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private void setupReadFeedDocuments(List<InternalObjectNode> createdDocuments, Map<String, ChangeFeedProcessorItem> receivedDocuments, CosmosAsyncContainer feedCollection, long count) {
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
        return createFeedCollection(createdDatabase, provisionedThroughput);
    }

    private CosmosAsyncContainer createFeedCollection(CosmosAsyncDatabase database, int provisionedThroughput) {
        CosmosContainerRequestOptions optionsFeedCollection = new CosmosContainerRequestOptions();
        return createCollection(database, getCollectionDefinitionWithFullFidelity(), optionsFeedCollection, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseCollection(int provisionedThroughput) {
        return createLeaseCollection(createdDatabase, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseCollection(CosmosAsyncDatabase database, int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "leases_" + UUID.randomUUID(),
            "/id");
        return createCollection(database, collectionDefinition, options, provisionedThroughput);
    }

    private CosmosAsyncContainer createLeaseMonitorCollection(int provisionedThroughput) {
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(
            "monitor_" + UUID.randomUUID(),
            "/id");
        return createCollection(createdDatabase, collectionDefinition, options, provisionedThroughput);
    }

    private Consumer<List<ChangeFeedProcessorItem>> leasesChangeFeedProcessorHandler(LeaseStateMonitor leaseStateMonitor) {
        return docs -> {
            log.info("LEASES processing from thread in test {}", Thread.currentThread().getId());
            for (ChangeFeedProcessorItem item : docs) {
                try {
                    log
                        .debug("LEASE RECEIVED {}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                } catch (JsonProcessingException e) {
                    log.error("Failure in processing json [{}]", e.getMessage(), e);
                }

                JsonNode leaseToken = item.getCurrent().get("LeaseToken");

                if (leaseToken != null) {
                    JsonNode continuationTokenNode = item.getCurrent().get("ContinuationToken");
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
                    leaseStateMonitor.receivedLeases.put(item.getCurrent().get("id").asText(), item.getCurrent());
                }
            }
            log.info("LEASES processing from thread {}", Thread.currentThread().getId());
        };
    }

    private static synchronized void processItem(ChangeFeedProcessorItem item, Map<String, ChangeFeedProcessorItem> receivedDocuments) {
        log.info("RECEIVED {}", item);
        receivedDocuments.put(item.getCurrent().get("id").asText(), item);
    }

    private static synchronized void processChangeFeedProcessorContext(
        ChangeFeedProcessorContext context,
        Set<String> receivedLeaseTokens) {

        if (context == null) {
            fail("The context cannot be null.");
        }

        if (context.getLeaseToken() == null || context.getLeaseToken().isEmpty()) {
            fail("The lease token cannot be null or empty.");
        }

        receivedLeaseTokens.add(context.getLeaseToken());
    }

    private static void stopChangeFeedProcessor(ChangeFeedProcessor changeFeedProcessor) {
        changeFeedProcessor.stop().subscribeOn(Schedulers.boundedElastic()).block();
    }

    class LeaseStateMonitor {
        public Map<String, JsonNode> receivedLeases = new ConcurrentHashMap<>();
        public volatile boolean isAfterLeaseInitialization = false;
        public volatile boolean isAfterSplits = false;
        public volatile long parentContinuationToken = 0;
        public volatile boolean isContinuationTokenAdvancing = true;
    }
}

/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.RetryAnalyzer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.implementation.guava25.collect.ArrayListMultimap;
import com.azure.cosmos.implementation.guava25.collect.Multimap;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertThrows;

public class CosmosContainerChangeFeedTest extends TestSuiteBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PARTITION_KEY_FIELD_NAME = "mypk";
    private CosmosClient client;
    private CosmosAsyncContainer createdAsyncContainer;
    private CosmosAsyncDatabase createdAsyncDatabase;
    private CosmosContainer createdContainer;
    private CosmosDatabase createdDatabase;
    private final Multimap<String, ObjectNode> partitionKeyToDocuments = ArrayListMultimap.create();
    private final String preExistingDatabaseId = CosmosDatabaseForTest.generateId();

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public CosmosContainerChangeFeedTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @AfterClass(groups = { "emulator" }, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @AfterMethod(groups = { "emulator" })
    public void afterTest() throws Exception {
        if (this.createdContainer != null) {
            try {
                this.createdContainer.delete();
            } catch (CosmosException error) {
                if (error.getStatusCode() != 404) {
                    throw error;
                }
            }
        }
    }

    @BeforeMethod(groups = { "emulator" })
    public void beforeTest() throws Exception {
        this.createdContainer = null;
        this.createdAsyncContainer = null;
        this.partitionKeyToDocuments.clear();
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
        createdAsyncDatabase = client.asyncClient().getDatabase(createdDatabase.getId());
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT * 5)
    public void asyncChangeFeed_fromBeginning_incremental_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(200, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);

        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final int expectedInitialEventCount =
            200 * 7   //inserted
                + 0       // updates won't show up as extra events in incremental mode
                - 2 * 3;  // updated then deleted documents won't show up at all in incremental mode

        final int expectedEventCountAfterUpdates =
            5 * 2     // event count for initial updates
                - 1 * Math.min(2,3);   // reducing events for 2 of the 3 deleted documents
        // (because they have also had been updated)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());

        String continuation = drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount);

        // applying updates
        updateAction.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        drainAndValidateChangeFeedResults(options, null, expectedEventCountAfterUpdates);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT )
    public void asyncChangeFeed_fromBeginning_incremental_forFullRange_withSmallPageSize() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(200, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);

        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final int expectedInitialEventCount =
            200 * 7   //inserted
            + 0       // updates won't show up as extra events in incremental mode
            - 2 * 3;  // updated then deleted documents won't show up at all in incremental mode

        final int expectedEventCountAfterUpdates =
            5 * 2     // event count for initial updates
            - 1 * Math.min(2,3);   // reducing events for 2 of the 3 deleted documents
                                   // (because they have also had been updated)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange())
            .setMaxItemCount(10);

        String continuation = drainAndValidateChangeFeedResults(
            options,
            (o) -> o.setMaxItemCount(10),
            expectedInitialEventCount);

        // applying updates
        updateAction.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation)
            .setMaxItemCount(10);

        drainAndValidateChangeFeedResults(
            options,
            (o) -> o.setMaxItemCount(10),
            expectedEventCountAfterUpdates);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT, retryAnalyzer = RetryAnalyzer.class)
    public void asyncChangeFeed_fromBeginning_incremental_forLogicalPartition() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(20, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);
        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final Map<String, String> continuations = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            String pkValue = partitionKeyToDocuments.keySet().stream().skip(i).findFirst().get();
            logger.info(String.format("Initial validation - PK value: '%s'", pkValue));

            final int initiallyDeletedDocuments = i < 2 ? 3 : 0;
            final int expectedInitialEventCount = 7 - initiallyDeletedDocuments;

            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(
                    FeedRange.forLogicalPartition(
                        new PartitionKey(pkValue)
                    ));

            continuations.put(
                pkValue,
                drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount));
        }

        // applying updates
        updateAction.run();

        Thread.sleep(3000);

        for (int i = 0; i < 20; i++) {
            String pkValue = partitionKeyToDocuments.keySet().stream().skip(i).findFirst().get();
            logger.info(String.format("Validation after updates - PK value: '%s'", pkValue));

            final int expectedEventCountAfterUpdates = i < 5 ?
                i < 1 ? 0 : 2  // on the first logical partitions all updated documents were deleted
                : 0; // no updates

            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuations.get(pkValue));

            drainAndValidateChangeFeedResults(options, null, expectedEventCountAfterUpdates);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void asyncChangeFeed_fromBeginning_incremental_forEPK() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(20, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);
        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final Map<String, String> continuations = new HashMap<>();

        for (int i = 0; i < 20; i++) {
            String pkValue = partitionKeyToDocuments.keySet().stream().skip(i).findFirst().get();
            logger.info(String.format("Initial validation - PK value: '%s'", pkValue));

            final int initiallyDeletedDocuments = i < 2 ? 3 : 0;
            final int expectedInitialEventCount = 7 - initiallyDeletedDocuments;

            FeedRangeInternal feedRangeForLogicalPartition =
                (FeedRangeInternal)FeedRange.forLogicalPartition(new PartitionKey(pkValue));
            Utils.ValueHolder<DocumentCollection> documentCollection = client
                .asyncClient()
                .getContextClient()
                .getCollectionCache()
                .resolveByRidAsync(
                    null,
                    createdContainer.read().getProperties().getResourceId(),
                    null)
                .block();

            Range<String> effectiveRange = feedRangeForLogicalPartition
                .getNormalizedEffectiveRange(
                    client.asyncClient().getContextClient().getPartitionKeyRangeCache(),
                    null,
                    Mono.just(documentCollection))
                .block();

            assertThat(effectiveRange).isNotNull();

            FeedRange feedRange = new FeedRangeEpkImpl(effectiveRange);

            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(feedRange);

            continuations.put(
                pkValue,
                drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount));
        }

        // applying updates
        updateAction.run();

        for (int i = 0; i < 20; i++) {
            String pkValue = partitionKeyToDocuments.keySet().stream().skip(i).findFirst().get();
            logger.info(String.format("Validation after updates - PK value: '%s'", pkValue));

            final int expectedEventCountAfterUpdates = i < 5 ?
                i < 1 ? 0 : 2  // on the first logical partitions all updated documents were deleted
                : 0; // no updates

            CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuations.get(pkValue));

            drainAndValidateChangeFeedResults(options, null, expectedEventCountAfterUpdates);
        }
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void asyncChangeFeed_fromBeginning_incremental_forFeedRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(200, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);
        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final int expectedTotalInitialEventCount =
            200 * 7   //inserted
                + 0       // updates won't show up as extra events in incremental mode
                - 2 * 3;  // updated then deleted documents won't show up at all in incremental mode

        final int expectedTotalEventCountAfterUpdates =
            5 * 2     // event count for initial updates
                - 1 * Math.min(2,3);   // reducing events for 2 of the 3 deleted documents
        // (because they have also had been updated)

        List<FeedRange> feedRanges = createdContainer.getFeedRanges();
        List<CosmosChangeFeedRequestOptions> options = new ArrayList<>();

        for (int i = 0; i < feedRanges.size(); i++) {
            options.add(CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(feedRanges.get(i)));
        }

        final Map<Integer, String> continuations = drainAndValidateChangeFeedResults(
            options,
            null,
            expectedTotalInitialEventCount);

        // applying updates
        updateAction.run();

        options.clear();
        for (int i = 0; i < feedRanges.size(); i++) {
            options.add(CosmosChangeFeedRequestOptions
                .createForProcessingFromContinuation(continuations.get(i)));
        }

        drainAndValidateChangeFeedResults(options, null, expectedTotalEventCountAfterUpdates);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void asyncChangeFeed_fromBeginning_fullFidelity_forFullRange() throws Exception {
        assertThrows(
            IllegalStateException.class,
            () -> CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forFullRange())
                .fullFidelity());
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void asyncChangeFeed_fromNow_incremental_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(20, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);

        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(2, 3);
            insertDocuments(100, 5);
        };

        final int expectedInitialEventCount = 0;

        final int expectedEventCountAfterUpdates =
                5 * 2               // event count for updates
            -   (2 * Math.min(2,3)) // reducing events for 2 of the 3 deleted documents
                                    // (because they have also had been updated) on each of the two
                                    // partitions documents are deleted from
            + 100 * 5;              // event count for inserts

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());


        String continuation = drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount);

        // applying updates
        updateAction.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        drainAndValidateChangeFeedResults(options, null, expectedEventCountAfterUpdates);
    }

    //TODO Temporarily disabling
    @Test(groups = { "emulator" }, timeOut = TIMEOUT, enabled = false)
    public void asyncChangeFeed_fromNow_fullFidelity_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(Duration.ofMinutes(10)))
        );
        insertDocuments(8, 15);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);

        Runnable updateAction1 = () -> {
            insertDocuments(5, 9);
            updateDocuments(3, 5);
            deleteDocuments(2, 3);
        };

        Runnable updateAction2 = () -> {
            updateDocuments(5, 2);
            deleteDocuments(2, 3);
            insertDocuments(10, 5);
        };

        final int expectedInitialEventCount = 0;

        final int expectedEventCountAfterFirstSetOfUpdates =
              5 * 9       // events for inserts
            + 3 * 5       // event count for updates
            + 2 * 3;      // plus deletes (which are all included in FF CF)

        final int expectedEventCountAfterSecondSetOfUpdates =
           10 * 5         // events for inserts
          + 5 * 2         // event count for updates
          + 2 * 3;        // plus deletes (which are all included in FF CF)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange());


        String continuation = drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount);

        // applying first set of  updates
        updateAction1.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        continuation = drainAndValidateChangeFeedResults(
            options,
            null,
            expectedEventCountAfterFirstSetOfUpdates);

        // applying first set of  updates
        updateAction2.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        drainAndValidateChangeFeedResults(
            options,
            null,
            expectedEventCountAfterSecondSetOfUpdates);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void asyncChangeFeed_fromPointInTime_incremental_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(20, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);

        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final int expectedInitialEventCount =
             20 * 7   //inserted
                + 0       // updates won't show up as extra events in incremental mode
                - 2 * 3;  // updated then deleted documents won't show up at all in incremental mode

        final int expectedEventCountAfterUpdates =
              5 * 2              // event count for initial updates
            - 1 * Math.min(2,3); // reducing events for 2 of the 3 deleted documents
                                 // (because they have also had been updated)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromPointInTime(
                Instant.now().minus(10, ChronoUnit.SECONDS),
                FeedRange.forFullRange());

        String continuation = drainAndValidateChangeFeedResults(options, null, expectedInitialEventCount);

        // applying updates
        updateAction.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation);

        drainAndValidateChangeFeedResults(options, null, expectedEventCountAfterUpdates);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void asyncChangeFeed_fromPointInTime_fullFidelity_forFullRange() throws Exception {
        assertThrows(
            IllegalStateException.class,
            () -> CosmosChangeFeedRequestOptions
                .createForProcessingFromPointInTime(
                    Instant.now().minus(10, ChronoUnit.SECONDS),
                    FeedRange.forFullRange())
                .fullFidelity());
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void syncChangeFeed_fromBeginning_incremental_forFullRange() throws Exception {
        this.createContainer(
            (cp) -> cp.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy())
        );
        insertDocuments(200, 7);
        updateDocuments(3, 5);
        deleteDocuments(2, 3);
        Runnable updateAction = () -> {
            updateDocuments(5, 2);
            deleteDocuments(1, 3);
        };

        final int expectedInitialEventCount =
            200 * 7   //inserted
                + 0       // updates won't show up as extra events in incremental mode
                - 2 * 3;  // updated then deleted documents won't show up at all in incremental mode

        final int expectedEventCountAfterUpdates =
            5 * 2     // event count for initial updates
                - 1 * Math.min(2,3);   // reducing events for 2 of the 3 deleted documents
        // (because they have also had been updated)

        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromBeginning(FeedRange.forFullRange());

        AtomicReference<String> continuation = new AtomicReference<>();
        List<ObjectNode> results = createdContainer
                    .queryChangeFeed(options, ObjectNode.class)
                    // NOTE - in real app you would need delaying persisting the
                    // continuation until you retrieve the next one
                    .handle((r) -> continuation.set(r.getContinuationToken()))
                    .stream()
                    .collect(Collectors.toList());

        assertThat(results)
            .isNotNull()
            .size()
            .isEqualTo(expectedInitialEventCount);

        // applying updates
        updateAction.run();

        options = CosmosChangeFeedRequestOptions
            .createForProcessingFromContinuation(continuation.get());
        results = createdContainer
            .queryChangeFeed(options, ObjectNode.class)
            // NOTE - in real app you would need delaying persisting the
            // continuation until you retrieve the next one
            .handle((r) -> continuation.set(r.getContinuationToken()))
            .stream()
            .collect(Collectors.toList());

        assertThat(results)
            .isNotNull()
            .size()
            .isEqualTo(expectedEventCountAfterUpdates);
    }

    void insertDocuments(
        int partitionCount,
        int documentCount) {

        List<ObjectNode> docs = new ArrayList<>();

        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = UUID.randomUUID().toString();
            for (int j = 0; j < documentCount; j++) {
                docs.add(getDocumentDefinition(partitionKey));
            }
        }

        ArrayList<Mono<CosmosItemResponse<ObjectNode>>> result = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(createdAsyncContainer
                .createItem(docs.get(i)));
        }

        List<ObjectNode> insertedDocs = Flux.merge(
            Flux.fromIterable(result),
            10)
                   .map(CosmosItemResponse::getItem).collectList().block();

        for (ObjectNode doc : insertedDocs) {
            partitionKeyToDocuments.put(
                doc.get(PARTITION_KEY_FIELD_NAME).textValue(),
                doc);
        }
        logger.info("FINISHED INSERT");
    }

    void deleteDocuments(
        int partitionCount,
        int documentCount) {

        assertThat(partitionCount)
            .isLessThanOrEqualTo(this.partitionKeyToDocuments.keySet().size());

        Collection<ObjectNode> docs;
        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = this.partitionKeyToDocuments
                .keySet()
                .stream()
                .skip(i)
                .findFirst()
                .get();

            docs = this.partitionKeyToDocuments.get(partitionKey);
            assertThat(docs)
                .isNotNull()
                .size()
                .isGreaterThanOrEqualTo(documentCount);

            for (int j = 0; j < documentCount; j++) {
                ObjectNode docToBeDeleted = docs.stream().findFirst().get();
                createdContainer.deleteItem(docToBeDeleted, null);
                docs.remove(docToBeDeleted);
            }
        }
    }

    void updateDocuments(
        int partitionCount,
        int documentCount) {

        assertThat(partitionCount)
            .isLessThanOrEqualTo(this.partitionKeyToDocuments.keySet().size());

        Collection<ObjectNode> docs;
        for (int i = 0; i < partitionCount; i++) {
            String partitionKey = this.partitionKeyToDocuments
                .keySet()
                .stream()
                .skip(i)
                .findFirst()
                .get();

            docs = this.partitionKeyToDocuments.get(partitionKey);
            assertThat(docs)
                .isNotNull()
                .size()
                .isGreaterThanOrEqualTo(documentCount);

            for (int j = 0; j < documentCount; j++) {
                ObjectNode docToBeUpdated = docs.stream().skip(j).findFirst().get();
                docToBeUpdated.put("someProperty", UUID.randomUUID().toString());
                createdContainer.replaceItem(
                    docToBeUpdated,
                    docToBeUpdated.get("id").textValue(),
                    new PartitionKey(docToBeUpdated.get("mypk").textValue()),
                    null);
            }
        }
    }

    private String drainAndValidateChangeFeedResults(
        CosmosChangeFeedRequestOptions changeFeedRequestOptions,
        Function<CosmosChangeFeedRequestOptions, CosmosChangeFeedRequestOptions> onNewRequestOptions,
        int expectedEventCount) {

        return drainAndValidateChangeFeedResults(
            Arrays.asList(changeFeedRequestOptions),
            onNewRequestOptions,
            expectedEventCount).get(0);
    }

    private Map<Integer, String> drainAndValidateChangeFeedResults(
        List<CosmosChangeFeedRequestOptions> changeFeedRequestOptions,
        Function<CosmosChangeFeedRequestOptions, CosmosChangeFeedRequestOptions> onNewRequestOptions,
        int expectedTotalEventCount) {

        Map<Integer, String> continuations = new HashMap<>();

        int totalRetrievedEventCount = 0;

        boolean isFinished = false;
        int emptyResultCount = 0;

        while (!isFinished) {
            for (Integer i = 0; i < changeFeedRequestOptions.size(); i++) {
                List<ObjectNode> results;

                CosmosChangeFeedRequestOptions effectiveOptions;
                if (continuations.containsKey(i)) {
                    logger.info(String.format(
                        "Continuation BEFORE: %s",
                        new String(
                            Base64.getUrlDecoder().decode(continuations.get(i)),
                            StandardCharsets.UTF_8)));
                    effectiveOptions = CosmosChangeFeedRequestOptions
                        .createForProcessingFromContinuation(continuations.get(i));
                    if (onNewRequestOptions != null) {
                        effectiveOptions = onNewRequestOptions.apply(effectiveOptions);
                    }
                } else {
                    effectiveOptions = changeFeedRequestOptions.get(i);
                }

                final Integer index = i;
                results = createdAsyncContainer
                    .queryChangeFeed(effectiveOptions, ObjectNode.class)
                    // NOTE - in real app you would need delaying persisting the
                    // continuation until you retrieve the next one
                    .handle((r) -> continuations.put(index, r.getContinuationToken()))
                    .collectList()
                    .block();

                logger.info(
                    String.format(
                        "Continuation AFTER: %s, records retrieved: %d",
                        new String(
                            Base64.getUrlDecoder().decode(continuations.get(i)),
                            StandardCharsets.UTF_8),
                        results.size()));

                totalRetrievedEventCount += results.size();
                if (totalRetrievedEventCount >= expectedTotalEventCount) {
                    isFinished = true;
                    break;
                }

                if (results.size() == 0) {
                    emptyResultCount += 1;

                    assertThat(emptyResultCount).isLessThan(6 * changeFeedRequestOptions.size());
                    logger.info(
                        String.format("Not all expected events retrieved yet. Retrieved %d out of " +
                            "expected %d events. Retrying... Retry count: %d",
                            totalRetrievedEventCount,
                            expectedTotalEventCount,
                            emptyResultCount));

                    try {
                        Thread.sleep(500 / changeFeedRequestOptions.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    emptyResultCount = 0;
                }

            }
        }

        assertThat(totalRetrievedEventCount)
            .isEqualTo(expectedTotalEventCount);

        return continuations;
    }

    private Range<String> convertToMaxExclusive(Range<String> maxInclusiveRange) {
        assertThat(maxInclusiveRange)
            .isNotNull()
            .matches(r -> r.isMaxInclusive(), "Ensure isMaxInclusive is set");

        String max = maxInclusiveRange.getMax();
        int i = max.length() - 1;

        while (i >= 0) {
            if (max.charAt(i) == 'F') {
                i--;
                continue;
            }

            char newChar = (char)(((int)max.charAt(i))+1);

            if (i < max.length() - 1) {
                max = max.substring(0, i) + newChar + max.substring(i + 1);
            } else {
                max = max.substring(0, i) + newChar;
            }

            break;
        }

        return new Range<>(maxInclusiveRange.getMin(), max, true, false);
    }

    private void createContainer(
        Function<CosmosContainerProperties, CosmosContainerProperties> onInitialization) {

        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        if (onInitialization != null) {
            containerProperties = onInitialization.apply(containerProperties);
        }

        CosmosContainerResponse containerResponse =
            createdDatabase.createContainer(containerProperties, 10100, null);
        assertThat(containerResponse.getRequestCharge()).isGreaterThan(0);
        validateContainerResponse(containerProperties, containerResponse);

        this.createdContainer = createdDatabase.getContainer(collectionName);
        this.createdAsyncContainer = createdAsyncDatabase.getContainer(collectionName);
    }

    private static ObjectNode getDocumentDefinition(String partitionKey) {
        String uuid = UUID.randomUUID().toString();
        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"prop\": \"%s\""
                + "}"
            , uuid, partitionKey, uuid);

        try {
            return
                OBJECT_MAPPER.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid partition key value provided.");
        }
    }

    private void validateContainerResponse(CosmosContainerProperties containerProperties,
                                           CosmosContainerResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());

    }
}

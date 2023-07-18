/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 *
 */

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.apache.commons.io.FileUtils.ONE_MB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CosmosItemTest extends TestSuiteBase {

    private final static
    ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor diagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuildersWithDirectSession")
    public CosmosItemTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosItemTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        logger.info("Testing log");
        CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createItem_alreadyExists() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        validateItemResponse(properties, itemResponse);

        properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse1 = container.createItem(properties, new CosmosItemRequestOptions());
        validateItemResponse(properties, itemResponse1);

        // Test for conflict
        try {
            container.createItem(properties, new CosmosItemRequestOptions());
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createLargeItem() throws Exception {
        InternalObjectNode docDefinition = getDocumentDefinition(UUID.randomUUID().toString());

        //Keep size as ~ 1.5MB to account for size of other props
        int size = (int) (ONE_MB * 1.5);
        BridgeInternal.setProperty(docDefinition, "largeString", StringUtils.repeat("x", size));

        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(docDefinition, new CosmosItemRequestOptions());

        validateItemResponse(docDefinition, itemResponse);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createItemWithVeryLargePartitionKey() throws Exception {
        InternalObjectNode docDefinition = getDocumentDefinition(UUID.randomUUID().toString());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        BridgeInternal.setProperty(docDefinition, "mypk", sb.toString());

        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(docDefinition, new CosmosItemRequestOptions());

        validateItemResponse(docDefinition, itemResponse);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItemWithVeryLargePartitionKey() throws Exception {
        InternalObjectNode docDefinition = getDocumentDefinition(UUID.randomUUID().toString());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            sb.append(i).append("x");
        }
        BridgeInternal.setProperty(docDefinition, "mypk", sb.toString());

        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(docDefinition);

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        CosmosItemResponse<InternalObjectNode> readResponse = container.readItem(docDefinition.getId(),
            new PartitionKey(sb.toString()), options,
            InternalObjectNode.class);

        validateItemResponse(docDefinition, readResponse);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        CosmosItemResponse<InternalObjectNode> readResponse1 = container.readItem(properties.getId(),
                                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                                    new CosmosItemRequestOptions(),
                                                                                    InternalObjectNode.class);
        validateItemResponse(properties, readResponse1);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readMany() throws Exception {
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        Set<String> idSet = new HashSet<>();
        int numDocuments = 5;

        for (int i = 0; i < numDocuments; i++) {
            InternalObjectNode document = getDocumentDefinition(UUID.randomUUID().toString());
            container.createItem(document);

            PartitionKey partitionKey = new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(document, "mypk"));
            CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, document.getId());
            cosmosItemIdentities.add(cosmosItemIdentity);
            idSet.add(document.getId());
        }

        FeedResponse<InternalObjectNode> feedResponse = container.readMany(cosmosItemIdentities, InternalObjectNode.class);

        assertThat(feedResponse).isNotNull();
        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(numDocuments);
        assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics())).isNotNull();
        assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics()).size()).isGreaterThanOrEqualTo(1);

        for (int i = 0; i < feedResponse.getResults().size(); i++) {
            InternalObjectNode fetchedResult = feedResponse.getResults().get(i);
            assertThat(idSet.contains(fetchedResult.getId())).isTrue();
        }
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyWithSamePartitionKey() throws Exception {
        String partitionKeyValue = UUID.randomUUID().toString();
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        Set<String> idSet = new HashSet<>();
        int numDocuments = 5;

        for (int i = 0; i < numDocuments; i++) {
            String documentId = UUID.randomUUID().toString();
            ObjectNode document = getDocumentDefinition(documentId, partitionKeyValue);
            container.createItem(document);

            PartitionKey partitionKey = new PartitionKey(partitionKeyValue);
            CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, documentId);

            cosmosItemIdentities.add(cosmosItemIdentity);
            idSet.add(documentId);
        }

        FeedResponse<InternalObjectNode> feedResponse = container.readMany(cosmosItemIdentities, InternalObjectNode.class);

        assertThat(feedResponse).isNotNull();
        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(numDocuments);
        assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics())).isNotNull();
        assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics()).size()).isEqualTo(1);


        for (int i = 0; i < feedResponse.getResults().size(); i++) {
            InternalObjectNode fetchedResult = feedResponse.getResults().get(i);
            assertThat(idSet.contains(fetchedResult.getId())).isTrue();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyWithPojo() throws Exception {
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        Set<String> idSet = new HashSet<>();
        Set<String> valSet = new HashSet<>();
        int numDocuments = 5;

        for (int i = 0; i < numDocuments; i++) {
            SampleType document = new SampleType(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
            container.createItem(document);

            PartitionKey partitionKey = new PartitionKey(document.getMypk());
            CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, document.getId());
            cosmosItemIdentities.add(cosmosItemIdentity);
            idSet.add(document.getId());
            valSet.add(document.getVal());
        }

        FeedResponse<SampleType> feedResponse = container.readMany(cosmosItemIdentities, SampleType.class);

        assertThat(feedResponse).isNotNull();
        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(numDocuments);
        assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics())).isNotNull();
        assertThat(diagnosticsAccessor.getClientSideRequestStatistics(feedResponse.getCosmosDiagnostics()).size()).isGreaterThanOrEqualTo(1);

        for (int i = 0; i < feedResponse.getResults().size(); i++) {
            SampleType fetchedResult = feedResponse.getResults().get(i);
            assertThat(idSet.contains(fetchedResult.getId())).isTrue();
            assertThat(valSet.contains(fetchedResult.getVal())).isTrue();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyWithPojoAndSingleTuple() throws Exception {
        List<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();

        SampleType document = new SampleType(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString());
        container.createItem(document);

        PartitionKey partitionKey = new PartitionKey(document.getMypk());
        CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, document.getId());
        cosmosItemIdentities.add(cosmosItemIdentity);

        FeedResponse<SampleType> feedResponse = container.readMany(cosmosItemIdentities, SampleType.class);

        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(1);
        SampleType fetchedDocument = feedResponse.getResults().get(0);

        assertThat(document.getId()).isEqualTo(fetchedDocument.getId());
        assertThat(document.getMypk()).isEqualTo(fetchedDocument.getMypk());
        assertThat(document.getVal()).isEqualTo(fetchedDocument.getVal());
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyWithSingleTuple() throws Exception {
        String partitionKeyValue = UUID.randomUUID().toString();
        ArrayList<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        HashSet<String> idSet = new HashSet<String>();
        int numDocuments = 5;

        for (int i = 0; i < numDocuments; i++) {
            String documentId = UUID.randomUUID().toString();
            ObjectNode document = getDocumentDefinition(documentId, partitionKeyValue);
            container.createItem(document);

            PartitionKey partitionKey = new PartitionKey(partitionKeyValue);
            CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, documentId);

            cosmosItemIdentities.add(cosmosItemIdentity);
            idSet.add(documentId);
        }

        for (int i = 0; i < numDocuments; i++) {
            FeedResponse<InternalObjectNode> feedResponse = container.readMany(Arrays.asList(cosmosItemIdentities.get(i)), InternalObjectNode.class);

            assertThat(feedResponse).isNotNull();
            assertThat(feedResponse.getResults()).isNotNull();
            assertThat(feedResponse.getResults().size()).isEqualTo(1);
            assertThat(idSet.contains(feedResponse.getResults().get(0).getId())).isTrue();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyWithManyNonExistentItemIds() throws Exception {
        String partitionKeyValue = UUID.randomUUID().toString();
        ArrayList<CosmosItemIdentity> cosmosItemIdentities = new ArrayList<>();
        ArrayList<CosmosItemIdentity> nonExistentCosmosItemIdentities = new ArrayList<>();
        HashSet<String> idSet = new HashSet<String>();
        int numDocuments = 5;
        int numNonExistentDocuments = 5;

        for (int i = 0; i < numNonExistentDocuments; i++) {
            CosmosItemIdentity nonExistentItemIdentity = new CosmosItemIdentity(new PartitionKey(UUID.randomUUID().toString()), UUID.randomUUID().toString());
            nonExistentCosmosItemIdentities.add(nonExistentItemIdentity);
        }

        for (int i = 0; i < numDocuments; i++) {
            String documentId = UUID.randomUUID().toString();
            ObjectNode document = getDocumentDefinition(documentId, partitionKeyValue);
            container.createItem(document);

            PartitionKey partitionKey = new PartitionKey(partitionKeyValue);
            CosmosItemIdentity cosmosItemIdentity = new CosmosItemIdentity(partitionKey, documentId);

            cosmosItemIdentities.add(cosmosItemIdentity);
            idSet.add(documentId);
        }

        cosmosItemIdentities.addAll(nonExistentCosmosItemIdentities);

        FeedResponse<InternalObjectNode> feedResponse = container.readMany(cosmosItemIdentities, InternalObjectNode.class);

        assertThat(feedResponse).isNotNull();
        assertThat(feedResponse.getResults()).isNotNull();
        assertThat(feedResponse.getResults().size()).isEqualTo(numDocuments);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readManyWithMultiplePartitionsAndSome404s() throws JsonProcessingException {

        CosmosDatabase readManyDatabase = null;
        CosmosContainer readManyContainer = null;

        int itemCount = 100;

        try {

            readManyDatabase = client
                .getDatabase(container.asyncContainer.getDatabase().getId());

            String readManyContainerId = "container-with-multiple-partitions";

            CosmosContainerProperties containerProperties = new CosmosContainerProperties(readManyContainerId, "/mypk");
            ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(30_000);

            readManyDatabase.createContainer(containerProperties, throughputProperties);

            readManyContainer = readManyDatabase.getContainer(readManyContainerId);

            for (int i = 0; i < itemCount; i++) {
                String id = UUID.randomUUID().toString();
                String myPk = UUID.randomUUID().toString();

                ObjectNode objectNode = getDocumentDefinition(id, myPk);

                readManyContainer.createItem(objectNode);
            }

            List<FeedRange> feedRanges = readManyContainer.getFeedRanges();

            assertThat(feedRanges).isNotNull();
            assertThat(feedRanges.size()).isGreaterThan(1);

            int feedRangeCount = feedRanges.size();

            // select 1 document per feed range
            // increase the no. of documents with faulty ids
            // see if documents fetched is (feed range count) - (faulty documents)
            for (int faultyIdCount = 0; faultyIdCount <= feedRangeCount; faultyIdCount++) {
                final Set<Integer> faultyIds = new HashSet<>();

                while (faultyIds.size() != faultyIdCount) {
                    faultyIds.add(ThreadLocalRandom.current().nextInt(feedRangeCount));
                }

                SqlQuerySpec sqlQuerySpec = new SqlQuerySpec();
                sqlQuerySpec.setQueryText("SELECT * FROM c OFFSET 0 LIMIT 1");

                List<ImmutablePair<String, String>> idToPkPairs = new ArrayList<>();

                for (int k = 0; k < feedRangeCount; k++) {
                    CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
                    cosmosQueryRequestOptions.setFeedRange(feedRanges.get(k));

                    int finalK = k;

                    readManyContainer
                        .queryItems(sqlQuerySpec, cosmosQueryRequestOptions, InternalObjectNode.class)
                        .iterableByPage()
                        .forEach(response -> {
                            InternalObjectNode queriedItem = response.getResults().get(0);

                            if (faultyIds.contains(finalK)) {
                                idToPkPairs.add(new ImmutablePair<>(queriedItem.getId(), UUID.randomUUID().toString()));
                            } else {
                                idToPkPairs.add(new ImmutablePair<>(queriedItem.getId(), queriedItem.getString("mypk")));
                            }
                        });
                }

                if (idToPkPairs.size() == feedRangeCount) {

                    List<CosmosItemIdentity> cosmosItemIdentities = idToPkPairs
                        .stream()
                        .map(pkToIdPair -> new CosmosItemIdentity(new PartitionKey(pkToIdPair.getRight()), pkToIdPair.getLeft()))
                        .collect(Collectors.toList());

                    FeedResponse<InternalObjectNode> readManyResult = readManyContainer
                        .readMany(cosmosItemIdentities, InternalObjectNode.class);

                    assertThat(readManyResult).isNotNull();
                    assertThat(readManyResult.getResults()).isNotNull();
                    assertThat(readManyResult.getResults().size()).isEqualTo(feedRangeCount - faultyIdCount);
                } else {
                    fail("Not all physical partitions have data!");
                }
            }

        } finally {
            readManyContainer.delete();
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readManyOptimizationRequestChargeComparisonForSingleTupleWithSmallSize() throws Exception {
        String idAndPkValue = UUID.randomUUID().toString();
        ObjectNode doc = getDocumentDefinition(idAndPkValue, idAndPkValue);
        container.createItem(doc);
        String query = String.format("SELECT * from c where c.id = '%s'", idAndPkValue);
        CosmosPagedIterable<ObjectNode> queryResult = container.queryItems(query, new CosmosQueryRequestOptions(), ObjectNode.class);
        FeedResponse<ObjectNode> readManyResult = container.readMany(Arrays.asList(new CosmosItemIdentity(new PartitionKey(idAndPkValue), idAndPkValue)), ObjectNode.class);

        AtomicReference<Double> queryRequestCharge = new AtomicReference<>(0d);
        double readManyRequestCharge = 0d;

        assertThat(queryResult).isNotNull();
        assertThat(queryResult.stream().count()).isEqualTo(1L);
        assertThat(readManyResult).isNotNull();
        assertThat(readManyResult.getRequestCharge()).isGreaterThan(0D);

        queryResult
                .iterableByPage(1)
                .forEach(feedResponse -> queryRequestCharge.updateAndGet(v -> v + feedResponse.getRequestCharge()));

        readManyRequestCharge += readManyResult.getRequestCharge();

        assertThat(readManyRequestCharge).isLessThan(queryRequestCharge.get());
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemWithDuplicateJsonProperties() throws Exception {
        String id = UUID.randomUUID().toString();
        String rawJson = String.format(
            "{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"property1\": \"5\", "
                + "\"property1\": \"7\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}",
            id,
            id);
        container.createItem(
            rawJson.getBytes(StandardCharsets.UTF_8),
            new PartitionKey(id),
            new CosmosItemRequestOptions());

        Utils.configureSimpleObjectMapper(true);
        try {
            CosmosPagedIterable<ObjectNode> pagedIterable = container.queryItems (
                "SELECT * FROM c WHERE c.id = '" + id + "'",
                new CosmosQueryRequestOptions(),
                ObjectNode.class);
            List<ObjectNode> items = pagedIterable.stream().collect(Collectors.toList());

            assertThat(items).hasSize(1);
            assertThat(items.get(0).get("property1").asText()).isEqualTo("7");
        } finally {
            Utils.configureSimpleObjectMapper(false);
            // remove the item with duplicate properties as it will break other tests after it
            container.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
        }
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItemWithSoftTimeoutAndFallback() throws Exception {
        String pk = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(id, pk);
        ObjectNode fallBackProperties = getDocumentDefinition("justFallback", "justFallback");
        container.createItem(properties);

        String successfulResponse = wrapWithSoftTimeoutAndFallback(
            container
                .asyncContainer
                .readItem(id,
                    new PartitionKey(pk),
                    new CosmosItemRequestOptions(),
                    ObjectNode.class),
            Duration.ofDays(3),
            fallBackProperties)
            .map(node -> node.get("id").asText())
            .block();

        assertThat(successfulResponse).isEqualTo(id);

        String timedOutResponse = wrapWithSoftTimeoutAndFallback(
            container
                .asyncContainer
                .readItem(id,
                    new PartitionKey(pk),
                    new CosmosItemRequestOptions(),
                    ObjectNode.class),
            Duration.ofNanos(10),
            fallBackProperties)
            .map(node -> node.get("id").asText())
            .block();

        assertThat(timedOutResponse).isEqualTo("justFallback");

        // Just ensure the logging of the soft timeout can finish
        Thread.sleep(1000);
    }

    static <T> Mono<T> wrapWithSoftTimeoutAndFallback(
        Mono<CosmosItemResponse<T>> source,
        Duration softTimeout,
        T fallback) {

        // Execute the readItem with transformation to return the json payload
        // asynchronously with a "soft timeout" - meaning when the "soft timeout"
        // elapses a default/fallback response is returned but the original async call is not
        // cancelled, but allowed to complete. This makes it possible to still emit diagnostics
        // or process the eventually successful call
        AtomicBoolean timeoutElapsed = new AtomicBoolean(false);
        return Mono
            .<T>create(sink -> {
                source
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                        response -> {
                            if (timeoutElapsed.get()) {
                                logger.warn(
                                    "COMPLETED SUCCESSFULLY after timeout elapsed. Diagnostics: {}",
                                    response.getDiagnostics().toString());
                            } else {
                                logger.info("COMPLETED SUCCESSFULLY");
                            }

                            sink.success(response.getItem());
                        },
                        error -> {
                            final Throwable unwrappedException = Exceptions.unwrap(error);
                            if (unwrappedException instanceof CosmosException) {
                                final CosmosException cosmosException = (CosmosException) unwrappedException;

                                logger.error(
                                    "COMPLETED WITH COSMOS FAILURE. Diagnostics: {}",
                                    cosmosException.getDiagnostics() != null ?
                                        cosmosException.getDiagnostics().toString() : "n/a",
                                    cosmosException);
                            } else {
                                logger.error("COMPLETED WITH GENERIC FAILURE", error);
                            }

                            if (timeoutElapsed.get()) {
                                // fallback returned already - don't emit unobserved error
                                sink.success();
                            } else {
                                sink.error(error);
                            }
                        }
                    );
            })
            .timeout(softTimeout)
            .onErrorResume(error -> {
                timeoutElapsed.set(true);
                return Mono.just(fallback);
            });
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readItemWithEventualConsistency() throws Exception {

        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        String idAndPkValue = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(idAndPkValue, idAndPkValue);
        CosmosItemResponse<ObjectNode> itemResponse = container.createItem(properties);

        CosmosItemResponse<ObjectNode> readResponse1 = container.readItem(
            idAndPkValue,
            new PartitionKey(idAndPkValue),
            new CosmosItemRequestOptions()
                // generate an invalid session token large enough to cause an error in Gateway
                // due to header being too long
                .setSessionToken(StringUtils.repeat("SomeManualInvalidSessionToken", 2000))
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL),
            ObjectNode.class);

        logger.info("REQUEST DIAGNOSTICS: {}", readResponse1.getDiagnostics().toString());
        validateIdOfItemResponse(idAndPkValue, readResponse1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceItem() throws Exception{
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        validateItemResponse(properties, itemResponse);
        String newPropValue = UUID.randomUUID().toString();
        BridgeInternal.setProperty(properties, "newProp", newPropValue);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        ModelBridgeInternal.setPartitionKey(options, new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")));
        // replace document
        CosmosItemResponse<InternalObjectNode> replace = container.replaceItem(properties,
                                                              properties.getId(),
                                                              new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                              options);
        assertThat(ModelBridgeInternal.getObjectFromJsonSerializable(BridgeInternal.getProperties(replace), "newProp")).isEqualTo(newPropValue);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItem() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(properties.getId(),
                                                                    new PartitionKey(ModelBridgeInternal.getObjectFromJsonSerializable(properties, "mypk")),
                                                                    options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteItemUsingEntity() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        CosmosItemResponse<?> deleteResponse = container.deleteItem(itemResponse.getItem(), options);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(204);
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItems() throws Exception {
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.readAllItems(cosmosQueryRequestOptions, InternalObjectNode.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItems() throws Exception{
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
                container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<InternalObjectNode> feedResponseIterator3 =
                container.queryItems(querySpec, cosmosQueryRequestOptions, InternalObjectNode.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void distinctQueryItems() throws Exception{

        for (int i = 0; i < 10; i++) {
            container.createItem(
                getDocumentDefinition(UUID.randomUUID().toString(), "somePartitionKey")
            );
        }

        String query = "SELECT DISTINCT c.mypk from c";
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<PartitionKeyWrapper> feedResponseIterator1 =
            container.queryItems(query, cosmosQueryRequestOptions, PartitionKeyWrapper.class);

        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
        long totalRecordCount = feedResponseIterator1.stream().count();
        assertThat(totalRecordCount == 1L);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemsWithCustomCorrelationActivityId() throws Exception{
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        UUID correlationId = UUID.randomUUID();
        ImplementationBridgeHelpers
            .CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor()
            .setCorrelationActivityId(cosmosQueryRequestOptions, correlationId);

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
            container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        feedResponseIterator1
            .iterableByPage()
            .forEach(response -> {
                assertThat(response.getCorrelationActivityId() == correlationId)
                    .withFailMessage("response.getCorrelationActivityId");
                assertThat(response.getCosmosDiagnostics().toString().contains(correlationId.toString()))
                    .withFailMessage("response.getCosmosDiagnostics");
            });
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemsWithEventualConsistency() throws Exception{

        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());

        String idAndPkValue = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(idAndPkValue, idAndPkValue);
        CosmosItemResponse<ObjectNode> itemResponse = container.createItem(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", idAndPkValue);
        CosmosQueryRequestOptions cosmosQueryRequestOptions =
            new CosmosQueryRequestOptions()
                // generate an invalid session token large enough to cause an error in Gateway
                // due to header being too long
                .setSessionToken(StringUtils.repeat("SomeManualInvalidSessionToken", 2000))
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL);

        CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
            container.queryItems(query, cosmosQueryRequestOptions, ObjectNode.class);
        feedResponseIterator1.handle(
            (r) -> logger.info("Query RequestDiagnostics: {}", r.getCosmosDiagnostics().toString()));

        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();
        assertThat(feedResponseIterator1.stream().count() == 1);

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<ObjectNode> feedResponseIterator3 =
            container.queryItems(querySpec, cosmosQueryRequestOptions, ObjectNode.class);
        feedResponseIterator3.handle(
            (r) -> logger.info("Query RequestDiagnostics: {}", r.getCosmosDiagnostics().toString()));
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
        assertThat(feedResponseIterator3.stream().count() == 1);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryItemsWithContinuationTokenAndPageSize() throws Exception{
        List<String> actualIds = new ArrayList<>();
        InternalObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.getId());
        properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.getId());
        properties = getDocumentDefinition(UUID.randomUUID().toString());
        container.createItem(properties);
        actualIds.add(properties.getId());


        String query = String.format("SELECT * from c where c.id in ('%s', '%s', '%s')", actualIds.get(0), actualIds.get(1), actualIds.get(2));
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
            container.queryItems(query, cosmosQueryRequestOptions, InternalObjectNode.class);

        do {
            Iterable<FeedResponse<InternalObjectNode>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<InternalObjectNode> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while(continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);

    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsOfLogicalPartition() throws Exception{
        String pkValue = UUID.randomUUID().toString();
        ObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        CosmosItemResponse<ObjectNode> itemResponse = container.createItem(properties);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<ObjectNode> feedResponseIterator1 =
            container.readAllItems(
                new PartitionKey(pkValue),
                cosmosQueryRequestOptions,
                ObjectNode.class);
        // Very basic validation
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        CosmosPagedIterable<ObjectNode> feedResponseIterator3 =
            container.readAllItems(
                new PartitionKey(pkValue),
                cosmosQueryRequestOptions,
                ObjectNode.class);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllItemsOfLogicalPartitionWithContinuationTokenAndPageSize() throws Exception{
        String pkValue = UUID.randomUUID().toString();
        List<String> actualIds = new ArrayList<>();
        ObjectNode properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        container.createItem(properties);

        properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        container.createItem(properties);

        properties = getDocumentDefinition(UUID.randomUUID().toString(), pkValue);
        container.createItem(properties);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        String continuationToken = null;
        int pageSize = 1;

        int initialDocumentCount = 3;
        int finalDocumentCount = 0;

        CosmosPagedIterable<InternalObjectNode> feedResponseIterator1 =
            container.readAllItems(
                new PartitionKey(pkValue),
                cosmosQueryRequestOptions,
                InternalObjectNode.class);

        do {
            Iterable<FeedResponse<InternalObjectNode>> feedResponseIterable =
                feedResponseIterator1.iterableByPage(continuationToken, pageSize);
            for (FeedResponse<InternalObjectNode> fr : feedResponseIterable) {
                int resultSize = fr.getResults().size();
                assertThat(resultSize).isEqualTo(pageSize);
                finalDocumentCount += fr.getResults().size();
                continuationToken = fr.getContinuationToken();
            }
        } while(continuationToken != null);

        assertThat(finalDocumentCount).isEqualTo(initialDocumentCount);
    }

    private InternalObjectNode getDocumentDefinition(String documentId) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                                                       + "\"id\": \"%s\", "
                                                       + "\"mypk\": \"%s\", "
                                                       + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                                                       + "}"
                , documentId, uuid));
        return properties;
    }

    private ObjectNode getDocumentDefinition(String documentId, String pkId) throws JsonProcessingException {

        String json = String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
            , documentId, pkId);
        return
            OBJECT_MAPPER.readValue(json, ObjectNode.class);
    }

    private void validateItemResponse(InternalObjectNode containerProperties,
                                      CosmosItemResponse<InternalObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(containerProperties.getId());
    }

    private void validateIdOfItemResponse(String expectedId, CosmosItemResponse<ObjectNode> createResponse) {
        // Basic validation
        assertThat(BridgeInternal.getProperties(createResponse).getId()).isNotNull();
        assertThat(BridgeInternal.getProperties(createResponse).getId())
            .as("check Resource Id")
            .isEqualTo(expectedId);
    }

    private static class PartitionKeyWrapper {
        private String mypk;

        public PartitionKeyWrapper() {
        }

        public String getMypk() {
            return mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }
    }

    private static class SampleType {
        private String id;
        private String val;
        private String mypk;

        public SampleType() {
        }

        SampleType(String id, String val, String mypk) {
            this.id = id;
            this.val = val;
            this.mypk = mypk;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMypk() {
            return this.mypk;
        }

        public void setMypk(String mypk) {
            this.mypk = mypk;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getVal() {
            return this.val;
        }
    }
}

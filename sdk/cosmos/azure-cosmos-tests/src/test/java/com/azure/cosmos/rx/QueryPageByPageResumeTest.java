// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mockito.Mockito;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Callers that resume a query from a continuation token and read one page per call, for example a stateless
 * "load more" API, must get every document exactly once and must not pay for pages they never read.
 * <p>
 * The container is provisioned with more than 10,000 RU/s, so on a service account it spans several physical
 * partitions and the queries cross partition boundaries and empty ranges. Scenarios that need more than one
 * physical partition are skipped when the account (for example the emulator) creates a single one.
 */
public class QueryPageByPageResumeTest extends TestSuiteBase {
    private static final int THROUGHPUT_SPANNING_PHYSICAL_PARTITIONS = 10_100;
    private static final int LOGICAL_PARTITIONS = 24;
    private static final int DOCUMENTS_PER_LOGICAL_PARTITION = 5;
    private static final Duration PAGE_TIMEOUT = Duration.ofSeconds(30);

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private final AtomicInteger documentQueries = new AtomicInteger();

    private List<FeedRange> feedRanges;
    private final Map<String, Integer> feedRangeIndexByPartitionKey = new HashMap<>();
    private final Map<String, List<String>> idsByPartitionKey = new HashMap<>();

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public QueryPageByPageResumeTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    /** How a caller reads exactly one page; each style cancels the query after the first page. */
    private enum OnePageRead {
        NEXT, TAKE_ONE
    }

    @DataProvider(name = "pageSizes")
    public Object[][] pageSizes() {
        return new Object[][] { { 1 }, { 7 }, { LOGICAL_PARTITIONS * DOCUMENTS_PER_LOGICAL_PARTITION } };
    }

    @Test(groups = { "query" }, dataProvider = "pageSizes", timeOut = 4 * TIMEOUT)
    public void crossPartitionQueryResumedPageByPageReturnsEveryDocumentOnce(int pageSize) {
        assertResumedPageByPageMatchesFullDrain(
            "SELECT * FROM c", new CosmosQueryRequestOptions(), pageSize, allIds(), OnePageRead.NEXT);
    }

    @Test(groups = { "query" }, timeOut = 4 * TIMEOUT)
    public void crossPartitionQueryResumedWithTakeOneReturnsEveryDocumentOnce() {
        assertResumedPageByPageMatchesFullDrain(
            "SELECT * FROM c", new CosmosQueryRequestOptions(), 7, allIds(), OnePageRead.TAKE_ONE);
    }

    @Test(groups = { "query" }, dataProvider = "pageSizes", timeOut = 4 * TIMEOUT)
    public void singlePartitionKeyQueryResumedPageByPageSendsOneBackendQueryPerPage(int pageSize) {
        String partitionKey = partitionKey(3);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setPartitionKey(new PartitionKey(partitionKey));

        int queriesBefore = documentQueries.get();
        List<FeedResponse<ObjectNode>> pages = assertResumedPageByPageMatchesFullDrain(
            "SELECT * FROM c", options, pageSize, new HashSet<>(idsByPartitionKey.get(partitionKey)), OnePageRead.NEXT);

        // The full drain inside the helper costs as many queries as the page-by-page read.
        assertThat(documentQueries.get() - queriesBefore).isEqualTo(2 * pages.size());
    }

    @Test(groups = { "query" }, dataProvider = "pageSizes", timeOut = 4 * TIMEOUT)
    public void queryWhoseMatchesEndBeforeTheLastRangeResumesThroughTrailingEmptyRanges(int pageSize) {
        requireMultiplePhysicalPartitions();
        List<String> partitionKeys = partitionKeysInFeedRange(0);
        assertResumedPageByPageMatchesFullDrain(
            queryForPartitionKeys(partitionKeys), new CosmosQueryRequestOptions(), pageSize, idsOf(partitionKeys), OnePageRead.NEXT);
    }

    @Test(groups = { "query" }, dataProvider = "pageSizes", timeOut = 4 * TIMEOUT)
    public void queryWhoseMatchesStartAfterTheFirstRangeResumesThroughLeadingEmptyRanges(int pageSize) {
        requireMultiplePhysicalPartitions();
        List<String> partitionKeys = partitionKeysInFeedRange(feedRanges.size() - 1);
        assertResumedPageByPageMatchesFullDrain(
            queryForPartitionKeys(partitionKeys), new CosmosQueryRequestOptions(), pageSize, idsOf(partitionKeys), OnePageRead.NEXT);
    }

    @Test(groups = { "query" }, timeOut = 4 * TIMEOUT)
    public void queryResumedExactlyAtAPartitionBoundaryReturnsEveryDocumentOnce() {
        requireMultiplePhysicalPartitions();
        int documentsInFirstRange = idsOf(partitionKeysInFeedRange(0)).size();
        assertResumedPageByPageMatchesFullDrain(
            "SELECT * FROM c", new CosmosQueryRequestOptions(), documentsInFirstRange, allIds(), OnePageRead.NEXT);
    }

    @Test(groups = { "query" }, timeOut = 4 * TIMEOUT)
    public void queryWithoutMatchesReturnsASingleEmptyPage() {
        List<FeedResponse<ObjectNode>> pages = assertResumedPageByPageMatchesFullDrain(
            "SELECT * FROM c WHERE c.tenant = 'no-such-tenant'", new CosmosQueryRequestOptions(), 7, new HashSet<>(),
            OnePageRead.NEXT);

        assertThat(pages).hasSize(1);
    }

    /**
     * Pages that are filtered out because they are empty still cost a backend request; their request statistics must
     * reach a returned page, including empty pages that follow the last document.
     */
    @Test(groups = { "query" }, timeOut = 4 * TIMEOUT)
    public void fullDrainReturnsTheRequestStatisticsOfEveryBackendQuery() {
        List<String> partitionKeys = feedRanges.size() > 1 ? partitionKeysInFeedRange(0) : partitionKeys(0, 2);
        String query = queryForPartitionKeys(partitionKeys);

        int queriesBefore = documentQueries.get();
        List<FeedResponse<ObjectNode>> pages = container
            .queryItems(query, new CosmosQueryRequestOptions(), ObjectNode.class)
            .byPage(3)
            .collectList()
            .block(PAGE_TIMEOUT);
        int backendQueries = documentQueries.get() - queriesBefore;

        Set<ClientSideRequestStatistics> requestStatistics = new HashSet<>();
        for (FeedResponse<ObjectNode> page : pages) {
            requestStatistics.addAll(requestStatisticsOf(page.getCosmosDiagnostics()));
        }
        assertThat(ids(pages)).containsExactlyInAnyOrderElementsOf(idsOf(partitionKeys));
        assertThat(requestStatistics).hasSize(backendQueries);
    }

    /**
     * Reads the query one page per call, each call resuming from the previous page's continuation token, and checks
     * it against a full drain of the same query: same documents in the same order, no duplicates, a continuation
     * token on every page but the last, and at most one extra backend query per physical partition boundary.
     */
    private List<FeedResponse<ObjectNode>> assertResumedPageByPageMatchesFullDrain(
        String query,
        CosmosQueryRequestOptions options,
        int pageSize,
        Set<String> expectedIds,
        OnePageRead onePageRead) {

        List<FeedResponse<ObjectNode>> pages = new ArrayList<>();
        String continuationToken = null;
        int queriesBefore = documentQueries.get();
        do {
            FeedResponse<ObjectNode> page = readOnePage(query, options, continuationToken, pageSize, onePageRead);
            assertThat(page).isNotNull();
            pages.add(page);
            continuationToken = page.getContinuationToken();
        } while (continuationToken != null && pages.size() <= expectedIds.size() + feedRanges.size());
        int pageByPageQueries = documentQueries.get() - queriesBefore;

        List<String> pageByPageIds = ids(pages);
        assertThat(continuationToken).as("continuation token of the last page").isNull();
        assertThat(pages.subList(0, pages.size() - 1))
            .allSatisfy(page -> assertThat(page.getContinuationToken()).isNotNull());
        assertThat(pageByPageIds).doesNotHaveDuplicates();
        assertThat(pageByPageIds).containsExactlyInAnyOrderElementsOf(expectedIds);
        assertThat(pageByPageIds).containsExactlyElementsOf(ids(fullDrain(query, options, pageSize)));
        assertThat(pageByPageQueries)
            .as("backend queries for %d pages across %d physical partitions", pages.size(), feedRanges.size())
            .isLessThanOrEqualTo(pages.size() + feedRanges.size() - 1);
        return pages;
    }

    private FeedResponse<ObjectNode> readOnePage(
        String query,
        CosmosQueryRequestOptions options,
        String continuationToken,
        int pageSize,
        OnePageRead onePageRead) {

        Flux<FeedResponse<ObjectNode>> pages =
            container.queryItems(query, options, ObjectNode.class).byPage(continuationToken, pageSize);
        Mono<FeedResponse<ObjectNode>> onePage = onePageRead == OnePageRead.NEXT ? pages.next() : pages.take(1, true).single();
        return onePage.block(PAGE_TIMEOUT);
    }

    private List<FeedResponse<ObjectNode>> fullDrain(String query, CosmosQueryRequestOptions options, int pageSize) {
        return container.queryItems(query, options, ObjectNode.class).byPage(pageSize).collectList().block(PAGE_TIMEOUT);
    }

    @BeforeClass(groups = { "query" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_QueryPageByPageResumeTest() {
        client = getClientBuilder().buildAsyncClient();
        database = createTestDatabase(client, "page-by-page-resume");
        container = createCollection(
            database,
            new CosmosContainerProperties("page-by-page-resume", "/mypk"),
            new CosmosContainerRequestOptions(),
            THROUGHPUT_SPANNING_PHYSICAL_PARTITIONS);

        List<ObjectNode> documents = new ArrayList<>();
        for (int partition = 0; partition < LOGICAL_PARTITIONS; partition++) {
            String partitionKey = partitionKey(partition);
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < DOCUMENTS_PER_LOGICAL_PARTITION; i++) {
                String id = partitionKey + "-" + i;
                ids.add(id);
                documents.add(JsonNodeFactory.instance.objectNode().put("id", id).put("mypk", partitionKey).put("tenant", partitionKey));
            }
            idsByPartitionKey.put(partitionKey, ids);
        }
        insertAllItemsBlocking(container, documents, true);

        feedRanges = container.getFeedRanges().block();
        for (int index = 0; index < feedRanges.size(); index++) {
            List<String> partitionKeysInRange = container
                .queryItems("SELECT DISTINCT VALUE c.mypk FROM c", new CosmosQueryRequestOptions().setFeedRange(feedRanges.get(index)), String.class)
                .collectList()
                .block();
            for (String partitionKey : partitionKeysInRange) {
                feedRangeIndexByPartitionKey.put(partitionKey, index);
            }
        }

        countDocumentQueriesSentByStoreModels();
    }

    @AfterClass(groups = { "query" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    /**
     * Counts document queries at the store model, so the count is the same for gateway, thin client and direct mode
     * and excludes query plan requests.
     */
    private void countDocumentQueriesSentByStoreModels() {
        RxDocumentClientImpl documentClient = (RxDocumentClientImpl) CosmosBridgeInternal.getAsyncDocumentClient(client);
        ReflectionUtils.setServerStoreModel(documentClient, countingSpy(ReflectionUtils.getRxServerStoreModel(documentClient)));
        ReflectionUtils.setGatewayProxy(documentClient, countingSpy(ReflectionUtils.getGatewayProxy(documentClient)));
        RxStoreModel thinProxy = ReflectionUtils.getThinProxy(documentClient);
        if (thinProxy != null) {
            ReflectionUtils.setThinProxy(documentClient, countingSpy(thinProxy));
        }
    }

    private RxStoreModel countingSpy(RxStoreModel storeModel) {
        RxStoreModel spy = Mockito.spy(storeModel);
        doAnswer(invocation -> {
            RxDocumentServiceRequest request = invocation.getArgument(0);
            if (request.getOperationType() == OperationType.Query && request.getResourceType() == ResourceType.Document) {
                documentQueries.incrementAndGet();
            }
            return invocation.callRealMethod();
        }).when(spy).processMessage(any(RxDocumentServiceRequest.class));
        return spy;
    }

    private void requireMultiplePhysicalPartitions() {
        if (feedRanges.size() < 2) {
            throw new SkipException("The account created a single physical partition");
        }
    }

    private static String partitionKey(int index) {
        return "pk-" + index;
    }

    private static List<String> partitionKeys(int fromInclusive, int toExclusive) {
        List<String> partitionKeys = new ArrayList<>();
        for (int index = fromInclusive; index < toExclusive; index++) {
            partitionKeys.add(partitionKey(index));
        }
        return partitionKeys;
    }

    private List<String> partitionKeysInFeedRange(int feedRangeIndex) {
        return feedRangeIndexByPartitionKey.entrySet().stream()
            .filter(entry -> entry.getValue() == feedRangeIndex)
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(TreeSet::new))
            .stream()
            .limit(3)
            .collect(Collectors.toList());
    }

    /**
     * Filters on {@code tenant}, a copy of the partition key that is not the partition key path, so the query fans
     * out to every physical partition, including those without matching documents.
     */
    private static String queryForPartitionKeys(List<String> partitionKeys) {
        return partitionKeys.stream()
            .map(partitionKey -> "'" + partitionKey + "'")
            .collect(Collectors.joining(", ", "SELECT * FROM c WHERE c.tenant IN (", ")"));
    }

    private Set<String> allIds() {
        return idsByPartitionKey.values().stream().flatMap(List::stream).collect(Collectors.toSet());
    }

    private Set<String> idsOf(List<String> partitionKeys) {
        return partitionKeys.stream().flatMap(partitionKey -> idsByPartitionKey.get(partitionKey).stream())
            .collect(Collectors.toSet());
    }

    private static List<String> ids(List<FeedResponse<ObjectNode>> pages) {
        return pages.stream()
            .flatMap(page -> page.getResults().stream())
            .map(item -> item.get("id").asText())
            .collect(Collectors.toList());
    }

    private static Collection<ClientSideRequestStatistics> requestStatisticsOf(CosmosDiagnostics diagnostics) {
        return ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor()
            .getClientSideRequestStatistics(diagnostics);
    }
}

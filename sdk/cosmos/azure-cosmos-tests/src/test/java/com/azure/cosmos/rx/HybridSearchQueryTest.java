// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ResourceId;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.query.HybridSearchBadRequestException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosFullTextIndex;
import com.azure.cosmos.models.CosmosFullTextPath;
import com.azure.cosmos.models.CosmosFullTextPolicy;
import com.azure.cosmos.models.CosmosFullTextScoreScope;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.rx.TestSuiteBase.createTestDatabase;
import static com.azure.cosmos.rx.TestSuiteBase.createCollection;
import static com.azure.cosmos.rx.TestSuiteBase.safeClose;
import static com.azure.cosmos.rx.TestSuiteBase.safeDeleteDatabase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Test(singleThreaded = true)
public class HybridSearchQueryTest {
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 80000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    protected static Logger logger = LoggerFactory.getLogger(HybridSearchQueryTest.class);

    private static final List<String> JOHN_AND_UNITED_IDS =
        Arrays.asList("2", "22", "24", "25", "49", "51", "54", "57", "61", "66", "75", "76", "77", "80", "85");
    private static final String RANKING_PROPERTY = "COSMOS.HYBRID_SEARCH_USE_COMPETITION_RANKING";
    private static final int RANKING_DOCUMENT_COUNT = 260;
    private final String containerId = UUID.randomUUID().toString();
    private final String hpkContainerId = UUID.randomUUID().toString();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncContainer hpkContainer;
    private final Map<String, Long> documentRids = new HashMap<>();
    private List<String> titleJohnOrder;
    private List<String> textJohnOrder;
    private List<String> textUnitedOrder;
    private List<String> vectorOrder;
    private CosmosAsyncContainer rankingContainer;
    private CosmosAsyncContainer rankingTierContainer;
    private List<ObjectNode> rankingDocumentsByRid;

    @BeforeClass(groups = {"query", "split"}, timeOut = SETUP_TIMEOUT* 10)
    public void before_HybridSearchQueryTest() {
        //set up the client
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        database = createTestDatabase(client, "hybrid-search");

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/pk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, partitionKeyDef);
        containerProperties.setIndexingPolicy(populateIndexingPolicy());
        containerProperties.setFullTextPolicy(populateFullTextPolicy());
        container = createCollection(
            database,
            containerProperties,
            new CosmosContainerRequestOptions(),
            10000);

        // Insert documents with pk field based on (index % 2) + 1, so even ids → pk="1", odd ids → pk="2"
        List<Document> documents = loadProductsFromJson();
        for (Document doc : documents) {
            int index = Integer.parseInt(doc.getId());
            doc.pk = String.valueOf((index % 2) + 1);
            container.createItem(doc).block();
        }

        // Create a hierarchical partition key container (/pk, /category) for HPK tests
        PartitionKeyDefinition hpkDef = new PartitionKeyDefinition();
        ArrayList<String> hpkPaths = new ArrayList<String>();
        hpkPaths.add("/pk");
        hpkPaths.add("/category");
        hpkDef.setPaths(hpkPaths);
        hpkDef.setKind(PartitionKind.MULTI_HASH);
        hpkDef.setVersion(PartitionKeyDefinitionVersion.V2);

        CosmosContainerProperties hpkContainerProperties = new CosmosContainerProperties(hpkContainerId, hpkDef);
        hpkContainerProperties.setIndexingPolicy(populateIndexingPolicy());
        hpkContainerProperties.setFullTextPolicy(populateFullTextPolicy());
        hpkContainer = createCollection(
            database,
            hpkContainerProperties,
            new CosmosContainerRequestOptions(),
            10000);

        // Insert documents with pk and category fields for hierarchical partition key
        // pk = (index % 2) + 1, category = "A" for index % 3 == 0, "B" otherwise
        for (Document doc : documents) {
            int index = Integer.parseInt(doc.getId());
            doc.pk = String.valueOf((index % 2) + 1);
            doc.category = (index % 3 == 0) ? "A" : "B";
            hpkContainer.createItem(doc).block();
        }

        List<Document> storedDocuments = container.queryItems("SELECT c.id, c._rid FROM c",
            new CosmosQueryRequestOptions(), Document.class).collectList().block();
        assertThat(storedDocuments).hasSize(100);
        for (Document doc : storedDocuments) {
            documentRids.put(doc.id, ResourceId.parse(doc.rid).getDocument());
        }

        // Single-component queries bypass client-side RRF. Keep their service score ordering,
        // but explicitly order the fixture's zero-score ties by numeric RID for ordinal fusion.
        titleJohnOrder = fullTextOrder("title", "John", Collections.singletonList("2"));
        textJohnOrder = fullTextOrder("text", "John", Arrays.asList("2", "57", "85"));
        textUnitedOrder = fullTextOrder("text", "United", Arrays.asList("22", "24", "25", "49", "51", "54", "61", "66", "75", "76", "77", "80"));
        SqlQuerySpec vectorQuery = new SqlQuerySpec(
            "SELECT TOP 100 c.id, VectorDistance(c.vector, @vector) AS score FROM c "
                + "ORDER BY VectorDistance(c.vector, @vector)",
            Collections.singletonList(new SqlParameter("@vector", getQueryVector())));
        List<Document> vectorDocuments = container.queryItems(vectorQuery,
            new CosmosQueryRequestOptions(), Document.class).collectList().block();
        assertThat(vectorDocuments).hasSize(100);
        // The default cosine VectorDistance projects similarity, not a lower-is-better distance.
        assertThat(vectorDocuments).extracting(document -> document.score)
            .isSortedAccordingTo(Collections.reverseOrder());
        vectorDocuments.sort((left, right) -> {
            int comparison = Double.compare(right.score, left.score);
            return comparison != 0 ? comparison : Long.compareUnsigned(documentRids.get(right.id), documentRids.get(left.id));
        });
        vectorOrder = vectorDocuments.stream().map(Document::getId).collect(Collectors.toList());
        createRankingFixtures();
    }

    @AfterClass(groups = {"query", "split"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(database);
        safeClose(client);
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryTest() {

        String query = "SELECT TOP 10 c.id, c.text, c.title FROM c WHERE FullTextContains(c.text, @term1) OR " +
            "FullTextContains(c.title, @term1) ORDER BY RANK FullTextScore(c.title, @term1)";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));
        List<Document> resultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(resultDocs).hasSize(3);
        validateTitleJohnResults(resultDocs, true);

        query = "SELECT c.id, c.title FROM c WHERE FullTextContains(c.title, @term1) " +
            "OR FullTextContains(c.text, @term1) ORDER BY RANK FullTextScore(c.title, @term1) OFFSET 1 LIMIT 5";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));
        resultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(resultDocs).hasSize(2);
        validateTitleJohnResults(resultDocs, false);

        query = "SELECT TOP 20 c.id, c.title FROM c WHERE FullTextContains(c.title, @term1) OR " +
            "FullTextContains(c.text, @term1) OR FullTextContains(c.text, @term2) " +
            "ORDER BY RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2))";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));
        resultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(resultDocs).hasSize(15);
        List<String> expected = expectedRrfOrder(JOHN_AND_UNITED_IDS, new double[] {1, 1},
            titleJohnOrder, textUnitedOrder);
        validateResults(expected, resultDocs);

        query = "SELECT c.id, c.title FROM c WHERE FullTextContains(c.title, @term1) " +
            "OR FullTextContains(c.text, @term1) OR FullTextContains(c.text, @term2) ORDER BY " +
            "RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2)) OFFSET 5 LIMIT 10";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));
        resultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(resultDocs).hasSize(10);
        validateResults(expected.subList(5, 15), resultDocs);

        List<Float> vector = getQueryVector();
        query = "SELECT TOP 10 c.id, c.text, c.title FROM c " +
            "ORDER BY RANK RRF(FullTextScore(c.text, @term1), FullTextScore(c.text, @term2), " +
            "VectorDistance(c.vector, @vector))";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United"),
            new SqlParameter("@vector", vector)
        ));
        resultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(resultDocs).hasSize(10);
        validateResults(expectedRrfOrder(new ArrayList<>(documentRids.keySet()), new double[] {1, 1, 1},
            textJohnOrder, textUnitedOrder, vectorOrder).subList(0, 10), resultDocs);
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryWeightedRRFTest(){
        // test case 1
        String query = "SELECT TOP 15 c.id, c.text, c.title FROM c " +
            "WHERE FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) OR FullTextContains(c.text, @term2)" +
            "ORDER BY RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2), [1, 1])";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));
        List<HybridSearchQueryTest.Document> results = container.queryItems(querySpec, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(results).hasSize(15);
        assertThat(results).isNotNull();

        validateResults(
            expectedRrfOrder(JOHN_AND_UNITED_IDS, new double[] {1, 1}, titleJohnOrder, textUnitedOrder),
            results
        );


        // test case 2
        query = "SELECT TOP 15 c.id, c.text, c.title AS Text FROM c " +
            "WHERE FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) OR FullTextContains(c.text, @term2)" +
            "ORDER BY RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2), [10, 10])";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));
        results = container.queryItems(querySpec, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(results).hasSize(15);
        assertThat(results).isNotNull();
        validateResults(
            expectedRrfOrder(JOHN_AND_UNITED_IDS, new double[] {10, 10}, titleJohnOrder, textUnitedOrder),
            results
        );

        // test case 3
        query = "SELECT TOP 10 c.id, c.text, c.title FROM c " +
            "WHERE FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) OR FullTextContains(c.text, @term2)" +
            "ORDER BY RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2), [0.1, 0.1])";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));
        results = container.queryItems(querySpec, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(results).hasSize(10);
        assertThat(results).isNotNull();
        validateResults(
            expectedRrfOrder(JOHN_AND_UNITED_IDS, new double[] {0.1, 0.1},
                titleJohnOrder, textUnitedOrder).subList(0, 10),
            results
        );

        // test case 4
        query = "SELECT TOP 10 c.id, c.text, c.title FROM c " +
            "WHERE FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) OR FullTextContains(c.text, @term2)" +
            "ORDER BY RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2), [-1, -1])";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));
        results = container.queryItems(querySpec, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(results).hasSize(10);
        assertThat(results).isNotNull();
        validateResults(
            expectedRrfOrder(JOHN_AND_UNITED_IDS, new double[] {-1, -1},
                titleJohnOrder, textUnitedOrder).subList(0, 10),
            results
        );

        // test case 5
        List<Float> vector = getQueryVector();
        query = "SELECT c.id, c.title FROM c " +
            "ORDER BY RANK RRF(FullTextScore(c.text, @term2), VectorDistance(c.vector, @vector), [1,1]) " +
            "OFFSET 0 LIMIT 10";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United"),
            new SqlParameter("@vector", vector)
        ));
        results = container.queryItems(querySpec, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(results).hasSize(10);
        assertThat(results).isNotNull();
        validateResults(
            expectedRrfOrder(new ArrayList<>(documentRids.keySet()), new double[] {1, 1},
                textUnitedOrder, vectorOrder).subList(0, 10),
            results
        );
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryWithLocalStatisticsTest() {
        // Documents with 'John': id=2 (pk="1"), id=57 (pk="2"), id=85 (pk="2")

        String query = "SELECT TOP 10 c.id, c.title, c.pk FROM c WHERE FullTextContains(c.title, @term1) OR " +
            "FullTextContains(c.text, @term1) ORDER BY RANK FullTextScore(c.title, @term1)";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));

        // Test 1: GLOBAL scope (default) cross-partition — should return all 3 'John' matches
        List<Document> globalResultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(globalResultDocs).isNotNull();
        assertThat(globalResultDocs).hasSize(3);

        // Test 2: Explicit GLOBAL scope cross-partition — same as default
        CosmosQueryRequestOptions globalScopeOptions = new CosmosQueryRequestOptions();
        globalScopeOptions.setFullTextScoreScope(CosmosFullTextScoreScope.GLOBAL);

        List<Document> explicitGlobalResultDocs = container.queryItems(querySpec, globalScopeOptions, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(explicitGlobalResultDocs).isNotNull();
        assertThat(explicitGlobalResultDocs).hasSize(3);

        validateTitleJohnResults(globalResultDocs, true);
        validateTitleJohnResults(explicitGlobalResultDocs, true);

        // Test 3: LOCAL scope with pk="2" — only id=57 and id=85 have 'John' in pk="2"
        // Stats are computed only over pk="2" partition
        CosmosQueryRequestOptions localScopeOptionsPk2 = new CosmosQueryRequestOptions();
        localScopeOptionsPk2.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localScopeOptionsPk2.setPartitionKey(new PartitionKey("2"));

        List<Document> localPk2ResultDocs = container.queryItems(querySpec, localScopeOptionsPk2, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(localPk2ResultDocs).isNotNull();
        assertThat(localPk2ResultDocs).hasSize(2);
        for (Document doc : localPk2ResultDocs) {
            assertThat(doc.getPk()).isEqualTo("2");
        }
        validateTitleJohnResults(localPk2ResultDocs, false);

        // Test 4: LOCAL scope with pk="1" — only id=2 has 'John' in pk="1"
        // Stats are computed only over pk="1" partition
        CosmosQueryRequestOptions localScopeOptionsPk1 = new CosmosQueryRequestOptions();
        localScopeOptionsPk1.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localScopeOptionsPk1.setPartitionKey(new PartitionKey("1"));

        List<Document> localPk1ResultDocs = container.queryItems(querySpec, localScopeOptionsPk1, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(localPk1ResultDocs).isNotNull();
        assertThat(localPk1ResultDocs).hasSize(1);
        assertThat(localPk1ResultDocs.get(0).getId()).isEqualTo("2");
        assertThat(localPk1ResultDocs.get(0).getPk()).isEqualTo("1");
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryRRFWithLocalStatisticsTest() {
        // Test LOCAL scope with RRF (multiple component queries)
        String query = "SELECT TOP 10 c.id, c.title, c.pk FROM c WHERE " +
            "FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) OR " +
            "FullTextContains(c.text, @term2) " +
            "ORDER BY RANK RRF(FullTextScore(c.title, @term1), FullTextScore(c.text, @term2))";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John"),
            new SqlParameter("@term2", "United")
        ));

        // GLOBAL scope (default) cross-partition
        List<Document> globalResultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(globalResultDocs).isNotNull();
        assertThat(globalResultDocs).isNotEmpty();

        // LOCAL scope with pk="2"
        CosmosQueryRequestOptions localScopeOptions = new CosmosQueryRequestOptions();
        localScopeOptions.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localScopeOptions.setPartitionKey(new PartitionKey("2"));

        List<Document> localResultDocs = container.queryItems(querySpec, localScopeOptions, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(localResultDocs).isNotNull();
        assertThat(localResultDocs).isNotEmpty();
        for (Document doc : localResultDocs) {
            assertThat(doc.getPk()).isEqualTo("2");
        }
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryWithLocalScopeWithoutPartitionKeyTest() {
        // LOCAL scope without a partition key filter should degenerate gracefully to GLOBAL behavior,
        // since targetFeedRanges equals allFeedRanges when no partition key is specified.
        CosmosQueryRequestOptions localScopeOptions = new CosmosQueryRequestOptions();
        localScopeOptions.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);

        String query = "SELECT TOP 10 c.id, c.title FROM c WHERE FullTextContains(c.title, @term1) OR " +
            "FullTextContains(c.text, @term1) ORDER BY RANK FullTextScore(c.title, @term1)";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));

        List<Document> localNoPkResults = container.queryItems(querySpec, localScopeOptions, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(localNoPkResults).isNotNull();
        assertThat(localNoPkResults).hasSize(3);

        // Should produce the same results as default/GLOBAL since all partitions are targeted
        List<Document> globalResults = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(globalResults).isNotNull();

        List<String> localIds = localNoPkResults.stream().map(Document::getId).collect(Collectors.toList());
        List<String> globalIds = globalResults.stream().map(Document::getId).collect(Collectors.toList());
        assertThat(localIds).isEqualTo(globalIds);
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryWithLocalScopeAndHierarchicalPartitionKeyTest() {
        // HPK container: /pk + /category
        // Documents with 'John': id=2 (pk="1", cat="B"), id=57 (pk="2", cat="A"), id=85 (pk="2", cat="B")

        String query = "SELECT TOP 10 c.id, c.title, c.pk, c.category FROM c " +
            "WHERE FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) " +
            "ORDER BY RANK FullTextScore(c.title, @term1)";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));

        // LOCAL scope with full HPK (pk="2", category="A") — only id=57 matches
        CosmosQueryRequestOptions localFullHpk = new CosmosQueryRequestOptions();
        localFullHpk.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localFullHpk.setPartitionKey(
            new PartitionKeyBuilder().add("2").add("A").build());

        List<Document> fullHpkResults = hpkContainer.queryItems(querySpec, localFullHpk, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(fullHpkResults).isNotNull();
        assertThat(fullHpkResults).hasSize(1);
        assertThat(fullHpkResults.get(0).getId()).isEqualTo("57");
        assertThat(fullHpkResults.get(0).getPk()).isEqualTo("2");
        assertThat(fullHpkResults.get(0).getCategory()).isEqualTo("A");

        // LOCAL scope with partial HPK prefix (pk="2") — id=57 and id=85 match
        CosmosQueryRequestOptions localPartialHpk = new CosmosQueryRequestOptions();
        localPartialHpk.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localPartialHpk.setPartitionKey(
            new PartitionKeyBuilder().add("2").build());

        List<Document> partialHpkResults = hpkContainer.queryItems(querySpec, localPartialHpk, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(partialHpkResults).isNotNull();
        assertThat(partialHpkResults).hasSize(2);
        for (Document doc : partialHpkResults) {
            assertThat(doc.getPk()).isEqualTo("2");
        }

        // GLOBAL scope cross-partition — all 3 'John' matches
        List<Document> globalResults = hpkContainer.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(globalResults).isNotNull();
        assertThat(globalResults).hasSize(3);
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void hybridQueryWithLocalScopeAndFeedRangeTest() {
        // Test LOCAL scope with FeedRange.forLogicalPartition() instead of setPartitionKey()
        String query = "SELECT TOP 10 c.id, c.title, c.pk FROM c " +
            "WHERE FullTextContains(c.title, @term1) OR FullTextContains(c.text, @term1) " +
            "ORDER BY RANK FullTextScore(c.title, @term1)";
        SqlQuerySpec querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));

        // LOCAL scope with FeedRange for pk="2" — only id=57 and id=85 match
        CosmosQueryRequestOptions localFeedRangeOptions = new CosmosQueryRequestOptions();
        localFeedRangeOptions.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localFeedRangeOptions.setFeedRange(FeedRange.forLogicalPartition(new PartitionKey("2")));

        List<Document> feedRangeResults = container.queryItems(querySpec, localFeedRangeOptions, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(feedRangeResults).isNotNull();
        assertThat(feedRangeResults).hasSize(2);
        for (Document doc : feedRangeResults) {
            assertThat(doc.getPk()).isEqualTo("2");
        }
        validateTitleJohnResults(feedRangeResults, false);

        // LOCAL scope with FeedRange for pk="1" — only id=2 matches
        CosmosQueryRequestOptions localFeedRangePk1 = new CosmosQueryRequestOptions();
        localFeedRangePk1.setFullTextScoreScope(CosmosFullTextScoreScope.LOCAL);
        localFeedRangePk1.setFeedRange(FeedRange.forLogicalPartition(new PartitionKey("1")));

        List<Document> feedRangePk1Results = container.queryItems(querySpec, localFeedRangePk1, Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(feedRangePk1Results).isNotNull();
        assertThat(feedRangePk1Results).hasSize(1);
        assertThat(feedRangePk1Results.get(0).getId()).isEqualTo("2");
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void wrongHybridQueryTest() {
        String query = "";
        try {
            query = "SELECT c.id, RRF(VectorDistance(c.vector, [1,2,3]), FullTextScore(c.text, “test”) FROM c";
            container.queryItems(query, new CosmosQueryRequestOptions(), Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("Attempting to project RRF in a query should fail.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("Syntax error, invalid token");
        }

        try {
            query = "SELECT TOP 10 c.id FROM c WHERE FullTextContains(c.title, 'John') ORDER BY RANK FullTextScore(c.title, 'John') DESC";
            container.queryItems(query, new CosmosQueryRequestOptions(), Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("Attempting to set an ordering direction in a full text score query should fail.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("Specifying a sort order (ASC or DESC) in the ORDER BY RANK clause is not allowed.");
        }

        try {
            query = "SELECT TOP 10 c.id FROM c WHERE FullTextContains(c.title, 'John') ORDER BY RANK RRF(FullTextScore(c.title, 'John'), VectorDistance(c.vector, [1,2,3])) DESC";
            container.queryItems(query, new CosmosQueryRequestOptions(), Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("Attempting to set an ordering direction in a hybrid search query should fail.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("Specifying a sort order (ASC or DESC) in the ORDER BY RANK clause is not allowed.");
        }

        try {
            query = "SELECT c.id FROM c WHERE FullTextContains(c.title, 'John') ORDER BY RANK RRF(FullTextScore(c.title, 'John'), VectorDistance(c.vector, [1,2,3]))";
            container.queryItems(query, new CosmosQueryRequestOptions(), Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("Attempting to run a hybrid search query without a top(Take) value should fail.");
        } catch (HybridSearchBadRequestException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("Executing a hybrid or full text query without Top or Limit can consume a large number of RUs " +
                "very fast and have long runtimes.");
        }

        try {
            query = "SELECT c.id FROM c WHERE FullTextContains(c.title, 'John') ORDER BY RANK RRF(FullTextScore(c.title, 'John'), VectorDistance(c.vector, [1,2,3])) OFFSET 10 LIMIT 5";
            container.queryItems(query, new CosmosQueryRequestOptions(), Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("Attempting to run a hybrid search query with a limit(Take) value smaller than the offset(Limit) value.");
        } catch (HybridSearchBadRequestException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("Executing a hybrid or full-text query with an offset(Skip) greater than or equal to limit(Take).");
        }

        try {
            query = "SELECT c.id FROM c WHERE FullTextContains(c.title, 'John') ORDER BY RANK RRF(FullTextScore(c.title, 'John'), VectorDistance(c.vector, [1,2,3])) OFFSET 10 LIMIT 10";
            container.queryItems(query, new CosmosQueryRequestOptions(), Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("Attempting to run a hybrid search query with a limit(Take) value equal to the offset(Limit) value.");
        } catch (HybridSearchBadRequestException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("Executing a hybrid or full-text query with an offset(Skip) greater than or equal to limit(Take).");
        }

        try {
            query = "SELECT TOP 15 c.id, c.text, c.title FROM c " +
                "WHERE FullTextContains(c.title, 'John') OR FullTextContains(c.text, 'John') OR FullTextContains(c.text, 'United')" +
                "ORDER BY RANK RRF(FullTextScore(c.title, 'John'), FullTextScore(c.text, 'United'), [1, 1, 1])";
            container.queryItems(query, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("The last parameter of the RRF function is an optional array of weights. When present, " +
                "it must be a literal array of numbers, one for each of the component scores used for the RRF function. " +
                "The length of this array must be the same as the number of the component scores.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("The length of this array must be the same as the number of the component scores.");
        }

        try {
            query = "SELECT TOP 15 c.id, c.text, c.title FROM c " +
                "WHERE FullTextContains(c.title, 'John') OR FullTextContains(c.text, 'John') OR FullTextContains(c.text, 'United')" +
                "ORDER BY RANK RRF(FullTextScore(c.title, 'John'), FullTextScore(c.text, 'United'), [1])";
            container.queryItems(query, new CosmosQueryRequestOptions(), HybridSearchQueryTest.Document.class).byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
                .collectList().block();
            fail("The last parameter of the RRF function is an optional array of weights. When present, " +
                "it must be a literal array of numbers, one for each of the component scores used for the RRF function. " +
                "The length of this array must be the same as the number of the component scores.");
        } catch (CosmosException ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpResponseStatus.BAD_REQUEST.code());
            assertThat(ex.getMessage()).contains("The length of this array must be the same as the number of the component scores.");
        }

    }

    private void createRankingFixtures() {
        CosmosFullTextIndex titleIndex = new CosmosFullTextIndex();
        titleIndex.setPath("/title");
        CosmosFullTextIndex textIndex = new CosmosFullTextIndex();
        textIndex.setPath("/text");
        IndexingPolicy indexing = new IndexingPolicy();
        indexing.setCosmosFullTextIndexes(Arrays.asList(titleIndex, textIndex));
        CosmosFullTextPath titlePath = new CosmosFullTextPath();
        titlePath.setPath("/title");
        titlePath.setLanguage("en-US");
        CosmosFullTextPath textPath = new CosmosFullTextPath();
        textPath.setPath("/text");
        textPath.setLanguage("en-US");
        CosmosFullTextPolicy fullText = new CosmosFullTextPolicy();
        fullText.setDefaultLanguage("en-US");
        fullText.setPaths(Arrays.asList(titlePath, textPath));
        CosmosContainerProperties properties = new CosmosContainerProperties("ranking-ties", "/pk");
        properties.setIndexingPolicy(indexing);
        properties.setFullTextPolicy(fullText);
        rankingContainer = createCollection(database, properties, new CosmosContainerRequestOptions(), 10000);
        CosmosContainerProperties tierProperties = new CosmosContainerProperties("ranking-tiers", "/pk");
        tierProperties.setIndexingPolicy(indexing);
        tierProperties.setFullTextPolicy(fullText);
        rankingTierContainer = createCollection(database, tierProperties, new CosmosContainerRequestOptions(), 400);
        String[] tierTexts = {"beta filler filler filler filler", "beta beta filler filler filler",
            "beta beta beta filler filler", "beta beta beta beta filler", "beta beta beta beta beta"};
        for (int i = 0; i < 5; i++) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.put("id", "tier-" + i);
            document.put("pk", "tiers");
            document.put("title", i == 0 ? "alpha alpha" : i == 4 ? "filler filler" : "alpha filler");
            document.put("text", tierTexts[i]);
            rankingTierContainer.createItem(document).block();
        }

        // Identical fields guarantee tied component scores without reproducing the service's BM25 implementation.
        // More than 256 inserts also exercise RID byte boundaries rather than assuming encoded RID order.
        for (int i = 0; i < RANKING_DOCUMENT_COUNT; i++) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.put("id", "doc-" + i);
            document.put("pk", Integer.toString(i % 2));
            document.put("title", "alpha");
            document.put("text", "beta");
            rankingContainer.createItem(document).block();
        }
        rankingDocumentsByRid = rankingContainer.queryItems("SELECT * FROM c",
            new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
        assertThat(rankingDocumentsByRid).hasSize(RANKING_DOCUMENT_COUNT);
        rankingDocumentsByRid.sort((left, right) -> Long.compareUnsigned(documentRid(right), documentRid(left)));
        List<ObjectNode> encodedOrder = new ArrayList<>(rankingDocumentsByRid);
        encodedOrder.sort((left, right) -> right.get("_rid").asText().compareTo(left.get("_rid").asText()));
        assertThat(rankingIds(encodedOrder)).as("Fixture must distinguish numeric from encoded RID ordering")
            .isNotEqualTo(rankingIds(rankingDocumentsByRid));
    }

    @DataProvider(name = "rankingModes")
    public Object[][] rankingModes() {
        return new Object[][] {{false}, {true}};
    }

    @Test(groups = {"query", "split"}, dataProvider = "rankingModes", timeOut = 60000)
    public void tiedComponentsRespectRankingModeAndDirection(boolean competition) {
        String previous = System.getProperty(RANKING_PROPERTY);
        try {
            System.setProperty(RANKING_PROPERTY, Boolean.toString(competition));
            for (double[] weights : new double[][] {{1, 1}, {-1, -1}, {1, -1}, {2, -1}}) {
                List<String> expected = expectedTiedRankingOrder(weights, competition);
                String orderBy = rankingOrderBy(weights);
                List<FeedResponse<ObjectNode>> pages = rankingContainer.queryItems(
                    "SELECT TOP 260 * FROM c " + orderBy, new CosmosQueryRequestOptions(), ObjectNode.class)
                    .byPage(17).collectList().block();
                List<ObjectNode> actual = pages.stream().flatMap(page -> page.getResults().stream())
                    .collect(Collectors.toList());
                assertThat(rankingIds(actual)).as("weights=%s, competition=%s", Arrays.toString(weights), competition)
                    .containsExactlyElementsOf(expected);
                assertThat(pages).hasSizeGreaterThan(1);
                assertThat(actual).allSatisfy(document -> {
                    assertThat(document.get("title").asText()).isEqualTo("alpha");
                    assertThat(document.get("text").asText()).isEqualTo("beta");
                });

                // Keep the full fixture in the service's bounded component candidate lists.
                List<ObjectNode> window = rankingContainer.queryItems(
                    "SELECT * FROM c " + orderBy + " OFFSET 3 LIMIT 257",
                    new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
                assertThat(rankingIds(window)).containsExactlyElementsOf(expected.subList(3, RANKING_DOCUMENT_COUNT));
            }
        } finally {
            restoreRankingProperty(previous);
        }
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void competitionRanksSkipTiedPositions() {
        List<ObjectNode> documents = rankingTierContainer.queryItems("SELECT * FROM c",
            new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
        assertThat(documents).hasSize(5);
        documents.sort((left, right) -> Long.compareUnsigned(documentRid(right), documentRid(left)));
        List<ObjectNode> titleOrder = new ArrayList<>(documents);
        // Equal document lengths: two occurrences outrank one, and no occurrence scores zero.
        titleOrder.sort((left, right) -> Integer.compare(titleTier(left), titleTier(right)));
        String previous = System.getProperty(RANKING_PROPERTY);
        try {
            for (boolean competition : new boolean[] {false, true}) {
                System.setProperty(RANKING_PROPERTY, Boolean.toString(competition));
                List<ObjectNode> expected = new ArrayList<>(documents);
                int[] titleRanks = competition ? new int[] {1, 2, 2, 2, 5} : new int[] {1, 2, 3, 4, 5};
                expected.sort((left, right) -> {
                    double leftScore = 1.0 / (60 + titleRanks[titleOrder.indexOf(left)])
                        + 1.0 / (60 + textRank(left));
                    double rightScore = 1.0 / (60 + titleRanks[titleOrder.indexOf(right)])
                        + 1.0 / (60 + textRank(right));
                    int comparison = Double.compare(rightScore, leftScore);
                    return comparison != 0 ? comparison : Long.compareUnsigned(documentRid(right), documentRid(left));
                });
                List<ObjectNode> results = rankingTierContainer.queryItems("SELECT TOP 5 * FROM c "
                    + rankingOrderBy(new double[] {1, 1}), new CosmosQueryRequestOptions(), ObjectNode.class)
                    .collectList().block();
                assertThat(rankingIds(results)).as("competition=%s", competition)
                    .containsExactlyElementsOf(rankingIds(expected));
                List<ObjectNode> topTwo = rankingTierContainer.queryItems("SELECT TOP 2 * FROM c "
                    + rankingOrderBy(new double[] {1, 1}), new CosmosQueryRequestOptions(), ObjectNode.class)
                    .collectList().block();
                assertThat(rankingIds(topTwo)).containsExactlyElementsOf(rankingIds(expected.subList(0, 2)));
            }
        } finally {
            restoreRankingProperty(previous);
        }
    }

    @Test(groups = {"query", "split"}, timeOut = TIMEOUT)
    public void emptyHybridQueryReturnsNoDocuments() {
        List<ObjectNode> results = rankingContainer.queryItems(
            "SELECT TOP 10 * FROM c WHERE c.id = 'missing' " + rankingOrderBy(new double[] {1, 1}),
            new CosmosQueryRequestOptions(), ObjectNode.class).collectList().block();
        assertThat(results).isEmpty();
    }

    private List<String> expectedTiedRankingOrder(double[] weights, boolean competition) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < rankingDocumentsByRid.size(); i++) {
            positions.add(i);
        }
        // In competition mode every tied score has rank 1. Ordinal rank follows the plan direction;
        // negative weights reverse that direction before their absolute magnitude is used in fusion.
        positions.sort((left, right) -> {
            int comparison = Double.compare(tiedRankingScore(right, weights, competition),
                tiedRankingScore(left, weights, competition));
            return comparison != 0 ? comparison : Integer.compare(left, right);
        });
        return positions.stream().map(position -> rankingDocumentsByRid.get(position).get("id").asText())
            .collect(Collectors.toList());
    }

    private static double tiedRankingScore(int descendingPosition, double[] weights, boolean competition) {
        double score = 0;
        for (double weight : weights) {
            int rank = competition ? 1 : weight < 0 ? RANKING_DOCUMENT_COUNT - descendingPosition : descendingPosition + 1;
            score += Math.abs(weight) / (60 + rank);
        }
        return score;
    }

    private static int titleTier(ObjectNode document) {
        String title = document.get("title").asText();
        return "alpha alpha".equals(title) ? 0 : "alpha filler".equals(title) ? 1 : 2;
    }

    private static int textRank(ObjectNode document) {
        // Texts have equal lengths and one through five occurrences of beta, so all scores are distinct.
        return 5 - Integer.parseInt(document.get("id").asText().substring("tier-".length()));
    }

    private static String rankingOrderBy(double[] weights) {
        return "ORDER BY RANK RRF(FullTextScore(c.title, 'alpha'), FullTextScore(c.text, 'beta'), "
            + Arrays.toString(weights) + ")";
    }

    private static long documentRid(ObjectNode document) {
        byte[] bytes = Base64.getDecoder().decode(document.get("_rid").asText().replace('-', '/'));
        return ByteBuffer.wrap(bytes, 8, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    private static List<String> rankingIds(List<ObjectNode> documents) {
        return documents.stream().map(document -> document.get("id").asText()).collect(Collectors.toList());
    }

    private static void restoreRankingProperty(String previous) {
        if (previous == null) {
            System.clearProperty(RANKING_PROPERTY);
        } else {
            System.setProperty(RANKING_PROPERTY, previous);
        }
    }

    private IndexingPolicy populateIndexingPolicy() {
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        IncludedPath includedPath1 = new IncludedPath("/*");
        indexingPolicy.setIncludedPaths(Collections.singletonList(includedPath1));

        CosmosFullTextIndex fullTextIndex = new CosmosFullTextIndex();
        fullTextIndex.setPath("/text");
        indexingPolicy.setCosmosFullTextIndexes(Collections.singletonList(fullTextIndex));
        return indexingPolicy;
    }

    private CosmosFullTextPolicy populateFullTextPolicy() {
        CosmosFullTextPolicy fullTextPolicy = new CosmosFullTextPolicy();
        CosmosFullTextPath fullTextPath = new CosmosFullTextPath();
        fullTextPath.setPath("/text");
        fullTextPath.setLanguage("en-US");
        fullTextPolicy.setDefaultLanguage("en-US");
        fullTextPolicy.setPaths(Collections.singletonList(fullTextPath));
        return fullTextPolicy;
    }

    private static List<Document> loadProductsFromJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File("src/test/java/com/azure/cosmos/rx/documents/text-3properties-1536dimensions-100documents.json"), new TypeReference<List<Document>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> fullTextOrder(String field, String term, List<String> matchingIds) {
        SqlQuerySpec query = new SqlQuerySpec(
            "SELECT TOP 100 c.id FROM c ORDER BY RANK FullTextScore(c." + field + ", @term)",
            Collections.singletonList(new SqlParameter("@term", term)));
        List<String> serviceOrder = container.queryItems(query, new CosmosQueryRequestOptions(), Document.class)
            .map(Document::getId).collectList().block();
        assertThat(serviceOrder).containsExactlyInAnyOrderElementsOf(documentRids.keySet());
        assertThat(serviceOrder.subList(0, matchingIds.size())).containsExactlyInAnyOrderElementsOf(matchingIds);

        List<String> order = new ArrayList<>(serviceOrder.subList(0, matchingIds.size()));
        List<String> zeroScores = serviceOrder.stream().filter(id -> !matchingIds.contains(id))
            .sorted((left, right) -> Long.compareUnsigned(documentRids.get(right), documentRids.get(left)))
            .collect(Collectors.toList());
        order.addAll(zeroScores);
        return order;
    }

    @SafeVarargs
    private final List<String> expectedRrfOrder(List<String> candidateIds, double[] weights,
        List<String>... componentOrders) {
        Map<String, Double> scores = new HashMap<>();
        for (String id : candidateIds) {
            scores.put(id, 0.0);
        }

        // Rank only the query's candidates. Negative weights reverse the component direction
        // (including RID ties); the plan normalizes their magnitude before applying RRF.
        for (int component = 0; component < componentOrders.length; component++) {
            List<String> order = componentOrders[component].stream().filter(candidateIds::contains)
                .collect(Collectors.toList());
            assertThat(order).containsExactlyInAnyOrderElementsOf(candidateIds);
            if (weights[component] < 0) {
                Collections.reverse(order);
            }
            for (int position = 0; position < order.size(); position++) {
                String id = order.get(position);
                scores.put(id, scores.get(id) + Math.abs(weights[component]) / (60 + position + 1));
            }
        }

        List<String> expected = new ArrayList<>(candidateIds);
        expected.sort((left, right) -> {
            int comparison = Double.compare(scores.get(right), scores.get(left));
            return comparison != 0 ? comparison
                : Long.compareUnsigned(documentRids.get(right), documentRids.get(left));
        });
        return expected;
    }

    private void validateTitleJohnResults(List<Document> results, boolean includesTitleMatch) {
        List<String> ids = results.stream().map(Document::getId).collect(Collectors.toList());
        // Only id 2 has "John" in its title. IDs 57/85 match text, but both title scores are zero.
        // A single FullTextScore query bypasses RRF, so it does not promise the RRF RID tie order.
        if (includesTitleMatch) {
            assertThat(ids.get(0)).isEqualTo("2");
            ids = ids.subList(1, ids.size());
        }
        assertThat(ids).containsExactlyInAnyOrder("57", "85");
    }

    private void validateResults(List<String> expectedIds, List<Document> results) {
        assertThat(results).isNotNull();

        List<String> resultsIds = results.stream().map(Document::getId).collect(Collectors.toList());
        assertThat(resultsIds).isEqualTo(expectedIds);
    }

    public static List<Float> getQueryVector() {
        String vector = "0.02, 0, -0.02, 0, -0.04, -0.01, -0.04, -0.01, 0.06, 0.08, -0.05, -0.04, -0.03, 0.05, -0.03, 0, -0.03, 0, 0.05, 0, 0.03,0.02, 0, 0.04, 0.05, 0.03, 0, 0, 0, -0.03, -0.01, 0.01, 0, -0.01, -0.03, -0.02, -0.05, 0.01, 0, 0.01, 0, 0.01, -0.03, -0.02, 0.02, 0.02, 0.04, 0.01, 0.04, 0.02, -0.01, -0.01, 0.02, 0.01, 0.02, -0.04, -0.01, 0.06, -0.01, -0.03, -0.04, -0.01, -0.01, 0, 0.03, -0.02, 0.03, 0.05, 0.01, 0.04, 0.05, -0.05, -0.01, 0.03, 0.02, -0.02, 0, -0.02, -0.02, -0.04, 0.01, -0.05, 0.01, 0.05, 0, -0.02, 0.03, -0.07, 0.05, 0.02, 0.03, 0.05, 0.05, -0.01, 0.03, -0.08, -0.01, -0.03, 0.04, -0.01, -0.02, -0.01, -0.02, -0.03, 0.03, 0.03,-0.04, 0.04, 0.02, 0, 0.03, -0.02, -0.04, 0.02, 0.01, 0.02, -0.01, 0.03, 0.02, 0.01, -0.02, 0, 0.02, 0, -0.01, 0.02, -0.05, 0.03, 0.03, 0.04, -0.02, 0.04, -0.04, 0.03, 0.03, -0.03, 0, 0.02, 0.06, 0.02, 0.02, -0.01, 0.03, 0, -0.03, -0.06, 0.02, 0, 0.02, -0.04,-0.05, 0.01, 0.02, 0.02, 0.07, 0.05, -0.01, 0.03, -0.03, -0.06, 0.04, 0.01, -0.01, 0.04, 0.02, 0.03, -0.03, 0.03, -0.01, 0.03, -0.04, -0.02, 0.02, -0.02, -0.03, -0.02, 0.02, -0.01, -0.05, -0.07, 0.02, -0.01, 0, -0.01, -0.02, -0.02, -0.03, -0.03, 0, -0.08, -0.01,0, -0.01, -0.03, 0.01, 0, -0.02, -0.03, -0.04, -0.01, 0.02, 0, 0, -0.04, 0.04, -0.01, 0.04, 0, -0.06, 0.02, 0.03, 0.01, 0.06, -0.02, 0, 0.01, 0.01, 0.01, 0, -0.02, 0.03, 0.02, 0.01, -0.01, -0.05, 0.03, -0.04, 0, 0.01, -0.02, -0.04, 0.02, 0, 0.09, -0.04, -0.01,0.02, 0.01, -0.03, 0.04, 0.02, -0.02, -0.02, -0.01, 0.01, -0.04, -0.01, 0.02, 0, 0, 0.07, 0.02, 0, 0, -0.01, 0.01, 0.03, -0.02, 0, 0.03, -0.02, -0.07, -0.04, -0.03, 0, -0.03, -0.02, 0, -0.02, -0.02, -0.05, -0.02, 0, 0.05, 0.01, -0.01, -0.04, 0.02, 0, 0, 0.03,0.02, -0.03, -0.01, -0.02, 0.06, -0.02, 0.01, 0.01, 0.04, -0.04, 0.06, -0.02, 0.01, 0.03, 0.01, 0.02, -0.02, 0.01, -0.04, 0.05, -0.03, 0.01, -0.01, 0, -0.03, -0.03, 0.04, 0.02, -0.03, -0.03, -0.02, 0.06, 0.04, -0.01, 0.01, 0.01, -0.01, -0.02, -0.02, 0.04, 0.01,-0.01, 0.01, -0.01, 0, 0.01, -0.04, 0.01, 0, -0.04, 0.05, 0.01, 0.01, 0.09, -0.04, -0.02, 0.04, 0, 0.04, -0.04, -0.04, 0, 0, -0.01, 0.05, -0.01, 0.02, 0.01, -0.03, 0, -0.06, 0.02, 0.04, 0.01, 0.03, 0.01, -0.04, 0, 0.01, 0.05, 0.02, -0.02, 0.02, 0, -0.02, -0.04,-0.07, -0.02, -0.05, 0.06, 0.01, 0.02, -0.03, 0.06, -0.01, -0.02, -0.02, -0.01, 0, -0.05, 0.06, -0.05, 0, -0.02, -0.02, 0, -0.01, 0.01, 0, -0.01, 0.05, 0.02, 0, 0.02, -0.02, 0.02, 0, 0.08, -0.02, 0.01, -0.03, 0.02, -0.03, 0, -0.01, -0.02, -0.04, 0.06, 0.01,-0.03, -0.03, 0.01, -0.01, 0.01, -0.01, 0.02, -0.03, 0.03, 0.04, 0.02, -0.02, 0.04, 0.01, 0.01, 0.02, 0.01, 0, -0.03, 0.03, -0.02, -0.03, -0.02, 0.02, 0, -0.01, -0.02, -0.02, 0, -0.01, -0.03, 0.02, -0.01, 0.01, -0.08, 0.01, -0.04, -0.05, 0.02, -0.01, -0.03,0.02, 0.01, -0.03, 0.01, 0.02, 0.03, 0.04, -0.04, 0.02, 0, 0.02, 0.02, 0.04, -0.04, -0.1, 0, 0.05, -0.01, 0.03, 0.05, 0.03, -0.02, 0.01, 0.02, -0.05, 0.01, 0, 0.05, -0.01, 0.03, -0.01, 0, 0.04, 0, 0, 0.08, 0.01, 0, -0.04, -0.03, 0, -0.02, -0.01, 0.02, 0.03,0, -0.01, 0, 0, 0, 0.06, 0, 0, 0.01, -0.01, 0.01, 0.04, 0.07, -0.01, 0.01, 0, -0.01, -0.02, 0.01, 0.01, 0, 0.02, 0.01, 0, -0.02, 0.03, 0.02, 0.06, 0.02, -0.01, 0.03, 0.02, -0.02, 0.01, -0.01, 0.03, 0.05, 0.02, 0.01, 0, 0, 0.01, 0.03, -0.03, -0.01, -0.04, 0.03,-0.02, 0.02, -0.02, -0.01, -0.02, 0.01, -0.04, 0.01, -0.04, 0.03, -0.02, -0.02, -0.01, -0.01, 0.07, 0.04, -0.01, 0.08, -0.04, -0.04, 0, 0, -0.01, -0.01, 0.03, -0.04, 0.02, -0.01, -0.04, 0.02, -0.07, -0.02, 0.02, -0.01, 0.02, 0.01, 0, 0.07, -0.01, 0.03, 0.01,-0.05, 0.02, 0.02, -0.01, 0.02, 0.02, -0.03, -0.02, 0.03, -0.01, 0.02, 0, 0, 0.02, -0.01, -0.02, 0.05, 0.02, 0.01, 0.01, -0.03, -0.05, -0.03, 0.01, 0.03, -0.02, -0.01, -0.01, -0.01, 0.03, -0.01, -0.03, 0.02, -0.02, -0.03, -0.02, -0.01, -0.01, -0.01, 0, -0.01,-0.04, -0.02, -0.02, -0.03, 0.04, 0.03, 0, -0.02, -0.01, -0.03, -0.01, -0.04, -0.04, 0.02, 0.01, -0.05, 0.04, -0.03, 0.01, -0.01, -0.03, 0.01, 0.01, 0.01, 0.02, -0.01, -0.02, -0.03, -0.01, -0.01, -0.01, -0.01, -0.03, 0, 0.01, -0.02, -0.01, -0.01, 0.01, 0, -0.04,0.01, -0.01, 0.02, 0, 0, -0.01, 0, 0, 0.03, -0.01, -0.06, -0.04, -0.01, 0, 0.02, -0.05, -0.02, 0.02, -0.01, 0.01, 0.01, -0.01, -0.02, 0, 0.02, -0.01, -0.02, 0.04, -0.01, 0, -0.02, -0.04, -0.03, -0.03, 0, 0.03, -0.01, -0.02, 0, 0.01, -0.01, -0.04, 0.01, -0.03,0.01, 0.03, 0, -0.02, 0, -0.04, -0.02, -0.02, 0.03, -0.02, 0.05, 0.02, 0.03, -0.02, -0.05, -0.01, 0.02, -0.04, 0.02, 0.01, -0.03, 0.01, 0.02, 0, 0.04, 0, -0.01, 0.02, 0.01, 0.02, 0.02, -0.02, 0.04, -0.01, 0, -0.01, 0, 0.01, -0.02, -0.04, 0.06, 0.01, 0, 0.01,-0.02, 0.02, 0.05, 0, 0.03, -0.02, 0.02, -0.03, -0.02, 0.01, 0, 0.06, -0.01, 0, -0.02, -0.02, 0.01, -0.01, 0, -0.03, 0.02, 0, -0.01, -0.02, -0.01, 0.03, -0.03, 0, 0, 0, -0.03, -0.06, 0.04, 0.02, -0.03, -0.06, -0.03, -0.01, -0.03, -0.02, -0.04, 0.01, 0, -0.01,0.02, -0.01, 0.03, 0.02, -0.02, -0.01, -0.02, -0.03, -0.01, 0.01, -0.04, 0.04, 0.03, 0.02, 0, -0.07, -0.02, -0.01, 0, 0.03, -0.01, -0.03, 0, 0.03, 0, -0.01, 0.02, 0.01, 0.02, -0.03, 0, 0.01, -0.02, 0.04, -0.04, 0, -0.05, 0, -0.02, -0.01, 0.03, 0.01, 0, -0.02,0, -0.05, 0.01, -0.01, 0, -0.08, -0.01, -0.02, 0.02, 0.01, -0.01, -0.01, -0.01, 0, 0, -0.01, -0.03, 0, 0, -0.02, 0.05, -0.03, 0.02, 0.01, -0.02, 0.01, 0.01, 0, 0.01, -0.01, 0, -0.04, -0.06, 0.03, -0.02, 0, -0.02, 0.01, 0.03, 0.03, -0.03, -0.01, 0, 0, 0.01,-0.02, -0.01, -0.01, -0.03, -0.02, 0.03, -0.02, 0.03, 0.01, 0.04, -0.04, 0.02, 0.02, 0.02, 0.03, 0, 0.06, -0.01, 0.02, -0.01, 0.01, -0.01, -0.01, -0.03, -0.01, 0.02, 0.01, 0.01, 0, -0.02, 0.03, 0.02, -0.01, -0.02, 0.01, 0.01, 0.04, -0.01, -0.05, 0, -0.01, 0,0.03, -0.01, 0.02, 0.02, -0.04, 0.01, -0.03, -0.02, 0, 0.02, 0, -0.01, 0.02, 0.01, 0.04, -0.04, 0, -0.01, -0.02, 0, -0.02, 0.01, -0.02, 0, 0, 0.03, 0.04, -0.01, 0, 0, 0.03, -0.02, 0.01, -0.02, 0, -0.03, 0.04, 0, 0.01, 0.04, 0, 0.03, -0.02, 0.01, 0.01, -0.02,0.02, -0.05, 0.03, -0.02, -0.01, 0.01, -0.01, 0.02, 0.04, 0.02, 0, -0.02, 0.02, -0.01, -0.03, -0.06, -0.01, -0.01, -0.04, 0.01, -0.01, -0.01, -0.01, -0.02, 0.03, -0.03, 0.05, 0, -0.01, -0.03, 0.03, 0.01, -0.01, -0.01, 0, 0.01, 0.01, 0.02, -0.01, 0.02, -0.02,-0.03, 0.03, -0.02, 0.01, 0, -0.03, 0.02, 0.02, -0.02, 0.01, 0.02, -0.01, 0.02, 0, 0.02, 0.01, 0, 0.05, -0.03, 0.01, 0.03, 0.04, 0.01, 0.01, -0.01, 0.02, -0.03, 0.02, 0.01, 0, -0.01, -0.03, -0.01, 0.02, 0.03, 0, 0.03, 0.02, 0, 0.01, 0.01, 0.02, 0.01, 0.02, 0.03,0.01, -0.03, 0.02, 0.01, 0.02, 0.03, -0.01, 0.01, -0.03, -0.01, -0.02, 0.01, 0, 0, -0.01, -0.02, -0.01, -0.01, 0.01, 0.06, 0.01, 0, -0.01, 0.01, 0, 0, -0.01, -0.01, 0, -0.02, -0.02, -0.01, -0.02, -0.01, -0.05, -0.02, 0.03, 0.02, 0, 0.03, -0.03, -0.03, 0.03, 0,0.02, -0.03, 0.04, -0.04, 0, -0.04, 0.04, 0.01, -0.03, 0.01, -0.02, -0.01, -0.04, 0.02, -0.01, 0.01, 0.01, 0.02, -0.02, 0.03, -0.01, 0, 0.01, 0, 0.02, 0.01, 0.01, 0.03, -0.06, 0.02, 0, -0.02, 0, 0.04, -0.03, 0, 0, -0.02, 0.06, 0.01, -0.03, -0.02, -0.01, -0.03,-0.04, 0.04, 0.03, -0.02, 0, 0.03, -0.04, -0.01, -0.02, -0.02, -0.01, 0.02, 0.02, 0.01, 0.01, 0.01, -0.02, -0.02, -0.03, -0.01, 0.01, 0, 0, 0, 0.02, -0.04, -0.01, -0.01, 0.04, -0.01, 0.01, -0.01, 0.01, -0.03, 0.01, -0.01, 0, -0.01, 0.01, 0, 0.01, -0.04, 0.01, 0,0, 0, 0, 0.02, 0.04, 0.01, 0.01, -0.01, -0.02, 0, 0, 0.01, -0.01, 0.01, -0.01, 0, 0.04, -0.01, -0.02, -0.01, -0.01, -0.01, 0, 0, 0.01, 0.01, 0.04, -0.01, -0.01, 0, -0.03, -0.01, 0.01, -0.01, -0.02, 0.01, -0.02, 0.01, -0.03, 0.02, 0, 0.03, 0.01, -0.03, -0.01,-0.01, 0.02, 0.01, 0, -0.01, 0.03, -0.04, 0.01, -0.01, -0.03, -0.02, 0.02, -0.01, 0, -0.01, 0.02, 0.02, 0.01, 0.03, 0, -0.03, 0, 0.02, -0.03, -0.01, 0.01, 0.06, -0.01, -0.02, 0.01, 0, 0.04, -0.04, 0.01, -0.02, 0, -0.04, 0, 0.02, 0.02, -0.02, 0.04, -0.01, 0.01,0, 0.03, -0.03, 0.04, -0.01, -0.02, -0.02, 0.01, -0.02, -0.01, 0, -0.03, -0.01, 0.02, -0.01, -0.05, 0.02, 0.01, 0, -0.02, -0.03, 0, 0, 0, -0.01, 0.02, 0, 0.02, 0.03, -0.02, 0.02, -0.02, 0.02, -0.01, 0.02, 0, -0.07, -0.01, 0.01, 0.01, -0.01, 0.02, 0, -0.01, 0,0.01, 0.01, -0.06, 0.04, 0, -0.04, -0.01, -0.03, -0.04, -0.01, -0.01, 0.03, -0.02, -0.01, 0.02, 0, -0.04, 0.01, 0.01, -0.01, 0.02, 0.01, 0.03, -0.01, 0, -0.02, -0.02, -0.01, 0.04, -0.02, 0.06, 0, 0, -0.02, 0, 0.01, 0, -0.02, 0.02, 0.02, -0.06, -0.02, 0, 0.02,0.01, -0.01, 0, 0, -0.01, 0.01, -0.04, -0.01, -0.01, 0.01, -0.02, -0.03, 0.01, 0.03, -0.01, -0.01, 0, -0.01, 0, -0.01, 0.05, 0.02, 0, 0, 0.02, -0.01, 0.02, -0.03, -0.01, -0.02, 0.02, 0, 0.01, -0.06, -0.01, 0.01, 0.01, 0.02, 0.02, -0.02, 0.03, 0.01, -0.01, -0.01,0, 0, 0.03, 0.05, 0.05, -0.01, 0.01, -0.03, 0, -0.01, -0.01, 0, -0.02, 0.02, 0, 0.02, -0.01, 0.01, -0.02, 0.01, 0, -0.02, 0.02, 0.01, -0.03, 0.03, -0.04, -0.02, -0.01, 0.01, -0.04, -0.03, -0.02, -0.03, 0.01, 0, 0, -0.02, -0.01, 0.02, 0.01, -0.01, 0.01, 0.03,-0.01, -0.02, -0.01, 0, 0, -0.03, 0, 0.02, 0.03, 0.01, -0.01, 0.02, 0.04, -0.04, 0.02, 0.01, -0.02, -0.01, 0.03, -0.04, -0.01, 0, 0.01, 0.01, 0, 0.03, 0.05, 0, 0, 0.05, 0.01, -0.01, 0, -0.01, 0, -0.01, -0.01, 0.03, -0.01, 0.02, 0, 0, -0.01, 0, -0.02, -0.02,0.05,-0.02, -0.01, -0.01, -0.01, 0.02, 0, -0.01, 0, 0, 0, -0.02, -0.04, 0.01, 0.01, -0.01, 0.01, 0, -0.06, -0.01, -0.04, -0.03, 0.01, 0, -0.01, 0.03, -0.04, -0.01, 0, 0.04, 0.03";
        return Arrays.stream(vector.split(","))
            .map(String::trim)
            .map(Float::parseFloat)
            .collect(Collectors.toList());
    }

    static class Document {
        @JsonProperty("id")
        String id;
        @JsonProperty("_rid")
        String rid;
        @JsonProperty("pk")
        String pk;
        @JsonProperty("category")
        String category;
        @JsonProperty("text")
        String text;
        @JsonProperty("title")
        String title;
        @JsonProperty("vector")
        double[] vector;
        @JsonProperty("score")
        double score;

        public Document() {
        }

        public String getId() {
            return id;
        }

        public String getPk() {
            return pk;
        }

        public String getCategory() {
            return category;
        }
    }
}

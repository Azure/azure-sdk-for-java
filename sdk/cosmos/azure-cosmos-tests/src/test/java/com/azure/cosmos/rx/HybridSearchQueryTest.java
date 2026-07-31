// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.query.HybridSearchBadRequestException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosFullTextIndex;
import com.azure.cosmos.models.CosmosFullTextPath;
import com.azure.cosmos.models.CosmosFullTextPolicy;
import com.azure.cosmos.models.CosmosFullTextScoreScope;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
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
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.cosmos.rx.TestSuiteBase.createDatabase;
import static com.azure.cosmos.rx.TestSuiteBase.safeClose;
import static com.azure.cosmos.rx.TestSuiteBase.safeDeleteDatabase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Ignore("TODO: Ignore these test cases until the public emulator is released.")
public class HybridSearchQueryTest {
    protected static final int TIMEOUT = 30000;
    protected static final int SETUP_TIMEOUT = 80000;
    protected static final int SHUTDOWN_TIMEOUT = 20000;

    protected static Logger logger = LoggerFactory.getLogger(HybridSearchQueryTest.class);

    private final String containerId = UUID.randomUUID().toString();
    private final String hpkContainerId = UUID.randomUUID().toString();
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncContainer hpkContainer;

    @BeforeClass(groups = {"query", "split"}, timeOut = SETUP_TIMEOUT* 10)
    public void before_HybridSearchQueryTest() {
        //set up the client
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        database = createDatabase(client, UUID.randomUUID().toString());

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/pk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, partitionKeyDef);
        containerProperties.setIndexingPolicy(populateIndexingPolicy());
        containerProperties.setFullTextPolicy(populateFullTextPolicy());
        database.createContainer(containerProperties, ThroughputProperties.createManualThroughput(10000)).block();
        container = database.getContainer(containerId);

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
        database.createContainer(hpkContainerProperties, ThroughputProperties.createManualThroughput(10000)).block();
        hpkContainer = database.getContainer(hpkContainerId);

        // Insert documents with pk and category fields for hierarchical partition key
        // pk = (index % 2) + 1, category = "A" for index % 3 == 0, "B" otherwise
        for (Document doc : documents) {
            int index = Integer.parseInt(doc.getId());
            doc.pk = String.valueOf((index % 2) + 1);
            doc.category = (index % 3 == 0) ? "A" : "B";
            hpkContainer.createItem(doc).block();
        }
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
        validateResults(Arrays.asList("2","57","85"), resultDocs);

        query = "SELECT c.id, c.title FROM c WHERE FullTextContains(c.title, @term1) " +
            "OR FullTextContains(c.text, @term1) ORDER BY RANK FullTextScore(c.title, @term1) OFFSET 1 LIMIT 5";
        querySpec = new SqlQuerySpec(query, Arrays.asList(
            new SqlParameter("@term1", "John")
        ));
        resultDocs = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Document.class).byPage()
            .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getResults()))
            .collectList().block();
        assertThat(resultDocs).hasSize(2);
        validateResults(Arrays.asList("57","85"), resultDocs);

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
        validateResults(Arrays.asList("61", "54", "51", "49", "24", "2", "57", "22", "75", "25", "77", "76", "66", "80", "85"), resultDocs);

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
        validateResults(Arrays.asList("2", "57", "22", "75", "25", "77", "76", "66", "80", "85"), resultDocs);

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
        validateResults(Arrays.asList("55", "61", "57", "24", "2", "54", "63", "9", "62", "75"), resultDocs);
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
            Arrays.asList("61", "54", "51", "49", "24", "2", "57", "22", "75", "25", "77", "76", "66", "80", "85"),
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
            Arrays.asList("61", "54", "51", "49", "24", "2", "57", "22", "75", "25", "77", "76", "66", "80", "85"),
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
            Arrays.asList("61", "54", "51", "49", "24", "2", "57", "22", "75", "25"),
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
            Arrays.asList("57", "22", "25", "54", "66", "24", "2", "85", "61", "76"),
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
            Arrays.asList("75", "24", "49", "55", "61", "21", "9", "26", "37", "57"),
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

        List<String> defaultIds = globalResultDocs.stream().map(Document::getId).collect(Collectors.toList());
        List<String> explicitGlobalIds = explicitGlobalResultDocs.stream().map(Document::getId).collect(Collectors.toList());
        assertThat(explicitGlobalIds).isEqualTo(defaultIds);

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
        validateResults(Arrays.asList("57", "85"), localPk2ResultDocs);

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
        validateResults(Arrays.asList("57", "85"), feedRangeResults);

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

    private void validateResults(List<String> actualIds, List<Document> results) {
        assertThat(results).isNotNull();

        List<String> resultsIds = results.stream().map(Document::getId).collect(Collectors.toList());
        assertThat(resultsIds).isEqualTo(actualIds);
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

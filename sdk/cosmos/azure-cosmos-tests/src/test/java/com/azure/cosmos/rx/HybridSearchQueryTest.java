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
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
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
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

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
        paths.add("/id");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerId, partitionKeyDef);
        containerProperties.setIndexingPolicy(populateIndexingPolicy());
        containerProperties.setFullTextPolicy(populateFullTextPolicy());
        database.createContainer(containerProperties, ThroughputProperties.createManualThroughput(10000)).block();
        container = database.getContainer(containerId);

        List<Document> documents = loadProductsFromJson();
        for (Document doc : documents) {
            container.createItem(doc).block();
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
        validateResults(Arrays.asList("51", "49", "24", "61", "54", "22", "2", "25", "75", "77", "57", "76", "66", "80", "85"), resultDocs);

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
        validateResults(Arrays.asList("22", "2", "25", "75", "77", "57", "76", "66", "80", "85"), resultDocs);

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
        validateResults(Arrays.asList("4", "24", "6", "9", "2", "3", "21", "5", "49", "13"), resultDocs);
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
            Arrays.asList("51", "49", "24", "61", "54", "22", "2", "25", "75", "77", "57", "76", "66", "80", "85"),
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
            Arrays.asList("51", "49", "24", "61", "54", "22", "2", "25", "75", "77", "57", "76", "66", "80", "85"),
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
            Arrays.asList("51", "49", "24", "61", "54", "22", "2", "25", "75", "77"),
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
            Arrays.asList("22", "57", "25", "24", "66", "2", "85", "49", "51", "54"),
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
            Arrays.asList("75", "24", "49", "61", "21", "9", "26", "4", "6", "37"),
            results
        );
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

        public Document(String id, String text, String title, double[] vector, double score) {
            this.id = id;
            this.text = text;
            this.title = title;
            this.vector = vector;
            this.score = score;
        }

        public String getId() {
            return id;
        }
    }
}

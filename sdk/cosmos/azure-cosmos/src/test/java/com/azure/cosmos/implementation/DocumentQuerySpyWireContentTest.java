// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.implementation.AsyncDocumentClient.Builder;
import com.azure.cosmos.implementation.http.HttpRequest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentQuerySpyWireContentTest extends TestSuiteBase {

    private Database createdDatabase;
    private DocumentCollection createdSinglePartitionCollection;
    private DocumentCollection createdMultiPartitionCollection;

    private List<Document> createdDocumentsInSinglePartitionCollection = new ArrayList<>();
    private List<Document> createdDocumentsInMultiPartitionCollection = new ArrayList<>();

    private SpyClientUnderTestFactory.ClientUnderTest client;

    public String getSinglePartitionCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdSinglePartitionCollection.getId());
    }

    public String getMultiPartitionCollectionLink() {
        return TestUtils.getCollectionNameLink(createdDatabase.getId(), createdMultiPartitionCollection.getId());
    }

    @Factory(dataProvider = "clientBuilders")
    public DocumentQuerySpyWireContentTest(Builder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "responseContinuationTokenLimitParamProvider")
    public static Object[][] responseContinuationTokenLimitParamProvider() {

        CosmosQueryRequestOptions options1 = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options1, 1);
        options1.setResponseContinuationTokenLimitInKb(5);
        options1.setPartitionKey(new PartitionKey("99"));
        String query1 = "Select * from r";
        boolean multiPartitionCollection1 = true;

        CosmosQueryRequestOptions options2 = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options2, 1);
        options2.setResponseContinuationTokenLimitInKb(5);
        options2.setPartitionKey(new PartitionKey("99"));
        String query2 = "Select * from r order by r.prop";
        boolean multiPartitionCollection2 = false;

        CosmosQueryRequestOptions options3 = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(options3, 1);
        options3.setResponseContinuationTokenLimitInKb(5);
        options3.setPartitionKey(new PartitionKey("99"));
        String query3 = "Select * from r";
        boolean multiPartitionCollection3 = false;

        CosmosQueryRequestOptions options4 = new CosmosQueryRequestOptions();
        options4.setPartitionKey(new PartitionKey("99"));
        String query4 = "Select * from r order by r.prop";
        boolean multiPartitionCollection4 = false;

        return new Object[][]{
                {options1, query1, multiPartitionCollection1},
                {options2, query2, multiPartitionCollection2},
                {options3, query3, multiPartitionCollection3},
                {options4, query4, multiPartitionCollection4},
        };
    }

    @Test(dataProvider = "responseContinuationTokenLimitParamProvider", groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithContinuationTokenLimit(CosmosQueryRequestOptions options, String query, boolean isMultiParitionCollection) throws Exception {
        String collectionLink;
        if (isMultiParitionCollection) {
            collectionLink = getMultiPartitionCollectionLink();
        } else {
            collectionLink = getSinglePartitionCollectionLink();
        }

        client.clearCapturedRequests();

        Flux<FeedResponse<Document>> queryObservable = client
                .queryDocuments(collectionLink, query, options);

        List<Document> results = queryObservable.flatMap(p -> Flux.fromIterable(p.getResults()))
            .collectList().block();

        assertThat(results.size()).describedAs("total results").isGreaterThanOrEqualTo(1);

        List<HttpRequest> requests = client.getCapturedRequests();

        for(HttpRequest req: requests) {
            validateRequestHasContinuationTokenLimit(req, options.getResponseContinuationTokenLimitInKb());
        }
    }

    private void validateRequestHasContinuationTokenLimit(HttpRequest request, Integer expectedValue) {
        Map<String, String> headers = request.headers().toMap();
        if (headers.get(HttpConstants.HttpHeaders.IS_QUERY) != null) {
            if (expectedValue != null && expectedValue > 0) {
                assertThat(headers
                               .containsKey(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB))
                    .isTrue();
                assertThat(headers
                               .get("x-ms-documentdb-responsecontinuationtokenlimitinkb"))
                    .isEqualTo(Integer.toString(expectedValue));
            } else {
                assertThat(headers
                               .containsKey(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB))
                    .isFalse();
            }
        }
    }

    public Document createDocument(AsyncDocumentClient client, String collectionLink, int cnt) {

        Document docDefinition = getDocumentDefinition(cnt);
        return client
                .createDocument(collectionLink, docDefinition, null, false).block().getResource();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_DocumentQuerySpyWireContentTest() throws Exception {

        client = new SpyClientBuilder(this.clientBuilder()).build();

        createdDatabase = SHARED_DATABASE;
        createdSinglePartitionCollection = SHARED_SINGLE_PARTITION_COLLECTION;
        truncateCollection(SHARED_SINGLE_PARTITION_COLLECTION);

        createdMultiPartitionCollection = SHARED_MULTI_PARTITION_COLLECTION;
        truncateCollection(SHARED_MULTI_PARTITION_COLLECTION);

        for(int i = 0; i < 3; i++) {
            createdDocumentsInSinglePartitionCollection.add(createDocument(client, getCollectionLink(createdSinglePartitionCollection), i));
            createdDocumentsInMultiPartitionCollection.add(createDocument(client, getCollectionLink(createdMultiPartitionCollection), i));
        }

        for(int i = 0; i < 5; i++) {
            createdDocumentsInSinglePartitionCollection.add(createDocument(client, getCollectionLink(createdSinglePartitionCollection), 99));
            createdDocumentsInMultiPartitionCollection.add(createDocument(client, getCollectionLink(createdMultiPartitionCollection), 99));
        }

        // wait for catch up
        TimeUnit.SECONDS.sleep(1);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        // do the query once to ensure the collection is cached.
        client.queryDocuments(getMultiPartitionCollectionLink(), "select * from root", options)
            .then().block();

        // do the query once to ensure the collection is cached.
        client.queryDocuments(getSinglePartitionCollectionLink(), "select * from root", options)
              .then().block();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static Document getDocumentDefinition(int cnt) {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"prop\" : %d, "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, cnt, cnt));
        return doc;
    }
}

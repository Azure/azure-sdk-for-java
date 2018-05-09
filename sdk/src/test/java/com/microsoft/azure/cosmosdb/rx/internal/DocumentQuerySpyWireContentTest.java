/**
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.DataType;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.IncludedPath;
import com.microsoft.azure.cosmosdb.Index;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;
import com.microsoft.azure.cosmosdb.rx.SpyClientBuilder;
import com.microsoft.azure.cosmosdb.rx.TestSuiteBase;
import com.microsoft.azure.cosmosdb.rx.Utils;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import rx.Observable;

public class DocumentQuerySpyWireContentTest extends TestSuiteBase {
    public final static String DATABASE_ID = getDatabaseId(DocumentQuerySpyWireContentTest.class);

    private Database createdDatabase;
    private DocumentCollection createdSinglePartitionCollection;
    private DocumentCollection createdMultiPartitionCollection;

    private List<Document> createdDocumentsInSinglePartitionCollection = new ArrayList<>();
    private List<Document> createdDocumentsInMultiPartitionCollection = new ArrayList<>();

    private Builder clientBuilder;
    private SpyClientUnderTestFactory.ClientUnderTest client;

    public String getSinglePartitionCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdSinglePartitionCollection.getId());
    }

    public String getMultiPartitionCollectionLink() {
        return Utils.getCollectionNameLink(createdDatabase.getId(), createdMultiPartitionCollection.getId());
    }

    static protected DocumentCollection getCollectionDefinition() {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        IndexingPolicy indexingPolicy = new IndexingPolicy();
        Collection<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.setPath("/*");
        Collection<Index> indexes = new ArrayList<>();
        Index stringIndex = Index.Range(DataType.String);
        stringIndex.set("precision", -1);
        indexes.add(stringIndex);

        Index numberIndex = Index.Range(DataType.Number);
        numberIndex.set("precision", -1);
        indexes.add(numberIndex);
        includedPath.setIndexes(indexes);
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);
        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());
        collectionDefinition.setPartitionKey(partitionKeyDef);
        collectionDefinition.setIndexingPolicy(indexingPolicy);

        return collectionDefinition;
    }

    @Factory(dataProvider = "clientBuilders")
    public DocumentQuerySpyWireContentTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @DataProvider(name = "responseContinuationTokenLimitParamProvider")
    public static Object[][] responseContinuationTokenLimitParamProvider() {

        FeedOptions options1 = new FeedOptions();
        options1.setMaxItemCount(1);
        options1.setResponseContinuationTokenLimitInKb(5);
        options1.setPartitionKey(new PartitionKey("99"));
        String query1 = "Select * from r";
        boolean multiPartitionCollection1 = true;

        FeedOptions options2 = new FeedOptions();
        options2.setMaxItemCount(1);
        options2.setResponseContinuationTokenLimitInKb(5);
        options2.setPartitionKey(new PartitionKey("99"));
        String query2 = "Select * from r order by r.prop";
        boolean multiPartitionCollection2 = false;

        FeedOptions options3 = new FeedOptions();
        options3.setMaxItemCount(1);
        options3.setResponseContinuationTokenLimitInKb(5);
        options3.setPartitionKey(new PartitionKey("99"));
        String query3 = "Select * from r";
        boolean multiPartitionCollection3 = false;

        FeedOptions options4 = new FeedOptions();
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
    public void queryWithContinuationTokenLimit(FeedOptions options, String query, boolean isMultiParitionCollection) throws Exception {
        String collectionLink;
        if (isMultiParitionCollection) {
            collectionLink = getMultiPartitionCollectionLink();
        } else {
            collectionLink = getSinglePartitionCollectionLink();
        }

        client.clearCapturedRequests();
        client.startCaptureRequests();
        
        Observable<FeedResponse<Document>> queryObservable = client
                .queryDocuments(collectionLink, query, options);

        List<Document> results = queryObservable.flatMap(p -> Observable.from(p.getResults()))
            .toList().toBlocking().single();

        assertThat(results.size()).describedAs("total results").isGreaterThanOrEqualTo(1);
        
        List<HttpClientRequest<ByteBuf>> requests = client.getCapturedRequests();

        for(HttpClientRequest<ByteBuf> req: requests) {
            validateRequestHasContinuationTokenLimit(req, options.getResponseContinuationTokenLimitInKb());
        }
    }

    private void validateRequestHasContinuationTokenLimit(HttpClientRequest<ByteBuf> request, Integer expectedValue) {
        if (expectedValue != null && expectedValue > 0) {
            assertThat(request.getHeaders()
                    .contains(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB))
                    .isTrue();
            assertThat(request.getHeaders()
                    .get("x-ms-documentdb-responsecontinuationtokenlimitinkb"))
                    .isEqualTo(Integer.toString(expectedValue));
        } else {
            assertThat(request.getHeaders()
                    .contains(HttpConstants.HttpHeaders.RESPONSE_CONTINUATION_TOKEN_LIMIT_IN_KB))
                    .isFalse();
        }
    }

    public Document createDocument(AsyncDocumentClient client, String collectionLink, int cnt) throws DocumentClientException {

        Document docDefinition = getDocumentDefinition(cnt);
        return client
                .createDocument(collectionLink, docDefinition, null, false).toBlocking().single().getResource();
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = new SpyClientBuilder(clientBuilder).build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        RequestOptions options1 = new RequestOptions();
        options1.setOfferThroughput(400);
        createdSinglePartitionCollection = safeCreateCollection(client, createdDatabase.getId(), getCollectionDefinition(), options1);

        RequestOptions options2 = new RequestOptions();
        options2.setOfferThroughput(10100);
        createdMultiPartitionCollection = safeCreateCollection(client, createdDatabase.getId(), getCollectionDefinition(), options2);

        for(int i = 0; i < 3; i++) {
            createdDocumentsInSinglePartitionCollection.add(createDocument(client, getCollectionLink(createdSinglePartitionCollection), i));
            createdDocumentsInMultiPartitionCollection.add(createDocument(client, getCollectionLink(createdMultiPartitionCollection), i));
        }

        for(int i = 0; i < 5; i++) {
            createdDocumentsInSinglePartitionCollection.add(createDocument(client, getCollectionLink(createdSinglePartitionCollection), 99));
            createdDocumentsInMultiPartitionCollection.add(createDocument(client, getCollectionLink(createdMultiPartitionCollection), 99));
        }
        
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        
        // do the query once to ensure the collection is cached.
        client.queryDocuments(getMultiPartitionCollectionLink(), "select * from root", options)
            .toCompletable().await();

        // do the query once to ensure the collection is cached.
        client.queryDocuments(getSinglePartitionCollectionLink(), "select * from root", options)
            .toCompletable().await();
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
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

/*
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
package com.microsoft.azure.cosmosdb.rx.examples;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.observable.ListenableFutureObservable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * This integration test class demonstrates how to use Async API to create,
 * delete, replace, and upsert Documents. If you are interested in examples for
 * querying for documents please see {@link DocumentQueryAsyncAPITest}
 * <p>
 * NOTE: you can use rxJava based async api with java8 lambda expression. Use
 * of rxJava based async APIs with java8 lambda expressions is much prettier.
 * <p>
 * You can also use the async API without java8 lambda expression.
 * <p>
 * For example
 * <ul>
 * <li>{@link #createDocument_Async()} demonstrates how to use async api
 * with java8 lambda expression.
 *
 * <li>{@link #createDocument_Async_withoutLambda()} demonstrates how to do
 * the same thing without lambda expression.
 * </ul>
 * <p>
 * Also if you need to work with Future or ListenableFuture it is possible to
 * transform an observable to ListenableFuture. Please see
 * {@link #transformObservableToGoogleGuavaListenableFuture()}
 */
public class DocumentCRUDAsyncAPITest {
    private final static String PARTITION_KEY_PATH = "/mypk";
    private final static int TIMEOUT = 60000;
    private AsyncDocumentClient asyncClient;
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void setUp() {
        // Sets up the requirements for each test
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.Direct);
        asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .withConsistencyLevel(ConsistencyLevel.Session)
                .build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        ArrayList<String> partitionKeyPaths = new ArrayList<String>();
        partitionKeyPaths.add(PARTITION_KEY_PATH);
        partitionKeyDefinition.setPaths(partitionKeyPaths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // Create database
        createdDatabase = Utils.createDatabaseForTest(asyncClient);

        // Create collection
        createdCollection = asyncClient
                .createCollection("dbs/" + createdDatabase.getId(), collectionDefinition, null)
                .toBlocking().single().getResource();
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(asyncClient, createdDatabase);
        Utils.safeClose(asyncClient);
    }

    /**
     * Create a document using java8 lambda expressions
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_Async() throws Exception {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(getCollectionLink(), doc, null, true);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Subscribe to Document resource response emitted by the observable
        createDocumentObservable.single() // We know there will be one response
                .subscribe(documentResourceResponse -> {
                    System.out.println(documentResourceResponse.getActivityId());
                    completionLatch.countDown();
                }, error -> {
                    System.err.println(
                            "an error occurred while creating the document: actual cause: " + error.getMessage());
                    completionLatch.countDown();
                });

        // Wait till document creation completes
        completionLatch.await();
    }

    /**
     * Create a document without java8 lambda expressions
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_Async_withoutLambda() throws Exception {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(getCollectionLink(), doc, null, true);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        Action1<ResourceResponse<Document>> onNext = new Action1<ResourceResponse<Document>>() {

            @Override
            public void call(ResourceResponse<Document> documentResourceResponse) {
                System.out.println(documentResourceResponse.getActivityId());
                completionLatch.countDown();
            }
        };

        Action1<Throwable> onError = new Action1<Throwable>() {

            public void call(Throwable error) {
                System.err
                        .println("an error occurred while creating the document: actual cause: " + error.getMessage());
                completionLatch.countDown();
            }
        };

        // Subscribe to Document resource response emitted by the observable
        createDocumentObservable.single() // We know there will be one response
                .subscribe(onNext, onError);

        // Wait till document creation completes
        completionLatch.await();
    }

    /**
     * Create a document in a blocking manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_toBlocking() {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(getCollectionLink(), doc, null, true);

        // toBlocking() converts to a blocking observable.
        // single() gets the only result.
        createDocumentObservable.toBlocking().single();
    }

    /**
     * Create a document with a programmatically set definition, in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocumentWithProgrammableDocumentDefinition() throws Exception {
        Document documentDefinition = new Document();
        documentDefinition.setId("test-document");
        documentDefinition.set("counter", 1);

        // Create a document
        Document createdDocument = asyncClient
                .createDocument(getCollectionLink(), documentDefinition, null, false).toBlocking().single()
                .getResource();

        // Read the created document
        Observable<ResourceResponse<Document>> readDocumentObservable = asyncClient
                .readDocument(getDocumentLink(createdDocument), null);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        readDocumentObservable.subscribe(documentResourceResponse -> {
            Document readDocument = documentResourceResponse.getResource();

            // The read document must be the same as the written document
            assertThat(readDocument.getId(), equalTo("test-document"));
            assertThat(readDocument.getInt("counter"), equalTo(1));
            System.out.println(documentResourceResponse.getActivityId());
            completionLatch.countDown();
        }, error -> {
            System.err.println("an error occured while creating the document: actual cause: " + error.getMessage());
            completionLatch.countDown();
        });

        completionLatch.await();
    }

    /**
     * Create 10 documents and sum up all the documents creation request charges
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentCreation_SumUpRequestCharge() throws Exception {
        // Create 10 documents
        List<Observable<ResourceResponse<Document>>> listOfCreateDocumentObservables = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), i));

            Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                    .createDocument(getCollectionLink(), doc, null, false);
            listOfCreateDocumentObservables.add(createDocumentObservable);
        }

        // Merge all document creation observables into one observable
        Observable<ResourceResponse<Document>> mergedObservable = Observable.merge(listOfCreateDocumentObservables);

        // Create a new observable emitting the total charge of creating all 10
        // documents.
        Observable<Double> totalChargeObservable = mergedObservable
                .map(ResourceResponse::getRequestCharge)
                // Map to request charge
                .reduce((totalCharge, charge) -> totalCharge + charge);
        // Sum up all the charges

        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Subscribe to the total request charge observable
        totalChargeObservable.subscribe(totalCharge -> {
                                            // Print the total charge
                                            System.out.println(totalCharge);
                                            completionLatch.countDown();
                                        }, e -> completionLatch.countDown()
        );

        completionLatch.await();
    }

    /**
     * Attempt to create a document which already exists
     * - First create a document
     * - Using the async api generate an async document creation observable
     * - Converts the Observable to blocking using Observable.toBlocking() api
     * - Catch already exist failure (409)
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_toBlocking_DocumentAlreadyExists_Fails() {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        asyncClient.createDocument(getCollectionLink(), doc, null, false).toBlocking().single();

        // Create the document
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(getCollectionLink(), doc, null, false);

        try {
            createDocumentObservable.toBlocking() // Converts the observable to a blocking observable
                    .single(); // Gets the single result
            Assert.fail("Document Already Exists. Document Creation must fail");
        } catch (Exception e) {
            assertThat("Document already exists.", ((DocumentClientException) e.getCause()).getStatusCode(),
                       equalTo(409));
        }
    }

    /**
     * Attempt to create a document which already exists
     * - First create a document
     * - Using the async api generate an async document creation observable
     * - Converts the Observable to blocking using Observable.toBlocking() api
     * - Catch already exist failure (409)
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_Async_DocumentAlreadyExists_Fails() throws Exception {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        asyncClient.createDocument(getCollectionLink(), doc, null, false).toBlocking().single();

        // Create the document
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(getCollectionLink(), doc, null, false);

        List<Throwable> errorList = Collections.synchronizedList(new ArrayList<>());

        createDocumentObservable.subscribe(resourceResponse -> {
        }, error -> {
            errorList.add(error);
            System.err.println("failed to create a document due to: " + error.getMessage());
        });

        Thread.sleep(2000);
        assertThat(errorList, hasSize(1));
        assertThat(errorList.get(0), is(instanceOf(DocumentClientException.class)));
        assertThat(((DocumentClientException) errorList.get(0)).getStatusCode(), equalTo(409));
    }

    /**
     * Replace a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentReplace_Async() throws Exception {
        // Create a document
        Document createdDocument = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        createdDocument = asyncClient.createDocument(getCollectionLink(), createdDocument, null, false).toBlocking()
                .single().getResource();

        // Try to replace the existing document
        Document replacingDocument = new Document(
                String.format("{ 'id': 'doc%s', 'counter': '%d', 'new-prop' : '2'}", createdDocument.getId(), 1));
        Observable<ResourceResponse<Document>> replaceDocumentObservable = asyncClient
                .replaceDocument(getDocumentLink(createdDocument), replacingDocument, null);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        replaceDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        Thread.sleep(2000);

        assertThat(capturedResponse, hasSize(1));
        assertThat(capturedResponse.get(0).getResource().get("new-prop"), equalTo("2"));
    }

    /**
     * Upsert a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentUpsert_Async() throws Exception {
        // Create a document
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        asyncClient.createDocument(getCollectionLink(), doc, null, false).toBlocking().single();

        // Upsert the existing document
        Document upsertingDocument = new Document(
                String.format("{ 'id': 'doc%s', 'counter': '%d', 'new-prop' : '2'}", doc.getId(), 1));
        Observable<ResourceResponse<Document>> upsertDocumentObservable = asyncClient
                .upsertDocument(getCollectionLink(), upsertingDocument, null, false);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        upsertDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        Thread.sleep(4000);

        assertThat(capturedResponse, hasSize(1));
        assertThat(capturedResponse.get(0).getResource().get("new-prop"), equalTo("2"));
    }

    /**
     * Delete a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentDelete_Async() throws Exception {
        // Create a document
        Document createdDocument = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d', 'mypk' : '%s'}", UUID.randomUUID().toString(), 1, UUID.randomUUID().toString()));
        createdDocument = asyncClient.createDocument(getCollectionLink(), createdDocument, null, false).toBlocking()
                .single().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getString("mypk")));

        // Delete the existing document
        Observable<ResourceResponse<Document>> deleteDocumentObservable = asyncClient
                .deleteDocument(getDocumentLink(createdDocument), options);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        deleteDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        Thread.sleep(2000);

        assertThat(capturedResponse, hasSize(1));

        // Assert document is deleted
        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setEnableCrossPartitionQuery(true);
        List<Document> listOfDocuments = asyncClient
                .queryDocuments(getCollectionLink(), String.format("SELECT * FROM r where r.id = '%s'", createdDocument.getId()), queryOptions)
                .map(FeedResponse::getResults) // Map page to its list of documents
                .concatMap(Observable::from) // Flatten the observable
                .toList() // Transform to a observable
                .toBlocking() // Block
                .single(); // Gets the List<Document>

        // Assert that there is no document found
        assertThat(listOfDocuments, hasSize(0));
    }

    /**
     * Read a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentRead_Async() throws Exception {
        // Create a document
        Document createdDocument = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d', 'mypk' : '%s'}", UUID.randomUUID().toString(), 1, UUID.randomUUID().toString()));
        createdDocument = asyncClient.createDocument(getCollectionLink(), createdDocument, null, false).toBlocking()
                .single().getResource();

        // Read the document
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getString("mypk")));
        Observable<ResourceResponse<Document>> readDocumentObservable = asyncClient
                .readDocument(getDocumentLink(createdDocument), options);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        readDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        Thread.sleep(2000);

        // Assert document is retrieved
        assertThat(capturedResponse, hasSize(1));
    }

    private static class TestObject {
        @JsonProperty("mypk")
        private String mypk;

        @JsonProperty("id")
        private String id;

        @JsonProperty("prop")
        private String prop;
    }

    @Test(groups = {"samples"}, timeOut = TIMEOUT)
    public void customSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

        TestObject testObject = new TestObject();
        testObject.id = UUID.randomUUID().toString();
        testObject.mypk = UUID.randomUUID().toString();
        testObject.prop = UUID.randomUUID().toString();
        String itemAsJsonString = mapper.writeValueAsString(testObject);
        Document doc = new Document(itemAsJsonString);

        Document createdDocument = asyncClient
                .createDocument(getCollectionLink(), doc, null, false)
                .toBlocking()
                .single()
                .getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(testObject.mypk));

        Document readDocument = asyncClient
                .readDocument(createdDocument.getSelfLink(), options)
                .toBlocking()
                .single()
                .getResource();

        TestObject readObject = mapper.readValue(readDocument.toJson(), TestObject.class);
        assertThat(readObject.prop, equalTo(testObject.prop));
    }

    /**
     * You can convert an Observable to a ListenableFuture.
     * ListenableFuture (part of google guava library) is a popular extension
     * of Java's Future which allows registering listener callbacks:
     * https://github.com/google/guava/wiki/ListenableFutureExplained
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void transformObservableToGoogleGuavaListenableFuture() throws Exception {
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", RandomUtils.nextInt(), 1));
        Observable<ResourceResponse<Document>> createDocumentObservable = asyncClient
                .createDocument(getCollectionLink(), doc, null, false);
        ListenableFuture<ResourceResponse<Document>> listenableFuture = ListenableFutureObservable
                .to(createDocumentObservable);

        ResourceResponse<Document> rrd = listenableFuture.get();

        assertThat(rrd.getRequestCharge(), greaterThan((double) 0));
        System.out.print(rrd.getRequestCharge());
    }

    private String getCollectionLink() {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId();
    }

    private String getDocumentLink(Document createdDocument) {
        return "dbs/" + createdDatabase.getId() + "/colls/" + createdCollection.getId() + "/docs/" + createdDocument.getId();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.examples;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.DocumentClientTest;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

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
 * Also if you need to work with Future or CompletableFuture it is possible to
 * transform a flux to CompletableFuture. Please see
 * {@link #transformObservableToCompletableFuture()}
 */

public class DocumentCRUDAsyncAPITest extends DocumentClientTest {

    private final static String PARTITION_KEY_PATH = "/mypk";
    private final static int TIMEOUT = 60000;

    private AsyncDocumentClient client;
    private Database createdDatabase;
    private DocumentCollection createdCollection;

    @BeforeClass(groups = "samples", timeOut = TIMEOUT)
    public void before_DocumentCRUDAsyncAPITest() {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        this.clientBuilder()
            .withServiceEndpoint(TestConfigurations.HOST)
            .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
            .withConnectionPolicy(connectionPolicy)
            .withConsistencyLevel(ConsistencyLevel.SESSION)
            .withContentResponseOnWriteEnabled(true);

        this.client = this.clientBuilder().build();

        DocumentCollection collectionDefinition = new DocumentCollection();
        collectionDefinition.setId(UUID.randomUUID().toString());

        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        ArrayList<String> partitionKeyPaths = new ArrayList<String>();
        partitionKeyPaths.add(PARTITION_KEY_PATH);
        partitionKeyDefinition.setPaths(partitionKeyPaths);
        collectionDefinition.setPartitionKey(partitionKeyDefinition);

        // CREATE database
        createdDatabase = Utils.createDatabaseForTest(client);

        // CREATE collection
        createdCollection = client
            .createCollection("dbs/" + createdDatabase.getId(), collectionDefinition, null)
            .single().block().getResource();
    }

    @AfterClass(groups = "samples", timeOut = TIMEOUT)
    public void shutdown() {
        Utils.safeClean(client, createdDatabase);
        Utils.safeClose(client);
    }

    /**
     * CREATE a document using java8 lambda expressions
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_Async() throws Exception {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        Mono<ResourceResponse<Document>> createDocumentObservable = client
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
     * CREATE a document without java8 lambda expressions
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_Async_withoutLambda() throws Exception {
        Document doc = new Document(String.format("{ 'getId': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        Mono<ResourceResponse<Document>> createDocumentObservable = client
                .createDocument(getCollectionLink(), doc, null, true);

        final CountDownLatch completionLatch = new CountDownLatch(1);

        Consumer<ResourceResponse<Document>> onNext = new Consumer<ResourceResponse<Document>>() {

            @Override
            public void accept(ResourceResponse<Document> documentResourceResponse) {
                System.out.println(documentResourceResponse.getActivityId());
                completionLatch.countDown();
            }
        };

        Consumer<Throwable> onError = new Consumer<Throwable>() {

            @Override
            public void accept(Throwable error) {
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
     * CREATE a document in a blocking manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocument_toBlocking() {
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        Mono<ResourceResponse<Document>> createDocumentObservable = client
                .createDocument(getCollectionLink(), doc, null, true);

        // toBlocking() converts to a blocking observable.
        // single() gets the only result.
        createDocumentObservable.single().block();
    }

    /**
     * CREATE a document with a programmatically set definition, in an Async manner
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void createDocumentWithProgrammableDocumentDefinition() throws Exception {
        Document documentDefinition = new Document();
        documentDefinition.setId("test-document");
        BridgeInternal.setProperty(documentDefinition, "counter", 1);

        // CREATE a document
        Document createdDocument = client
                .createDocument(getCollectionLink(), documentDefinition, null, false).single().block()
                .getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        // READ the created document
        Mono<ResourceResponse<Document>> readDocumentObservable = client
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
     * CREATE 10 documents and sum up all the documents creation request charges
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentCreation_SumUpRequestCharge() throws Exception {
        // CREATE 10 documents
        List<Mono<ResourceResponse<Document>>> listOfCreateDocumentObservables = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), i));

            Mono<ResourceResponse<Document>> createDocumentObservable = client
                    .createDocument(getCollectionLink(), doc, null, false);
            listOfCreateDocumentObservables.add(createDocumentObservable);
        }

        // Merge all document creation observables into one observable
        Flux<ResourceResponse<Document>> mergedObservable = Flux.merge(listOfCreateDocumentObservables);

        // CREATE a new observable emitting the total charge of creating all 10
        // documents.
        Flux<Double> totalChargeObservable = mergedObservable
                .map(ResourceResponse::getRequestCharge)
                // Map to request charge
                .reduce(Double::sum).flux();
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
        client.createDocument(getCollectionLink(), doc, null, false).single().block();

        // CREATE the document
        Mono<ResourceResponse<Document>> createDocumentObservable = client
                .createDocument(getCollectionLink(), doc, null, false);

        try {
            createDocumentObservable.single() // Converts the observable to a single observable
                    .block(); // Blocks and gets the result
            Assert.fail("Document Already Exists. Document Creation must fail");
        } catch (CosmosClientException e) {
            assertThat("Document already exists.", e.getStatusCode(),
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
        client.createDocument(getCollectionLink(), doc, null, false).single().block();

        // CREATE the document
        Mono<ResourceResponse<Document>> createDocumentObservable = client
                .createDocument(getCollectionLink(), doc, null, false);

        List<Throwable> errorList = Collections.synchronizedList(new ArrayList<>());

        createDocumentObservable.subscribe(resourceResponse -> {
        }, error -> {
            errorList.add(error);
            System.err.println("failed to create a document due to: " + error.getMessage());
        });

        waitForConditionOrTimeout(() -> errorList.size() == 1);

        assertThat(errorList, hasSize(1));
        assertThat(errorList.get(0), is(instanceOf(CosmosClientException.class)));
        assertThat(((CosmosClientException) errorList.get(0)).getStatusCode(), equalTo(409));
    }

    /**
     * REPLACE a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentReplace_Async() throws Exception {
        // CREATE a document
        Document createdDocument = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        createdDocument = client.createDocument(getCollectionLink(), createdDocument, null, false).single()
                .block().getResource();

        // Try to replace the existing document
        Document replacingDocument = new Document(
                String.format("{ 'id': 'doc%s', 'counter': '%d', 'new-prop' : '2'}", createdDocument.getId(), 1));
        Mono<ResourceResponse<Document>> replaceDocumentObservable = client
                .replaceDocument(getDocumentLink(createdDocument), replacingDocument, null);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        replaceDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        waitForConditionOrTimeout(() -> capturedResponse.size() == 1);

        assertThat(capturedResponse, hasSize(1));
        assertThat(capturedResponse.get(0).getResource().get("new-prop"), equalTo("2"));
    }

    /**
     * Upsert a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentUpsert_Async() throws Exception {
        // CREATE a document
        Document doc = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d'}", UUID.randomUUID().toString(), 1));
        client.createDocument(getCollectionLink(), doc, null, false).single().block();

        // Upsert the existing document
        Document upsertingDocument = new Document(
                String.format("{ 'id': 'doc%s', 'counter': '%d', 'new-prop' : '2'}", doc.getId(), 1));
        Mono<ResourceResponse<Document>> upsertDocumentObservable = client
                .upsertDocument(getCollectionLink(), upsertingDocument, null, false);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        upsertDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        waitForConditionOrTimeout(() -> capturedResponse.size() == 1);

        assertThat(capturedResponse, hasSize(1));
        assertThat(capturedResponse.get(0).getResource().get("new-prop"), equalTo("2"));
    }

    /**
     * DELETE a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentDelete_Async() throws Exception {
        // CREATE a document
        Document createdDocument = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d', 'mypk' : '%s'}", UUID.randomUUID().toString(), 1, UUID.randomUUID().toString()));
        createdDocument = client.createDocument(getCollectionLink(), createdDocument, null, false).single()
                .block().getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getString("mypk")));

        // DELETE the existing document
        Mono<ResourceResponse<Document>> deleteDocumentObservable = client
                .deleteDocument(getDocumentLink(createdDocument), options);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        deleteDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        waitForConditionOrTimeout(() -> capturedResponse.size() == 1);

        assertThat(capturedResponse, hasSize(1));

        // Assert document is deleted
        FeedOptions queryOptions = new FeedOptions();
        List<Document> listOfDocuments = client
                .queryDocuments(getCollectionLink(), String.format("SELECT * FROM r where r.id = '%s'", createdDocument.getId()), queryOptions)
                .map(FeedResponse::getResults) // Map page to its list of documents
                .concatMap(Flux::fromIterable) // Flatten the observable
                .collectList() // Transform to a observable
                .single() // Gets the Mono<List<Document>>
                .block(); // Block

        // Assert that there is no document found
        assertThat(listOfDocuments, hasSize(0));
    }

    /**
     * READ a document
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void documentRead_Async() throws Exception {
        // CREATE a document
        Document createdDocument = new Document(String.format("{ 'id': 'doc%s', 'counter': '%d', 'mypk' : '%s'}", UUID.randomUUID().toString(), 1, UUID.randomUUID().toString()));
        createdDocument = client.createDocument(getCollectionLink(), createdDocument, null, false).single()
                .block().getResource();

        // READ the document
        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(createdDocument.getString("mypk")));
        Mono<ResourceResponse<Document>> readDocumentObservable = client
                .readDocument(getDocumentLink(createdDocument), options);

        List<ResourceResponse<Document>> capturedResponse = Collections
                .synchronizedList(new ArrayList<>());

        readDocumentObservable.subscribe(resourceResponse -> {
            capturedResponse.add(resourceResponse);
        });

        waitForConditionOrTimeout(() -> capturedResponse.size() == 1);

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

        Document createdDocument = client
                .createDocument(getCollectionLink(), doc, null, false)
                .single()
                .block()
                .getResource();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey(testObject.mypk));

        Document readDocument = client
                .readDocument(createdDocument.getSelfLink(), options)
                .single()
                .block()
                .getResource();

        TestObject readObject = mapper.readValue(readDocument.toJson(), TestObject.class);
        assertThat(readObject.prop, equalTo(testObject.prop));
    }

    /**
     * You can convert a Flux to a CompletableFuture.
     */
    @Test(groups = "samples", timeOut = TIMEOUT)
    public void transformObservableToCompletableFuture() throws Exception {
        Document doc = new Document(String.format("{ 'id': 'doc%d', 'counter': '%d'}", RandomUtils.nextInt(), 1));
        Mono<ResourceResponse<Document>> createDocumentObservable = client
                .createDocument(getCollectionLink(), doc, null, false);
        CompletableFuture<ResourceResponse<Document>> listenableFuture = createDocumentObservable.single().toFuture();

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

    private void waitForConditionOrTimeout(Callable<Boolean> completionCondition) throws Exception {
        long start = System.currentTimeMillis();
        while(!completionCondition.call()) {
            Thread.sleep(1000);
            if ((System.currentTimeMillis() - start) > TIMEOUT) {
                break;
            }
        }
    }
}

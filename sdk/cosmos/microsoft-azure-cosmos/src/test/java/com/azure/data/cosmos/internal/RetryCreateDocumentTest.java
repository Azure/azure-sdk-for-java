// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosError;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;

public class RetryCreateDocumentTest extends TestSuiteBase {

    private SpyClientUnderTestFactory.ClientWithGatewaySpy client;

    private Database database;
    private DocumentCollection collection;

    @Factory(dataProvider = "clientBuilders")
    public RetryCreateDocumentTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void retryDocumentCreate() throws Exception {
        // create a document to ensure collection is cached
        client.createDocument(collection.selfLink(),  getDocumentDefinition(), null, false).single().block();

        Document docDefinition = getDocumentDefinition();

        Flux<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.selfLink(), docDefinition, null, false);
        AtomicInteger count = new AtomicInteger();

        doAnswer((Answer<Flux<RxDocumentServiceResponse>>) invocation -> {
            RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
            if (req.getOperationType() != OperationType.Create) {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }

            int currentAttempt = count.getAndIncrement();
            if (currentAttempt == 0) {
                Map<String, String> header = ImmutableMap.of(
                        HttpConstants.HttpHeaders.SUB_STATUS,
                        Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_MISMATCH));

                return Flux.error(BridgeInternal.createCosmosClientException(HttpConstants.StatusCodes.BADREQUEST, new CosmosError() , header));
            } else {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }
        }).when(client.getSpyGatewayStoreModel()).processMessage(anyObject());

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.id()).build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument_noRetryOnNonRetriableFailure() throws Exception {

        AtomicInteger count = new AtomicInteger();
        doAnswer((Answer<Flux<RxDocumentServiceResponse>>) invocation -> {
            RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];

            if (req.getResourceType() != ResourceType.Document) {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }

            int currentAttempt = count.getAndIncrement();
            if (currentAttempt == 0) {
                return client.getOrigGatewayStoreModel().processMessage(req);
            } else {
                Map<String, String> header = ImmutableMap.of(
                        HttpConstants.HttpHeaders.SUB_STATUS,
                        Integer.toString(2));

                return Flux.error(BridgeInternal.createCosmosClientException(1, new CosmosError() , header));
            }
        }).when(client.getSpyGatewayStoreModel()).processMessage(anyObject());

        // create a document to ensure collection is cached
        client.createDocument(collection.selfLink(),  getDocumentDefinition(), null, false)
                .single()
                .block();

        Document docDefinition = getDocumentDefinition();

        Flux<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.selfLink(), docDefinition, null, false);

        // validate
        FailureValidator validator = new FailureValidator.Builder().statusCode(1).subStatusCode(2).build();
        validateFailure(createObservable, validator, TIMEOUT);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createDocument_failImmediatelyOnNonRetriable() throws Exception {
        // create a document to ensure collection is cached
        client.createDocument(collection.selfLink(),  getDocumentDefinition(), null, false).single().block();
        AtomicInteger count = new AtomicInteger();

        doAnswer((Answer<Flux<RxDocumentServiceResponse>>) invocation -> {
            RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
            if (req.getOperationType() != OperationType.Create) {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }
            int currentAttempt = count.getAndIncrement();
            if (currentAttempt == 0) {
                Map<String, String> header = ImmutableMap.of(
                        HttpConstants.HttpHeaders.SUB_STATUS,
                        Integer.toString(2));

                return Flux.error(BridgeInternal.createCosmosClientException(1, new CosmosError() , header));
            } else {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }
        }).when(client.getSpyGatewayStoreModel()).processMessage(anyObject());

        Document docDefinition = getDocumentDefinition();

        Flux<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.selfLink(), docDefinition, null, false);
        // validate

        FailureValidator validator = new FailureValidator.Builder().statusCode(1).subStatusCode(2).build();
        validateFailure(createObservable.timeout(Duration.ofMillis(100)), validator);
    }
    
    @BeforeMethod(groups = { "simple" })
    public void beforeMethod(Method method) {
        Mockito.reset(client.getSpyGatewayStoreModel());
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        // set up the client        
        client = SpyClientUnderTestFactory.createClientWithGatewaySpy(clientBuilder());

        database = SHARED_DATABASE;
        collection = SHARED_SINGLE_PARTITION_COLLECTION;
    }

    private Document getDocumentDefinition() {
        String uuid = UUID.randomUUID().toString();
        Document doc = new Document(String.format("{ "
                + "\"id\": \"%s\", "
                + "\"mypk\": \"%s\", "
                + "\"sgmts\": [[6519456, 1471916863], [2498434, 1455671440]]"
                + "}"
                , uuid, uuid));
        return doc;
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import org.assertj.core.api.Assertions;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class RetryThrottleTest extends TestSuiteBase {
    private final static int TIMEOUT = 10000;
    private final static int TOTAL_DOCS = 500;
    private final static int LARGE_TIMEOUT = 30000;

    private SpyClientUnderTestFactory.ClientWithGatewaySpy client;
    private Database database;
    private DocumentCollection collection;

    @Test(groups = { "long" }, timeOut = LARGE_TIMEOUT, enabled = false)
    public void retryCreateDocumentsOnSpike() throws Exception {
        ConnectionPolicy policy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
        throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(Integer.MAX_VALUE);
        throttlingRetryOptions.setMaxRetryWaitTime(Duration.ofSeconds(LARGE_TIMEOUT));
        policy.setThrottlingRetryOptions(throttlingRetryOptions);

        AsyncDocumentClient.Builder builder = new AsyncDocumentClient.Builder()
                .withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(policy)
                .withConsistencyLevel(ConsistencyLevel.EVENTUAL)
                .withContentResponseOnWriteEnabled(true);

        client = SpyClientUnderTestFactory.createClientWithGatewaySpy(builder);

        // create a document to ensure collection is cached
        client.createDocument(getCollectionLink(collection), getDocumentDefinition(), null, false).block();

        List<Mono<ResourceResponse<Document>>> list = new ArrayList<>();
        for(int i = 0; i < TOTAL_DOCS; i++) {
            Mono<ResourceResponse<Document>> obs = client.createDocument(getCollectionLink(collection),  getDocumentDefinition(), null, false);
            list.add(obs);
        }

        // registers a spy to count number of invocation
        AtomicInteger totalCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();

        doAnswer((Answer<Mono<RxDocumentServiceResponse>>) invocation -> {
                RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
                if (req.getResourceType() ==  ResourceType.Document && req.getOperationType() == OperationType.Create) {
                    // increment the counter per Document CREATE operations
                    totalCount.incrementAndGet();
                }
                return client.getOrigGatewayStoreModel().processMessage(req).doOnNext(rsp -> successCount.incrementAndGet());
        }).when(client.getSpyGatewayStoreModel()).processMessage(any());

        List<ResourceResponse<Document>> rsps = Flux.merge(Flux.fromIterable(list), 100).collectList().single().block();
        System.out.println("total: " + totalCount.get());
        Assertions.assertThat(rsps).hasSize(TOTAL_DOCS);
        assertThat(successCount.get()).isEqualTo(TOTAL_DOCS);
        System.out.println("total count is " + totalCount.get());
    }

    @Test(groups = { "long" }, timeOut = TIMEOUT, enabled = false)
    public void retryDocumentCreate() throws Exception {
        client = SpyClientUnderTestFactory.createClientWithGatewaySpy(createGatewayRxDocumentClient());

        // create a document to ensure collection is cached
        client.createDocument(getCollectionLink(collection),  getDocumentDefinition(), null, false).block();

        Document docDefinition = getDocumentDefinition();

        Mono<ResourceResponse<Document>> createObservable = client
                .createDocument(collection.getSelfLink(), docDefinition, null, false);
        AtomicInteger count = new AtomicInteger();

        doAnswer((Answer<Mono<RxDocumentServiceResponse>>) invocation -> {
                RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
                if (req.getOperationType() != OperationType.Create) {
                    return client.getOrigGatewayStoreModel().processMessage(req);
                }
                int currentAttempt = count.getAndIncrement();
                if (currentAttempt == 0) {
                    return Mono.error(BridgeInternal.createCosmosException(HttpConstants.StatusCodes.TOO_MANY_REQUESTS));
                } else {
                    return client.getOrigGatewayStoreModel().processMessage(req);
                }
        }).when(client.getSpyGatewayStoreModel()).processMessage(any());

        // validate
        ResourceResponseValidator<Document> validator = new ResourceResponseValidator.Builder<Document>()
                .withId(docDefinition.getId()).build();
        validateSuccess(createObservable, validator, TIMEOUT);
    }

    @AfterMethod(groups = { "long" }, enabled = false)
    private void afterMethod() {
        safeClose(client);
    }

    @BeforeClass(groups = { "long" }, timeOut = SETUP_TIMEOUT, enabled = false)
    public void before_RetryThrottleTest() {
        // set up the client
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

    @AfterClass(groups = { "long" }, timeOut = SHUTDOWN_TIMEOUT, enabled = false)
    public void afterClass() {
    }
}

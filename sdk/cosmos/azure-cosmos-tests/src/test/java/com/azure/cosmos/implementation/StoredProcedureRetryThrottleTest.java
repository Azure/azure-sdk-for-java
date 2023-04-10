// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class StoredProcedureRetryThrottleTest extends TestSuiteBase {
    private final static int TIMEOUT = 10000;

    private SpyClientUnderTestFactory.ClientWithGatewaySpy client;
    private DocumentCollection createdCollection;

    public StoredProcedureRetryThrottleTest() {}

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void storedProcedureRetryThrottle() {
        client = SpyClientUnderTestFactory.createClientWithGatewaySpy(createGatewayRxDocumentClient());

        StoredProcedure storedProcedure = new StoredProcedure();
        storedProcedure.setId(UUID.randomUUID().toString());
        storedProcedure.setBody("function() {var x = 10;}");

        StoredProcedure createdStoreProcedure = client
            .createStoredProcedure(getCollectionLink(createdCollection), storedProcedure, null).block().getResource();

        AtomicInteger count = new AtomicInteger();

        doAnswer((Answer<Mono<RxDocumentServiceResponse>>) invocation -> {
            RxDocumentServiceRequest req = (RxDocumentServiceRequest) invocation.getArguments()[0];
            if (req.getOperationType() != OperationType.ExecuteJavaScript) {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }
            int currentAttempt = count.getAndIncrement();
            if (currentAttempt == 0) {
                return Mono.error(BridgeInternal.createCosmosException(HttpConstants.StatusCodes.TOO_MANY_REQUESTS));
            } else {
                return client.getOrigGatewayStoreModel().processMessage(req);
            }
        }).when(client.getSpyGatewayStoreModel()).processMessage(any());

        client.getCapturedRequests().clear();

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(new PartitionKey("dummy"));

        StoredProcedureResponse storedProcedureResponse =
            client.executeStoredProcedure(
                createdStoreProcedure.getSelfLink(), options,null).single().block();

        assertThat(storedProcedureResponse.getStatusCode()).isEqualTo(200);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_RetryThrottleTest() {
        createdCollection = SHARED_SINGLE_PARTITION_COLLECTION;
    }

    @AfterMethod(groups = { "emulator" })
    private void afterMethod() {
        safeClose(client);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterClass() {
    }
}

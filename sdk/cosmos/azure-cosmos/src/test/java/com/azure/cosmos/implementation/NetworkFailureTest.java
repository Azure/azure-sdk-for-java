// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.UnknownHostException;
import java.time.Instant;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class NetworkFailureTest extends TestSuiteBase {
    private static final int TIMEOUT = ClientRetryPolicy.MaxRetryCount * ClientRetryPolicy.RetryIntervalInMS + 60000;
    private final DocumentCollection collectionDefinition;

    @Factory(dataProvider = "simpleClientBuildersWithDirect")
    public NetworkFailureTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
        this.collectionDefinition = getCollectionDefinition();
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createCollectionWithUnreachableHost() {
        SpyClientUnderTestFactory.ClientWithGatewaySpy client = null;

        try {
            client = SpyClientUnderTestFactory.createClientWithGatewaySpy(clientBuilder());

            Database database = SHARED_DATABASE;

            Mono<ResourceResponse<DocumentCollection>> createObservable = client
                    .createCollection(database.getSelfLink(), collectionDefinition, null);


            final RxGatewayStoreModel origGatewayStoreModel = client.getOrigGatewayStoreModel();

            Mockito.doAnswer(invocation -> {
                RxDocumentServiceRequest request = invocation.getArgument(0, RxDocumentServiceRequest.class);

                if (request.getResourceType() == ResourceType.DocumentCollection) {
                    CosmosException exception = BridgeInternal.createCosmosException(null, 0, new UnknownHostException());
                    BridgeInternal.setSubStatusCode(exception, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
                    return Mono.error(exception);
                }

                return origGatewayStoreModel.processMessage(request);

            }).when(client.getSpyGatewayStoreModel()).processMessage(Mockito.any());


            FailureValidator validator = new FailureValidator.Builder().instanceOf(CosmosException.class).build();
            Instant start = Instant.now();
            validateFailure(createObservable, validator, TIMEOUT);
            Instant after = Instant.now();
            assertThat(after.toEpochMilli() - start.toEpochMilli())
                    .isGreaterThanOrEqualTo(ClientRetryPolicy.MaxRetryCount * ClientRetryPolicy.RetryIntervalInMS);

        } finally {
            safeClose(client);
        }
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        AsyncDocumentClient client = createGatewayHouseKeepingDocumentClient().build();
        safeDeleteCollection(client, collectionDefinition);
        client.close();
    }
}

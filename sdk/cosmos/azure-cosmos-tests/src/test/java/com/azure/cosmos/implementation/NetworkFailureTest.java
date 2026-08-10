// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;
import com.azure.cosmos.rx.TestSuiteBase;

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
    private static final int TEST_MAX_RETRY_COUNT = 5;
    private static final int TEST_RETRY_INTERVAL_MS = 100;
    private static final int TIMEOUT = TEST_MAX_RETRY_COUNT * TEST_RETRY_INTERVAL_MS + 60000;
    private final DocumentCollection collectionDefinition;

    @Factory(dataProvider = "internalClientBuilders")
    public NetworkFailureTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
        this.collectionDefinition = getInternalCollectionDefinition();
    }

    @Test(groups = { "long-emulator" }, timeOut = TIMEOUT)
    public void createCollectionWithUnreachableHost() {
        // Save previous values to restore later (in case CI sets these)
        String prevMaxRetryCount = System.getProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_MAX_RETRY_COUNT");
        String prevRetryIntervalMs = System.getProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_RETRY_INTERVAL_IN_MS");

        // Override retry constants for this test to avoid 120 × 1s = 2 min wait
        System.setProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_MAX_RETRY_COUNT", String.valueOf(TEST_MAX_RETRY_COUNT));
        System.setProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_RETRY_INTERVAL_IN_MS", String.valueOf(TEST_RETRY_INTERVAL_MS));

        SpyClientUnderTestFactory.ClientWithGatewaySpy client = null;

        try {
            client = SpyClientUnderTestFactory.createClientWithGatewaySpy(clientBuilder());

            Database database = SHARED_DATABASE_INTERNAL;

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
            validateResourceResponseFailure(createObservable, validator, TIMEOUT);
            Instant after = Instant.now();
            assertThat(after.toEpochMilli() - start.toEpochMilli())
                    .isGreaterThanOrEqualTo(TEST_MAX_RETRY_COUNT * TEST_RETRY_INTERVAL_MS);

        } finally {
            safeClose(client);
            // Restore previous values (or clear if previously unset)
            if (prevMaxRetryCount != null) {
                System.setProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_MAX_RETRY_COUNT", prevMaxRetryCount);
            } else {
                System.clearProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_MAX_RETRY_COUNT");
            }
            if (prevRetryIntervalMs != null) {
                System.setProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_RETRY_INTERVAL_IN_MS", prevRetryIntervalMs);
            } else {
                System.clearProperty("COSMOS.CLIENT_ENDPOINT_FAILOVER_RETRY_INTERVAL_IN_MS");
            }
        }
    }

    @AfterClass(groups = { "long-emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        AsyncDocumentClient client = createGatewayHouseKeepingDocumentClient().build();
        safeDeleteCollection(client, collectionDefinition);
        client.close();
    }
}

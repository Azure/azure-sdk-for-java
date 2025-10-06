// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import io.netty.channel.ConnectTimeoutException;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Use fault injection to verify the handling of barrier requests for various scenarios.
 */
public class BarrierRequestTests  extends TestSuiteBase {
    String primaryRegion = "central us";
    String secondaryRegion = "east us";

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public BarrierRequestTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test
    public void validateBarriersOnFailover() {
        AtomicBoolean simulateAddressRefreshFailures = new AtomicBoolean(false);
        AtomicBoolean failoverTriggered = new AtomicBoolean(false);

        CosmosClientBuilder clientBuilder = getClientBuilder()
                .consistencyLevel(ConsistencyLevel.STRONG)
                .directMode();

        clientBuilder.httpRequestInterceptor((request, uri) -> {
            logger.info("inside httpRequestInterceptor, simulateAddressRefreshFailures: {}, operationType: {}, resourceType: {}, uri: {}",
                simulateAddressRefreshFailures.get(), request.getOperationType(), request.getResourceType(), uri);

            // Once the failover is triggered, intercept the subsequent metadata refresh call.
            logger.info("Checking failoverTriggered to intercept metadata refresh call: " + failoverTriggered.get());
            logger.info("isMetadataRequest: " + request.isMetadataRequest());
            logger.info("ResourceType: " + request.getResourceType());
            logger.info("OperationType: " + request.getOperationType());
            if (failoverTriggered.get() &&
                request.isMetadataRequest() &&
                request.getResourceType() == ResourceType.DatabaseAccount)
               // request.getOperationType() == OperationType.Read)
            {
                // Return the modified account properties, making the SDK believe a failover has occurred.
                logger.info("Intercepting metadata call and returning modified account properties. New write region: " + this.secondaryRegion);
                return new RxDocumentServiceResponse(null, null);
            }

            // After the initial write, simulate a network failure on address resolution.
            // This will trigger the SDK's failover logic.
            if (simulateAddressRefreshFailures.get() &&
                request.isAddressRefresh() &&
                request.requestContext.regionalRoutingContextToRoute.getRegion().equals(this.primaryRegion)) // Target the primary region
            {
                logger.info("Simulating network failure for address resolution for region " + this.primaryRegion);
                logger.info("failoverTriggered: " + failoverTriggered.get());
                failoverTriggered.compareAndSet(false, true);
                logger.info("failoverTriggered: " + failoverTriggered.get());
                Map<String, String> headers = new HashMap<>();
                headers.put(HttpConstants.HttpHeaders.SUB_STATUS, Integer.toString(GATEWAY_ENDPOINT_UNAVAILABLE));
                throw new CosmosException(HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE, "Simulating network failure for address resolution for region", headers, new ConnectTimeoutException());
            }

            return null; // let other requests proceed normally
        });

        clientBuilder.storeResponseInterceptor((request, storeResponse) -> {
            if ((request.getOperationType() == OperationType.Create && request.getResourceType() == ResourceType.Document)) {

                String lsn = storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN);

                // Decrement so that GCLSN < LSN to simulate the replication lag
                String manipulatedGclsn = String.valueOf(Long.parseLong(lsn) - 2L);

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, manipulatedGclsn);

                // Enable address refresh failures for subsequent barrier requests in the primary region.
                simulateAddressRefreshFailures.compareAndSet(false, true);
                logger.info("inside storeResponseInterceptor, set simulateAddressRefreshFailures to {}", simulateAddressRefreshFailures.get());
            }

            // Track barrier requests (Head operations on a collection)
            if (request.getOperationType() == OperationType.Head && request.getResourceType() == ResourceType.DocumentCollection)
            {
                // If the barrier request is in the secondary region, allow it to succeed.
                if (request.requestContext.regionalRoutingContextToRoute.getRegion().equals(this.secondaryRegion))
                {
                    // Satisfy the barrier condition by setting GCLSN >= LSN
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(storeResponse.getLSN()));
                }
                    else
                {
                    // For any other region (initially the primary), keep the barrier condition unmet.
                    long lsn = storeResponse.getLSN() - 2;
                    storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(lsn));
                }
            }

            return storeResponse;
        });

        CosmosAsyncClient client = clientBuilder.buildAsyncClient();
        CosmosAsyncContainer container = getSharedSinglePartitionCosmosContainer(client);

        CosmosItemResponse<CosmosDiagnosticsTest.TestItem> response = container.createItem(CosmosDiagnosticsTest.TestItem.createNewItem()).block();
        validateDiagnosticsIsPresent(response);

        CosmosDiagnosticsContext diagnosticsContext = response.getDiagnostics().getDiagnosticsContext();
        System.out.println(diagnosticsContext);
    }

    private void validateDiagnosticsIsPresent(CosmosItemResponse<CosmosDiagnosticsTest.TestItem> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        assertThat(response.getDiagnostics()).isNotNull();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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

        clientBuilder.httpRequestInterceptor((request) -> {
            // After the initial write, simulate a network failure on address resolution.
            // This will trigger the SDK's failover logic.
            if (request.requestContext.regionalRoutingContextToRoute.getRegion().equals(this.primaryRegion)) // Target the primary region
            {
                while (!failoverTriggered.compareAndSet(false, true)) { // Signal that the failover process has started
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                logger.info("Simulating network failure for address resolution for region " + this.primaryRegion);
                throw new InternalServerErrorException("Simulated network failure for address resolution.", HttpConstants.SubStatusCodes.UNKNOWN);
            }

            // Once the failover is triggered, intercept the subsequent metadata refresh call.
            if (failoverTriggered.get())
            {
                // Return the modified account properties, making the SDK believe a failover has occurred.
                logger.info("Intercepting metadata call and returning modified account properties. New write region: " + this.secondaryRegion);
                return new RxDocumentServiceResponse(null, null);
            }

            return null; // let other requests proceed normally
        });

        clientBuilder.storeResponseInterceptor((request, storeResponse) -> {

            if ((request.getOperationType() == OperationType.Create && request.getResourceType() == ResourceType.Document)
                    || request.getOperationType() == OperationType.Head) {

                String lsn = storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN);

                // Decrement so that GCLSN < LSN to simulate the replication lag
                String manipulatedGclsn = String.valueOf(Long.parseLong(lsn) - 2L);

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, manipulatedGclsn);

                // Enable address refresh failures for subsequent barrier requests in the primary region.
                simulateAddressRefreshFailures.set(true);
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

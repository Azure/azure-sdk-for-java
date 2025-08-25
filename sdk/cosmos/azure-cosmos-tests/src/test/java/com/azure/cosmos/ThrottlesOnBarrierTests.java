package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ThrottlesOnBarrierTests extends TestSuiteBase {

    CosmosAsyncContainer container;

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public ThrottlesOnBarrierTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test
    public void forceBarrierOnCreate() {
        CosmosClientBuilder clientBuilder = getClientBuilder()
            .consistencyLevel(ConsistencyLevel.STRONG)
            .directMode();

        clientBuilder.storeResponseInterceptor((request, storeResponse) -> {

            if (request.getOperationType() == OperationType.Create
                && request.getResourceType() == ResourceType.Document) {

                String lsn = storeResponse.getHeaderValue(WFConstants.BackendHeaders.LSN);

                // Decrementing the GCLSN < LSN to force a barrier retry post a Create
                String manipulatedGclsn = String.valueOf(Long.parseLong(lsn) - 2L);

                storeResponse.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, manipulatedGclsn);
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

package com.azure.cosmos;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceEndpointTest extends TestSuiteBase {

    @DataProvider
    private Object[][] serviceEndpointArgProvider() {
        return new Object[][] {
            { TestConfigurations.HOST.substring(0,TestConfigurations.HOST.length() - 1) },
            { TestConfigurations.HOST },
            { TestConfigurations.HOST.replace("https://", "") },
            { TestConfigurations.HOST.replace("https://", "").replace("/", "") },
            { TestConfigurations.HOST + ";AccountKey=" + TestConfigurations.MASTER_KEY },
            { TestConfigurations.HOST + ";" + TestConfigurations.MASTER_KEY + ";" },
            { TestConfigurations.HOST + TestConfigurations.MASTER_KEY + ";" },
            { TestConfigurations.HOST + TestConfigurations.MASTER_KEY },
            { null },
            { "" }
        };
    }

    @Test(groups = { "unit" }, dataProvider = "serviceEndpointArgProvider")
    public void validateServiceEndpoint(String serviceEndpoint) {
        CosmosClient cosmosClient;
        try {
            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(serviceEndpoint)
                .key(TestConfigurations.MASTER_KEY);
            cosmosClient = cosmosClientBuilder.buildClient();
        } catch (NullPointerException e) {
            assertThat(serviceEndpoint).isNull();
            System.out.println();
            return;
        } catch (IllegalArgumentException e) {
            assertThat(serviceEndpoint).isEmpty();
            return;
        }

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);

        assertThat(asyncDocumentClient.getServiceEndpoint().toString()).isEqualTo(TestConfigurations.HOST);

        safeCloseSyncClient(cosmosClient);
    }

    @Test(groups = { "fast" }, dataProvider = "serviceEndpointArgProvider")
    public void createWithDiffServiceEndpoints(String serviceEndpoint) {
        if (serviceEndpoint != null && !serviceEndpoint.isEmpty()) {
            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(serviceEndpoint)
                .key(TestConfigurations.MASTER_KEY);

            CosmosAsyncClient cosmosClient = cosmosClientBuilder.buildAsyncClient();
            CosmosAsyncContainer cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(cosmosClient);

            CosmosItemResponse<TestObject> response = cosmosAsyncContainer.createItem(TestObject.create()).block();
            assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);


            safeClose(cosmosClient);
        }

    }

}

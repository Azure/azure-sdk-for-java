// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
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
            { TestConfigurations.HOST.substring(0,TestConfigurations.HOST.length() - 1), true }, // https://localhost:8080
            { TestConfigurations.HOST, true }, // https://localhost:8080/
            { TestConfigurations.HOST + ";AccountKey=" + TestConfigurations.MASTER_KEY + ";", true }, // https://localhost:8081/;AccountKey=<secret>;
            { TestConfigurations.HOST + ";AccountKey=" + TestConfigurations.MASTER_KEY, true}, // https://localhost:8081/;AccountKey=<secret>
            { TestConfigurations.HOST + ";" + TestConfigurations.MASTER_KEY + ";", true }, // https://localhost:8081/;<secret>;
            { TestConfigurations.HOST + TestConfigurations.MASTER_KEY + ";", true }, // https://localhost:8081/<secret>;
            { TestConfigurations.HOST + TestConfigurations.MASTER_KEY, true }, // https://localhost:8081/<secret>
            { TestConfigurations.HOST.replace("https://", ""), false }, // localhost:8080/
            { TestConfigurations.HOST.replace("https://", "").replace("/", ""), false },  // localhost:8081
            { "AccountEndpoint=" + TestConfigurations.HOST + ";AccountKey=" + TestConfigurations.MASTER_KEY + ";", false }, // AccountEndpoint=https://localhost:8081/;AccountKey=<secret>;
            { TestConfigurations.HOST.substring(0,TestConfigurations.HOST.length() - 1) + TestConfigurations.MASTER_KEY, false }, // https://localhost:8081<secret>
            { null, false },
            { "", false },
            { "  ", false },
        };
    }

    @Test(groups = { "emulator, fast" }, dataProvider = "serviceEndpointArgProvider", timeOut = TIMEOUT)
    public void validateServiceEndpoint(String serviceEndpoint, boolean succeed) {
        CosmosAsyncClient cosmosClient;
        try {
            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(serviceEndpoint)
                .key(TestConfigurations.MASTER_KEY);
            cosmosClient = cosmosClientBuilder.buildAsyncClient();
        } catch (NullPointerException | IllegalArgumentException e) {
            assertThat(succeed).isFalse();
            return;
        } catch (RuntimeException e) {
            assertThat(e.getMessage().contains("Client initialization failed. Check if the endpoint is reachable and if your auth token is valid.")).isTrue();
            assertThat(succeed).isFalse();
            return;
        }

        AsyncDocumentClient asyncDocumentClient =
            CosmosBridgeInternal.getAsyncDocumentClient(cosmosClient);

        assertThat(asyncDocumentClient.getServiceEndpoint().getPath()).isNullOrEmpty();
        assertThat(asyncDocumentClient.getServiceEndpoint().getQuery()).isNullOrEmpty();

        CosmosAsyncContainer cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(cosmosClient);
        // basic validation to ensure the client is working
        CosmosItemResponse<TestObject> response = cosmosAsyncContainer.createItem(TestObject.create()).block();
        assertThat(response.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        assertThat(succeed).isTrue();
        cleanUpContainer(cosmosAsyncContainer);
        safeClose(cosmosClient);
    }

}

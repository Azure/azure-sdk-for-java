// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FabricCosmosUserTest extends FabricTestSuiteBase {

    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer container;
    private static final String CONTAINER_ID = "fabric-java-test-container-" + UUID.randomUUID();

    @Factory(dataProvider = "clientBuildersWithGatewayForFabric")
    public FabricCosmosUserTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = { "fabric-test" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        createdDatabase = this.client.getDatabase(DATABASE_ID);
        createdDatabase.createContainerIfNotExists(CONTAINER_ID, "/mypk",
            ThroughputProperties.createManualThroughput(16000));
        this.container = createdDatabase.getContainer(CONTAINER_ID);
    }

    @AfterClass(groups = { "fabric-test" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        deleteCollection(container);
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void createUser() throws Exception {
        CosmosUserProperties user = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(user);

        validateResponse(user, response);
    }

    private CosmosUserProperties getUserProperties() {
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        return user;
    }


    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        CosmosUser user = createdDatabase.getUser(userProperties.getId());
        CosmosUserResponse readResponse = user.read();
        validateResponse(userProperties, readResponse);
    }

    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        CosmosUser user = createdDatabase.getUser(userProperties.getId());
        CosmosUserResponse delete = user.delete();

    }



    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void readAllUsers() throws Exception{
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        CosmosPagedIterable<CosmosUserProperties> feedResponseIterator = createdDatabase.readAllUsers();
        assertThat(feedResponseIterator.iterator().hasNext()).isTrue();

        CosmosPagedIterable<CosmosUserProperties> feedResponseIterator2 = createdDatabase.readAllUsers(new CosmosQueryRequestOptions());
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();

    }


    @Test(groups = { "fabric-test" }, timeOut = TIMEOUT)
    public void queryUsers() throws Exception{
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        String query = String.format("SELECT * from c where c.id = '%s'", userProperties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosUserProperties> feedResponseIterator1 =
                createdDatabase.queryUsers(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosUserProperties> feedResponseIterator2 =
                createdDatabase.queryUsers(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();

    }

    private void validateResponse(CosmosUserProperties properties,
                                  CosmosUserResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
                .as("check Resource Id")
                .isEqualTo(properties.getId());

    }
}

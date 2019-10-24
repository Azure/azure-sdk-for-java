// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosUserTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosUserTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteSyncDatabase(createdDatabase);
        for (String dbId : databases) {
            safeDeleteSyncDatabase(client.getDatabase(dbId));
        }
        safeCloseSyncClient(client);
    }
    

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
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


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        CosmosUser user = createdDatabase.getUser(userProperties.getId());
        CosmosUserResponse readResponse = user.read();
        validateResponse(userProperties, readResponse);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        CosmosUser user = createdDatabase.getUser(userProperties.getId());
        CosmosUserResponse delete = user.delete();

    }

   

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllUsers() throws Exception{
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator = createdDatabase.readAllUsers();
        assertThat(feedResponseIterator.hasNext()).isTrue();

        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator2 = createdDatabase.readAllUsers(new FeedOptions());
        assertThat(feedResponseIterator2.hasNext()).isTrue();

    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsers() throws Exception{
        CosmosUserProperties userProperties = getUserProperties();
        CosmosUserResponse response = createdDatabase.createUser(userProperties);

        String query = String.format("SELECT * from c where c.id = '%s'", userProperties.getId());
        FeedOptions feedOptions = new FeedOptions().setEnableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator1 =
                createdDatabase.queryUsers(query, feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator2 =
                createdDatabase.queryUsers(query, feedOptions);
        assertThat(feedResponseIterator2.hasNext()).isTrue();
  
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

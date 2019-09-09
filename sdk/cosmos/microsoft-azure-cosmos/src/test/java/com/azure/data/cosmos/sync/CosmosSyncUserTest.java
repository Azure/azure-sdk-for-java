// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.rx.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncUserTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosSyncClient client;
    private CosmosSyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncUserTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildSyncClient();
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
        CosmosSyncUserResponse response = createdDatabase.createUser(user);
        
        validateResponse(user, response);
    }

    private CosmosUserProperties getUserProperties() {
        CosmosUserProperties user = new CosmosUserProperties();
        user.id(UUID.randomUUID().toString());
        return user;
    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
        CosmosUserProperties userProperties = getUserProperties();
        CosmosSyncUserResponse response = createdDatabase.createUser(userProperties);

        CosmosSyncUser user = createdDatabase.getUser(userProperties.id());
        CosmosSyncUserResponse readResponse = user.read();
        validateResponse(userProperties, readResponse);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        CosmosUserProperties userProperties = getUserProperties();
        CosmosSyncUserResponse response = createdDatabase.createUser(userProperties);

        CosmosSyncUser user = createdDatabase.getUser(userProperties.id());
        CosmosSyncUserResponse delete = user.delete();

    }

   

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readAllUsers() throws Exception{
        CosmosUserProperties userProperties = getUserProperties();
        CosmosSyncUserResponse response = createdDatabase.createUser(userProperties);

        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator = createdDatabase.readAllUsers();
        assertThat(feedResponseIterator.hasNext()).isTrue();

        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator2 = createdDatabase.readAllUsers(new FeedOptions());
        assertThat(feedResponseIterator2.hasNext()).isTrue();

    }


    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsers() throws Exception{
        CosmosUserProperties userProperties = getUserProperties();
        CosmosSyncUserResponse response = createdDatabase.createUser(userProperties);

        String query = String.format("SELECT * from c where c.id = '%s'", userProperties.id());
        FeedOptions feedOptions = new FeedOptions().enableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator1 =
                createdDatabase.queryUsers(query, feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosUserProperties>> feedResponseIterator2 =
                createdDatabase.queryUsers(query, feedOptions);
        assertThat(feedResponseIterator2.hasNext()).isTrue();
  
    }

    private void validateResponse(CosmosUserProperties properties,
                                  CosmosSyncUserResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
                .as("check Resource Id")
                .isEqualTo(properties.id());

    }
}

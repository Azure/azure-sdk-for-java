// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.HttpConstants;
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

public class CosmosSyncStoredProcTest extends TestSuiteBase {
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosSyncClient client;
    private CosmosSyncContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncStoredProcTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }


    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        assertThat(this.client).isNull();
        this.client = clientBuilder().buildSyncClient();
        CosmosContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().id()).getContainer(asyncContainer.id());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosSyncStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        storedProcedureDef.id(UUID.randomUUID().toString());
        storedProcedureDef.body("function() {var x = 11;}");
        CosmosSyncStoredProcedureResponse response1 = container.getScripts()
                                                              .createStoredProcedure(storedProcedureDef,
                                                                      new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, response1);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createSproc_alreadyExists() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosSyncStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        // Test for conflict
        try {
            container.getScripts().createStoredProcedure(storedProcedureDef);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosClientException.class);
            assertThat(((CosmosClientException) e).statusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosSyncStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        CosmosSyncStoredProcedure storedProcedure = container.getScripts().getStoredProcedure(storedProcedureDef.id());
        CosmosSyncStoredProcedureResponse readResponse = storedProcedure.read();
        validateResponse(storedProcedureDef, readResponse);

        CosmosSyncStoredProcedureResponse readResponse2 =
                storedProcedure.read(new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, readResponse2);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosSyncStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        CosmosSyncStoredProcedureResponse readResponse = container.getScripts()
                                                                 .getStoredProcedure(storedProcedureDef.id())
                                                                 .read();
        validateResponse(storedProcedureDef, readResponse);
        //replace
        storedProcedureDef = readResponse.properties();
        storedProcedureDef.body("function(){ var y = 20;}");
        CosmosSyncStoredProcedureResponse replaceResponse = container.getScripts()
                                                                    .getStoredProcedure(storedProcedureDef.id())
                                                                    .replace(storedProcedureDef);
        validateResponse(storedProcedureDef, replaceResponse);

        storedProcedureDef.body("function(){ var z = 2;}");
        CosmosSyncStoredProcedureResponse replaceResponse2 = container.getScripts()
                                                                     .getStoredProcedure(storedProcedureDef.id())
                                                                     .replace(storedProcedureDef,
                                                                             new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, replaceResponse2);

    }

    private CosmosStoredProcedureProperties getCosmosStoredProcedureProperties() {
        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.id(UUID.randomUUID().toString());
        storedProcedureDef.body("function() {var x = 10;}");
        return storedProcedureDef;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosSyncStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);
        CosmosSyncResponse deleteResponse = container.getScripts()
                                                    .getStoredProcedure(storedProcedureDef.id())
                                                    .delete();

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties sproc = new CosmosStoredProcedureProperties()
                                                        .id(UUID.randomUUID().toString());
        sproc.body("function() {var x = 10;}");

        CosmosSyncStoredProcedureResponse response = container.getScripts().createStoredProcedure(sproc);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.partitionKey(PartitionKey.None);
        CosmosSyncStoredProcedureResponse executeResponse = container.getScripts()
                                                        .getStoredProcedure(sproc.id())
                                                        .execute(null, options);
        assertThat(executeResponse.activityId()).isNotEmpty();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    private void readAllSprocs() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();
        container.getScripts().createStoredProcedure(storedProcedureDef);

        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);
        Iterator<FeedResponse<CosmosStoredProcedureProperties>> feedResponseIterator3 =
                container.getScripts().readAllStoredProcedures(feedOptions);
        assertThat(feedResponseIterator3.hasNext()).isTrue();

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    private void querySprocs() throws Exception {
        CosmosStoredProcedureProperties properties = getCosmosStoredProcedureProperties();
        container.getScripts().createStoredProcedure(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.id());
        FeedOptions feedOptions = new FeedOptions().enableCrossPartitionQuery(true);

        Iterator<FeedResponse<CosmosStoredProcedureProperties>> feedResponseIterator1 =
                container.getScripts().queryStoredProcedures(query, feedOptions);
        assertThat(feedResponseIterator1.hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        Iterator<FeedResponse<CosmosStoredProcedureProperties>> feedResponseIterator2 =
                container.getScripts().queryStoredProcedures(query, feedOptions);
        assertThat(feedResponseIterator2.hasNext()).isTrue();
    }

    private void validateResponse(CosmosStoredProcedureProperties properties,
                                  CosmosSyncStoredProcedureResponse createResponse) {
        // Basic validation
        assertThat(createResponse.properties().id()).isNotNull();
        assertThat(createResponse.properties().id())
                .as("check Resource Id")
                .isEqualTo(properties.id());

    }
}

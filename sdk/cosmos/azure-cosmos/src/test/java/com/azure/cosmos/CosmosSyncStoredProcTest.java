// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.QueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosSyncStoredProcTest extends TestSuiteBase {
    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private List<String> databases = new ArrayList<>();
    private CosmosClient client;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public CosmosSyncStoredProcTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }


    @BeforeClass(groups = {"simple"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosSyncStoredProcTest() {
        assertThat(this.client).isNull();
        this.client = getClientBuilder().buildClient();
        CosmosAsyncContainer asyncContainer = getSharedMultiPartitionCosmosContainer(this.client.asyncClient());
        container = client.getDatabase(asyncContainer.getDatabase().getId()).getContainer(asyncContainer.getId());
    }

    @AfterClass(groups = {"simple"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        assertThat(this.client).isNotNull();
        this.client.close();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createStoredProcedure() throws Exception {

        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 11;}");
        CosmosStoredProcedureResponse response1 = container.getScripts()
                                                           .createStoredProcedure(storedProcedureDef,
                                                                      new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, response1);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createSproc_alreadyExists() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        // Test for conflict
        try {
            container.getScripts().createStoredProcedure(storedProcedureDef);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CosmosException.class);
            assertThat(((CosmosException) e).getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CONFLICT);
        }
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        CosmosStoredProcedure storedProcedure = container.getScripts().getStoredProcedure(storedProcedureDef.getId());
        CosmosStoredProcedureResponse readResponse = storedProcedure.read();
        validateResponse(storedProcedureDef, readResponse);

        CosmosStoredProcedureResponse readResponse2 =
                storedProcedure.read(new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, readResponse2);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);

        CosmosStoredProcedureResponse readResponse = container.getScripts()
                                                              .getStoredProcedure(storedProcedureDef.getId())
                                                              .read();
        validateResponse(storedProcedureDef, readResponse);
        //replace
        storedProcedureDef = readResponse.getProperties();
        storedProcedureDef.setBody("function(){ var y = 20;}");
        CosmosStoredProcedureResponse replaceResponse = container.getScripts()
                                                                 .getStoredProcedure(storedProcedureDef.getId())
                                                                 .replace(storedProcedureDef);
        validateResponse(storedProcedureDef, replaceResponse);

        storedProcedureDef.setBody("function(){ var z = 2;}");
        CosmosStoredProcedureResponse replaceResponse2 = container.getScripts()
                                                                  .getStoredProcedure(storedProcedureDef.getId())
                                                                  .replace(storedProcedureDef,
                                                                             new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, replaceResponse2);

    }

    private CosmosStoredProcedureProperties getCosmosStoredProcedureProperties() {
        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        return storedProcedureDef;
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void deleteStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);
        container.getScripts()
            .getStoredProcedure(storedProcedureDef.getId())
            .delete();

    }
    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties sproc = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(sproc);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                                                                 .getStoredProcedure(sproc.getId())
                                                                 .execute(null, options);

        assertThat(executeResponse.getActivityId()).isNotEmpty();
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    private void readAllSprocs() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();
        container.getScripts().createStoredProcedure(storedProcedureDef);

        QueryRequestOptions queryRequestOptions = new QueryRequestOptions();

        CosmosPagedIterable<CosmosStoredProcedureProperties> feedResponseIterator3 =
                container.getScripts().readAllStoredProcedures(queryRequestOptions);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    private void querySprocs() throws Exception {
        CosmosStoredProcedureProperties properties = getCosmosStoredProcedureProperties();
        container.getScripts().createStoredProcedure(properties);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        QueryRequestOptions queryRequestOptions = new QueryRequestOptions();

        CosmosPagedIterable<CosmosStoredProcedureProperties> feedResponseIterator1 =
                container.getScripts().queryStoredProcedures(query, queryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosStoredProcedureProperties> feedResponseIterator2 =
                container.getScripts().queryStoredProcedures(query, queryRequestOptions);
        assertThat(feedResponseIterator2.iterator().hasNext()).isTrue();
    }

    private void validateResponse(CosmosStoredProcedureProperties properties,
                                  CosmosStoredProcedureResponse createResponse) {
        // Basic validation
        assertThat(createResponse.getProperties().getId()).isNotNull();
        assertThat(createResponse.getProperties().getId())
                .as("check Resource Id")
                .isEqualTo(properties.getId());

    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        validateDiagnostics(response, false);

        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 11;}");
        CosmosStoredProcedureResponse response1 = container.getScripts()
                                                           .createStoredProcedure(storedProcedureDef,
                                                                      new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, response1);
        validateDiagnostics(response1, false);

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void createSproc_alreadyExists() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);
        validateDiagnostics(response, false);

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
        validateDiagnostics(response, false);

        CosmosStoredProcedure storedProcedure = container.getScripts().getStoredProcedure(storedProcedureDef.getId());
        CosmosStoredProcedureResponse readResponse = storedProcedure.read();
        validateResponse(storedProcedureDef, readResponse);
        validateDiagnostics(readResponse, false);

        CosmosStoredProcedureResponse readResponse2 =
                storedProcedure.read(new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, readResponse2);
        validateDiagnostics(readResponse2, false);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void replaceStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedureDef);
        validateResponse(storedProcedureDef, response);
        validateDiagnostics(response, false);

        CosmosStoredProcedureResponse readResponse = container.getScripts()
                                                              .getStoredProcedure(storedProcedureDef.getId())
                                                              .read();
        validateResponse(storedProcedureDef, readResponse);
        validateDiagnostics(readResponse, false);
        //replace
        storedProcedureDef = readResponse.getProperties();
        storedProcedureDef.setBody("function(){ var y = 20;}");
        CosmosStoredProcedureResponse replaceResponse = container.getScripts()
                                                                 .getStoredProcedure(storedProcedureDef.getId())
                                                                 .replace(storedProcedureDef);
        validateResponse(storedProcedureDef, replaceResponse);
        validateDiagnostics(replaceResponse, false);

        storedProcedureDef.setBody("function(){ var z = 2;}");
        CosmosStoredProcedureResponse replaceResponse2 = container.getScripts()
                                                                  .getStoredProcedure(storedProcedureDef.getId())
                                                                  .replace(storedProcedureDef,
                                                                             new CosmosStoredProcedureRequestOptions());
        validateResponse(storedProcedureDef, replaceResponse2);
        validateDiagnostics(replaceResponse2, false);

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
        validateDiagnostics(response, false);

        container.getScripts()
            .getStoredProcedure(storedProcedureDef.getId())
            .delete();

    }
    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        CosmosStoredProcedureProperties storedProcedure = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {" +
                "        var mytext = \"x\";" +
                "        var myval = 1;" +
                "        try {" +
                "            console.log(\"The value of %s is %s.\", mytext, myval);" +
                "            getContext().getResponse().setBody(\"Success!\");" +
                "        }" +
                "        catch(err) {" +
                "            getContext().getResponse().setBody(\"inline err: [\" + err.number + \"] \" + err);" +
                "        }" +
                "}");

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedure);
        validateDiagnostics(response, false);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                                                                 .getStoredProcedure(storedProcedure.getId())
                                                                 .execute(null, options);

        assertThat(executeResponse.getActivityId()).isNotEmpty();
        assertThat(executeResponse.getScriptLog()).isNull();
        validateDiagnostics(executeResponse, true);
    }

    @Test(groups = "simple", timeOut = TIMEOUT)
    public void executeStoredProcedureWithScriptLoggingEnabled() throws Exception {
        // Create a stored procedure
        CosmosStoredProcedureProperties storedProcedure = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {" +
                "        var mytext = \"x\";" +
                "        var myval = 1;" +
                "        try {" +
                "            console.log(\"The value of %s is %s.\", mytext, myval);" +
                "            getContext().getResponse().setBody(\"Success!\");" +
                "        }" +
                "        catch(err) {" +
                "            getContext().getResponse().setBody(\"inline err: [\" + err.number + \"] \" + err);" +
                "        }" +
                "}");

        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(storedProcedure);
        validateDiagnostics(response, false);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setScriptLoggingEnabled(true);
        options.setPartitionKey(PartitionKey.NONE);

        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                                                                 .getStoredProcedure(storedProcedure.getId())
                                                                 .execute(null, options);

        String logResult = "The value of x is 1.";
        assertThat(executeResponse.getScriptLog()).isEqualTo(logResult);
        validateDiagnostics(executeResponse, true);
    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void readAllSprocs() throws Exception {
        CosmosStoredProcedureProperties storedProcedureDef = getCosmosStoredProcedureProperties();
        CosmosStoredProcedureResponse response =
            container.getScripts().createStoredProcedure(storedProcedureDef);
        validateDiagnostics(response, false);

        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosStoredProcedureProperties> feedResponseIterator3 =
                container.getScripts().readAllStoredProcedures(cosmosQueryRequestOptions);
        assertThat(feedResponseIterator3.iterator().hasNext()).isTrue();

    }

    @Test(groups = {"simple"}, timeOut = TIMEOUT)
    public void querySprocs() throws Exception {
        CosmosStoredProcedureProperties properties = getCosmosStoredProcedureProperties();
        CosmosStoredProcedureResponse response = container.getScripts().createStoredProcedure(properties);
        validateDiagnostics(response, false);

        String query = String.format("SELECT * from c where c.id = '%s'", properties.getId());
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosStoredProcedureProperties> feedResponseIterator1 =
                container.getScripts().queryStoredProcedures(query, cosmosQueryRequestOptions);
        assertThat(feedResponseIterator1.iterator().hasNext()).isTrue();

        SqlQuerySpec querySpec = new SqlQuerySpec(query);
        CosmosPagedIterable<CosmosStoredProcedureProperties> feedResponseIterator2 =
                container.getScripts().queryStoredProcedures(query, cosmosQueryRequestOptions);
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

    private void validateDiagnostics(CosmosStoredProcedureResponse response, boolean storedProcedureExecuted ) {
        CosmosDiagnostics diagnostics = response.getDiagnostics();
        Duration duration = response.getDuration();
        Map<String, String> responseHeaders = response.getResponseHeaders();
        assertThat(diagnostics).isNotNull();
        assertThat(duration).isNotNull();
        assertThat(duration.toMillis()).isGreaterThan(0);
        assertThat(responseHeaders).isNotEmpty();
        if (storedProcedureExecuted) {
            String responseAsString = response.getResponseAsString();
            assertThat(responseAsString).isNotNull();
        }
    }
}

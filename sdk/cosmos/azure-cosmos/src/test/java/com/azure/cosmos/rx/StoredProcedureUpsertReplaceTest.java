// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncStoredProcedure;
import com.azure.cosmos.models.CosmosStoredProcedureResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosStoredProcedureProperties;
import com.azure.cosmos.models.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StoredProcedureUpsertReplaceTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public StoredProcedureUpsertReplaceTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceStoredProcedure() throws Exception {

        // create a stored procedure
        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        CosmosStoredProcedureProperties readBackSp = createdCollection.getScripts()
                .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block()
                .getProperties();

        // read stored procedure to validate creation
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosStoredProcedureResponse> readObservable = createdCollection.getScripts()
                                                                              .getStoredProcedure(readBackSp.getId()).read(null);

        // validate stored procedure creation
        CosmosResponseValidator<CosmosStoredProcedureResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
                .withId(readBackSp.getId()).withStoredProcedureBody("function() {var x = 10;}").notNullEtag().build();
        validateSuccess(readObservable, validatorForRead);

        // update stored procedure
        readBackSp.setBody("function() {var x = 11;}");

        Mono<CosmosStoredProcedureResponse> replaceObservable = createdCollection.getScripts()
                                                                                 .getStoredProcedure(readBackSp.getId()).replace(readBackSp);

        // validate stored procedure replace
        CosmosResponseValidator<CosmosStoredProcedureResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
                .withId(readBackSp.getId()).withStoredProcedureBody("function() {var x = 11;}").notNullEtag().build();
        validateSuccess(replaceObservable, validatorForReplace);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        // create a stored procedure
        CosmosStoredProcedureProperties storedProcedureDef = BridgeInternal
                .createCosmosStoredProcedureProperties("{" + "  'id': '" + UUID.randomUUID().toString() + "',"
                        + "  'body':" + "    'function () {" + "      for (var i = 0; i < 10; i++) {"
                        + "        getContext().getResponse().appendValue(\"Body\", i);" + "      }" + "    }'" + "}");

        CosmosAsyncStoredProcedure storedProcedure = null;

        CosmosStoredProcedureResponse response = createdCollection.getScripts()
                                                               .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block();

        storedProcedure = createdCollection.getScripts().getStoredProcedure(response.getProperties().getId());

        String result = null;

        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.NONE);
        result = storedProcedure.execute(null, options).block().getResponseAsString();

        assertThat(result).isEqualTo("\"0123456789\"");
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

        createdCollection.getScripts().createStoredProcedure(storedProcedure).block();
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setScriptLoggingEnabled(true);
        options.setPartitionKey(PartitionKey.NONE);

        CosmosStoredProcedureResponse executeResponse = createdCollection.getScripts()
                                                                         .getStoredProcedure(storedProcedure.getId())
                                                                         .execute(null, options).block();

        String logResult = "The value of x is 1.";
        assert executeResponse != null;
        assertThat(executeResponse.getScriptLog()).isEqualTo(logResult);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_StoredProcedureUpsertReplaceTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.annotations.Ignore;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import reactor.core.publisher.Mono;

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
        CosmosStoredProcedureProperties storedProcedureDef = new CosmosStoredProcedureProperties();
        storedProcedureDef.id(UUID.randomUUID().toString());
        storedProcedureDef.body("function() {var x = 10;}");
        CosmosStoredProcedureProperties readBackSp = createdCollection.getScripts()
                .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block()
                .properties();

        // read stored procedure to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosAsyncStoredProcedureResponse> readObservable = createdCollection.getScripts()
                .getStoredProcedure(readBackSp.id()).read(null);

        // validate stored procedure creation
        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
                .withId(readBackSp.id()).withStoredProcedureBody("function() {var x = 10;}").notNullEtag().build();
        validateSuccess(readObservable, validatorForRead);

        // update stored procedure
        readBackSp.body("function() {var x = 11;}");

        Mono<CosmosAsyncStoredProcedureResponse> replaceObservable = createdCollection.getScripts()
                .getStoredProcedure(readBackSp.id()).replace(readBackSp);

        // validate stored procedure replace
        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
                .withId(readBackSp.id()).withStoredProcedureBody("function() {var x = 11;}").notNullEtag().build();
        validateSuccess(replaceObservable, validatorForReplace);
    }

    // FIXME test times out inconsistently
    @Ignore
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        // create a stored procedure
        CosmosStoredProcedureProperties storedProcedureDef = BridgeInternal
                .createCosmosStoredProcedureProperties("{" + "  'id': '" + UUID.randomUUID().toString() + "',"
                        + "  'body':" + "    'function () {" + "      for (var i = 0; i < 10; i++) {"
                        + "        getContext().getResponse().appendValue(\"Body\", i);" + "      }" + "    }'" + "}");

        CosmosAsyncStoredProcedure storedProcedure = null;

        storedProcedure = createdCollection.getScripts()
                .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block()
                .storedProcedure();

        String result = null;

        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.partitionKey(PartitionKey.None);
        result = storedProcedure.execute(null, options).block().responseAsString();

        assertThat(result).isEqualTo("\"0123456789\"");
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

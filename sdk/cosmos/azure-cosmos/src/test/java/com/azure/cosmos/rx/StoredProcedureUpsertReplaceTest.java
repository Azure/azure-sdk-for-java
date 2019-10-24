// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncStoredProcedure;
import com.azure.cosmos.CosmosAsyncStoredProcedureResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.CosmosStoredProcedureProperties;
import com.azure.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.cosmos.PartitionKey;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
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
        storedProcedureDef.setId(UUID.randomUUID().toString());
        storedProcedureDef.setBody("function() {var x = 10;}");
        CosmosStoredProcedureProperties readBackSp = createdCollection.getScripts()
                .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block()
                .getProperties();

        // read stored procedure to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosAsyncStoredProcedureResponse> readObservable = createdCollection.getScripts()
                .getStoredProcedure(readBackSp.getId()).read(null);

        // validate stored procedure creation
        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
                .withId(readBackSp.getId()).withStoredProcedureBody("function() {var x = 10;}").notNullEtag().build();
        validateSuccess(readObservable, validatorForRead);

        // update stored procedure
        readBackSp.setBody("function() {var x = 11;}");

        Mono<CosmosAsyncStoredProcedureResponse> replaceObservable = createdCollection.getScripts()
                .getStoredProcedure(readBackSp.getId()).replace(readBackSp);

        // validate stored procedure replace
        CosmosResponseValidator<CosmosAsyncStoredProcedureResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosAsyncStoredProcedureResponse>()
                .withId(readBackSp.getId()).withStoredProcedureBody("function() {var x = 11;}").notNullEtag().build();
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
                .getStoredProcedure();

        String result = null;

        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(PartitionKey.None);
        result = storedProcedure.execute(null, options).block().getResponseAsString();

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

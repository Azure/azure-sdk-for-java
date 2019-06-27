/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosStoredProcedure;
import com.azure.data.cosmos.CosmosStoredProcedureRequestOptions;
import com.azure.data.cosmos.CosmosStoredProcedureResponse;
import com.azure.data.cosmos.CosmosStoredProcedureProperties;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.RequestOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class StoredProcedureUpsertReplaceTest extends TestSuiteBase {

    private CosmosContainer createdCollection;

    private CosmosClient client;

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
        Mono<CosmosStoredProcedureResponse> readObservable = createdCollection.getScripts()
                .getStoredProcedure(readBackSp.id()).read(null);

        // validate stored procedure creation
        CosmosResponseValidator<CosmosStoredProcedureResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
                .withId(readBackSp.id()).withStoredProcedureBody("function() {var x = 10;}").notNullEtag().build();
        validateSuccess(readObservable, validatorForRead);

        // update stored procedure
        readBackSp.body("function() {var x = 11;}");

        Mono<CosmosStoredProcedureResponse> replaceObservable = createdCollection.getScripts()
                .getStoredProcedure(readBackSp.id()).replace(readBackSp, new RequestOptions());

        // validate stored procedure replace
        CosmosResponseValidator<CosmosStoredProcedureResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosStoredProcedureResponse>()
                .withId(readBackSp.id()).withStoredProcedureBody("function() {var x = 11;}").notNullEtag().build();
        validateSuccess(replaceObservable, validatorForReplace);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void executeStoredProcedure() throws Exception {
        // create a stored procedure
        CosmosStoredProcedureProperties storedProcedureDef = BridgeInternal
                .createCosmosStoredProcedureProperties("{" + "  'id': '" + UUID.randomUUID().toString() + "',"
                        + "  'body':" + "    'function () {" + "      for (var i = 0; i < 10; i++) {"
                        + "        getContext().getResponse().appendValue(\"Body\", i);" + "      }" + "    }'" + "}");

        CosmosStoredProcedure storedProcedure = null;

        storedProcedure = createdCollection.getScripts()
                .createStoredProcedure(storedProcedureDef, new CosmosStoredProcedureRequestOptions()).block()
                .storedProcedure();

        String result = null;

        RequestOptions options = new RequestOptions();
        options.setPartitionKey(PartitionKey.None);
        result = storedProcedure.execute(null, options).block().responseAsString();

        assertThat(result).isEqualTo("\"0123456789\"");
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

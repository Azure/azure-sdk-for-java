// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosAsyncUserDefinedFunctionResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserDefinedFunctionUpsertReplaceTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public UserDefinedFunctionUpsertReplaceTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceUserDefinedFunction() throws Exception {

        // create a udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");

        CosmosUserDefinedFunctionProperties readBackUdf = null;

            readBackUdf = createdCollection.getScripts().createUserDefinedFunction(udf).block().getProperties();

        // read udf to validate creation
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosAsyncUserDefinedFunctionResponse> readObservable = createdCollection.getScripts().getUserDefinedFunction(readBackUdf.getId()).read();

        // validate udf creation
        CosmosResponseValidator<CosmosAsyncUserDefinedFunctionResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosAsyncUserDefinedFunctionResponse>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);

        //update udf
        readBackUdf.setBody("function() {var x = 11;}");

        Mono<CosmosAsyncUserDefinedFunctionResponse> replaceObservable = createdCollection.getScripts().getUserDefinedFunction(readBackUdf.getId()).replace(readBackUdf);

        //validate udf replace
        CosmosResponseValidator<CosmosAsyncUserDefinedFunctionResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosAsyncUserDefinedFunctionResponse>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(replaceObservable, validatorForReplace);
    }

    @BeforeClass(groups = { "simple" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_UserDefinedFunctionUpsertReplaceTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncUserDefinedFunction;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserDefinedFunctionCrudTest extends TestSuiteBase {

    private CosmosAsyncContainer createdCollection;
    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public UserDefinedFunctionCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createUserDefinedFunction() throws Exception {
        // create udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        Mono<CosmosUserDefinedFunctionResponse> createObservable = createdCollection.getScripts().createUserDefinedFunction(udf);

        // validate udf creation
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validator = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(udf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readUserDefinedFunction() throws Exception {
        // create a udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        createdCollection.getScripts().createUserDefinedFunction(udf).block();
        CosmosAsyncUserDefinedFunction readBackUdf = createdCollection.getScripts().getUserDefinedFunction(udf.getId());

        // read udf
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosUserDefinedFunctionResponse> readObservable = readBackUdf.read();

        //validate udf read
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validator = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(udf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteUserDefinedFunction() throws Exception {
        // create a udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );
        createdCollection.getScripts().createUserDefinedFunction(udf).block();
        CosmosAsyncUserDefinedFunction readBackUdf = createdCollection.getScripts().getUserDefinedFunction(udf.getId());

            // delete udf
        Mono<CosmosUserDefinedFunctionResponse> deleteObservable = readBackUdf.delete();

        // validate udf delete
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validator = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_UserDefinedFunctionCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

}

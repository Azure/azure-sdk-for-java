// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosResponse;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUserDefinedFunction;
import com.azure.data.cosmos.CosmosUserDefinedFunctionProperties;
import com.azure.data.cosmos.CosmosUserDefinedFunctionResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserDefinedFunctionCrudTest extends TestSuiteBase {

    private CosmosContainer createdCollection;
    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public UserDefinedFunctionCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createUserDefinedFunction() throws Exception {
        // create udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");

        Mono<CosmosUserDefinedFunctionResponse> createObservable = createdCollection.getScripts().createUserDefinedFunction(udf);

        // validate udf creation
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validator = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(udf.id())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readUserDefinedFunction() throws Exception {
        // create a udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");
        CosmosUserDefinedFunction readBackUdf = createdCollection.getScripts().createUserDefinedFunction(udf).block().userDefinedFunction();

        // read udf
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosUserDefinedFunctionResponse> readObservable = readBackUdf.read();

        //validate udf read
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validator = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(udf.id())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteUserDefinedFunction() throws Exception {
        // create a udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");
        CosmosUserDefinedFunction readBackUdf = createdCollection.getScripts().createUserDefinedFunction(udf).block().userDefinedFunction();

        // delete udf
        Mono<CosmosResponse> deleteObservable = readBackUdf.delete();

        // validate udf delete
        CosmosResponseValidator<CosmosResponse> validator = new CosmosResponseValidator.Builder<CosmosResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);
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

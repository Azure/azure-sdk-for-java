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

//FIXME beforeClass times out inconsistently.
@Ignore
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
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");

        CosmosUserDefinedFunctionProperties readBackUdf = null;

            readBackUdf = createdCollection.getScripts().createUserDefinedFunction(udf).block().properties();

        // read udf to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder());
        Mono<CosmosAsyncUserDefinedFunctionResponse> readObservable = createdCollection.getScripts().getUserDefinedFunction(readBackUdf.id()).read();

        // validate udf creation
        CosmosResponseValidator<CosmosAsyncUserDefinedFunctionResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosAsyncUserDefinedFunctionResponse>()
                .withId(readBackUdf.id())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update udf
        readBackUdf.body("function() {var x = 11;}");

        Mono<CosmosAsyncUserDefinedFunctionResponse> replaceObservable = createdCollection.getScripts().getUserDefinedFunction(readBackUdf.id()).replace(readBackUdf);

        //validate udf replace
        CosmosResponseValidator<CosmosAsyncUserDefinedFunctionResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosAsyncUserDefinedFunctionResponse>()
                .withId(readBackUdf.id())
                .withUserDefinedFunctionBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(replaceObservable, validatorForReplace);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

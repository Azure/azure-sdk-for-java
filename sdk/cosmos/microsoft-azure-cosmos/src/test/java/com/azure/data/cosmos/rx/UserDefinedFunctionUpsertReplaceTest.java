// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUserDefinedFunctionProperties;
import com.azure.data.cosmos.CosmosUserDefinedFunctionResponse;
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

    private CosmosContainer createdCollection;

    private CosmosClient client;

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
        Mono<CosmosUserDefinedFunctionResponse> readObservable = createdCollection.getScripts().getUserDefinedFunction(readBackUdf.id()).read();

        // validate udf creation
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(readBackUdf.id())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update udf
        readBackUdf.body("function() {var x = 11;}");

        Mono<CosmosUserDefinedFunctionResponse> replaceObservable = createdCollection.getScripts().getUserDefinedFunction(readBackUdf.id()).replace(readBackUdf);

        //validate udf replace
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(readBackUdf.id())
                .withUserDefinedFunctionBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(replaceObservable, validatorForReplace);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

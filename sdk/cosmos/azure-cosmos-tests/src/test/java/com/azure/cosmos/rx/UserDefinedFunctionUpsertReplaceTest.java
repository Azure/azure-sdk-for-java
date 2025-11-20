// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosNettyLeakDetectorFactory;
import com.azure.cosmos.models.CosmosUserDefinedFunctionResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
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

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void replaceUserDefinedFunction() throws Exception {

        // create a udf
        CosmosUserDefinedFunctionProperties udf = new CosmosUserDefinedFunctionProperties(
            UUID.randomUUID().toString(),
            "function() {var x = 10;}"
        );

        CosmosUserDefinedFunctionProperties readBackUdf = null;

            readBackUdf = createdCollection.getScripts().createUserDefinedFunction(udf).block().getProperties();

        // read udf to validate creation
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        Mono<CosmosUserDefinedFunctionResponse> readObservable =
            createdCollection.getScripts().getUserDefinedFunction(readBackUdf.getId()).read();

        // validate udf creation
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);

        //update udf
        readBackUdf.setBody("function() {var x = 11;}");

        Mono<CosmosUserDefinedFunctionResponse> replaceObservable =
            createdCollection.getScripts().getUserDefinedFunction(readBackUdf.getId()).replace(readBackUdf);

        //validate udf replace
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validatorForReplace = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(replaceObservable, validatorForReplace);
    }

    @BeforeClass(groups = { "fast" }, timeOut = 4 * SETUP_TIMEOUT)
    public void before_UserDefinedFunctionUpsertReplaceTest() {
        client = getClientBuilder().buildAsyncClient();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "fast" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

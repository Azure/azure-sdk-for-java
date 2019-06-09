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

import java.util.UUID;

import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.directconnectivity.Protocol;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosRequestOptions;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUserDefinedFunctionResponse;
import com.azure.data.cosmos.CosmosUserDefinedFunctionSettings;
import com.azure.data.cosmos.RequestOptions;

import reactor.core.publisher.Mono;

public class UserDefinedFunctionUpsertReplaceTest extends TestSuiteBase {

    private CosmosContainer createdCollection;

    private CosmosClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public UserDefinedFunctionUpsertReplaceTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceUserDefinedFunction() throws Exception {

        // create a udf
        CosmosUserDefinedFunctionSettings udf = new CosmosUserDefinedFunctionSettings();
        udf.id(UUID.randomUUID().toString());
        udf.body("function() {var x = 10;}");

        CosmosUserDefinedFunctionSettings readBackUdf = null;

        try {
            readBackUdf = createdCollection.createUserDefinedFunction(udf, new CosmosRequestOptions()).block().settings();
        } catch (Throwable error) {
            if (this.clientBuilder.getConfigs().getProtocol() == Protocol.TCP) {
                String message = String.format("DIRECT TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.getDesiredConsistencyLevel());
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
        
        // read udf to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder);
        Mono<CosmosUserDefinedFunctionResponse> readObservable = createdCollection.getUserDefinedFunction(readBackUdf.id()).read(new RequestOptions());

        // validate udf creation
        CosmosResponseValidator<CosmosUserDefinedFunctionResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosUserDefinedFunctionResponse>()
                .withId(readBackUdf.id())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update udf
        readBackUdf.body("function() {var x = 11;}");

        Mono<CosmosUserDefinedFunctionResponse> replaceObservable = createdCollection.getUserDefinedFunction(readBackUdf.id()).replace(readBackUdf, new RequestOptions());

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
        client = clientBuilder.build();
        createdCollection = getSharedMultiPartitionCosmosContainer(client);
        truncateCollection(createdCollection);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }
}

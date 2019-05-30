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
package com.microsoft.azure.cosmosdb.rx;

import java.util.UUID;

import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.UserDefinedFunction;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import rx.Observable;

import javax.net.ssl.SSLException;


public class UserDefinedFunctionUpsertReplaceTest extends TestSuiteBase {

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuildersWithDirect")
    public UserDefinedFunctionUpsertReplaceTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertUserDefinedFunction() throws Exception {

        // create a udf
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");

        UserDefinedFunction readBackUdf = null;

        try {
            readBackUdf = client.upsertUserDefinedFunction(getCollectionLink(), udf, null).toBlocking().single().getResource();
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }

        // read udf to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder);
        Observable<ResourceResponse<UserDefinedFunction>> readObservable = client.readUserDefinedFunction(readBackUdf.getSelfLink(), null);

        // validate udf create
        ResourceResponseValidator<UserDefinedFunction> validatorForRead = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update udf
        readBackUdf.setBody("function() {var x = 11;}");

        Observable<ResourceResponse<UserDefinedFunction>> updateObservable = client.upsertUserDefinedFunction(getCollectionLink(), readBackUdf, null);

        // validate udf update
        ResourceResponseValidator<UserDefinedFunction> validatorForUpdate = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replaceUserDefinedFunction() throws Exception {

        // create a udf
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");

        UserDefinedFunction readBackUdf = null;

        try {
            readBackUdf = client.createUserDefinedFunction(getCollectionLink(), udf, null).toBlocking().single().getResource();
        } catch (Throwable error) {
            if (this.clientBuilder.configs.getProtocol() == Protocol.Tcp) {
                String message = String.format("Direct TCP test failure ignored: desiredConsistencyLevel=%s", this.clientBuilder.desiredConsistencyLevel);
                logger.info(message, error);
                throw new SkipException(message, error);
            }
            throw error;
        }
        
        // read udf to validate creation
        waitIfNeededForReplicasToCatchUp(clientBuilder);
        Observable<ResourceResponse<UserDefinedFunction>> readObservable = client.readUserDefinedFunction(readBackUdf.getSelfLink(), null);

        // validate udf creation
        ResourceResponseValidator<UserDefinedFunction> validatorForRead = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validatorForRead);
        
        //update udf
        readBackUdf.setBody("function() {var x = 11;}");

        Observable<ResourceResponse<UserDefinedFunction>> replaceObservable = client.replaceUserDefinedFunction(readBackUdf, null);

        //validate udf replace
        ResourceResponseValidator<UserDefinedFunction> validatorForReplace = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .withId(readBackUdf.getId())
                .withUserDefinedFunctionBody("function() {var x = 11;}")
                .notNullEtag()
                .build();
        validateSuccess(replaceObservable, validatorForReplace);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = SHARED_DATABASE;
        truncateCollection(createdCollection = SHARED_SINGLE_PARTITION_COLLECTION_WITHOUT_PARTITION_KEY);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }
}

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


public class UserDefinedFunctionCrudTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(UserDefinedFunctionCrudTest.class);

    private Database createdDatabase;
    private DocumentCollection createdCollection;

    private AsyncDocumentClient.Builder clientBuilder;
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public UserDefinedFunctionCrudTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createUserDefinedFunction() throws Exception {
        // create udf
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");

        Observable<ResourceResponse<UserDefinedFunction>> createObservable = client.createUserDefinedFunction(getCollectionLink(), udf, null);

        // validate udf creation
        ResourceResponseValidator<UserDefinedFunction> validator = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .withId(udf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readUserDefinedFunction() throws Exception {
        // create a udf
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");
        UserDefinedFunction readBackUdf = client.createUserDefinedFunction(getCollectionLink(), udf, null).toBlocking().single().getResource();

        // read udf
        Observable<ResourceResponse<UserDefinedFunction>> readObservable = client.readUserDefinedFunction(readBackUdf.getSelfLink(), null);

        //validate udf read
        ResourceResponseValidator<UserDefinedFunction> validator = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .withId(udf.getId())
                .withUserDefinedFunctionBody("function() {var x = 10;}")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deleteUserDefinedFunction() throws Exception {
        // create a udf
        UserDefinedFunction udf = new UserDefinedFunction();
        udf.setId(UUID.randomUUID().toString());
        udf.setBody("function() {var x = 10;}");
        UserDefinedFunction readBackUdf = client.createUserDefinedFunction(getCollectionLink(), udf, null).toBlocking().single().getResource();

        // delete udf
        Observable<ResourceResponse<UserDefinedFunction>> deleteObservable = client.deleteUserDefinedFunction(readBackUdf.getSelfLink(), null);

        // validate udf delete
        ResourceResponseValidator<UserDefinedFunction> validator = new ResourceResponseValidator.Builder<UserDefinedFunction>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        Database d = new Database();
        d.setId(DATABASE_ID);
        createdDatabase = safeCreateDatabase(client, d);
        createdCollection = createCollection(client, createdDatabase.getId(), getCollectionDefinition());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, createdDatabase.getId());
        safeClose(client);
    }

    private String getCollectionLink() {
        return createdCollection.getSelfLink();
    }
}

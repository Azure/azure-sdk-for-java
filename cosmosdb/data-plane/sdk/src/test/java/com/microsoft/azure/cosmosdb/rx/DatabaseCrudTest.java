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

import com.microsoft.azure.cosmosdb.DatabaseForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

import javax.net.ssl.SSLException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseCrudTest extends TestSuiteBase {
    private final String preExistingDatabaseId = DatabaseForTest.generateId();
    private final List<String> databases = new ArrayList<>();
    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public DatabaseCrudTest(Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase() throws Exception {
        Database databaseDefinition = new Database();
        databaseDefinition.setId(DatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        // create the database
        Observable<ResourceResponse<Database>> createObservable = client.createDatabase(databaseDefinition, null);

        // validate
        ResourceResponseValidator<Database> validator = new ResourceResponseValidator.Builder<Database>()
                .withId(databaseDefinition.getId()).build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createDatabase_AlreadyExists() throws Exception {
        Database databaseDefinition = new Database();
        databaseDefinition.setId(DatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());

        client.createDatabase(databaseDefinition, null).toBlocking().single();

        // attempt to create the database
        Observable<ResourceResponse<Database>> createObservable = client.createDatabase(databaseDefinition, null);

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase() throws Exception {
        // read database
        Observable<ResourceResponse<Database>> readObservable = client
                .readDatabase(Utils.getDatabaseNameLink(preExistingDatabaseId), null);

        // validate
        ResourceResponseValidator<Database> validator = new ResourceResponseValidator.Builder<Database>()
                .withId(preExistingDatabaseId).build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readDatabase_DoesntExist() throws Exception {
        // read database
        Observable<ResourceResponse<Database>> readObservable = client
                .readDatabase(Utils.getDatabaseNameLink("I don't exist"), null);

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, validator);
    }


    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        // create the database
        Database databaseDefinition = new Database();
        databaseDefinition.setId(DatabaseForTest.generateId());
        databases.add(databaseDefinition.getId());
        client.createDatabase(databaseDefinition, null).toCompletable().await();

        // delete the database
        Observable<ResourceResponse<Database>> deleteObservable = client
                .deleteDatabase(Utils.getDatabaseNameLink(databaseDefinition.getId()), null);

        // validate
        ResourceResponseValidator<Database> validator = new ResourceResponseValidator.Builder<Database>()
                .nullResource().build();
        validateSuccess(deleteObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteDatabase_DoesntExist() throws Exception {
        // delete the database
        Observable<ResourceResponse<Database>> deleteObservable = client
                .deleteDatabase(Utils.getDatabaseNameLink("I don't exist"), null);

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteObservable, validator);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, preExistingDatabaseId);
        for(String dbId: databases) {
            safeDeleteDatabase(client, dbId);
        }
        safeClose(client);
    }
}

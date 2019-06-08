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
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.rx.FailureValidator;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import reactor.core.publisher.Mono;

public class CosmosDatabaseCrudTest extends CosmosTestSuiteBase {
    private final static String PRE_EXISTING_DATABASE_ID = getDatabaseId(CosmosDatabaseCrudTest.class) + "1";
    private final static String DATABASE_ID2 = getDatabaseId(CosmosDatabaseCrudTest.class) + "2";

    private CosmosClient client;
    private CosmosClientBuilder clientBuilder;

    @Factory(dataProvider = "clientBuilders")
    public CosmosDatabaseCrudTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void createDatabase() throws Exception {
        CosmosDatabaseSettings databaseSettings = new CosmosDatabaseSettings(DATABASE_ID2);

        // create the database
        Mono<CosmosDatabaseResponse> createMono = client.createDatabase(databaseSettings);

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse> validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(databaseSettings.getId()).build();
        validateSuccess(createMono , validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void createDatabase_AlreadyExists() throws Exception {
        CosmosDatabaseSettings databaseSettings = new CosmosDatabaseSettings(DATABASE_ID2);

        client.createDatabase(databaseSettings).block();

        // attempt to create the database again
        Mono<CosmosDatabaseResponse> createMono = client.createDatabase(databaseSettings);

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceAlreadyExists().build();
        validateFailure(createMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void readDatabase() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readMono = client.getDatabase(PRE_EXISTING_DATABASE_ID).read();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse>  validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .withId(PRE_EXISTING_DATABASE_ID).build();
        validateSuccess(readMono, validator);
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void readDatabase_DoesntExist() throws Exception {
        // read database
        Mono<CosmosDatabaseResponse> readMono = client.getDatabase("I dont exist").read();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readMono, validator);
    }


    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void deleteDatabase() throws Exception {
        // delete the database
        Mono<CosmosDatabaseResponse> deleteMono = client.getDatabase(PRE_EXISTING_DATABASE_ID).delete();

        // validate
        CosmosResponseValidator<CosmosDatabaseResponse>  validator = new CosmosResponseValidator.Builder<CosmosDatabaseResponse>()
                .nullResource().build();
        validateSuccess(deleteMono, validator);
//        //TODO validate after deletion the resource is actually deleted (not found)
    }

    @Test(groups = { "cosmosv3" }, timeOut = TIMEOUT)
    public void deleteDatabase_DoesntExist() throws Exception {
        // delete the database
        Mono<CosmosDatabaseResponse> deleteMono = client
                .getDatabase("I don't exist").delete();

        // validate
        FailureValidator validator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(deleteMono, validator);

    }

    @BeforeClass(groups = { "cosmosv3" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
    }

    @AfterClass(groups = { "cosmosv3" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    @AfterMethod(groups = { "cosmosv3" }, timeOut = SHUTDOWN_TIMEOUT)
    public void afterMethod() {
        safeDeleteDatabase(client, PRE_EXISTING_DATABASE_ID);
        safeDeleteDatabase(client, DATABASE_ID2);
    }

    @BeforeMethod(groups = { "cosmosv3" }, timeOut = SETUP_TIMEOUT)
    public void beforeMethod() {
        createDatabase(client, PRE_EXISTING_DATABASE_ID);
    }
}

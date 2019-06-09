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

import com.microsoft.azure.cosmos.CosmosClientBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosDatabaseForTest;
import com.microsoft.azure.cosmos.CosmosResponseValidator;
import com.microsoft.azure.cosmos.CosmosUser;
import com.microsoft.azure.cosmos.CosmosUserResponse;
import com.microsoft.azure.cosmos.CosmosUserSettings;

import reactor.core.publisher.Mono;

public class UserCrudTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosDatabase createdDatabase;
    
    private CosmosClient client;

    @Factory(dataProvider = "clientBuilders")
    public UserCrudTest(CosmosClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createUser() throws Exception {
        //create user
        CosmosUserSettings user = new CosmosUserSettings();
        user.setId(UUID.randomUUID().toString());
        
        Mono<CosmosUserResponse> createObservable = createdDatabase.createUser(user, null);

        // validate user creation
        CosmosResponseValidator<CosmosUserResponse> validator = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(user.getId())
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
 
        //create user
        CosmosUserSettings user = new CosmosUserSettings();
        user.setId(UUID.randomUUID().toString());
       
        CosmosUser readBackUser = createdDatabase.createUser(user, null).block().getUser();

        // read user
        Mono<CosmosUserResponse> readObservable = readBackUser.read(null);
        
        //validate user read
        CosmosResponseValidator<CosmosUserResponse> validator = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        //create user
        CosmosUserSettings user = new CosmosUserSettings();
        user.setId(UUID.randomUUID().toString());
        
        CosmosUser readBackUser = createdDatabase.createUser(user, null).block().getUser();

        // delete user
        Mono<CosmosUserResponse> deleteObservable = readBackUser.delete(null);

        // validate user delete
        CosmosResponseValidator<CosmosUserResponse> validator = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        // attempt to read the user which was deleted
        Mono<CosmosUserResponse> readObservable = readBackUser.read(null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }
    
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void upsertUser() throws Exception {

        //create user
        CosmosUserSettings user = new CosmosUserSettings();
        user.setId(UUID.randomUUID().toString());
        
        Mono<CosmosUserResponse> upsertObservable = createdDatabase.upsertUser(user, null);
        
        //validate user upsert
        CosmosResponseValidator<CosmosUserResponse> validatorForUpsert = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(user.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(upsertObservable, validatorForUpsert);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replaceUser() throws Exception {

        //create user
        CosmosUserSettings user = new CosmosUserSettings();
        user.setId(UUID.randomUUID().toString());
        
        CosmosUserSettings readBackUser = createdDatabase.createUser(user, null).block().getCosmosUserSettings();
        
        // read user to validate creation
        Mono<CosmosUserResponse> readObservable = createdDatabase.getUser(user.getId()).read();
        
        //validate user read
        CosmosResponseValidator<CosmosUserResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosUserResponse>()
        .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validatorForRead);
        
        //update user
        String oldId = readBackUser.getId();
        readBackUser.setId(UUID.randomUUID().toString());

        Mono<CosmosUserResponse> updateObservable = createdDatabase.getUser(oldId).replace(readBackUser, null);

        // validate user replace
        CosmosResponseValidator<CosmosUserResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(updateObservable, validatorForUpdate);  
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}

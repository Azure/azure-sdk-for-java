// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;


import com.azure.data.cosmos.*;
import com.azure.data.cosmos.CosmosAsyncClient;
import com.azure.data.cosmos.internal.FailureValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserCrudTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncDatabase createdDatabase;
    
    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public UserCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createUser() throws Exception {
        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        
        Mono<CosmosAsyncUserResponse> createObservable = createdDatabase.createUser(user);

        // validate user creation
        CosmosResponseValidator<CosmosAsyncUserResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncUserResponse>()
                .withId(user.getId())
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
 
        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
       
        CosmosAsyncUser readBackUser = createdDatabase.createUser(user).block().getUser();

        // read user
        Mono<CosmosAsyncUserResponse> readObservable = readBackUser.read();
        
        //validate user read
        CosmosResponseValidator<CosmosAsyncUserResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncUserResponse>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        
        CosmosAsyncUser readBackUser = createdDatabase.createUser(user).block().getUser();

        // delete user
        Mono<CosmosAsyncUserResponse> deleteObservable = readBackUser.delete();

        // validate user delete
        CosmosResponseValidator<CosmosAsyncUserResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncUserResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        // attempt to read the user which was deleted
        Mono<CosmosAsyncUserResponse> readObservable = readBackUser.read();
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }
    
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void upsertUser() throws Exception {

        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        
        Mono<CosmosAsyncUserResponse> upsertObservable = createdDatabase.upsertUser(user);
        
        //validate user upsert
        CosmosResponseValidator<CosmosAsyncUserResponse> validatorForUpsert = new CosmosResponseValidator.Builder<CosmosAsyncUserResponse>()
                .withId(user.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(upsertObservable, validatorForUpsert);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replaceUser() throws Exception {

        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());
        
        CosmosUserProperties readBackUser = createdDatabase.createUser(user).block().getProperties();
        
        // read getUser to validate creation
        Mono<CosmosAsyncUserResponse> readObservable = createdDatabase.getUser(user.getId()).read();
        
        //validate user read
        CosmosResponseValidator<CosmosAsyncUserResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosAsyncUserResponse>()
        .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validatorForRead);
        
        //update getUser
        String oldId = readBackUser.getId();
        readBackUser.setId(UUID.randomUUID().toString());

        Mono<CosmosAsyncUserResponse> updateObservable = createdDatabase.getUser(oldId).replace(readBackUser);

        // validate user replace
        CosmosResponseValidator<CosmosAsyncUserResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosAsyncUserResponse>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();
        
        validateSuccess(updateObservable, validatorForUpdate);  
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}

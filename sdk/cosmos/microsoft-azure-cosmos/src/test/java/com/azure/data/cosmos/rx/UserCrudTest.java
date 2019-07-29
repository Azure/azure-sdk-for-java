// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;


import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUser;
import com.azure.data.cosmos.CosmosUserResponse;
import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.internal.FailureValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class UserCrudTest extends TestSuiteBase {

    public final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosDatabase createdDatabase;
    
    private CosmosClient client;

    @Factory(dataProvider = "clientBuilders")
    public UserCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void createUser() throws Exception {
        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.id(UUID.randomUUID().toString());
        
        Mono<CosmosUserResponse> createObservable = createdDatabase.createUser(user);

        // validate user creation
        CosmosResponseValidator<CosmosUserResponse> validator = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(user.id())
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void readUser() throws Exception {
 
        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.id(UUID.randomUUID().toString());
       
        CosmosUser readBackUser = createdDatabase.createUser(user).block().user();

        // read user
        Mono<CosmosUserResponse> readObservable = readBackUser.read();
        
        //validate user read
        CosmosResponseValidator<CosmosUserResponse> validator = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(readBackUser.id())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void deleteUser() throws Exception {
        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.id(UUID.randomUUID().toString());
        
        CosmosUser readBackUser = createdDatabase.createUser(user).block().user();

        // delete user
        Mono<CosmosUserResponse> deleteObservable = readBackUser.delete();

        // validate user delete
        CosmosResponseValidator<CosmosUserResponse> validator = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        // attempt to read the user which was deleted
        Mono<CosmosUserResponse> readObservable = readBackUser.read();
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }
    
    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void upsertUser() throws Exception {

        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.id(UUID.randomUUID().toString());
        
        Mono<CosmosUserResponse> upsertObservable = createdDatabase.upsertUser(user);
        
        //validate user upsert
        CosmosResponseValidator<CosmosUserResponse> validatorForUpsert = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(user.id())
                .notNullEtag()
                .build();
        
        validateSuccess(upsertObservable, validatorForUpsert);
    }

    @Test(groups = { "emulator" }, timeOut = TIMEOUT)
    public void replaceUser() throws Exception {

        //create user
        CosmosUserProperties user = new CosmosUserProperties();
        user.id(UUID.randomUUID().toString());
        
        CosmosUserProperties readBackUser = createdDatabase.createUser(user).block().properties();
        
        // read user to validate creation
        Mono<CosmosUserResponse> readObservable = createdDatabase.getUser(user.id()).read();
        
        //validate user read
        CosmosResponseValidator<CosmosUserResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosUserResponse>()
        .withId(readBackUser.id())
                .notNullEtag()
                .build();
        
        validateSuccess(readObservable, validatorForRead);
        
        //update user
        String oldId = readBackUser.id();
        readBackUser.id(UUID.randomUUID().toString());

        Mono<CosmosUserResponse> updateObservable = createdDatabase.getUser(oldId).replace(readBackUser);

        // validate user replace
        CosmosResponseValidator<CosmosUserResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(readBackUser.id())
                .notNullEtag()
                .build();
        
        validateSuccess(updateObservable, validatorForUpdate);  
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}

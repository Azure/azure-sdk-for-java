// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;


import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.implementation.FailureValidator;
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

        Mono<CosmosUserResponse> createObservable = createdDatabase.createUser(user);

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
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());

        CosmosUserResponse userResponse = createdDatabase.createUser(user).block();
        CosmosAsyncUser readBackUser = createdDatabase.getUser(userResponse.getProperties().getId());

        // read user
        Mono<CosmosUserResponse> readObservable = readBackUser.read();

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
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());

        CosmosUserResponse userResponse = createdDatabase.createUser(user).block();
        CosmosAsyncUser readBackUser = createdDatabase.getUser(userResponse.getProperties().getId());

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
        user.setId(UUID.randomUUID().toString());

        Mono<CosmosUserResponse> upsertObservable = createdDatabase.upsertUser(user);

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
        CosmosUserProperties user = new CosmosUserProperties();
        user.setId(UUID.randomUUID().toString());

        CosmosUserProperties readBackUser = createdDatabase.createUser(user).block().getProperties();

        // read getUser to validate creation
        Mono<CosmosUserResponse> readObservable = createdDatabase.getUser(user.getId()).read();

        //validate user read
        CosmosResponseValidator<CosmosUserResponse> validatorForRead = new CosmosResponseValidator.Builder<CosmosUserResponse>()
        .withId(readBackUser.getId())
                .notNullEtag()
                .build();

        validateSuccess(readObservable, validatorForRead);

        //update getUser
        String oldId = readBackUser.getId();
        readBackUser.setId(UUID.randomUUID().toString());

        Mono<CosmosUserResponse> updateObservable = createdDatabase.getUser(oldId).replace(readBackUser);

        // validate user replace
        CosmosResponseValidator<CosmosUserResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosUserResponse>()
                .withId(readBackUser.getId())
                .notNullEtag()
                .build();

        validateSuccess(updateObservable, validatorForUpdate);
    }

    @BeforeClass(groups = { "emulator" }, timeOut = SETUP_TIMEOUT)
    public void before_UserCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(client);
    }
}

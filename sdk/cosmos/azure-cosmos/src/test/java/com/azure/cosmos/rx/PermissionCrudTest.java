// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncPermission;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.models.ContainerChildResourceType;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.models.CosmosPermissionResponse;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.PermissionMode;
import com.azure.cosmos.implementation.FailureValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

//TODO: change to use external TestSuiteBase
public class PermissionCrudTest extends TestSuiteBase {

    private CosmosAsyncDatabase createdDatabase;
    private CosmosAsyncUser createdUser;
    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosAsyncClient client;

    @Factory(dataProvider = "clientBuilders")
    public PermissionCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createPermission() throws Exception {

        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserDefinition());
        //create getPermission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .setId(UUID.randomUUID().toString())
                .setPermissionMode(PermissionMode.READ)
                .setContainerName("myContainer")
                .setResourcePath(ContainerChildResourceType.ITEM, "doc1");

        Mono<CosmosPermissionResponse> createObservable = createdUser.createPermission(permissionSettings, null);

        // validate getPermission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(permissionSettings.getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionContainerName("myContainer")
                .withPermissionResourceKind(ContainerChildResourceType.ITEM)
                .withPermissionResourceName("doc1")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readPermission() throws Exception {
        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserDefinition());

        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .setId(UUID.randomUUID().toString())
                .setPermissionMode(PermissionMode.READ)
                .setContainerName("myContainer");
        createdUser.createPermission(permissionSettings, null).block();

        // read Permission
        Mono<CosmosPermissionResponse> readObservable = createdUser.getPermission(permissionSettings.getId()).read(null);

        // validate permission read
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(permissionSettings.getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionContainerName("myContainer")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deletePermission() throws Exception {

        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserDefinition());

        // create getPermission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .setId(UUID.randomUUID().toString())
                .setPermissionMode(PermissionMode.READ)
                .setContainerName("myContainer");
        createdUser.createPermission(permissionSettings, null).block();
        CosmosAsyncPermission readBackPermission = createdUser.getPermission(permissionSettings.getId());

        // delete
        Mono<CosmosPermissionResponse> deleteObservable = readBackPermission.delete(null);

        // validate delete permission
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        waitIfNeededForReplicasToCatchUp(getClientBuilder());

        // attempt to read the getPermission which was deleted
        Mono<CosmosPermissionResponse> readObservable = readBackPermission.read( null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertPermission() throws Exception {

        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserDefinition());

        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .setId(UUID.randomUUID().toString())
                .setPermissionMode(PermissionMode.READ)
                .setContainerName("myContainer");
        CosmosPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();

        CosmosPermissionProperties readBackPermissionProperties = readBackPermissionResponse.getProperties();
        // read Permission
        Mono<CosmosPermissionResponse> readObservable = createdUser.getPermission(permissionSettings.getId()).read( null);

        // validate getPermission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermissionProperties.getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionContainerName("myContainer")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);

        //update getPermission
        readBackPermissionProperties = readBackPermissionProperties.setPermissionMode(PermissionMode.ALL);

        Mono<CosmosPermissionResponse> updateObservable = createdUser.upsertPermission(readBackPermissionProperties, null);

        // validate permission update
        CosmosResponseValidator<CosmosPermissionResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermissionProperties.getId())
                .withPermissionMode(PermissionMode.ALL)
                .withPermissionContainerName("myContainer")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replacePermission() throws Exception {

        createdUser = safeCreateUser(client, createdDatabase.getId(), getUserDefinition());

        String id = UUID.randomUUID().toString();
        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .setId(id)
                .setPermissionMode(PermissionMode.READ)
                .setContainerName("myContainer");
        CosmosPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null).block();
        CosmosAsyncPermission readBackPermission = createdUser.getPermission(permissionSettings.getId());
        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermission.read(null);

        // validate getPermission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermissionResponse.getProperties().getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionContainerName("myContainer")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);

        //update getPermission
        CosmosPermissionProperties readBackPermissionProperties = readBackPermissionResponse.getProperties();
        readBackPermissionProperties = readBackPermissionProperties.setPermissionMode(PermissionMode.ALL);

        Mono<CosmosPermissionResponse> updateObservable = readBackPermission.replace(readBackPermissionProperties, null);

        // validate permission replace
        CosmosResponseValidator<CosmosPermissionResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermissionProperties.getId())
                .withPermissionMode(PermissionMode.ALL)
                .withPermissionContainerName("myContainer")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void before_PermissionCrudTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static CosmosUserProperties getUserDefinition() {
        return new CosmosUserProperties()
                .setId(UUID.randomUUID().toString());
    }

}

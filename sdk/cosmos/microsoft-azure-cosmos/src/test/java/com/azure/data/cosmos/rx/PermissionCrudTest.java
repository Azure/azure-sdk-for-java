// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosPermission;
import com.azure.data.cosmos.CosmosPermissionResponse;
import com.azure.data.cosmos.CosmosPermissionProperties;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUser;
import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.internal.FailureValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

//TODO: change to use external TestSuiteBase
public class PermissionCrudTest extends TestSuiteBase {

    private CosmosDatabase createdDatabase;
    private CosmosUser createdUser;
    private final String databaseId = CosmosDatabaseForTest.generateId();

    private CosmosClient client;

    @Factory(dataProvider = "clientBuilders")
    public PermissionCrudTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void createPermission() throws Exception {

        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());
        //create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");

        Mono<CosmosPermissionResponse> createObservable = createdUser.createPermission(permissionSettings, null);

        // validate permission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(permissionSettings.id())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(createObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void readPermission() throws Exception {
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());
        
        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermission = createdUser.createPermission(permissionSettings, null)
                .block();

        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermission.permission().read(null);

        // validate permission read
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(permissionSettings.id())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void deletePermission() throws Exception {
        
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());
        
        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermission = createdUser.createPermission(permissionSettings, null)
                .block();        
        // delete
        Mono<CosmosPermissionResponse> deleteObservable = readBackPermission.permission()
                .delete(null);

        // validate delete permission
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        // attempt to read the permission which was deleted
        Mono<CosmosPermissionResponse> readObservable = readBackPermission.permission()
                .read( null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertPermission() throws Exception {
        
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());
        
        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();
        CosmosPermissionProperties readBackPermission = readBackPermissionResponse.properties();
        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermissionResponse.permission()
                .read( null);
        
        // validate permission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermission.id())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update permission
        readBackPermission = readBackPermission.permissionMode(PermissionMode.ALL);

        Mono<CosmosPermissionResponse> updateObservable = createdUser.upsertPermission(readBackPermission, null);

        // validate permission update
        CosmosResponseValidator<CosmosPermissionResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermission.id())
                .withPermissionMode(PermissionMode.ALL)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }
    
    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void replacePermission() throws Exception {
        
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());
        
        String id = UUID.randomUUID().toString();
        // create permission
        CosmosPermissionProperties permissionSettings = new CosmosPermissionProperties()
                .id(id)
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();        
        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermissionResponse.permission()
                .read(null);

        // validate permission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermissionResponse.permission().id())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update permission
        CosmosPermissionProperties readBackPermission = readBackPermissionResponse.properties();
        readBackPermission = readBackPermission.permissionMode(PermissionMode.ALL);
        
        CosmosPermission cosmosPermission = createdUser.getPermission(id);
        Mono<CosmosPermissionResponse> updateObservable = readBackPermissionResponse.permission()
                .replace(readBackPermission, null);

        // validate permission replace
        CosmosResponseValidator<CosmosPermissionResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermission.id())
                .withPermissionMode(PermissionMode.ALL)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        createdDatabase = createDatabase(client, databaseId);
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private static CosmosUserProperties getUserDefinition() {
        return new CosmosUserProperties()
                .id(UUID.randomUUID().toString());
    }
    
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncPermission;
import com.azure.cosmos.CosmosAsyncPermissionResponse;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDatabaseForTest;
import com.azure.cosmos.CosmosPermissionProperties;
import com.azure.cosmos.CosmosResponseValidator;
import com.azure.cosmos.CosmosUserProperties;
import com.azure.cosmos.PermissionMode;
import com.azure.cosmos.internal.FailureValidator;
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
                .setResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");

        Mono<CosmosAsyncPermissionResponse> createObservable = createdUser.createPermission(permissionSettings, null);

        // validate getPermission creation
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .withId(permissionSettings.getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
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
                .setResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosAsyncPermissionResponse readBackPermission = createdUser.createPermission(permissionSettings, null)
                .block();

        // read Permission
        Mono<CosmosAsyncPermissionResponse> readObservable = readBackPermission.getPermission().read(null);

        // validate permission read
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .withId(permissionSettings.getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
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
                .setResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosAsyncPermissionResponse readBackPermission = createdUser.createPermission(permissionSettings, null)
                .block();        
        // delete
        Mono<CosmosAsyncPermissionResponse> deleteObservable = readBackPermission.getPermission()
                .delete(null);

        // validate delete permission
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        // attempt to read the getPermission which was deleted
        Mono<CosmosAsyncPermissionResponse> readObservable = readBackPermission.getPermission()
                .read( null);
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
                .setResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosAsyncPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();
        CosmosPermissionProperties readBackPermission = readBackPermissionResponse.getProperties();
        // read Permission
        Mono<CosmosAsyncPermissionResponse> readObservable = readBackPermissionResponse.getPermission()
                .read( null);
        
        // validate getPermission creation
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .withId(readBackPermission.getId())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update getPermission
        readBackPermission = readBackPermission.setPermissionMode(PermissionMode.ALL);

        Mono<CosmosAsyncPermissionResponse> updateObservable = createdUser.upsertPermission(readBackPermission, null);

        // validate permission update
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .withId(readBackPermission.getId())
                .withPermissionMode(PermissionMode.ALL)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
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
                .setResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosAsyncPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();        
        // read Permission
        Mono<CosmosAsyncPermissionResponse> readObservable = readBackPermissionResponse.getPermission()
                .read(null);

        // validate getPermission creation
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .withId(readBackPermissionResponse.getPermission().id())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update getPermission
        CosmosPermissionProperties readBackPermission = readBackPermissionResponse.getProperties();
        readBackPermission = readBackPermission.setPermissionMode(PermissionMode.ALL);
        
        CosmosAsyncPermission cosmosPermission = createdUser.getPermission(id);
        Mono<CosmosAsyncPermissionResponse> updateObservable = readBackPermissionResponse.getPermission()
                .replace(readBackPermission, null);

        // validate permission replace
        CosmosResponseValidator<CosmosAsyncPermissionResponse> validatorForUpdate = new CosmosResponseValidator.Builder<CosmosAsyncPermissionResponse>()
                .withId(readBackPermission.getId())
                .withPermissionMode(PermissionMode.ALL)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(updateObservable, validatorForUpdate);   
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().buildAsyncClient();
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

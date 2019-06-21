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
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientBuilder;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosDatabaseForTest;
import com.azure.data.cosmos.CosmosPermission;
import com.azure.data.cosmos.CosmosPermissionResponse;
import com.azure.data.cosmos.CosmosPermissionSettings;
import com.azure.data.cosmos.CosmosResponseValidator;
import com.azure.data.cosmos.CosmosUser;
import com.azure.data.cosmos.CosmosUserSettings;
import com.azure.data.cosmos.PermissionMode;
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
        CosmosPermissionSettings permissionSettings = new CosmosPermissionSettings()
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
        CosmosPermissionSettings permissionSettings = new CosmosPermissionSettings()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermission = createdUser.createPermission(permissionSettings, null)
                .block();

        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermission.getPermission().read(null);

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
        CosmosPermissionSettings permissionSettings = new CosmosPermissionSettings()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermission = createdUser.createPermission(permissionSettings, null)
                .block();        
        // delete
        Mono<CosmosPermissionResponse> deleteObservable = readBackPermission.getPermission()
                .delete(null);

        // validate delete permission
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .nullResource()
                .build();
        validateSuccess(deleteObservable, validator);

        waitIfNeededForReplicasToCatchUp(clientBuilder());

        // attempt to read the permission which was deleted
        Mono<CosmosPermissionResponse> readObservable = readBackPermission.getPermission()
                .read( null);
        FailureValidator notFoundValidator = new FailureValidator.Builder().resourceNotFound().build();
        validateFailure(readObservable, notFoundValidator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void upsertPermission() throws Exception {
        
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());
        
        // create permission
        CosmosPermissionSettings permissionSettings = new CosmosPermissionSettings()
                .id(UUID.randomUUID().toString())
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();
        CosmosPermissionSettings readBackPermission = readBackPermissionResponse.settings();
        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermissionResponse.getPermission()
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
        CosmosPermissionSettings permissionSettings = new CosmosPermissionSettings()
                .id(id)
                .permissionMode(PermissionMode.READ)
                .resourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=");
        CosmosPermissionResponse readBackPermissionResponse = createdUser.createPermission(permissionSettings, null)
                .block();        
        // read Permission
        Mono<CosmosPermissionResponse> readObservable = readBackPermissionResponse.getPermission()
                .read(null);

        // validate permission creation
        CosmosResponseValidator<CosmosPermissionResponse> validator = new CosmosResponseValidator.Builder<CosmosPermissionResponse>()
                .withId(readBackPermissionResponse.getPermission().id())
                .withPermissionMode(PermissionMode.READ)
                .withPermissionResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgTc=")
                .notNullEtag()
                .build();
        validateSuccess(readObservable, validator);
        
        //update permission
        CosmosPermissionSettings readBackPermission = readBackPermissionResponse.settings();
        readBackPermission = readBackPermission.permissionMode(PermissionMode.ALL);
        
        CosmosPermission cosmosPermission = createdUser.getPermission(id);
        Mono<CosmosPermissionResponse> updateObservable = readBackPermissionResponse.getPermission()
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

    private static CosmosUserSettings getUserDefinition() {
        return new CosmosUserSettings()
                .id(UUID.randomUUID().toString());
    }
    
}

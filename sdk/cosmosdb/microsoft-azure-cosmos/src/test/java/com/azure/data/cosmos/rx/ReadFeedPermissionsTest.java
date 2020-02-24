// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.rx;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.*;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.TestSuiteBase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO: change to use external TestSuiteBase
public class ReadFeedPermissionsTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private User createdUser;
    private List<Permission> createdPermissions = new ArrayList<>();

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedPermissionsTest(AsyncDocumentClient.Builder clientBuilder) {
        super(clientBuilder);
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readPermissions() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Flux<FeedResponse<Permission>> feedObservable = client.readPermissions(getUserLink(), options);

        int expectedPageSize = (createdPermissions.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Permission> validator = new FeedResponseListValidator.Builder<Permission>()
                .totalSize(createdPermissions.size())
                .numberOfPages(expectedPageSize)
                .exactlyContainsInAnyOrder(createdPermissions.stream().map(Resource::resourceId).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<Permission>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder().build();
        Database d = new Database();
        d.id(databaseId);
        createdDatabase = createDatabase(client, d);
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());

        for(int i = 0; i < 5; i++) {
            createdPermissions.add(createPermissions(client, i));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder());
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, databaseId);
        safeClose(client);
    }

    private static User getUserDefinition() {
        User user = new User();
        user.id(UUID.randomUUID().toString());
        return user;
    }

    public Permission createPermissions(AsyncDocumentClient client, int index) {
        Permission permission = new Permission();
        permission.id(UUID.randomUUID().toString());
        permission.setPermissionMode(PermissionMode.READ);
        permission.setResourceLink("dbs/AQAAAA==/colls/AQAAAJ0fgT" + Integer.toString(index) + "=");
        return client.createPermission(getUserLink(), permission, null).single().block().getResource();
    }

    private String getUserLink() {
        return "dbs/" + getDatabaseId() + "/users/" + getUserId();
    }

    private String getDatabaseId() {
        return createdDatabase.id();
    }

    private String getUserId() {
        return createdUser.id();
    }
}

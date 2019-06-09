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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.azure.data.cosmos.AsyncDocumentClient;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DatabaseForTest;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Permission;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.User;
import com.azure.data.cosmos.internal.TestSuiteBase;

import rx.Observable;

//TODO: change to use external TestSuiteBase 
public class ReadFeedPermissionsTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private User createdUser;
    private List<Permission> createdPermissions = new ArrayList<>();

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public ReadFeedPermissionsTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = FEED_TIMEOUT)
    public void readPermissions() throws Exception {

        FeedOptions options = new FeedOptions();
        options.maxItemCount(2);

        Observable<FeedResponse<Permission>> feedObservable = client.readPermissions(getUserLink(), options);

        int expectedPageSize = (createdPermissions.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Permission> validator = new FeedResponseListValidator.Builder<Permission>()
                .totalSize(createdPermissions.size())
                .numberOfPages(expectedPageSize)
                .exactlyContainsInAnyOrder(createdPermissions.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .allPagesSatisfy(new FeedResponseValidator.Builder<Permission>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(feedObservable, validator, FEED_TIMEOUT);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        client = clientBuilder.build();
        Database d = new Database();
        d.id(databaseId);
        createdDatabase = createDatabase(client, d);
        createdUser = safeCreateUser(client, createdDatabase.id(), getUserDefinition());

        for(int i = 0; i < 5; i++) {
            createdPermissions.add(createPermissions(client, i));
        }

        waitIfNeededForReplicasToCatchUp(clientBuilder);
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
        return client.createPermission(getUserLink(), permission, null).toBlocking().single().getResource();
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

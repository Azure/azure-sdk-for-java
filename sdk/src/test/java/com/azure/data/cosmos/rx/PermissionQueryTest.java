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

import static org.assertj.core.api.Assertions.assertThat;

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
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.Permission;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.User;
import com.azure.data.cosmos.internal.TestSuiteBase;

import rx.Observable;

//TODO: change to use external TestSuiteBase 
public class PermissionQueryTest extends TestSuiteBase {

    public final String databaseId = DatabaseForTest.generateId();

    private Database createdDatabase;
    private User createdUser;
    private List<Permission> createdPermissions = new ArrayList<>();

    private AsyncDocumentClient client;

    @Factory(dataProvider = "clientBuilders")
    public PermissionQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryWithFilter() throws Exception {

        String filterId = createdPermissions.get(0).id();
        String query = String.format("SELECT * from c where c.id = '%s'", filterId);

        FeedOptions options = new FeedOptions();
        options.maxItemCount(5);
        Observable<FeedResponse<Permission>> queryObservable = client
                .queryPermissions(getUserLink(), query, options);

        List<Permission> expectedDocs = createdPermissions.stream().filter(sp -> filterId.equals(sp.id()) ).collect(Collectors.toList());
        assertThat(expectedDocs).isNotEmpty();

        int expectedPageSize = (expectedDocs.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Permission> validator = new FeedResponseListValidator.Builder<Permission>()
                .totalSize(expectedDocs.size())
                .exactlyContainsInAnyOrder(expectedDocs.stream().map(d -> d.resourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Permission>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator, TIMEOUT);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void query_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Observable<FeedResponse<Permission>> queryObservable = client
                .queryPermissions(getUserLink(), query, options);

        FeedResponseListValidator<Permission> validator = new FeedResponseListValidator.Builder<Permission>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<Permission>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAll() throws Exception {

        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.maxItemCount(3);
        options.enableCrossPartitionQuery(true);
        Observable<FeedResponse<Permission>> queryObservable = client
                .queryPermissions(getUserLink(), query, options);

        int expectedPageSize = (createdPermissions.size() + options.maxItemCount() - 1) / options.maxItemCount();

        FeedResponseListValidator<Permission> validator = new FeedResponseListValidator
                .Builder<Permission>()
                .exactlyContainsInAnyOrder(createdPermissions
                        .stream()
                        .map(d -> d.resourceId())
                        .collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .allPagesSatisfy(new FeedResponseValidator.Builder<Permission>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void invalidQuerySytax() throws Exception {
        String query = "I am an invalid query";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        Observable<FeedResponse<Permission>> queryObservable = client
                .queryPermissions(getUserLink(), query, options);

        FailureValidator validator = new FailureValidator.Builder()
                .instanceOf(CosmosClientException.class)
                .statusCode(400)
                .notNullActivityId()
                .build();
        validateQueryFailure(queryObservable, validator);
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
        safeDeleteDatabase(client, createdDatabase);
        safeClose(client);
    }

    private static User getUserDefinition() {
        User user = new User();
        user.id(UUID.randomUUID().toString());
        return user;
    }

    public Permission createPermissions(AsyncDocumentClient client, int index) {
        DocumentCollection collection = new DocumentCollection();
        collection.id(UUID.randomUUID().toString());
        
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

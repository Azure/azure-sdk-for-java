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
package com.microsoft.azure.cosmosdb.rx;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.User;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient.Builder;

import rx.Observable;

public class UserQueryTest extends TestSuiteBase {

    public final static String DATABASE_ID = getDatabaseId(UserQueryTest.class);

    private List<User> createdUsers = new ArrayList<>();

    private Builder clientBuilder;
    private AsyncDocumentClient client;

    private static String getDatabaseLink() {
        return Utils.getDatabaseNameLink(DATABASE_ID);
    }
    
    @Factory(dataProvider = "clientBuilders")
    public UserQueryTest(AsyncDocumentClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsersWithFilter() throws Exception {
        
        String filterUserId = createdUsers.get(0).getId();
        String query = String.format("SELECT * from c where c.id = '%s'", filterUserId);

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(5);
        Observable<FeedResponse<User>> queryObservable = client.queryUsers(getDatabaseLink(), query, options);

        List<User> expectedUsers = createdUsers.stream()
                .filter(c -> StringUtils.equals(filterUserId, c.getId()) ).collect(Collectors.toList());

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<User> validator = new FeedResponseListValidator.Builder<User>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<User>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryAllUsers() throws Exception {

        String query = String.format("SELECT * from c");

        FeedOptions options = new FeedOptions();
        options.setMaxItemCount(2);
        String databaseLink = Utils.getDatabaseNameLink(DATABASE_ID);
        Observable<FeedResponse<User>> queryObservable = client.queryUsers(databaseLink, query, options);

        List<User> expectedUsers = createdUsers;

        assertThat(expectedUsers).isNotEmpty();

        int expectedPageSize = (expectedUsers.size() + options.getMaxItemCount() - 1) / options.getMaxItemCount();

        FeedResponseListValidator<User> validator = new FeedResponseListValidator.Builder<User>()
                .totalSize(expectedUsers.size())
                .exactlyContainsInAnyOrder(expectedUsers.stream().map(d -> d.getResourceId()).collect(Collectors.toList()))
                .numberOfPages(expectedPageSize)
                .pageSatisfy(0, new FeedResponseValidator.Builder<User>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();

        validateQuerySuccess(queryObservable, validator, 10000);
    }

    @Test(groups = { "simple" }, timeOut = TIMEOUT)
    public void queryUsers_NoResults() throws Exception {

        String query = "SELECT * from root r where r.id = '2'";
        FeedOptions options = new FeedOptions();
        Observable<FeedResponse<User>> queryObservable = client.queryUsers(getDatabaseLink(), query, options);

        FeedResponseListValidator<User> validator = new FeedResponseListValidator.Builder<User>()
                .containsExactly(new ArrayList<>())
                .numberOfPages(1)
                .pageSatisfy(0, new FeedResponseValidator.Builder<User>()
                        .requestChargeGreaterThanOrEqualTo(1.0).build())
                .build();
        validateQuerySuccess(queryObservable, validator);
    }

    @BeforeClass(groups = { "simple" }, timeOut = SETUP_TIMEOUT)
    public void beforeClass() throws Exception {
        client = clientBuilder.build();

        Database d1 = new Database();
        d1.setId(DATABASE_ID);
        safeCreateDatabase(client, d1);

        for(int i = 0; i < 5; i++) {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            createdUsers.add(createUser(client, DATABASE_ID, user));
        }
    }

    @AfterClass(groups = { "simple" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(client, DATABASE_ID);
        safeClose(client);
    }
}

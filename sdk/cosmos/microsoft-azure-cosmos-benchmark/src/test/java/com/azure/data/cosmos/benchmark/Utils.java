// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.ConnectionPolicy;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.DatabaseForTest;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.RetryOptions;
import com.azure.data.cosmos.SqlQuerySpec;
import com.azure.data.cosmos.internal.TestConfigurations;
import reactor.core.publisher.Flux;

public class Utils {
    public static AsyncDocumentClient housekeepingClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        RetryOptions options = new RetryOptions();
        options.maxRetryAttemptsOnThrottledRequests(100);
        options.maxRetryWaitTimeInSeconds(60);
        connectionPolicy.retryOptions(options);
        return new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();
    }

    public static String getCollectionLink(Database db, DocumentCollection collection) {
        return "dbs/" + db.id() + "/colls/" + collection;
    }

    public static Database createDatabaseForTest(AsyncDocumentClient client) {
        return DatabaseForTest.create(DatabaseManagerImpl.getInstance(client)).createdDatabase;
    }

    public static void safeCleanDatabases(AsyncDocumentClient client) {
        if (client != null) {
            DatabaseForTest.cleanupStaleTestDatabases(DatabaseManagerImpl.getInstance(client));
        }
    }

    public static void safeClean(AsyncDocumentClient client, Database database) {
        if (database != null) {
            safeClean(client, database.id());
        }
    }

    public static void safeClean(AsyncDocumentClient client, String databaseId) {
        if (client != null) {
            if (databaseId != null) {
                try {
                    client.deleteDatabase("/dbs/" + databaseId, null).then().block();
                } catch (Exception e) {
                }
            }
        }
    }

    public static String generateDatabaseId() {
        return DatabaseForTest.generateId();
    }

    public static void safeClose(AsyncDocumentClient client) {
        if (client != null) {
            client.close();
        }
    }

    private static class DatabaseManagerImpl implements DatabaseForTest.DatabaseManager {
        public static DatabaseManagerImpl getInstance(AsyncDocumentClient client) {
            return new DatabaseManagerImpl(client);
        }

        private final AsyncDocumentClient client;

        private DatabaseManagerImpl(AsyncDocumentClient client) {
            this.client = client;
        }

        @Override
        public Flux<FeedResponse<Database>> queryDatabases(SqlQuerySpec query) {
            return client.queryDatabases(query, null);
        }

        @Override
        public Flux<ResourceResponse<Database>> createDatabase(Database databaseDefinition) {
            return client.createDatabase(databaseDefinition, null);
        }

        @Override
        public Flux<ResourceResponse<Database>> deleteDatabase(String id) {

            return client.deleteDatabase("dbs/" + id, null);
        }
    }
}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.internal.AsyncDocumentClient;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.internal.Database;
import com.azure.cosmos.internal.DatabaseForTest;
import com.azure.cosmos.internal.DocumentCollection;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.internal.ResourceResponse;
import com.azure.cosmos.RetryOptions;
import com.azure.cosmos.SqlQuerySpec;
import com.azure.cosmos.internal.TestConfigurations;
import reactor.core.publisher.Flux;

public class Utils {
    public static AsyncDocumentClient housekeepingClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        RetryOptions options = new RetryOptions();
        options.setMaxRetryAttemptsOnThrottledRequests(100);
        options.setMaxRetryWaitTimeInSeconds(60);
        connectionPolicy.setRetryOptions(options);
        return new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy)
                .build();
    }

    public static String getCollectionLink(Database db, DocumentCollection collection) {
        return "dbs/" + db.getId() + "/colls/" + collection;
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
            safeClean(client, database.getId());
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.examples;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import org.testng.annotations.AfterSuite;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Utils {

    @AfterSuite(groups = "samples")
    public void cleanupStaleDatabase() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.setConnectionMode(ConnectionMode.DIRECT);
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        connectionPolicy.setThrottlingRetryOptions(options);
        AsyncDocumentClient client = new AsyncDocumentClient.Builder().withServiceEndpoint(TestConfigurations.HOST)
                .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                .withConnectionPolicy(connectionPolicy).withContentResponseOnWriteEnabled(true)
                .build();
        safeCleanDatabases(client);
        client.close();
    }

    public static String getCollectionLink(Database db, DocumentCollection collection) {
        return "dbs/" + db.getId() + "/colls/" + collection;
    }

    public static Database createDatabaseForTest(AsyncDocumentClient client) {
        return DatabaseForTest.create(DatabaseManagerImpl.getInstance(client)).createdDatabase;
    }

    private static void safeCleanDatabases(AsyncDocumentClient client) {
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
                    client.deleteDatabase("/dbs/" + databaseId, null).single().block();
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
        public Mono<ResourceResponse<Database>> createDatabase(Database databaseDefinition) {
            return client.createDatabase(databaseDefinition, null);
        }

        @Override
        public Mono<ResourceResponse<Database>> deleteDatabase(String id) {

            return client.deleteDatabase("dbs/" + id, null);
        }
    }
}

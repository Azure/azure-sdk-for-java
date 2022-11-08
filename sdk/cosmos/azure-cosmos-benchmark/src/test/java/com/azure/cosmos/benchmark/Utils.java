// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DatabaseForTest;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlQuerySpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class Utils {
    public static AsyncDocumentClient housekeepingClient() {
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());
        ThrottlingRetryOptions options = new ThrottlingRetryOptions();
        options.setMaxRetryAttemptsOnThrottledRequests(100);
        options.setMaxRetryWaitTime(Duration.ofSeconds(60));
        connectionPolicy.setThrottlingRetryOptions(options);
        return new AsyncDocumentClient.Builder()
                        .withServiceEndpoint(TestConfigurations.HOST)
                        .withMasterKeyOrResourceToken(TestConfigurations.MASTER_KEY)
                        .withConnectionPolicy(connectionPolicy)
                        .withContentResponseOnWriteEnabled(true)
                        .withClientTelemetryConfig(
                            new CosmosClientTelemetryConfig().sendClientTelemetryToService(false))
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
        public Mono<ResourceResponse<Database>> createDatabase(Database databaseDefinition) {
            return client.createDatabase(databaseDefinition, null);
        }

        @Override
        public Mono<ResourceResponse<Database>> deleteDatabase(String id) {

            return client.deleteDatabase("dbs/" + id, null);
        }
    }
}

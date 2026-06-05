// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    // Tracks CosmosAsyncClient instances created by the AsyncDocumentClient overload of
    // createDummyQueryFeedOperationState below. Those callers historically passed an
    // AsyncDocumentClient (no enclosing CosmosAsyncClient to reuse), forcing this helper
    // to build its own CosmosAsyncClient that previously was never closed. The
    // CosmosNettyLeakDetectorFactory flags such clients as leaks at @AfterClass.
    // Test base classes call closeDummyClients() in @AfterClass to drain this list.
    private static final List<CosmosAsyncClient> DUMMY_CLIENTS = new CopyOnWriteArrayList<>();

    private static final String DATABASES_PATH_SEGMENT = "dbs";
    private static final String COLLECTIONS_PATH_SEGMENT = "colls";
    private static final String DOCUMENTS_PATH_SEGMENT = "docs";
    private static final String USERS_PATH_SEGMENT = "users";

    public static String getDatabaseLink(Database database, boolean isNameBased) {
        if (isNameBased) {
            return getDatabaseNameLink(database.getId());
        } else {
            return database.getSelfLink();
        }
    }

    public static String getDatabaseNameLink(String databaseId) {
        return DATABASES_PATH_SEGMENT + "/" + databaseId;
    }

    public static String getCollectionNameLink(String databaseId, String collectionId) {

        return DATABASES_PATH_SEGMENT + "/" + databaseId + "/" + COLLECTIONS_PATH_SEGMENT + "/" + collectionId;
    }

    public static String getDocumentNameLink(String databaseId, String collectionId, String docId) {

        return DATABASES_PATH_SEGMENT + "/" + databaseId + "/" + COLLECTIONS_PATH_SEGMENT + "/" +collectionId + "/" + DOCUMENTS_PATH_SEGMENT + "/" + docId;
    }
    public static String getUserNameLink(String databaseId, String userId) {

        return DATABASES_PATH_SEGMENT + "/" + databaseId + "/" + USERS_PATH_SEGMENT + "/" + userId;
    }

    public static QueryFeedOperationState createDummyQueryFeedOperationStateWithoutPagedFluxOptions(
        ResourceType resourceType,
        OperationType operationType,
        CosmosQueryRequestOptions options,
        CosmosAsyncClient client) {
        return new QueryFeedOperationState(
            client,
            "SomeSpanName",
            "SomeDBName",
            "SomeContainerName",
            resourceType,
            operationType,
            null,
            options,
            null
        );
    }


    public static QueryFeedOperationState createDummyQueryFeedOperationState(
        ResourceType resourceType,
        OperationType operationType,
        CosmosQueryRequestOptions options,
        AsyncDocumentClient client) {
        CosmosAsyncClient cosmosClient = new CosmosClientBuilder()
            .key(client.getMasterKeyOrResourceToken())
            .endpoint(client.getServiceEndpoint().toString())
            .buildAsyncClient();
        DUMMY_CLIENTS.add(cosmosClient);
        return new QueryFeedOperationState(
            cosmosClient,
            "SomeSpanName",
            "SomeDBName",
            "SomeContainerName",
            resourceType,
            operationType,
            null,
            options,
            new CosmosPagedFluxOptions()
        );
    }

    /**
     * Closes every {@link CosmosAsyncClient} created by the
     * {@link #createDummyQueryFeedOperationState(ResourceType, OperationType, CosmosQueryRequestOptions, AsyncDocumentClient)}
     * overload. Safe to call multiple times. Test base classes invoke this in
     * {@code @AfterClass} so the leak detector does not flag these throw-away
     * inner clients.
     */
    public static void closeDummyClients() {
        for (CosmosAsyncClient client : DUMMY_CLIENTS) {
            try {
                client.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close dummy CosmosAsyncClient created by TestUtils", e);
            }
        }
        DUMMY_CLIENTS.clear();
    }

    public static QueryFeedOperationState createDummyQueryFeedOperationState(ResourceType resourceType,
                                                                             OperationType operationType,
                                                                             CosmosQueryRequestOptions options,
                                                                             CosmosAsyncClient cosmosClient) {
        return new QueryFeedOperationState(
            cosmosClient,
            "SomeSpanName",
            "SomeDBName",
            "SomeContainerName",
            resourceType,
            operationType,
            null,
            options,
            new CosmosPagedFluxOptions()
        );
    }

    public static DiagnosticsClientContext mockDiagnosticsClientContext() {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);
        Mockito.doReturn(new DiagnosticsClientContext.DiagnosticsClientConfig()).when(clientContext).getConfig();
        Mockito
            .doReturn(ImplementationBridgeHelpers
                .CosmosDiagnosticsHelper
                .getCosmosDiagnosticsAccessor()
                .create(clientContext, 1d))
            .when(clientContext).createDiagnostics();

        return clientContext;
    }

    public static RxDocumentServiceRequest mockDocumentServiceRequest(DiagnosticsClientContext clientContext) {
        RxDocumentServiceRequest dsr = Mockito.mock(RxDocumentServiceRequest.class);
        dsr.requestContext = new DocumentServiceRequestContext();
        dsr.requestContext.cosmosDiagnostics = clientContext.createDiagnostics();
        Mockito.doReturn(clientContext.createDiagnostics()).when(dsr).createCosmosDiagnostics();
        Mockito.doReturn(UUID.randomUUID()).when(dsr).getActivityId();
        return dsr;
    }

    private TestUtils() {
    }
}

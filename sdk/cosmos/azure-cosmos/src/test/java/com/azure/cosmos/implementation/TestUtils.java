// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import org.mockito.Mockito;

public class TestUtils {
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

    public static DiagnosticsClientContext mockDiagnosticsClientContext() {
        DiagnosticsClientContext clientContext = Mockito.mock(DiagnosticsClientContext.class);
        Mockito.doReturn(new DiagnosticsClientContext.DiagnosticsClientConfig()).when(clientContext).getConfig();
        Mockito.doReturn(BridgeInternal.createCosmosDiagnostics(clientContext)).when(clientContext).createDiagnostics();

        return clientContext;
    }

    public static RxDocumentServiceRequest mockDocumentServiceRequest(DiagnosticsClientContext clientContext) {
        RxDocumentServiceRequest dsr = Mockito.mock(RxDocumentServiceRequest.class);
        dsr.requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        dsr.requestContext.cosmosDiagnostics = clientContext.createDiagnostics();
        Mockito.doReturn(clientContext.createDiagnostics()).when(dsr).createCosmosDiagnostics();
        return dsr;
    }

    private TestUtils() {
    }
}

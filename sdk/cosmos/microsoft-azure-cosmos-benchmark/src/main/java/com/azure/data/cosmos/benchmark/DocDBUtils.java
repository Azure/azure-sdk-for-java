// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.benchmark;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.SqlParameter;
import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;

class DocDBUtils {

    private DocDBUtils() {
    }

    static Database getDatabase(AsyncDocumentClient client, String databaseId) {
        FeedResponse<Database> feedResponsePages = client
                .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                        new SqlParameterList(new SqlParameter("@id", databaseId))), null)
                .single().block();

        if (feedResponsePages.results().isEmpty()) {
            throw new RuntimeException("cannot find datatbase " + databaseId);
        }
        return feedResponsePages.results().get(0);
    }

    static DocumentCollection getCollection(AsyncDocumentClient client, String databaseLink,
            String collectionId) {
        FeedResponse<DocumentCollection> feedResponsePages = client
                .queryCollections(databaseLink,
                        new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                                new SqlParameterList(new SqlParameter("@id", collectionId))),
                        null)
                .single().block();

        if (feedResponsePages.results().isEmpty()) {
            throw new RuntimeException("cannot find collection " + collectionId);
        }
        return feedResponsePages.results().get(0);
    }
}

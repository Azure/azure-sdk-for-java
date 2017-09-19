package com.microsoft.azure.documentdb.benchmark;

import java.util.Iterator;

import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.documentdb.SqlParameter;
import com.microsoft.azure.documentdb.SqlParameterCollection;
import com.microsoft.azure.documentdb.SqlQuerySpec;

class DocDBUtils {

    private DocDBUtils() {}
    
    public static Database getDatabase(DocumentClient client, String databaseId) {
        FeedResponse<Database> feedResponsePages = client
                .queryDatabases(new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                        new SqlParameterCollection(new SqlParameter("@id", databaseId))), null);
        Iterator<Database> it = feedResponsePages.getQueryIterator();
        if (it == null || !it.hasNext()) {
            throw new RuntimeException("cannot find database " + databaseId);
        }
        return it.next();
    }

    public static DocumentCollection getCollection(DocumentClient client, String databaseLink, String collectionId) {
        FeedResponse<DocumentCollection> feedResponsePages = client
                .queryCollections(databaseLink, new SqlQuerySpec("SELECT * FROM root r WHERE r.id=@id",
                        new SqlParameterCollection(new SqlParameter("@id", collectionId))), null);
        Iterator<DocumentCollection> it = feedResponsePages.getQueryIterator();
        if (it == null || !it.hasNext()) {
            throw new RuntimeException("cannot find collection " + collectionId);
        }
        return it.next();
    }
}

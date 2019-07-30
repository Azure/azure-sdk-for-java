// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static com.azure.data.cosmos.internal.changefeed.implementation.ChangeFeedHelper.Paths.COLLECTIONS_PATH_SEGMENT;
import static com.azure.data.cosmos.internal.changefeed.implementation.ChangeFeedHelper.Paths.DATABASES_ROOT;
import static com.azure.data.cosmos.internal.changefeed.implementation.ChangeFeedHelper.Paths.DOCUMENTS_PATH_SEGMENT;

/**
 * Implement static methods used for various simple transformations and tasks.
 */
class ChangeFeedHelper {
    private static final String DEFAULT_USER_AGENT_SUFFIX = "changefeed-2.2.6";

    public static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
    public static final int HTTP_STATUS_CODE_CONFLICT = 409;
    public static final int HTTP_STATUS_CODE_GONE = 410;
    public static final int HTTP_STATUS_CODE_PRECONDITION_FAILED = 412;
    public static final int HTTP_STATUS_CODE_TOO_MANY_REQUESTS = 429;
    public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

    public static String getDatabaseLink(String databaseName) {
        return String.format("/dbs/%s", databaseName);
    }

    public static String getCollectionLink(String databaseName, String collectionName) {
        return String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    }

    public static class UriFactory {
        /**
         * A database link in the format of "dbs/{0}/".
         *
         * @param databaseId the database ID.
         * @return a database link in the format of "dbs/{0}/".
         */
        public static String createDatabaseUri(String databaseId) {
            String path = String.format("%s/%s/", DATABASES_ROOT, databaseId);

            return getUrlPath(path);
        }

        /**
         * A collection link in the format of "dbs/{0}/colls/{1}/".
         *
         * @param databaseId the database ID.
         * @param collectionId the collection ID.
         * @return a collection link in the format of "dbs/{0}/colls/{1}/".
         */
        public static String createDocumentCollectionUri(String databaseId, String collectionId) {
            String path = String.format("%s/%s/%s/%s/",DATABASES_ROOT, databaseId,
                COLLECTIONS_PATH_SEGMENT, collectionId);

            return getUrlPath(path);
        }

        /**
         * A document link in the format of "dbs/{0}/colls/{1}/docs/{2}/".
         *
         * @param databaseId the database ID.
         * @param collectionId the collection ID.
         * @param documentId the document ID.
         * @return a document link in the format of "dbs/{0}/colls/{1}/docs/{2}/".
         */
        public static String createDocumentUri(String databaseId, String collectionId, String documentId) {
            String path = String.format("%s/%s/%s/%s/%s/%s/",DATABASES_ROOT, databaseId,
                COLLECTIONS_PATH_SEGMENT, collectionId, DOCUMENTS_PATH_SEGMENT, documentId);

            return getUrlPath(path);
        }

        public static String getUrlPath(String path) {
            try {
                URI uri = new URI(
                    "http",
                    "localhost",
                    path,
                    null
                );

                URL url = uri.toURL();

                return url.getPath().substring(1);
            } catch (URISyntaxException | MalformedURLException uriEx) {return null;}
        }
    }

    /**
     * Copied from com.azure.data.cosmos.internal.Paths.
     */
    public static class Paths {
        static final String ROOT = "/";

        public static final String DATABASES_PATH_SEGMENT = "dbs";
        public static final String DATABASES_ROOT = ROOT + DATABASES_PATH_SEGMENT;

        public static final String USERS_PATH_SEGMENT = "users";
        public static final String PERMISSIONS_PATH_SEGMENT = "permissions";
        public static final String COLLECTIONS_PATH_SEGMENT = "colls";
        public static final String STORED_PROCEDURES_PATH_SEGMENT = "sprocs";
        public static final String TRIGGERS_PATH_SEGMENT = "triggers";
        public static final String USER_DEFINED_FUNCTIONS_PATH_SEGMENT = "udfs";
        public static final String CONFLICTS_PATH_SEGMENT = "conflicts";
        public static final String DOCUMENTS_PATH_SEGMENT = "docs";
        public static final String ATTACHMENTS_PATH_SEGMENT = "attachments";

        // /offers
        public static final String OFFERS_PATH_SEGMENT = "offers";
        public static final String OFFERS_ROOT = ROOT + OFFERS_PATH_SEGMENT + "/";

        public static final String ADDRESS_PATH_SEGMENT = "addresses";
        public static final String PARTITIONS_PATH_SEGMENT = "partitions";
        public static final String DATABASE_ACCOUNT_PATH_SEGMENT = "databaseaccount";
        public static final String TOPOLOGY_PATH_SEGMENT = "topology";
        public static final String MEDIA_PATH_SEGMENT = "media";
        public static final String MEDIA_ROOT = ROOT + MEDIA_PATH_SEGMENT;
        public static final String SCHEMAS_PATH_SEGMENT = "schemas";
        public static final String PARTITION_KEY_RANGES_PATH_SEGMENT = "pkranges";

        public static final String USER_DEFINED_TYPES_PATH_SEGMENT = "udts";

        public static final String RID_RANGE_PATH_SEGMENT = "ridranges";
    }

    public static class KeyValuePair<K, V> implements Map.Entry<K, V>
    {
        private K key;
        private V value;

        public KeyValuePair(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public K getKey()
        {
            return this.key;
        }

        public V getValue()
        {
            return this.value;
        }

        public K setKey(K key)
        {
            return this.key = key;
        }

        public V setValue(V value)
        {
            return this.value = value;
        }
    }
}

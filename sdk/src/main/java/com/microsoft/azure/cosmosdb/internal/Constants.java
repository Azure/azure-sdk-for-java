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

package com.microsoft.azure.cosmosdb.internal;

/**
 * Used internally. Constants in the Azure Cosmos DB database service Java SDK.
 */
public final class Constants {

    public static final class Quota {
        // Quota Strings
        public static final String DATABASE = "databases";
        public static final String COLLECTION = "collections";
        public static final String USER = "users";
        public static final String PERMISSION = "permissions";
        public static final String COLLECTION_SIZE = "collectionSize";
        public static final String DOCUMENTS_SIZE = "documentsSize";
        public static final String STORED_PROCEDURE = "storedProcedures";
        public static final String TRIGGER = "triggers";
        public static final String USER_DEFINED_FUNCTION = "functions";
        public static final String DELIMITER_CHARS = "=|;";
        public static final String DOCUMENTS_COUNT = "documentsCount";
    }

    public static final class Properties {
        public static final String ID = "id";
        public static final String R_ID = "_rid";
        public static final String SELF_LINK = "_self";
        public static final String LAST_MODIFIED = "_ts";
        public static final String COUNT = "_count";
        public static final String E_TAG = "_etag";
        public static final String AGGREGATE = "_aggregate";

        public static final String CONSISTENCY_POLICY = "consistencyPolicy";
        public static final String DEFAULT_CONSISTENCY_LEVEL = "defaultConsistencyLevel";
        public static final String MAX_STALENESS_PREFIX = "maxStalenessPrefix";
        public static final String MAX_STALENESS_INTERVAL_IN_SECONDS = "maxIntervalInSeconds";
        public static final String PARENTS = "parents";
        
        public static final String DATABASES_LINK = "_dbs";
        public static final String COLLECTIONS_LINK = "_colls";
        public static final String USERS_LINK = "_users";
        public static final String PERMISSIONS_LINK = "_permissions";
        public static final String ATTACHMENTS_LINK = "_attachments";
        public static final String STORED_PROCEDURES_LINK = "_sprocs";
        public static final String TRIGGERS_LINK = "_triggers";
        public static final String USER_DEFINED_FUNCTIONS_LINK = "_udfs";
        public static final String CONFLICTS_LINK = "_conflicts";
        public static final String DOCUMENTS_LINK = "_docs";
        public static final String RESOURCE_LINK = "resource";
        public static final String MEDIA_LINK = "media";

        public static final String PERMISSION_MODE = "permissionMode";
        public static final String RESOURCE_KEY = "key";
        public static final String TOKEN = "_token";
        
        // Scripting
        public static final String BODY = "body";
        public static final String TRIGGER_TYPE = "triggerType";
        public static final String TRIGGER_OPERATION = "triggerOperation";

        public static final String MAX_SIZE = "maxSize";
        public static final String CURRENT_USAGE = "currentUsage";

        public static final String CONTENT = "content";

        public static final String CONTENT_TYPE = "contentType";

        // ErrorResource.
        public static final String CODE = "code";
        public static final String MESSAGE = "message";
        public static final String ERROR_DETAILS = "errorDetails";
        public static final String ADDITIONAL_ERROR_INFO = "additionalErrorInfo";
        
        // PartitionInfo.
        public static final String RESOURCE_TYPE = "resourceType";
        public static final String SERVICE_INDEX = "serviceIndex";
        public static final String PARTITION_INDEX = "partitionIndex";

        public static final String ADDRESS_LINK = "addresses";
        public static final String USER_REPLICATION_POLICY = "userReplicationPolicy";
        public static final String USER_CONSISTENCY_POLICY = "userConsistencyPolicy";
        public static final String SYSTEM_REPLICATION_POLICY = "systemReplicationPolicy";
        public static final String READ_POLICY = "readPolicy";
        
        //ReplicationPolicy
        public static final String REPLICATION_POLICY = "replicationPolicy";
        public static final String ASYNC_REPLICATION = "asyncReplication";
        public static final String MAX_REPLICA_SET_SIZE = "maxReplicasetSize";
        public static final String MIN_REPLICA_SET_SIZE = "minReplicaSetSize";

        //Indexing Policy.
        public static final String INDEXING_POLICY = "indexingPolicy";            
        public static final String AUTOMATIC = "automatic";
        public static final String STRING_PRECISION = "StringPrecision";
        public static final String NUMERIC_PRECISION = "NumericPrecision";
        public static final String MAX_PATH_DEPTH = "maxPathDepth";
        public static final String INDEXING_MODE = "indexingMode";
        public static final String INDEX_TYPE = "IndexType";
        public static final String INDEX_KIND = "kind";
        public static final String DATA_TYPE = "dataType";
        public static final String PRECISION = "precision";

        public static final String PATHS = "paths";
        public static final String PATH = "path";
        public static final String INCLUDED_PATHS = "includedPaths";
        public static final String EXCLUDED_PATHS = "excludedPaths";
        public static final String INDEXES = "indexes";

        // Conflict.
        public static final String CONFLICT = "conflict";
        public static final String OPERATION_TYPE = "operationType";

        // Offer resource
        public static final String OFFER_TYPE = "offerType";
        public static final String OFFER_VERSION = "offerVersion";
        public static final String OFFER_CONTENT = "content";
        public static final String OFFER_THROUGHPUT = "offerThroughput";
        public static final String OFFER_VERSION_V1 = "V1";
        public static final String OFFER_VERSION_V2 = "V2";
        public static final String OFFER_RESOURCE_ID = "offerResourceId";

        // PartitionKey
        public static final String PARTITION_KEY = "partitionKey";
        public static final String PARTITION_KEY_PATHS = "paths";
        public static final String PARTITION_KIND = "kind";
        public static final String RESOURCE_PARTITION_KEY = "resourcePartitionKey";
        public static final String PARTITION_KEY_RANGE_ID = "partitionKeyRangeId";
        public static final String MIN_INCLUSIVE_EFFECTIVE_PARTITION_KEY = "minInclusiveEffectivePartitionKey";
        public static final String MAX_EXCLUSIVE_EFFECTIVE_PARTITION_KEY = "maxExclusiveEffectivePartitionKey";
        
        // AddressResource
        public static final String IS_PRIMARY = "isPrimary";
        public static final String PROTOCOL = "protocol";
        public static final String LOGICAL_URI = "logicalUri";
        public static final String PHYISCAL_URI = "physcialUri";

        // Time-to-Live
        public static final String TTL = "ttl";
        public static final String DEFAULT_TTL = "defaultTtl";
        
        // Global DB account properties
        public static final String Name = "name";
        public static final String WRITABLE_LOCATIONS = "writableLocations";
        public static final String READABLE_LOCATIONS = "readableLocations";
        public static final String DATABASE_ACCOUNT_ENDPOINT = "databaseAccountEndpoint";
        
        public static final int DEFAULT_MAX_PAGE_SIZE = 100;
    }

    public static final class PartitionedQueryExecutionInfo {
        public static final int VERSION_1 = 1;
    }

    public static final class QueryExecutionContext {
        public static final String INCREMENTAL_FEED_HEADER_VALUE = "Incremental feed";
    }
}

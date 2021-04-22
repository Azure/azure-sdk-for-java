// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

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
        public static final String VALUE = "_value";

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
        public static final String AUTOPILOT_MAX_THROUGHPUT = "maxThroughput";
        public static final String AUTOPILOT_AUTO_UPGRADE_POLICY = "autoUpgradePolicy";
        public static final String AUTOPILOT_AUTO_THROUGHPUT_POLICY = "throughputPolicy";
        public static final String AUTOPILOT_THROUGHPUT_POLICY_INCREMENT_PERCENT = "incrementPercent";
        public static final String AUTOPILOT_SETTINGS = "offerAutopilotSettings";

        public static final String PERMISSION_MODE = "permissionMode";
        public static final String RESOURCE_KEY = "key";
        public static final String TOKEN = "_token";
        public static final String SQL_API_TYPE = "0x10";

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
        public static final String QUERY_ENGINE_CONFIGURATION = "queryEngineConfiguration";

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
        public static final String COMPOSITE_INDEXES = "compositeIndexes";
        public static final String ORDER = "order";
        public static final String SPATIAL_INDEXES = "spatialIndexes";
        public static final String TYPES = "types";

        // Unique index.
        public static final String UNIQUE_KEY_POLICY = "uniqueKeyPolicy";
        public static final String UNIQUE_KEYS = "uniqueKeys";

        // Conflict.
        public static final String CONFLICT = "conflict";
        public static final String OPERATION_TYPE = "operationType";
        public static final String SOURCE_RESOURCE_ID = "resourceId";

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
        public static final String PARTITION_KEY_DEFINITION_VERSION = "version";
        public static final String SYSTEM_KEY = "systemKey";

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
        public static final String ANALYTICAL_STORAGE_TTL = "analyticalStorageTtl";

        // Global DB account properties
        public static final String Name = "name";
        public static final String WRITABLE_LOCATIONS = "writableLocations";
        public static final String READABLE_LOCATIONS = "readableLocations";
        public static final String DATABASE_ACCOUNT_ENDPOINT = "databaseAccountEndpoint";

      //Authorization
        public static final String MASTER_TOKEN = "master";
        public static final String RESOURCE_TOKEN = "resource";
        public static final String AAD_TOKEN = "aad";
        public static final String TOKEN_VERSION = "1.0";
        public static final String AUTH_SCHEMA_TYPE = "type";
        public static final String AUTH_VERSION = "ver";
        public static final String AUTH_SIGNATURE = "sig";
        public static final String READ_PERMISSION_MODE = "read";
        public static final String ALL_PERMISSION_MODE = "all";
        public static final String PATH_SEPARATOR = "/";

        public static final int DEFAULT_MAX_PAGE_SIZE = 100;
        public static final String ENABLE_MULTIPLE_WRITE_LOCATIONS = "enableMultipleWriteLocations";

        // Change feed policy
        public static final String CHANGE_FEED_POLICY = "changeFeedPolicy";
        public static final String LOG_RETENTION_DURATION = "retentionDuration";

        // Conflict resolution policy
        public static final String CONFLICT_RESOLUTION_POLICY = "conflictResolutionPolicy";
        public static final String MODE = "mode";
        public static final String CONFLICT_RESOLUTION_PATH = "conflictResolutionPath";
        public static final String CONFLICT_RESOLUTION_PROCEDURE = "conflictResolutionProcedure";

        //Handler names for RXNetty httpClient
        public static final String SSL_HANDLER_NAME = "ssl-handler";
        public static final String SSL_COMPLETION_HANDLER_NAME = "ssl-completion-handler";
        public static final String HTTP_PROXY_HANDLER_NAME = "http-proxy-handler";
        public static final String LOGGING_HANDLER_NAME = "logging-handler";

        // encryption
        public static final String ENCRYPTION_ALGORITHM_ID = "encryptionAlgorithmId";
        public static final String KEY_WRAP_METADATA = "keyWrapMetadata";
        public static final String WRAPPED_DATA_ENCRYPTION_KEY = "wrappedDataEncryptionKey";
        public static final String CLIENT_ENCRYPTION_POLICY = "clientEncryptionPolicy";
        public static final String  KeyWrapMetadataType = "type";
        public static final String  KeyWrapMetadataValue = "value";
        public static final String  EncryptedInfo = "_ei";
        public final static  String RANDOMIZED = "Randomized";
        public final static  String DETERMINISTIC = "Deterministic";

        // Feed Ranges
        public static final String RANGE = "Range";
        public static final String FEED_RANGE_PARTITION_KEY = "PK";
        public static final String FEED_RANGE_PARTITION_KEY_RANGE_ID = "PKRangeId";

        // Feed Range Composite Continuation Token
        public static final String FEED_RANGE_COMPOSITE_CONTINUATION_VERSION = "V";
        public static final String FEED_RANGE_COMPOSITE_CONTINUATION_RESOURCE_ID = "Rid";
        public static final String FEED_RANGE_COMPOSITE_CONTINUATION_CONTINUATION = "Continuation";

        // Change feed state
        public static final String CHANGE_FEED_STATE_VERSION = "V";
        public static final String CHANGE_FEED_STATE_RESOURCE_ID = "Rid";
        public static final String CHANGE_FEED_STATE_MODE = "Mode";
        public static final String CHANGE_FEED_STATE_CONTINUATION = "Continuation";
        public static final String CHANGE_FEED_STATE_START_FROM = "StartFrom";

        // Change feed startFrom
        public static final String CHANGE_FEED_START_FROM_TYPE = "Type";
        public static final String CHANGE_FEED_START_FROM_POINT_IN_TIME_MS = "PointInTimeMs";
        public static final String CHANGE_FEED_START_FROM_ETAG = "Etag";
    }

    public static final class UrlEncodingInfo {
        public static final String PLUS_SYMBOL_ESCAPED = "\\+";
        public static final String PLUS_SYMBOL_URI_ENCODING = "%2b";
        public static final String SINGLE_SPACE_URI_ENCODING = "%20";
        public static final String UTF_8 = "UTF-8";
    }

    public static final class PartitionedQueryExecutionInfo {
        public static final int VERSION_1 = 1;
    }

    public static final class QueryExecutionContext {
        public static final String INCREMENTAL_FEED_HEADER_VALUE = "Incremental feed";
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

public class WFConstants {
    public static class BackendHeaders {
        public static final String RESOURCE_ID = "x-docdb-resource-id";
        public static final String OWNER_ID = "x-docdb-owner-id";
        public static final String ENTITY_ID = "x-docdb-entity-id";
        public static final String DATABASE_ENTITY_MAX_COUNT = "x-ms-database-entity-max-count";
        public static final String DATABASE_ENTITY_CURRENT_COUNT = "x-ms-database-entity-current-count";
        public static final String COLLECTION_ENTITY_MAX_COUNT = "x-ms-collection-entity-max-count";
        public static final String COLLECTION_ENTITY_CURRENT_COUNT = "x-ms-collection-entity-current-count";
        public static final String USER_ENTITY_MAX_COUNT = "x-ms-user-entity-max-count";
        public static final String USER_ENTITY_CURRENT_COUNT = "x-ms-user-entity-current-count";
        public static final String PERMISSION_ENTITY_MAX_COUNT = "x-ms-permission-entity-max-count";
        public static final String PERMISSION_ENTITY_CURRENT_COUNT = "x-ms-permission-entity-current-count";
        public static final String ROOT_ENTITY_MAX_COUNT = "x-ms-root-entity-max-count";
        public static final String ROOT_ENTITY_CURRENT_COUNT = "x-ms-root-entity-current-count";
        public static final String RESOURCE_SCHEMA_NAME = "x-ms-resource-schema-name";
        public static final String LSN = "lsn";
        public static final String QUORUM_ACKED_LSN = "x-ms-quorum-acked-lsn";
        public static final String QUORUM_ACKED_LLSN = "x-ms-cosmos-quorum-acked-llsn";
        public static final String CURRENT_WRITE_QUORUM = "x-ms-current-write-quorum";
        public static final String CURRENT_REPLICA_SET_SIZE = "x-ms-current-replica-set-size";
        public static final String COLLECTION_PARTITION_INDEX = "collection-partition-index";
        public static final String COLLECTION_SERVICE_INDEX = "collection-service-index";
        public static final String STATUS = "Status";
        public static final String ACTIVITY_ID = "ActivityId";
        public static final String IS_FANOUT_REQUEST = "x-ms-is-fanout-request";
        public static final String PRIMARY_MASTER_KEY = "x-ms-primary-master-key";
        public static final String SECONDARY_MASTER_KEY = "x-ms-secondary-master-key";
        public static final String PRIMARY_READONLY_KEY = "x-ms-primary-readonly-key";
        public static final String SECONDARY_READONLY_KEY = "x-ms-secondary-readonly-key";
        public static final String BIND_REPLICA_DIRECTIVE = "x-ms-bind-replica";
        public static final String DATABASE_ACCOUNT_ID = "x-ms-database-account-id";
        public static final String REQUEST_VALIDATION_FAILURE = "x-ms-request-validation-failure";
        public static final String SUB_STATUS = "x-ms-substatus";
        public static final String PARTITION_KEY_RANGE_ID = "x-ms-documentdb-partitionkeyrangeid";
        public static final String BIND_MIN_EFFECTIVE_PARTITION_KEY = "x-ms-documentdb-bindmineffectivepartitionkey";
        public static final String BIND_MAX_EFFECTIVE_PARTITION_KEY = "x-ms-documentdb-bindmaxeffectivepartitionkey";
        public static final String BIND_PARTITION_KEY_RANGE_ID = "x-ms-documentdb-bindpartitionkeyrangeid";
        public static final String BIND_PARTITION_KEY_RANGE_RID_PREFIX = "x-ms-documentdb-bindpartitionkeyrangeridprefix";
        public static final String MINIMUM_ALLOWED_CLIENT_VERSION = "x-ms-documentdb-minimumallowedclientversion";
        public static final String PARTITION_COUNT = "x-ms-documentdb-partitioncount";
        public static final String COLLECTION_RID = "x-ms-documentdb-collection-rid";
        public static final String XP_ROLE = "x-ms-xp-role";
        public static final String HAS_TENTATIVE_WRITES = "x-ms-cosmosdb-has-tentative-writes";
        public static final String IS_RU_PER_MINUTE_USED = "x-ms-documentdb-is-ru-per-minute-used";
        public static final String QUERY_METRICS = "x-ms-documentdb-query-metrics";
        public static final String GLOBAL_COMMITTED_LSN = "x-ms-global-Committed-lsn";
        public static final String NUMBER_OF_READ_REGIONS = "x-ms-number-of-read-regions";
        public static final String OFFER_REPLACE_PENDING = "x-ms-offer-replace-pending";
        public static final String ITEM_LSN = "x-ms-item-lsn";
        public static final String REMOTE_STORAGE_TYPE = "x-ms-remote-storage-type";
        public static final String RESTORE_STATE = "x-ms-restore-state";
        public static final String COLLECTION_SECURITY_IDENTIFIER = "x-ms-collection-security-identifier";
        public static final String RESTORE_PARAMS = "x-ms-restore-params";
        public static final String SHARE_THROUGHPUT = "x-ms-share-throughput";
        public static final String PARTITION_RESOURCE_FILTER = "x-ms-partition-resource-filter";
        public static final String FEDERATION_ID_FOR_AUTH = "x-ms-federation-for-auth";
        public static final String FORCE_QUERY_SCAN = "x-ms-documentdb-force-query-scan";
        public static final String ENABLE_DYNAMIC_RID_RANGE_ALLOCATION = "x-ms-enable-dynamic-rid-range-allocation";
        public static final String EXCLUDE_SYSTEM_PROPERTIES = "x-ms-exclude-system-properties";
        public static final String LOCAL_LSN = "x-ms-cosmos-llsn";
        public static final String QUORUM_ACKED_LOCAL_LSN = "x-ms-cosmos-quorum-acked-llsn";
        public static final String ITEM_LOCAL_LSN = "x-ms-cosmos-item-llsn";
        public static final String BINARY_ID = "x-ms-binary-id";
        public static final String TIME_TO_LIVE_IN_SECONDS = "x-ms-time-to-live-in-seconds";
        public static final String EFFECTIVE_PARTITION_KEY = "x-ms-effective-partition-key";
        public static final String BINARY_PASSTHROUGH_REQUEST = "x-ms-binary-passthrough-request";
        public static final String FANOUT_OPERATION_STATE = "x-ms-fanout-operation-state";
        public static final String CONTENT_SERIALIZATION_FORMAT = "x-ms-documentdb-content-serialization-format";
        public static final String ALLOW_TENTATIVE_WRITES = "x-ms-cosmos-allow-tentative-writes";
        public static final String IS_USER_REQUEST = "x-ms-cosmos-internal-is-user-request";
    }
}

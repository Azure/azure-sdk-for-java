// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.models.IndexingMode;

/**
 * Constants class of CosmosDB properties
 */
public final class Constants {

    /**
     * Default container name.
     */
    public static final String DEFAULT_CONTAINER_NAME = "";

    /**
     * Default indexing policy automatic.
     */
    public static final boolean DEFAULT_INDEXING_POLICY_OVERWRITE_POLICY = false;

    /**
     * Default indexing policy automatic.
     */
    public static final boolean DEFAULT_INDEXING_POLICY_AUTOMATIC = true;

    /**
     * Default indexing policy mode.
     */
    public static final IndexingMode DEFAULT_INDEXING_POLICY_MODE = IndexingMode.CONSISTENT;

    /**
     * Default repository implement postfix.
     */
    public static final String DEFAULT_REPOSITORY_IMPLEMENT_POSTFIX = "Impl";

    /**
     * Default time to live.
     */
    public static final int DEFAULT_TIME_TO_LIVE = -1; // Indicates never expire

    /**
     * Default auto create container.
     */
    public static final boolean DEFAULT_AUTO_CREATE_CONTAINER = true;

    /**
     * Default auto scale.
     */
    public static final boolean DEFAULT_AUTO_SCALE = false;

    /**
     * ID property name.
     */
    public static final String ID_PROPERTY_NAME = "id";

    /**
     * ETag property default name.
     */
    public static final String ETAG_PROPERTY_DEFAULT_NAME = "_etag";

    /**
     * Cosmos module name.
     */
    public static final String COSMOS_MODULE_NAME = "cosmos";

    /**
     * Cosmos module prefix.
     */
    public static final String COSMOS_MODULE_PREFIX = "cosmos";

    private static final String AZURE_SPRING_DATA_COSMOS = "az-sd-cos";

    /**
     * User agent suffix.
     */
    public static final String USER_AGENT_SUFFIX = AZURE_SPRING_DATA_COSMOS + "/";

    /**
     * Object mapper bean name.
     */
    public static final String OBJECT_MAPPER_BEAN_NAME = "cosmosObjectMapper";

    /**
     * Auditing handler bean name.
     */
    public static final String AUDITING_HANDLER_BEAN_NAME = "cosmosAuditingHandler";

    /**
     * ISO-8601 compatible date pattern.
     */
    public static final String ISO_8601_COMPATIBLE_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSSXXX";

    /**
     * Cosmos Exception Status Codes.
     */
    public static class CosmosExceptionStatusCodes {
        public static final int BADREQUEST = 400;
        public static final int CONFLICT = 409;
        public static final int FORBIDDEN = 403;
        public static final int GONE = 410;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int METHOD_NOT_ALLOWED = 405;
        public static final int NOTFOUND = 404;
        public static final int REQUEST_TIMEOUT = 408;
        public static final int PRECONDITION_FAILED = 412;
        public static final int REQUEST_ENTITY_TOO_LARGE = 413;
        public static final int TOO_MANY_REQUESTS = 429;
        public static final int RETRY_WITH = 449;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int UNAUTHORIZED = 401;
    }

    /**
     * Cosmos Exception Sub Status Codes.
     */
    public static class CosmosExceptionSubStatusCodes {
        // For 410 GONE
        public static final int NAME_CACHE_IS_STALE = 1000;
        public static final int PARTITION_KEY_RANGE_GONE = 1002;
        public static final int COMPLETING_SPLIT_OR_MERGE = 1007;
        public static final int COMPLETING_PARTITION_MIGRATION = 1008;

        // For 408 REQUEST_TIMEOUT
        public static final int CLIENT_OPERATION_TIMEOUT = 20008;
    }

    private Constants() {
    }
}


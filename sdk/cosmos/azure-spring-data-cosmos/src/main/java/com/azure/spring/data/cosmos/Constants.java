// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.models.IndexingMode;

/**
 * Constants class of CosmosDB properties
 */
public final class Constants {

    public static final String DEFAULT_CONTAINER_NAME = "";
    public static final boolean DEFAULT_INDEXING_POLICY_AUTOMATIC = true;
    public static final IndexingMode DEFAULT_INDEXING_POLICY_MODE = IndexingMode.CONSISTENT;
    public static final String DEFAULT_REPOSITORY_IMPLEMENT_POSTFIX = "Impl";
    public static final int DEFAULT_TIME_TO_LIVE = -1; // Indicates never expire
    public static final boolean DEFAULT_AUTO_CREATE_CONTAINER = true;
    public static final boolean DEFAULT_AUTO_SCALE = false;

    public static final String ID_PROPERTY_NAME = "id";
    public static final String ETAG_PROPERTY_DEFAULT_NAME = "_etag";

    public static final String COSMOS_MODULE_NAME = "cosmos";
    public static final String COSMOS_MODULE_PREFIX = "cosmos";

    private static final String AZURE_SPRING_DATA_COSMOS = "az-sd-cos";
    public static final String USER_AGENT_SUFFIX = AZURE_SPRING_DATA_COSMOS + "/";

    public static final String OBJECT_MAPPER_BEAN_NAME = "cosmosObjectMapper";
    public static final String AUDITING_HANDLER_BEAN_NAME = "cosmosAuditingHandler";
    public static final String ISO_8601_COMPATIBLE_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSSXXX";

    private Constants() {
    }
}


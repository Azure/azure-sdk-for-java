// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.data.cosmos.IndexingMode;

/**
 * Constants class of CosmosDB properties
 */
public final class Constants {

    public static final String DEFAULT_COLLECTION_NAME = "";
    public static final String DEFAULT_REQUEST_UNIT = "4000";
    public static final boolean DEFAULT_INDEXINGPOLICY_AUTOMATIC = true;
    public static final IndexingMode DEFAULT_INDEXINGPOLICY_MODE = IndexingMode.CONSISTENT;
    public static final String DEFAULT_REPOSITORY_IMPLEMENT_POSTFIX = "Impl";
    public static final int DEFAULT_TIME_TO_LIVE = -1; // Indicates never expire
    public static final boolean DEFAULT_AUTO_CREATE_CONTAINER = true;

    public static final String ID_PROPERTY_NAME = "id";

    public static final String COSMOSDB_MODULE_NAME = "cosmosdb";
    public static final String COSMOSDB_MODULE_PREFIX = "cosmosdb";
    public static final String COSMOS_MAPPING_CONTEXT = "cosmosMappingContext";

    public static final String USER_AGENT_SUFFIX = "spring-data/";

    public static final String OBJECTMAPPER_BEAN_NAME = "cosmosdbObjectMapper";

    public static final String ISO_8601_COMPATIBLE_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss:SSSXXX";

    private Constants() {
    }
}


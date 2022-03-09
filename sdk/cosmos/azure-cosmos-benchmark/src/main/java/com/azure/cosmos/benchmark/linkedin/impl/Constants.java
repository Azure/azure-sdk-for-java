// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

public class Constants {
    public static final String ID = "id";
    public static final String PARTITION_KEY = "partitioningKey";
    public static final String PARTITION_KEY_PATH = "/" + PARTITION_KEY;

    public final static String METHOD_GET = "GET";
    public final static String METHOD_SQL_QUERY = "SQL_QUERY";
    public final static String ERROR_COUNT = "ErrorCount";
    public final static String TOO_MANY_REQUESTS = "TooManyRequests";
    public final static String DELETED_INDICATOR = "__deletedTs__";

    // IndexingPolicy Constants
    public static final String PARTITIONING_KEY_INDEXING_INCLUDE_PATH = "/" + PARTITION_KEY + "/*";
    public static final String WILDCARD_INDEXING_EXCLUDE_PATH = "/*";

    private Constants() {
    }
}

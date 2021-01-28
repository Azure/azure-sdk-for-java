// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl;

public class Constants {
    public static final String ID = "id";
    public static final String PARTITION_KEY = "partitioningKey";
    public static final String PARTITION_KEY_PATH = "/" + PARTITION_KEY;

    public final static String METHOD_GET = "GET";
    public final static String METHOD_GET_ETAG = "GET_ETAG";
    public final static String ERROR_COUNT = "ErrorCount";
    public final static String CALL_COUNT_TOTAL = "CallCountTotal";
    public final static String NOT_FOUND = "NotFound";
    public final static String TOO_MANY_REQUESTS = "TooManyRequests";
    public final static String DELETED_INDICATOR = "__deletedTs__";

    private Constants() {
    }
}

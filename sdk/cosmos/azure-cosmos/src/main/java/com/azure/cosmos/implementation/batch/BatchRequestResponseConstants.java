// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

/**
 * This contains all the extra constants needed for batch/bulk. This will be usefull even if Hybrid row comes in.
 * This contains all the constants we have in Backend. Any addition to backend should be added here.
 */
public final class BatchRequestResponseConstants {

    // Size limits:
    public static final int MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES = 220201;
    public static final int MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST = 100;

    public static final int DEFAULT_MAX_MICRO_BATCH_INTERVAL_IN_MILLISECONDS = 1000;
    public static final int DEFAULT_MAX_MICRO_BATCH_CONCURRENCY = 1;
    public static final double DEFAULT_MIN_MICRO_BATCH_RETRY_RATE = 0.1;
    public static final double DEFAULT_MAX_MICRO_BATCH_RETRY_RATE = 0.2;

    static final String FIELD_OPERATION_TYPE = "operationType";
    static final String FIELD_RESOURCE_TYPE = "resourceType";
    static final String FIELD_TIME_TO_LIVE_IN_SECONDS = "timeToLiveInSeconds";
    static final String FIELD_ID = "id";
    static final String FIELD_INDEXING_DIRECTIVE = "indexingDirective";
    static final String FIELD_IF_MATCH = "ifMatch";
    static final String FIELD_IF_NONE_MATCH = "ifNoneMatch";
    static final String FIELD_PARTITION_KEY = "partitionKey";
    static final String FIELD_RESOURCE_BODY = "resourceBody";
    static final String FIELD_BINARY_ID = "binaryId";
    static final String FIELD_EFFECTIVE_PARTITIONKEY = "effectivePartitionKey";
    static final String FIELD_STATUS_CODE = "statusCode";
    static final String FIELD_SUBSTATUS_CODE = "subStatusCode";
    static final String FIELD_REQUEST_CHARGE = "requestCharge";
    static final String FIELD_RETRY_AFTER_MILLISECONDS = "retryAfterMilliseconds";
    static final String FIELD_ETAG = "eTag";
    static final String FIELD_MINIMAL_RETURN_PREFERENCE = "minimalReturnPreference";
    static final String FIELD_IS_CLIENTENCRYPTED = "isClientEncrypted";

    // Batch supported operation type for json
    public static final String OPERATION_CREATE = "Create";
    public static final String OPERATION_PATCH = "Patch";
    public static final String OPERATION_READ = "Read";
    public static final String OPERATION_UPSERT = "Upsert";
    public static final String OPERATION_DELETE = "Delete";
    public static final String OPERATION_REPLACE = "Replace";
}

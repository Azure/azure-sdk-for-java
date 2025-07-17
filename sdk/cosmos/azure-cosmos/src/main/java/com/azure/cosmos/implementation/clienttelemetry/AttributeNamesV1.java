// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

public enum AttributeNamesV1 {
    DB_NAMESPACE("db.namespace"),
    DB_SYSTEM_NAME("db.system.name"),
    DB_OPERATION_NAME("db.operation.name"),
    DB_QUERY_TEXT("db.query.text"),
    DB_COLLECTION_NAME("db.collection.name"),
    DB_RESPONSE_STATUS_CODE("db.response.status_code"),
    DB_OPERATION_BATCH_SIZE("db.operation.batch.size"),
    ERROR_TYPE("error.type"),
    SERVER_ADDRESS("server.address"),
    CLIENT_ID("azure.client.id"),
    CDB_NAMESPACE("azure.cosmosdb.namespace"),
    CDB_COLLECTION_NAME("azure.cosmosdb.collection.name"),
    CDB_CONNECTION_MODE("azure.cosmosdb.connection.mode"),
    CDB_OPERATION("azure.cosmosdb.operation"),
    CDB_QUERY_TEXT("azure.cosmosdb.query.text"),
    CDB_RESPONSE_STATUS_CODE("azure.cosmosdb.response.status_code"),
    CDB_RESPONSE_SUB_STATUS_CODE("azure.cosmosdb.response.sub_status_code"),
    CDB_ERROR_MESSAGE("azure.cosmosdb.error.message"),
    CDB_ERROR_STACKTRACE("azure.cosmosdb.error.stacktrace"),
    CDB_CONSISTENCY_LEVEL("azure.cosmosdb.consistency.level"),
    CDB_READ_CONSISTENCY_STRATEGY("azure.cosmosdb.read_consistency.strategy"),
    CDB_REQUEST_CHARGE("azure.cosmosdb.operation.request_charge"),
    CDB_REQUEST_BODY_SIZE("azure.cosmosdb.request.body.size"),
    CDB_MAX_RESPONSE_BODY_SIZE("azure.cosmosdb.response.body.size"),
    CDB_RETRY_COUNT("azure.cosmosdb.retry_count"),
    CDB_CONTACTED_REGIONS("azure.cosmosdb.operation.contacted_regions"),
    CDB_OPERATION_BATCH_SIZE("azure.cosmosdb.operation.batch.size"),
    CDB_IS_EMPTY_COMPLETION("azure.cosmosdb.is_empty_completion");

    private final String name;

    private AttributeNamesV1(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

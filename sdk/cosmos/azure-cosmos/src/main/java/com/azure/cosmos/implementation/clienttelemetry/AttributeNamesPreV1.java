// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

public enum AttributeNamesPreV1 {
    NET_PEER_NAME("net.peer.name"),
    EXCEPTION_ESCAPED("exception.escaped"),
    EXCEPTION_TYPE("exception.type"),
    EXCEPTION_MESSAGE("exception.message"),
    EXCEPTION_STACKTRACE("exception.stacktrace"),
    CDB_OPERATION_TYPE("db.cosmosdb.operation_type"),
    CDB_RESOURCE_TYPE("db.cosmosdb.resource_type"),
    CDB_DB_NAME("db.name"),
    CDB_CLIENT_ID("db.cosmosdb.client_id"),
    CDB_CONNECTION_MODE("db.cosmosdb.connection_mode"),
    CDB_QUERY_STATEMENT("db.statement"),
    CDB_OPERATION_ID("db.cosmosdb.operation_id"),
    CDB_CONTAINER_NAME("db.cosmosdb.container"),
    CDB_STATUS_CODE("db.cosmosdb.status_code"),
    CDB_SUB_STATUS_CODE("db.cosmosdb.sub_status_code"),
    CDB_REQUEST_CHARGE("db.cosmosdb.request_charge"),
    CDB_REQUEST_CONTENT_SIZE("db.cosmosdb.request_content_length"),
    CDB_RESPONSE_CONTENT_SIZE("db.cosmosdb.max_response_content_length_bytes"),
    CDB_RETRY_COUNT("db.cosmosdb.retry_count"),
    CDB_CONTACTED_REGIONS("db.cosmosdb.regions_contacted"),
    CDB_IS_EMPTY_COMPLETION("db.cosmosdb.is_empty_completion"),
    ;

    private final String name;

    private AttributeNamesPreV1(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

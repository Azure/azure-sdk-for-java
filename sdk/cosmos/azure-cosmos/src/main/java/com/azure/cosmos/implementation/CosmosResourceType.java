// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource types in the Azure Cosmos DB database service.
 */
public enum CosmosResourceType {

    SYSTEM(-100, "System"),
    ATTACHMENT(3, "Attachment"),
    DOCUMENT_COLLECTION(1, "DocumentCollection"),
    CONFLICT(107, "Conflict"),
    DATABASE(0, "Database"),
    DOCUMENT(2, "Document"),
    INDEX(104, "Index"),
    OFFER(113, "Offer"),
    PERMISSION(5, "Permission"),
    STORED_PROCEDURE(109, "StoredProcedure"),
    TRIGGER(110, "Trigger"),
    USER(4, "User"),
    USER_DEFINED_FUNCTION(111, "UserDefinedFunction");

    private final int value;
    private final String overWireValue;

    private static Map<String, CosmosResourceType> cosmosResourceTypeMap = new HashMap<>();

    static {
        for (CosmosResourceType crt : CosmosResourceType.values()) {
            cosmosResourceTypeMap.put(crt.toString(), crt);
        }
    }

    /**
     * Given the over wire version of CosmosResourceType gives the corresponding enum or return null
     *
     * @param cosmosResourceType String value of cosmos resource type
     * @return CosmosResourceType Enum Cosmos Resource Type
     */
    public static CosmosResourceType fromServiceSerializedFormat(String cosmosResourceType) {
        // this is 100x faster than org.apache.commons.lang3.EnumUtils.getEnum(String)
        // for more detail refer to https://github.com/moderakh/azure-cosmosdb-benchmark
        return cosmosResourceTypeMap.get(cosmosResourceType);
    }

    CosmosResourceType(int value, String overWireValue) {
        this.value = value;
        this.overWireValue = overWireValue;
    }

    @Override
    public String toString() {
        return this.overWireValue;
    }
}

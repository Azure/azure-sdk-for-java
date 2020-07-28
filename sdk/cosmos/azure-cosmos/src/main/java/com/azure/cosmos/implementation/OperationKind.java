// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.HashMap;
import java.util.Map;

/**
 * Types of operation in Azure Cosmos DB result in conflict
 */
public enum OperationKind {
    CREATE("create"),
    DELETE("delete"),
    REPLACE("replace"),
    UPDATE("update"),
    UNKNOWN("unknown");

    private final String stringValue;
    private static Map<String, OperationKind> operationKindHashMap = new HashMap<>();
    static {
        for (OperationKind cl : OperationKind.values()) {
            operationKindHashMap.put(cl.toString(), cl);
        }
    }
    OperationKind(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Given the over wire version of OperationKind gives the corresponding enum or return UNKNOWN
     *
     * @param operationKind String value of OperationKind
     * @return OperationKind Enum operation kind
     */
    static OperationKind fromServiceSerializedFormat(String operationKind) {

        OperationKind operationKindEnum = operationKindHashMap.get(operationKind);
        if (operationKindEnum == null) {
            operationKindEnum = OperationKind.UNKNOWN;
        }

        return operationKindEnum;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }
}

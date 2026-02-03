// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

public enum AttributeNamesCommon {
    DB_SYSTEM("db.system"),
    DB_OPERATION("db.operation"),
    USER_AGENT("user_agent.original");

    private final String name;

    private AttributeNamesCommon(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

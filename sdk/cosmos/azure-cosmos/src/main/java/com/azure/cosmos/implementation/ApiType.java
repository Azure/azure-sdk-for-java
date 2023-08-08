// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * ApiTypes in the Azure Cosmos DB database service.
 */
public enum ApiType {
    NONE("None"),

    MONGODB("MongoDB"),

    GREMLIN("Gremlin"),

    CASSANDRA("Cassandra"),

    TABLE("Table"),

    ETCD("Etcd"),

    SQL("Sql"),

    GREMLINV2("GremlinV2");

    ApiType(String overWireValue) {
        this.overWireValue = overWireValue;
    }

    private final String overWireValue;

    @Override
    public String toString() {
        return this.overWireValue;
    }
}

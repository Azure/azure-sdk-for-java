// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

/**
 * Defines values for DataFeedSourceType.
 */
public enum DataFeedSourceType {

    /**
     * Enum value AzureApplicationInsights.
     */
    AZURE_APP_INSIGHTS("AzureApplicationInsights"),

    /**
     * Enum value AzureBlob.
     */
    AZURE_BLOB("AzureBlob"),

    /**
     * Enum value AzureDataExplorer.
     */
    AZURE_DATA_EXPLORER("AzureDataExplorer"),

    /**
     * Enum value AzureTable.
     */
    AZURE_TABLE("AzureTable"),

    /**
     * Enum value HttpRequest.
     */
    HTTP_REQUEST("HttpRequest"),

    /**
     * Enum value InfluxDB.
     */
    INFLUX_DB("InfluxDB"),

    /**
     * Enum value MongoDB.
     */
    MONGO_DB("MongoDB"),

    /**
     * Enum value MySql.
     */
    MYSQL_DB("MySql"),

    /**
     * Enum value PostgreSql.
     */
    POSTGRE_SQL_DB("PostgreSql"),

    /**
     * Enum value SqlServer.
     */
    SQL_SERVER_DB("SqlServer"),

    /**
     * Enum value AzureCosmosDB.
     */
    AZURE_COSMOS_DB("AzureCosmosDB"),

    /**
     * Enum value SqlServer.
     */
    ELASTIC_SEARCH("Elasticsearch"),

    /**
     * Enum value AzureCosmosDB.
     */
    AZURE_DATA_LAKE_STORAGE_GEN2("AzureDataLakeStorageGen2");

    /**
     /**
     * The actual serialized value for a DataFeedSourceType instance.
     */
    private final String value;

    DataFeedSourceType(final String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a DataFeedSourceType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DataFeedSourceType object, or null if unable to parse.
     */
    public static DataFeedSourceType fromString(final String value) {
        final DataFeedSourceType[] items = DataFeedSourceType.values();
        for (final DataFeedSourceType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

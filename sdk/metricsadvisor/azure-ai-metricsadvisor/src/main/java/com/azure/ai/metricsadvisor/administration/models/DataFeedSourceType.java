// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for all supported data sources types.
 */
public final class DataFeedSourceType extends ExpandableStringEnum<DataFeedSourceType> {

    /**
     * Static value AzureApplicationInsights for DataFeedSourceType.
     */
    public static final  DataFeedSourceType AZURE_APP_INSIGHTS = fromString("AzureApplicationInsights");

    /**
     * Static value AzureBlob for DataFeedSourceType.
     */
    public static final  DataFeedSourceType AZURE_BLOB = fromString("AzureBlob");

    /**
     * Static value AzureDataExplorer for DataFeedSourceType..
     */
    public static final  DataFeedSourceType AZURE_DATA_EXPLORER = fromString("AzureDataExplorer");

    /**
     * Static value AzureEventHubs for DataFeedSourceType..
     */
    public static final  DataFeedSourceType AZURE_EVENT_HUBS = fromString("AzureEventHubs");

    /**
     * Static value AzureTable for DataFeedSourceType..
     */
    public static final  DataFeedSourceType AZURE_TABLE = fromString("AzureTable");

    /**
     * Static value InfluxDB for DataFeedSourceType..
     */
    public static final  DataFeedSourceType INFLUX_DB = fromString("InfluxDB");

    /**
     * Static value MongoDB for DataFeedSourceType..
     */
    public static final  DataFeedSourceType MONGO_DB = fromString("MongoDB");

    /**
     * Static value MySql for DataFeedSourceType..
     */
    public static final  DataFeedSourceType MYSQL_DB = fromString("MySql");

    /**
     * Static value PostgreSql for DataFeedSourceType..
     */
    public static final  DataFeedSourceType POSTGRE_SQL_DB = fromString("PostgreSql");

    /**
     * Static value SqlServer.
     */
    public static final  DataFeedSourceType SQL_SERVER_DB = fromString("SqlServer");

    /**
     * Static value AzureCosmosDB for DataFeedSourceType..
     */
    public static final  DataFeedSourceType AZURE_COSMOS_DB = fromString("AzureCosmosDB");

    /**
     * Enum value AzureDataLakeStorageGen2 for DataFeedSourceType..
     */
    public static final  DataFeedSourceType AZURE_DATA_LAKE_STORAGE_GEN2 = fromString("AzureDataLakeStorageGen2");

    /**
     * Enum value AzureLogAnalytics for DataFeedSourceType..
     */
    public static final  DataFeedSourceType AZURE_LOG_ANALYTICS = fromString("AzureLogAnalytics");

    /**
     * Creates or finds a DataFeedSourceType from its string representation.
     *
     * @param name a name to look for.
     *
     * @return the corresponding DataFeedSourceType.
     */
    public static DataFeedSourceType fromString(String name) {
        return fromString(name, DataFeedSourceType.class);
    }

    /**
     * @return known DataFeedSourceType values.
     */
    public static Collection<DataFeedSourceType> values() {
        return values(DataFeedSourceType.class);
    }
}

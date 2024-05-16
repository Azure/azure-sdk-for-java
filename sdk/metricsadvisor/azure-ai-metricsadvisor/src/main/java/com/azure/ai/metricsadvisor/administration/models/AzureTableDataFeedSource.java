// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.AzureTableDataFeedSourceAccessor;
import com.azure.core.annotation.Immutable;

/**
 * The AzureTableDataFeedSource model.
 */
@Immutable
public final class AzureTableDataFeedSource extends DataFeedSource {
    /*
     * Azure Table connection string
     */
    private final String connectionString;

    /*
     * Query script
     */
    private final String script;

    /*
     * Table name
     */
    private final String table;

    static {
        AzureTableDataFeedSourceAccessor.setAccessor(
            new AzureTableDataFeedSourceAccessor.Accessor() {
                @Override
                public String getConnectionString(AzureTableDataFeedSource feedSource) {
                    return feedSource.getConnectionString();
                }
            });
    }

    /**
     * Create a AzureTableDataFeedSource instance.
     *
     * @param connectionString the Azure Table connection string
     * @param script the query script.
     * @param table the table name.
     */
    public AzureTableDataFeedSource(final String connectionString, final String script, final String table) {
        this.connectionString = connectionString;
        this.script = script;
        this.table = table;
    }

    /**
     * Get the script property: Query script.
     *
     * @return the script value.
     */
    public String getQueryScript() {
        return this.script;
    }

    /**
     * Get the table property: Table name.
     *
     * @return the table value.
     */
    public String getTableName() {
        return this.table;
    }

    private String getConnectionString() {
        return this.connectionString;
    }
}

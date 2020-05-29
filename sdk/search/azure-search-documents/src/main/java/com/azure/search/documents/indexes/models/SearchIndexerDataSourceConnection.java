// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a datasource definition, which can be used to configure an
 * indexer.
 */
@Fluent
public final class SearchIndexerDataSourceConnection {
    /*
     * The name of the datasource.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * The description of the datasource.
     */
    @JsonProperty(value = "description")
    private String description;

    /*
     * The type of the datasource. Possible values include: 'AzureSql',
     * 'CosmosDb', 'AzureBlob', 'AzureTable', 'MySql'
     */
    @JsonProperty(value = "type", required = true)
    private SearchIndexerDataSourceType type;

    /*
     * The connection string for the datasource.
     */
    @JsonProperty(value = "connectionString")
    private String connectionString;

    /*
     * The data container for the datasource.
     */
    @JsonProperty(value = "container", required = true)
    private SearchIndexerDataContainer container;

    /*
     * The data change detection policy for the datasource.
     */
    @JsonProperty(value = "dataChangeDetectionPolicy")
    private DataChangeDetectionPolicy dataChangeDetectionPolicy;

    /*
     * The data deletion detection policy for the datasource.
     */
    @JsonProperty(value = "dataDeletionDetectionPolicy")
    private DataDeletionDetectionPolicy dataDeletionDetectionPolicy;

    /*
     * The ETag of the data source.
     */
    @JsonProperty(value = "@odata.etag")
    private String eTag;

    /**
     * Get the name property: The name of the datasource.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name of the datasource.
     *
     * @param name the name value to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the description property: The description of the datasource.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: The description of the datasource.
     *
     * @param description the description value to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the type property: The type of the datasource. Possible values
     * include: 'AzureSql', 'CosmosDb', 'AzureBlob', 'AzureTable', 'MySql'.
     *
     * @return the type value.
     */
    public SearchIndexerDataSourceType getType() {
        return this.type;
    }

    /**
     * Set the type property: The type of the datasource. Possible values
     * include: 'AzureSql', 'CosmosDb', 'AzureBlob', 'AzureTable', 'MySql'.
     *
     * @param type the type value to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setType(SearchIndexerDataSourceType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the connectionString property: The connection string for the
     * datasource.
     *
     * @return the connectionString value.
     */
    public String getConnectionString() {
        return this.connectionString;
    }

    /**
     * Set the connectionString property: The connection string for the
     * datasource.
     *
     * @param connectionString the connectionString value to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setConnectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    /**
     * Get the container property: The data container for the datasource.
     *
     * @return the container value.
     */
    public SearchIndexerDataContainer getContainer() {
        return this.container;
    }

    /**
     * Set the container property: The data container for the datasource.
     *
     * @param container the container value to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setContainer(SearchIndexerDataContainer container) {
        this.container = container;
        return this;
    }

    /**
     * Get the dataChangeDetectionPolicy property: The data change detection
     * policy for the datasource.
     *
     * @return the dataChangeDetectionPolicy value.
     */
    public DataChangeDetectionPolicy getDataChangeDetectionPolicy() {
        return this.dataChangeDetectionPolicy;
    }

    /**
     * Set the dataChangeDetectionPolicy property: The data change detection
     * policy for the datasource.
     *
     * @param dataChangeDetectionPolicy the dataChangeDetectionPolicy value to
     * set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setDataChangeDetectionPolicy(
        DataChangeDetectionPolicy dataChangeDetectionPolicy) {
        this.dataChangeDetectionPolicy = dataChangeDetectionPolicy;
        return this;
    }

    /**
     * Get the dataDeletionDetectionPolicy property: The data deletion
     * detection policy for the datasource.
     *
     * @return the dataDeletionDetectionPolicy value.
     */
    public DataDeletionDetectionPolicy getDataDeletionDetectionPolicy() {
        return this.dataDeletionDetectionPolicy;
    }

    /**
     * Set the dataDeletionDetectionPolicy property: The data deletion
     * detection policy for the datasource.
     *
     * @param dataDeletionDetectionPolicy the dataDeletionDetectionPolicy value
     * to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setDataDeletionDetectionPolicy(
        DataDeletionDetectionPolicy dataDeletionDetectionPolicy) {
        this.dataDeletionDetectionPolicy = dataDeletionDetectionPolicy;
        return this;
    }

    /**
     * Get the eTag property: The ETag of the data source.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag property: The ETag of the data source.
     *
     * @param eTag the eTag value to set.
     * @return the SearchIndexerDataSource object itself.
     */
    public SearchIndexerDataSourceConnection setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents service-level resource counters and quotas.
 */
@Fluent
public final class SearchServiceCounters {
    /*
     * Total number of documents across all indexes in the service.
     */
    @JsonProperty(value = "documentCount", required = true)
    private ResourceCounter documentCounter;

    /*
     * Total number of indexes.
     */
    @JsonProperty(value = "indexesCount", required = true)
    private ResourceCounter indexCounter;

    /*
     * Total number of indexers.
     */
    @JsonProperty(value = "indexersCount", required = true)
    private ResourceCounter indexerCounter;

    /*
     * Total number of data sources.
     */
    @JsonProperty(value = "dataSourcesCount", required = true)
    private ResourceCounter dataSourceCounter;

    /*
     * Total size of used storage in bytes.
     */
    @JsonProperty(value = "storageSize", required = true)
    private ResourceCounter storageSizeCounter;

    /*
     * Total number of synonym maps.
     */
    @JsonProperty(value = "synonymMaps", required = true)
    private ResourceCounter synonymMapCounter;

    /**
     * Constructor of {@link SearchServiceCounters}.
     *
     * @param documentCounter Total number of documents across all indexes in the service.
     * @param indexCounter Total number of indexes.
     * @param indexerCounter Total number of indexers.
     * @param dataSourceCounter Total number of data sources.
     * @param storageSizeCounter Total size of used storage in bytes.
     * @param synonymMapCounter Total number of synonym maps.
     */
    @JsonCreator
    SearchServiceCounters(
        @JsonProperty(value = "documentCount") ResourceCounter documentCounter,
        @JsonProperty(value = "indexesCount") ResourceCounter indexCounter,
        @JsonProperty(value = "indexersCount") ResourceCounter indexerCounter,
        @JsonProperty(value = "dataSourcesCount") ResourceCounter dataSourceCounter,
        @JsonProperty(value = "storageSize") ResourceCounter storageSizeCounter,
        @JsonProperty(value = "synonymMaps") ResourceCounter synonymMapCounter) {
        this.documentCounter = documentCounter;
        this.indexCounter = indexCounter;
        this.indexerCounter = indexerCounter;
        this.dataSourceCounter = dataSourceCounter;
        this.storageSizeCounter = storageSizeCounter;
        this.synonymMapCounter = synonymMapCounter;
    }

    /**
     * Get the documentCounter property: Total number of documents across all
     * indexes in the service.
     *
     * @return the documentCounter value.
     */
    public ResourceCounter getDocumentCounter() {
        return this.documentCounter;
    }

    /**
     * Get the indexCounter property: Total number of indexes.
     *
     * @return the indexCounter value.
     */
    public ResourceCounter getIndexCounter() {
        return this.indexCounter;
    }

    /**
     * Get the indexerCounter property: Total number of indexers.
     *
     * @return the indexerCounter value.
     */
    public ResourceCounter getIndexerCounter() {
        return this.indexerCounter;
    }

    /**
     * Get the dataSourceCounter property: Total number of data sources.
     *
     * @return the dataSourceCounter value.
     */
    public ResourceCounter getDataSourceCounter() {
        return this.dataSourceCounter;
    }

    /**
     * Get the storageSizeCounter property: Total size of used storage in
     * bytes.
     *
     * @return the storageSizeCounter value.
     */
    public ResourceCounter getStorageSizeCounter() {
        return this.storageSizeCounter;
    }

    /**
     * Get the synonymMapCounter property: Total number of synonym maps.
     *
     * @return the synonymMapCounter value.
     */
    public ResourceCounter getSynonymMapCounter() {
        return this.synonymMapCounter;
    }

}

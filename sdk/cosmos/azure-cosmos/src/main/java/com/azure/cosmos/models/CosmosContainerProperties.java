// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a item container in the Azure Cosmos DB database service. A cosmos container is a named logical container
 * for cosmos items.
 * <p>
 * A database may contain zero or more named containers and each container consists of zero or more JSON items.
 * Being schema-free, the items in a container do not need to share the same structure or fields. Since containers
 * are application resources, they can be authorized using either the master key or resource keys.
 */
public final class CosmosContainerProperties extends ResourceWrapper {

    private DocumentCollection documentCollection;

    /**
     * Constructor
     *
     * @param id id of the Container
     * @param partitionKeyPath partition key path
     */
    public CosmosContainerProperties(String id, String partitionKeyPath) {
        this.documentCollection = new DocumentCollection();
        documentCollection.setId(id);

        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add(partitionKeyPath);
        partitionKeyDef.setPaths(paths);
        this.documentCollection.setPartitionKey(partitionKeyDef);
    }

    /**
     * Constructor
     *
     * @param id id of the container
     * @param partitionKeyDefinition the {@link PartitionKeyDefinition}
     */
    public CosmosContainerProperties(String id, PartitionKeyDefinition partitionKeyDefinition) {
        this.documentCollection = new DocumentCollection();
        documentCollection.setId(id);
        documentCollection.setPartitionKey(partitionKeyDefinition);
    }

    CosmosContainerProperties(String json) {
        this.documentCollection = new DocumentCollection(json);
    }

    // Converting document collection to CosmosContainerProperties
    CosmosContainerProperties(DocumentCollection collection) {
        this.documentCollection = new DocumentCollection(collection.toJson());
    }

    static List<CosmosContainerProperties> getFromV2Results(List<DocumentCollection> results) {
        return results.stream().map(CosmosContainerProperties::new).collect(Collectors.toList());
    }

    /**
     * Gets the container's indexing policy.
     *
     * @return the indexing policy.
     */
    public IndexingPolicy getIndexingPolicy() {
        return this.documentCollection.getIndexingPolicy();
    }

    /**
     * Sets the container's indexing policy
     *
     * @param indexingPolicy {@link IndexingPolicy} the indexing policy
     * @return the CosmosContainerProperties.
     * @throws IllegalArgumentException the cosmos client exception
     */
    public CosmosContainerProperties setIndexingPolicy(IndexingPolicy indexingPolicy) {
        this.documentCollection.setIndexingPolicy(indexingPolicy);
        return this;
    }

    /**
     * Gets the containers unique key policy
     *
     * @return the unique key policy
     */
    public UniqueKeyPolicy getUniqueKeyPolicy() {
        return this.documentCollection.getUniqueKeyPolicy();
    }

    /**
     * Sets the Containers unique key policy
     *
     * @param uniqueKeyPolicy the unique key policy
     * @return the CosmosContainerProperties.
     * @throws IllegalArgumentException the cosmos client exception
     */
    public CosmosContainerProperties setUniqueKeyPolicy(UniqueKeyPolicy uniqueKeyPolicy) {
        this.documentCollection.setUniqueKeyPolicy(uniqueKeyPolicy);
        return this;
    }

    /**
     * Gets the containers's partition key definition.
     *
     * @return the partition key definition.
     */
    public PartitionKeyDefinition getPartitionKeyDefinition() {
        return this.documentCollection.getPartitionKey();
    }

    /**
     * Sets the containers's partition key definition.
     *
     * @param partitionKeyDefinition the partition key definition.
     * @return the CosmosContainerProperties.
     * @throws IllegalArgumentException the cosmos client exception
     */
    public CosmosContainerProperties setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.documentCollection.setPartitionKey(partitionKeyDefinition);
        return this;
    }

    /**
     * Gets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on documents in different regions, in a collection in the Azure Cosmos DB service.
     *
     * @return ConflictResolutionPolicy
     */
    public ConflictResolutionPolicy getConflictResolutionPolicy() {
        return this.documentCollection.getConflictResolutionPolicy();
    }

    /**
     * Sets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on documents in different regions, in a collection in the Azure Cosmos DB service.
     *
     * @param value ConflictResolutionPolicy to be used.
     * @return the CosmosContainerProperties.
     * @throws IllegalArgumentException the cosmos client exception
     */
    public CosmosContainerProperties setConflictResolutionPolicy(ConflictResolutionPolicy value) {
        this.documentCollection.setConflictResolutionPolicy(value);
        return this;
    }

    /**
     * Gets the collection's default time-to-live value.
     *
     * @return the default time-to-live value in seconds.
     */
    public Integer getDefaultTimeToLiveInSeconds() {
        return this.documentCollection.getDefaultTimeToLive();
    }

    /**
     * Sets the collection's default time-to-live value.
     * <p>
     * The default time-to-live value on a collection is an optional property. If set, the documents within the
     * collection
     * expires after the specified number of seconds since their last write time. The value of this property should
     * be one of the following:
     * <p>
     * null - indicates evaluation of time-to-live is disabled and documents within the collection will never expire,
     * regardless whether
     * individual documents have their time-to-live set.
     * <p>
     * nonzero positive integer - indicates the default time-to-live value for all documents within the collection.
     * This value can be overridden
     * by individual documents' time-to-live value.
     * <p>
     * -1 - indicates by default all documents within the collection never expire. This value can be overridden by
     * individual documents'
     * time-to-live value.
     *
     * @param timeToLive the default time-to-live value in seconds.
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties setDefaultTimeToLiveInSeconds(Integer timeToLive) {
        // a "null" value is represented as a missing element on the wire.
        // setting timeToLive to null should remove the property from the property bag.
        this.documentCollection.setDefaultTimeToLive(timeToLive);

        return this;
    }

    /**
     * Sets the analytical store time to live in seconds for items in a container from the Azure Cosmos DB service.
     *
     * It is an optional property. A valid value must be either a nonzero positive integer, '-1', or 0.
     * By default, AnalyticalStoreTimeToLive is set to 0 meaning the analytical store is turned off for the container;
     * -1 means documents in analytical store never expire.
     * The unit of measurement is seconds. The maximum allowed value is 2147483647.
     *
     * @param timeToLive the analytical store time to live in seconds.
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties setAnalyticalStoreTimeToLiveInSeconds(Integer timeToLive) {
        this.documentCollection.setAnalyticalStorageTimeToLiveInSeconds(timeToLive);

        return this;
    }

    /**
     * Gets the analytical store time to live in seconds for items in a container from the Azure Cosmos DB service.
     *
     * It is an optional property. A valid value must be either a nonzero positive integer, '-1', or 0.
     * By default, AnalyticalStoreTimeToLive is set to 0 meaning the analytical store is turned off for the container;
     * -1 means documents in analytical store never expire.
     * The unit of measurement is seconds. The maximum allowed value is 2147483647.
     *
     * @return analytical ttl
     */
    public Integer getAnalyticalStoreTimeToLiveInSeconds() {
        return this.documentCollection.getAnalyticalStorageTimeToLiveInSeconds();
    }

    DocumentCollection getV2Collection() {
        DocumentCollection collection = new DocumentCollection(this.documentCollection.toJson());
        collection.setPartitionKey(this.getPartitionKeyDefinition());
        collection.setIndexingPolicy(this.getIndexingPolicy());
        return collection;
    }

    @Override
    Resource getResource() {
        return this.documentCollection;
    }
}

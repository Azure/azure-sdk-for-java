// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a container in the Azure Cosmos DB database service. A cosmos container is a named logical container
 * for cosmos items.
 * <p>
 * A database may contain zero or more named containers and each container consists of zero or more JSON items.
 * Being schema-free, the items in a container do not need to share the same structure or fields. Since containers
 * are application resources, they can be authorized using either the master key or resource keys.
 */
public final class CosmosContainerProperties {

    private final DocumentCollection documentCollection;
    private static final String PARTITION_KEY_TOKEN_DELIMETER = "/";

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

    // Converting container to CosmosContainerProperties
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
     */
    public CosmosContainerProperties setPartitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        this.documentCollection.setPartitionKey(partitionKeyDefinition);
        if (this.getClientEncryptionPolicy() != null) {
            this.getClientEncryptionPolicy().validatePartitionKeyPathsAreNotEncrypted(this.getPartitionKeyPathTokensList());
        }

        return this;
    }

    /**
     * Gets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on items in different regions, in a container in the Azure Cosmos DB service.
     *
     * @return ConflictResolutionPolicy
     */
    public ConflictResolutionPolicy getConflictResolutionPolicy() {
        return this.documentCollection.getConflictResolutionPolicy();
    }

    /**
     * Sets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on items in different regions, in a container in the Azure Cosmos DB service.
     *
     * @param value ConflictResolutionPolicy to be used.
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties setConflictResolutionPolicy(ConflictResolutionPolicy value) {
        this.documentCollection.setConflictResolutionPolicy(value);
        return this;
    }

    /**
     * Gets the changeFeedPolicy for this container in the Azure Cosmos DB service.
     *
     * @return ChangeFeedPolicy
     */
    @Beta(value = Beta.SinceVersion.V4_12_0,
        warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ChangeFeedPolicy getChangeFeedPolicy() {
        return this.documentCollection.getChangeFeedPolicy();
    }

    /**
     * Sets the changeFeedPolicy for this container in the Azure Cosmos DB service.
     *
     * @param value ChangeFeedPolicy to be used.
     * @return the CosmosContainerProperties.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0,
        warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosContainerProperties setChangeFeedPolicy(ChangeFeedPolicy value) {
        this.documentCollection.setChangeFeedPolicy(value);
        return this;
    }

    /**
     * Gets the container's default time-to-live value.
     *
     * @return the default time-to-live value in seconds.
     */
    public Integer getDefaultTimeToLiveInSeconds() {
        return this.documentCollection.getDefaultTimeToLive();
    }

    /**
     * Sets the container's default time-to-live value.
     * <p>
     * The default time-to-live value on a container is an optional property. If set, the items within the
     * container
     * expires after the specified number of seconds since their last write time. The value of this property should
     * be one of the following:
     * <p>
     * null - indicates evaluation of time-to-live is disabled and items within the container will never expire,
     * regardless whether
     * individual items have their time-to-live set.
     * <p>
     * nonzero positive integer - indicates the default time-to-live value for all items within the container.
     * This value can be overridden
     * by individual items time-to-live value.
     * <p>
     * -1 - indicates by default all items within the container never expire. This value can be overridden by
     * individual items
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
     * -1 means items in analytical store never expire.
     * The unit of measurement is seconds. The maximum allowed value is 2147483647.
     *
     * @param timeToLive the analytical store time to live in seconds.
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties setAnalyticalStoreTimeToLiveInSeconds(Integer timeToLive) {
        this.documentCollection.setAnalyticalStoreTimeToLiveInSeconds(timeToLive);

        return this;
    }

    /**
     * Gets the analytical store time to live in seconds for items in a container from the Azure Cosmos DB service.
     *
     * It is an optional property. A valid value must be either a nonzero positive integer, '-1', or 0.
     * By default, AnalyticalStoreTimeToLive is set to 0 meaning the analytical store is turned off for the container;
     * -1 means items in analytical store never expire.
     * The unit of measurement is seconds. The maximum allowed value is 2147483647.
     *
     * @return analytical ttl
     */
    public Integer getAnalyticalStoreTimeToLiveInSeconds() {
        return this.documentCollection.getAnalyticalStoreTimeToLiveInSeconds();
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.documentCollection.getId();
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     * @return the current instance of {@link CosmosContainerProperties}.
     */
    public CosmosContainerProperties setId(String id) {
        this.documentCollection.setId(id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    public String getResourceId() {
        return this.documentCollection.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.documentCollection.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.documentCollection.getETag();
    }

    /**
     * Gets the ClientEncryptionPolicy that is used for encrypting item fields
     *
     * @return ClientEncryptionPolicy
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public ClientEncryptionPolicy getClientEncryptionPolicy() {
        return this.documentCollection.getClientEncryptionPolicy();
    }

    /**
     * Sets the ClientEncryptionPolicy that is used for encrypting item fields
     *
     * @param value ClientEncryptionPolicy to be used.
     * @return the CosmosContainerProperties.
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosContainerProperties setClientEncryptionPolicy(ClientEncryptionPolicy value) {
        if (value != null) {
            value.validatePartitionKeyPathsAreNotEncrypted(this.getPartitionKeyPathTokensList());
        }

        this.documentCollection.setClientEncryptionPolicy(value);
        return this;
    }

    Resource getResource() {
        return this.documentCollection;
    }

    String getSelfLink(){
        return this.documentCollection.getSelfLink();
    }

    DocumentCollection getV2Collection() {
        DocumentCollection collection = new DocumentCollection(this.documentCollection.toJson());
        collection.setPartitionKey(this.getPartitionKeyDefinition());
        collection.setIndexingPolicy(this.getIndexingPolicy());
        return collection;
    }

    List<List<String>> getPartitionKeyPathTokensList() {
        if (this.getPartitionKeyDefinition() == null) {
            throw new IllegalStateException("Container partition key is empty");
        }

        List<List<String>> partitionKeyPathTokensList = new ArrayList<>();
        for (String path : this.getPartitionKeyDefinition().getPaths()) {
            String[] splitPaths = path.split(PARTITION_KEY_TOKEN_DELIMETER);
            List<String> splitPathsList = new ArrayList<>();
            for (int i = 0; i < splitPaths.length; i++) {
                if (StringUtils.isNotEmpty(splitPaths[i])) {
                    splitPathsList.add(splitPaths[i]);
                }
            }
            partitionKeyPathTokensList.add(splitPathsList);
        }
        return partitionKeyPathTokensList;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosContainerPropertiesHelper.setCosmosContainerPropertiesAccessor(
            new ImplementationBridgeHelpers.CosmosContainerPropertiesHelper.CosmosContainerPropertiesAccessor() {
                @Override
                public String getSelfLink(CosmosContainerProperties cosmosContainerProperties) {
                    return cosmosContainerProperties.getSelfLink();
                }
            });
    }
}

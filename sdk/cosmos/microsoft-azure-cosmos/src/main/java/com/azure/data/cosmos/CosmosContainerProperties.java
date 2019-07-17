// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.ResourceResponse;

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
public class CosmosContainerProperties extends Resource {

    private IndexingPolicy indexingPolicy;
    private UniqueKeyPolicy uniqueKeyPolicy;
    private PartitionKeyDefinition partitionKeyDefinition;

    /**
     * Constructor 
     * @param id id of the Container
     * @param partitionKeyPath partition key path
     */
    public CosmosContainerProperties(String id, String partitionKeyPath) {
        super.id(id);
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add(partitionKeyPath);
        partitionKeyDef.paths(paths);
        partitionKeyDefinition(partitionKeyDef);
    }

    /**
     * Constructor
     * @param id id of the container
     * @param partitionKeyDefinition the {@link PartitionKeyDefinition}
     */
    public CosmosContainerProperties(String id, PartitionKeyDefinition partitionKeyDefinition) {
        super.id(id);
        partitionKeyDefinition(partitionKeyDefinition);
    }

    CosmosContainerProperties(ResourceResponse<DocumentCollection> response) {
        super(response.getResource().toJson());
    }
    
    // Converting document collection to CosmosContainerProperties
    CosmosContainerProperties(DocumentCollection collection){
        super(collection.toJson());
    }
    
    static List<CosmosContainerProperties> getFromV2Results(List<DocumentCollection> results){
        return results.stream().map(CosmosContainerProperties::new).collect(Collectors.toList());
    }

    /**
     * Gets the container's indexing policy.
     *
     * @return the indexing policy.
     */
    public IndexingPolicy indexingPolicy() {
        if (this.indexingPolicy == null) {
            if (super.has(Constants.Properties.INDEXING_POLICY)) {
                this.indexingPolicy = super.getObject(Constants.Properties.INDEXING_POLICY, IndexingPolicy.class);
            } else {
                this.indexingPolicy = new IndexingPolicy();
            }
        }

        return this.indexingPolicy;
    }

    /**
     * Sets the container's indexing policy
     *
     * @param indexingPolicy {@link IndexingPolicy} the indexing policy
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties indexingPolicy(IndexingPolicy indexingPolicy) {
        if (indexingPolicy == null) {
            throw new IllegalArgumentException("IndexingPolicy cannot be null.");
        }
        this.indexingPolicy = indexingPolicy;
        super.set(Constants.Properties.INDEXING_POLICY, indexingPolicy);
        return this;
    }

    /**
     * Gets the containers unique key policy
     * 
     * @return the unique key policy
     */
    public UniqueKeyPolicy uniqueKeyPolicy() {

        // Thread safe lazy initialization for case when collection is cached (and is basically readonly).
        if (this.uniqueKeyPolicy == null) {
            this.uniqueKeyPolicy = super.getObject(Constants.Properties.UNIQUE_KEY_POLICY, UniqueKeyPolicy.class);

            if (this.uniqueKeyPolicy == null) {
                this.uniqueKeyPolicy = new UniqueKeyPolicy();
            }
        }

        return this.uniqueKeyPolicy;
    }

    /**
     * Sets the Containers unique key policy
     * 
     * @param uniqueKeyPolicy the unique key policy
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties uniqueKeyPolicy(UniqueKeyPolicy uniqueKeyPolicy) {
        if (uniqueKeyPolicy == null) {
            throw new IllegalArgumentException("uniqueKeyPolicy cannot be null.");
        }

        this.uniqueKeyPolicy = uniqueKeyPolicy;
        super.set(Constants.Properties.UNIQUE_KEY_POLICY, uniqueKeyPolicy);
        return this;
    }

    /**
     * Gets the containers's partition key definition.
     *
     * @return the partition key definition.
     */
    public PartitionKeyDefinition partitionKeyDefinition() {
        if (this.partitionKeyDefinition == null) {

            if (super.has(Constants.Properties.PARTITION_KEY)) {
                this.partitionKeyDefinition = super.getObject(Constants.Properties.PARTITION_KEY, PartitionKeyDefinition.class);
            } else {
                this.partitionKeyDefinition = new PartitionKeyDefinition();
            }
        }

        return this.partitionKeyDefinition;
    }

    /**
     * Sets the containers's partition key definition.
     *
     * @param partitionKeyDefinition the partition key definition.
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties partitionKeyDefinition(PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition == null) {
            throw new IllegalArgumentException("partitionKeyDefinition cannot be null.");
        }

        this.partitionKeyDefinition = partitionKeyDefinition;
        return this;
    }
    
    /**
     * Gets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on documents in different regions, in a collection in the Azure Cosmos DB service.
     *
     * @return ConflictResolutionPolicy
     */
    public ConflictResolutionPolicy conflictResolutionPolicy() {
        return super.getObject(Constants.Properties.CONFLICT_RESOLUTION_POLICY, ConflictResolutionPolicy.class);
    }

    /**
     * Sets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on documents in different regions, in a collection in the Azure Cosmos DB service.
     *
     * @param value ConflictResolutionPolicy to be used.
     * @return the CosmosContainerProperties.
     */
    public CosmosContainerProperties conflictResolutionPolicy(ConflictResolutionPolicy value) {
        if (value == null) {
            throw new IllegalArgumentException("CONFLICT_RESOLUTION_POLICY cannot be null.");
        }

        super.set(Constants.Properties.CONFLICT_RESOLUTION_POLICY, value);
        return this;
    }

    DocumentCollection getV2Collection(){
        DocumentCollection collection = new DocumentCollection(this.toJson());
        collection.setPartitionKey(this.partitionKeyDefinition());
        collection.setIndexingPolicy(this.indexingPolicy());
        return collection;
    }
}

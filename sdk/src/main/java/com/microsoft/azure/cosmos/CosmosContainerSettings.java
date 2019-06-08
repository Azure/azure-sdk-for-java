/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.IndexingPolicy;
import com.microsoft.azure.cosmosdb.PartitionKeyDefinition;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.UniqueKeyPolicy;
import com.microsoft.azure.cosmosdb.internal.Constants;

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
public class CosmosContainerSettings extends Resource {

    private IndexingPolicy indexingPolicy;
    private UniqueKeyPolicy uniqueKeyPolicy;
    private PartitionKeyDefinition partitionKeyDefinition;

    /**
     * Constructor 
     * @param id id of the Container
     * @param partitionKeyPath partition key path
     */
    public CosmosContainerSettings(String id, String partitionKeyPath) {
        super.setId(id);
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add(partitionKeyPath);
        partitionKeyDef.setPaths(paths);
        setPartitionKey(partitionKeyDef);
    }

    /**
     * Constructor
     * @param id id of the container
     * @param partitionKeyDefinition the (@link PartitionKeyDefinition)
     */
    public CosmosContainerSettings(String id, PartitionKeyDefinition partitionKeyDefinition) {
        super.setId(id);
        setPartitionKey(partitionKeyDefinition);
    }

    CosmosContainerSettings(ResourceResponse<DocumentCollection> response) {
        super(response.getResource().toJson());
    }
    
    // Converting document collection to CosmosContainerSettings
    CosmosContainerSettings(DocumentCollection collection){
        super(collection.toJson());
    }
    
    static List<CosmosContainerSettings> getFromV2Results(List<DocumentCollection> results){
        return results.stream().map(CosmosContainerSettings::new).collect(Collectors.toList());
    }

    /**
     * Gets the container's indexing policy.
     *
     * @return the indexing policy.
     */
    public IndexingPolicy getIndexingPolicy() {
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
     */
    public void setIndexingPolicy(IndexingPolicy indexingPolicy) {
        if (indexingPolicy == null) {
            throw new IllegalArgumentException("IndexingPolicy cannot be null.");
        }
        this.indexingPolicy = indexingPolicy;
        super.set(Constants.Properties.INDEXING_POLICY, indexingPolicy);
    }

    /**
     * Gets the containers unique key policy
     * 
     * @return the unique key policy
     */
    public UniqueKeyPolicy getUniqueKeyPolicy() {

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
     */
    public void setUniqueKeyPolicy(UniqueKeyPolicy uniqueKeyPolicy) {
        if (uniqueKeyPolicy == null) {
            throw new IllegalArgumentException("uniqueKeyPolicy cannot be null.");
        }

        this.uniqueKeyPolicy = uniqueKeyPolicy;
        super.set(Constants.Properties.UNIQUE_KEY_POLICY, uniqueKeyPolicy);
    }

    /**
     * Gets the containers's partition key definition.
     *
     * @return the partition key definition.
     */
    public PartitionKeyDefinition getPartitionKey() {
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
     */
    public void setPartitionKey(PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition == null) {
            throw new IllegalArgumentException("partitionKey cannot be null.");
        }

        this.partitionKeyDefinition = partitionKeyDefinition;
    }
    
    DocumentCollection getV2Collection(){
        DocumentCollection collection = new DocumentCollection(this.toJson());
        collection.setPartitionKey(this.getPartitionKey());
        collection.setIndexingPolicy(this.getIndexingPolicy());
        return collection;
    }
}

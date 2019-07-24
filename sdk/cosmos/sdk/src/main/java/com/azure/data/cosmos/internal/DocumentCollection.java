// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ConflictResolutionPolicy;
import com.azure.data.cosmos.IndexingPolicy;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.UniqueKeyPolicy;
import org.apache.commons.lang3.StringUtils;

import static com.azure.data.cosmos.BridgeInternal.populatePropertyBagJsonSerializable;
import static com.azure.data.cosmos.BridgeInternal.setProperty;
import static com.azure.data.cosmos.BridgeInternal.remove;

/**
 * Represents a document collection in the Azure Cosmos DB database service. A collection is a named logical container
 * for documents.
 * <p>
 * A database may contain zero or more named collections and each collection consists of zero or more JSON documents.
 * Being schema-free, the documents in a collection do not need to share the same structure or fields. Since collections
 * are application resources, they can be authorized using either the master key or resource keys.
 */
public final class DocumentCollection extends Resource {
    private IndexingPolicy indexingPolicy;
    private UniqueKeyPolicy uniqueKeyPolicy;
    private PartitionKeyDefinition partitionKeyDefinition;

    /**
     * Initialize a document collection object.
     */
    public DocumentCollection() {
        super();
    }

    /**
     * Sets the id and returns the document collection
     * @param id the name of the resource.
     * @return
     */
    public DocumentCollection id(String id){
        super.id(id);
        return this;
    }

    /**
     * Initialize a document collection object from json string.
     *
     * @param jsonString the json string that represents the document collection.
     */
    public DocumentCollection(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the indexing policy.
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
     * Sets the indexing policy.
     *
     * @param indexingPolicy the indexing policy.
     */
    public void setIndexingPolicy(IndexingPolicy indexingPolicy) {
        if (indexingPolicy == null) {
            throw new IllegalArgumentException("IndexingPolicy cannot be null.");
        }

        this.indexingPolicy = indexingPolicy;
    }

    /**
     * Gets the collection's partition key definition.
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
     * Sets the collection's partition key definition.
     *
     * @param partitionKey the partition key definition.
     */
    public void setPartitionKey(PartitionKeyDefinition partitionKey) {
        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKeyDefinition cannot be null.");
        }

        this.partitionKeyDefinition = partitionKey;
    }

    /**
     * Gets the collection's default time-to-live value.
     *
     * @return the default time-to-live value in seconds.
     */
    public Integer getDefaultTimeToLive() {
        if (super.has(Constants.Properties.DEFAULT_TTL)) {
            return super.getInt(Constants.Properties.DEFAULT_TTL);
        }

        return null;
    }

    /**
     * Sets the collection's default time-to-live value.
     * <p>
     * The default time-to-live value on a collection is an optional property. If set, the documents within the collection
     * expires after the specified number of seconds since their last write time. The value of this property should be one of the following:
     * <p>
     * null - indicates evaluation of time-to-live is disabled and documents within the collection will never expire, regardless whether
     * individual documents have their time-to-live set.
     * <p>
     * nonzero positive integer - indicates the default time-to-live value for all documents within the collection. This value can be overridden
     * by individual documents' time-to-live value.
     * <p>
     * -1 - indicates by default all documents within the collection never expire. This value can be overridden by individual documents'
     * time-to-live value.
     *
     * @param timeToLive the default time-to-live value in seconds.
     */
    public void setDefaultTimeToLive(Integer timeToLive) {
        // a "null" value is represented as a missing element on the wire.
        // setting timeToLive to null should remove the property from the property bag.
        if (timeToLive != null) {
            setProperty(this, Constants.Properties.DEFAULT_TTL, timeToLive);
        } else if (super.has(Constants.Properties.DEFAULT_TTL)) {
            remove(this, Constants.Properties.DEFAULT_TTL);
        }
    }

    /**
     * Sets the Uni that guarantees uniqueness of documents in collection in the Azure Cosmos DB service.
     * @return UniqueKeyPolicy
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

    public void setUniqueKeyPolicy(UniqueKeyPolicy uniqueKeyPolicy) {
        if (uniqueKeyPolicy == null) {
            throw new IllegalArgumentException("uniqueKeyPolicy cannot be null.");
        }

        this.uniqueKeyPolicy = uniqueKeyPolicy;
        setProperty(this, Constants.Properties.UNIQUE_KEY_POLICY, uniqueKeyPolicy);
    }

    /**
     * Gets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on documents in different regions, in a collection in the Azure Cosmos DB service.
     *
     * @return ConflictResolutionPolicy
     */
    public ConflictResolutionPolicy getConflictResolutionPolicy() {
        return super.getObject(Constants.Properties.CONFLICT_RESOLUTION_POLICY, ConflictResolutionPolicy.class);
    }

    /**
     * Sets the conflictResolutionPolicy that is used for resolving conflicting writes
     * on documents in different regions, in a collection in the Azure Cosmos DB service.
     *
     * @param value ConflictResolutionPolicy to be used.
     */
    public void setConflictResolutionPolicy(ConflictResolutionPolicy value) {
        if (value == null) {
            throw new IllegalArgumentException("CONFLICT_RESOLUTION_POLICY cannot be null.");
        }

        setProperty(this, Constants.Properties.CONFLICT_RESOLUTION_POLICY, value);
    }


    /**
     * Gets the self-link for documents in a collection.
     *
     * @return the document link.
     */
    public String getDocumentsLink() {
        return String.format("%s/%s",
                StringUtils.stripEnd(super.selfLink(), "/"),
                super.getString(Constants.Properties.DOCUMENTS_LINK));
    }

    /**
     * Gets the self-link for stored procedures in a collection.
     *
     * @return the stored procedures link.
     */
    public String getStoredProceduresLink() {
        return String.format("%s/%s",
                StringUtils.stripEnd(super.selfLink(), "/"),
                super.getString(Constants.Properties.STORED_PROCEDURES_LINK));
    }

    /**
     * Gets the self-link for triggers in a collection.
     *
     * @return the trigger link.
     */
    public String getTriggersLink() {
        return StringUtils.removeEnd(this.selfLink(), "/") +
                "/" + super.getString(Constants.Properties.TRIGGERS_LINK);
    }

    /**
     * Gets the self-link for user defined functions in a collection.
     *
     * @return the user defined functions link.
     */
    public String getUserDefinedFunctionsLink() {
        return StringUtils.removeEnd(this.selfLink(), "/") +
                "/" + super.getString(Constants.Properties.USER_DEFINED_FUNCTIONS_LINK);
    }

    /**
     * Gets the self-link for conflicts in a collection.
     *
     * @return the conflicts link.
     */
    public String getConflictsLink() {
        return StringUtils.removeEnd(this.selfLink(), "/") +
                "/" + super.getString(Constants.Properties.CONFLICTS_LINK);
    }

    void populatePropertyBag() {
        if (this.indexingPolicy == null) {
            this.getIndexingPolicy();
        }
        if (this.uniqueKeyPolicy == null) {
            this.getUniqueKeyPolicy();
        }

        if (this.partitionKeyDefinition != null) {
            populatePropertyBagJsonSerializable(this.partitionKeyDefinition);
            setProperty(this, Constants.Properties.PARTITION_KEY, this.partitionKeyDefinition);
        }
        populatePropertyBagJsonSerializable(this.indexingPolicy);
        populatePropertyBagJsonSerializable(this.uniqueKeyPolicy);

        setProperty(this, Constants.Properties.INDEXING_POLICY, this.indexingPolicy);
        setProperty(this, Constants.Properties.UNIQUE_KEY_POLICY, this.uniqueKeyPolicy);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !DocumentCollection.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        DocumentCollection typedObj = (DocumentCollection) obj;
        return typedObj.resourceId().equals(this.resourceId());
    }

    @Override
    public int hashCode() {
        return this.resourceId().hashCode();
    }

    @Override
    public String toJson() {
        this.populatePropertyBag();
        return super.toJson();
    }
}
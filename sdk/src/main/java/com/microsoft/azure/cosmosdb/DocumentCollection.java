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

package com.microsoft.azure.cosmosdb;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents a document collection in the Azure Cosmos DB database service. A collection is a named logical container
 * for documents.
 * <p>
 * A database may contain zero or more named collections and each collection consists of zero or more JSON documents.
 * Being schema-free, the documents in a collection do not need to share the same structure or fields. Since collections
 * are application resources, they can be authorized using either the master key or resource keys.
 */
@SuppressWarnings("serial")
public final class DocumentCollection extends Resource {
    private IndexingPolicy indexingPolicy = null;

    /**
     * Initialize a document collection object.
     */
    public DocumentCollection() {
        super();
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
     * Initialize a document collection object from json object.
     *
     * @param jsonObject the json object that represents the document collection.
     */
    public DocumentCollection(JSONObject jsonObject) {
        super(jsonObject);
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
        if (super.has(Constants.Properties.PARTITION_KEY)) {
            return super.getObject(Constants.Properties.PARTITION_KEY, PartitionKeyDefinition.class);
        }

        this.setPartitionKey(new PartitionKeyDefinition());

        return this.getPartitionKey();
    }

    /**
     * Sets the collection's partition key definition.
     *
     * @param partitionKey the partition key definition.
     */
    public void setPartitionKey(PartitionKeyDefinition partitionKey) {
        if (partitionKey == null) {
            throw new IllegalArgumentException("partitionKey cannot be null.");
        }

        super.set(Constants.Properties.PARTITION_KEY, partitionKey);
    }

    /**
     * Gets the collection's default time-to-live value.
     *
     * @return the the default time-to-live value in seconds.
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
            super.set(Constants.Properties.DEFAULT_TTL, timeToLive);
        } else if (super.has(Constants.Properties.DEFAULT_TTL)) {
            super.remove(Constants.Properties.DEFAULT_TTL);
        }
    }

    /**
     * Gets the self-link for documents in a collection.
     *
     * @return the document link.
     */
    public String getDocumentsLink() {
        return String.format("%s/%s",
                StringUtils.stripEnd(super.getSelfLink(), "/"),
                super.getString(Constants.Properties.DOCUMENTS_LINK));
    }

    /**
     * Gets the self-link for stored procedures in a collection.
     *
     * @return the stored procedures link.
     */
    public String getStoredProceduresLink() {
        return String.format("%s/%s",
                StringUtils.stripEnd(super.getSelfLink(), "/"),
                super.getString(Constants.Properties.STORED_PROCEDURES_LINK));
    }

    /**
     * Gets the self-link for triggers in a collection.
     *
     * @return the trigger link.
     */
    public String getTriggersLink() {
        return StringUtils.removeEnd(this.getSelfLink(), "/") +
                "/" + super.getString(Constants.Properties.TRIGGERS_LINK);
    }

    /**
     * Gets the self-link for user defined functions in a collection.
     *
     * @return the user defined functions link.
     */
    public String getUserDefinedFunctionsLink() {
        return StringUtils.removeEnd(this.getSelfLink(), "/") +
                "/" + super.getString(Constants.Properties.USER_DEFINED_FUNCTIONS_LINK);
    }

    /**
     * Gets the self-link for conflicts in a collection.
     *
     * @return the conflicts link.
     */
    public String getConflictsLink() {
        return StringUtils.removeEnd(this.getSelfLink(), "/") +
                "/" + super.getString(Constants.Properties.CONFLICTS_LINK);
    }

    @Override
    void populatePropertyBag() {
        if (this.indexingPolicy == null) {
            this.getIndexingPolicy();
        }
        this.indexingPolicy.populatePropertyBag();
        super.set(Constants.Properties.INDEXING_POLICY, this.indexingPolicy);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !DocumentCollection.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        DocumentCollection typedObj = (DocumentCollection) obj;
        return typedObj.getResourceId().equals(this.getResourceId());
    }

    @Override
    public int hashCode() {
        return this.getResourceId().hashCode();
    }
}
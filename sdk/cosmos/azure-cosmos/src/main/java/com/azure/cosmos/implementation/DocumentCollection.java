// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.SerializableWrapper;
import com.azure.cosmos.models.ClientEncryptionPolicy;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ConflictResolutionPolicy;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.UniqueKeyPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.azure.cosmos.BridgeInternal.setProperty;

/**
 * Represents a document collection in the Azure Cosmos DB database service. A collection is a named logical container
 * for documents.
 * <p>
 * A database may contain zero or more named collections and each collection consists of zero or more JSON documents.
 * Being schema-free, the documents in a collection do not need to share the same structure or fields. Since collections
 * are application resources, they can be authorized using either the master key or resource keys.
 */
public final class DocumentCollection extends Resource {
    private static final String COLLECTIONS_ROOT_PROPERTY_NAME = "col";
    private static final String ALT_LINK_PROPERTY_NAME = "altLink";

    private IndexingPolicy indexingPolicy;
    private UniqueKeyPolicy uniqueKeyPolicy;
    private PartitionKeyDefinition partitionKeyDefinition;
    private ClientEncryptionPolicy clientEncryptionPolicyInternal;

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    public DocumentCollection(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Initialize a document collection object.
     */
    public DocumentCollection() {
        super();
    }

    /**
     * Sets the id and returns the document collection
     * @param id the name of the resource.
     * @return the document collection
     */
    public DocumentCollection setId(String id){
        super.setId(id);
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
            remove(Constants.Properties.DEFAULT_TTL);
        }
    }

    /**
     * Sets the analytical storage time to live in seconds for items in a container from the Azure Cosmos DB service.
     *
     * It is an optional property. A valid value must be either a nonzero positive integer, '-1', or 0.
     * By default, AnalyticalStorageTimeToLive is set to 0 meaning the analytical store is turned off for the container;
     * -1 means documents in analytical store never expire.
     * The unit of measurement is seconds. The maximum allowed value is 2147483647.
     *
     * @param timeToLive the analytical storage time to live in seconds.
     */
    public void setAnalyticalStoreTimeToLiveInSeconds(Integer timeToLive) {
        // a "null" value is represented as a missing element on the wire.
        // setting timeToLive to null should remove the property from the property bag.
        if (timeToLive != null) {
            super.set(Constants.Properties.ANALYTICAL_STORAGE_TTL, timeToLive);
        } else if (super.has(Constants.Properties.ANALYTICAL_STORAGE_TTL)) {
            super.remove(Constants.Properties.ANALYTICAL_STORAGE_TTL);
        }
    }

    /**
     * Gets the analytical storage time to live in seconds for items in a container from the Azure Cosmos DB service.
     *
     * It is an optional property. A valid value must be either a nonzero positive integer, '-1', or 0.
     * By default, AnalyticalStorageTimeToLive is set to 0 meaning the analytical store is turned off for the container;
     * -1 means documents in analytical store never expire.
     * The unit of measurement is seconds. The maximum allowed value is 2147483647.
     *
     * @return analytical ttl
     */
    public Integer getAnalyticalStoreTimeToLiveInSeconds() {
        if (super.has(Constants.Properties.ANALYTICAL_STORAGE_TTL)) {
            return super.getInt(Constants.Properties.ANALYTICAL_STORAGE_TTL);
        }

        return null;
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
     * Gets the changeFeedPolicy for this container in the Azure Cosmos DB service.
     *
     * @return ChangeFeedPolicy
     */
    public ChangeFeedPolicy getChangeFeedPolicy() {
        ChangeFeedPolicy policy = super.getObject(Constants.Properties.CHANGE_FEED_POLICY, ChangeFeedPolicy.class);

        if (policy == null) {
            return ChangeFeedPolicy.createIncrementalPolicy();
        }

        return policy;
    }

    /**
     * Sets the changeFeedPolicy for this container in the Azure Cosmos DB service.
     *
     * @param value ChangeFeedPolicy to be used.
     */
    public void setChangeFeedPolicy(ChangeFeedPolicy value) {
        if (value == null) {
            throw new IllegalArgumentException("CHANGE_FEED_POLICY cannot be null.");
        }

        setProperty(this, Constants.Properties.CHANGE_FEED_POLICY, value);
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

    /**
     * Gets the client encryption policy.
     *
     * @return the client encryption policy.
     */
    public ClientEncryptionPolicy getClientEncryptionPolicy() {
        if (this.clientEncryptionPolicyInternal == null) {
            if (super.has(Constants.Properties.CLIENT_ENCRYPTION_POLICY)) {
                this.clientEncryptionPolicyInternal = super.getObject(Constants.Properties.CLIENT_ENCRYPTION_POLICY,
                    ClientEncryptionPolicy.class);
            }
        }

        return this.clientEncryptionPolicyInternal;
    }

    /**
     * Sets the ClientEncryptionPolicy that is used for encryption on documents,
     * in a collection in the Azure Cosmos DB service.
     *
     * @param value ClientEncryptionPolicy to be used.
     */
    public void setClientEncryptionPolicy(ClientEncryptionPolicy value) {
        if (value == null) {
            throw new IllegalArgumentException("ClientEncryptionPolicy cannot be null.");
        }

        setProperty(this, Constants.Properties.CLIENT_ENCRYPTION_POLICY, value);
    }

    public void populatePropertyBag() {
        super.populatePropertyBag();
        if (this.indexingPolicy == null) {
            this.getIndexingPolicy();
        }
        if (this.uniqueKeyPolicy == null) {
            this.getUniqueKeyPolicy();
        }

        if (this.partitionKeyDefinition != null) {
            ModelBridgeInternal.populatePropertyBag(this.partitionKeyDefinition);
            setProperty(this, Constants.Properties.PARTITION_KEY, this.partitionKeyDefinition);
        }
        ModelBridgeInternal.populatePropertyBag(this.indexingPolicy);
        ModelBridgeInternal.populatePropertyBag(this.uniqueKeyPolicy);

        setProperty(this, Constants.Properties.INDEXING_POLICY, this.indexingPolicy);
        setProperty(this, Constants.Properties.UNIQUE_KEY_POLICY, this.uniqueKeyPolicy);
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

    @Override
    public String toJson() {
        this.populatePropertyBag();
        return super.toJson();
    }

    public static class SerializableDocumentCollection implements SerializableWrapper<DocumentCollection> {
        private static final long serialVersionUID = 2l;
        private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
        public static SerializableDocumentCollection from(DocumentCollection documentCollection) {
            SerializableDocumentCollection serializableDocumentCollection = new SerializableDocumentCollection();
            serializableDocumentCollection.documentCollection = documentCollection;
            return serializableDocumentCollection;
        }

        transient DocumentCollection documentCollection;

        public DocumentCollection getWrappedItem() {
            return documentCollection;
        }

        private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
            documentCollection.populatePropertyBag();
            ObjectNode docCollectionNode = OBJECT_MAPPER.createObjectNode();
            docCollectionNode.set(COLLECTIONS_ROOT_PROPERTY_NAME, documentCollection.getPropertyBag());
            docCollectionNode.set(ALT_LINK_PROPERTY_NAME, TextNode.valueOf(documentCollection.getAltLink()));
            objectOutputStream.writeObject(docCollectionNode);
        }

        private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
            ObjectNode objectNode = (ObjectNode) objectInputStream.readObject();
            ObjectNode collectionNode = (ObjectNode)objectNode.get(COLLECTIONS_ROOT_PROPERTY_NAME);
            String altLink = objectNode.get(ALT_LINK_PROPERTY_NAME).asText();
            this.documentCollection = new DocumentCollection(collectionNode);
            this.documentCollection.setAltLink(altLink);
        }
    }
}

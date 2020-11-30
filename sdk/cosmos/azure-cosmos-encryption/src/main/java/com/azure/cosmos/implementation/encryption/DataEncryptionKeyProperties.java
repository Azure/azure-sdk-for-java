// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.EncryptionKeyWrapMetadata;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Details of an encryption key for use with the Azure Cosmos DB service.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "encryptionAlgorithm", "wrappedDataEncryptionKey", "keyWrapMetadata", "createTime", "_rid", "_self", "_etag", "_ts"})
class DataEncryptionKeyProperties {

    /**
     * Initializes a new instance of {@link DataEncryptionKeyProperties}
     *
     * @param id Unique identifier for the data encryption key.
     * @param encryptionAlgorithm Encryption algorithm that will be used along with this data encryption key to encrypt/decrypt data.
     * @param wrappedDataEncryptionKey Wrapped (encrypted) form of the data encryption key.
     * @param encryptionKeyWrapMetadata Metadata used by the configured key wrapping provider in order to unwrap the key.
     * @param createdTime created time
     */
    public DataEncryptionKeyProperties(String id,
                                       String encryptionAlgorithm,
                                       byte[] wrappedDataEncryptionKey,
                                       EncryptionKeyWrapMetadata encryptionKeyWrapMetadata,
                                       Instant createdTime) {

        Preconditions.checkArgument(StringUtils.isNotEmpty(id), "id is null");
        Preconditions.checkNotNull(wrappedDataEncryptionKey, "wrappedDataEncryptionKey is null");
        Preconditions.checkNotNull(encryptionKeyWrapMetadata, "encryptionKeyWrapMetadata is null");

        this.id = id;
        this.encryptionAlgorithm = encryptionAlgorithm;
        this.wrappedDataEncryptionKey = wrappedDataEncryptionKey;
        this.encryptionKeyWrapMetadata = encryptionKeyWrapMetadata;
        this.createdTime = createdTime;
    }

    protected DataEncryptionKeyProperties() {
    }

    DataEncryptionKeyProperties(DataEncryptionKeyProperties source) {
        this.createdTime = source.createdTime;
        this.eTag = source.eTag;
        this.id = source.id;
        this.encryptionAlgorithm = source.encryptionAlgorithm;
        this.encryptionKeyWrapMetadata = new EncryptionKeyWrapMetadata(source.encryptionKeyWrapMetadata);
        this.lastModified = source.lastModified;
        this.resourceId = source.resourceId;
        this.selfLink = source.selfLink;
        if (source.wrappedDataEncryptionKey != null) {
            this.wrappedDataEncryptionKey = new byte[source.wrappedDataEncryptionKey.length];

            System.arraycopy(source.wrappedDataEncryptionKey, 0, this.wrappedDataEncryptionKey, 0, this.wrappedDataEncryptionKey.length);
        }
    }

    /**
     * The identifier of the resource.
     * <p>
     * Every resource within an Azure Cosmos DB database account needs to have a unique identifier.
     * The following characters are restricted and cannot be used in the Id property:
     * '/', '\\', '?', '#'
     */
    @JsonProperty(value = "id", required = true)
    public String id;

    /**
     * Encryption algorithm that will be used along with this data encryption key to encrypt/decrypt data.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("encryptionAlgorithm")
    public String encryptionAlgorithm;

    /**
     * Wrapped form of the data encryption key.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(value = "wrappedDataEncryptionKey")
    public byte[] wrappedDataEncryptionKey;

    /**
     * Metadata for the wrapping provider that can be used to unwrap the wrapped data encryption key.
     */
    @JsonProperty("keyWrapMetadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public EncryptionKeyWrapMetadata encryptionKeyWrapMetadata;

    /**
     * Gets the creation time of the resource from the Azure Cosmos DB service.
     */
    @JsonProperty("createTime")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    @JsonSerialize(using = UnixTimestampSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Instant createdTime;

    /**
     * Gets the entity tag associated with the resource from the Azure Cosmos DB service.
     * <p>
     * The entity tag associated with the resource.
     * ETags are used for concurrency checking when updating resources.
     */
    @JsonProperty("_etag")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String eTag;

    /**
     * Gets the last modified time stamp associated with the resource from the Azure Cosmos DB service.
     * The last modified time stamp associated with the resource.
     */
    @JsonProperty("_ts")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = UnixTimestampSerializer.class)
    public Instant lastModified;

    /**
     * Gets the self-link associated with the resource from the Azure Cosmos DB service.
     * <p>
     * The self-link associated with the resource.
     * <p>
     * A self-link is a static addressable Uri for each resource within a database account and follows the Azure Cosmos DB resource model.
     * E.g. a self-link for a document could be dbs/db_resourceid/colls/coll_resourceid/documents/doc_resourceid
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("_self")
    public String selfLink;

    /**
     * Gets the Resource Id associated with the resource in the Azure Cosmos DB service.
     * <p>
     * The Resource Id associated with the resource.
     * A Resource Id is the unique, immutable, identifier assigned to each Azure Cosmos DB
     * <p>
     * A Resource Id is the unique, immutable, identifier assigned to each Azure Cosmos DB
     * resource whether that is a database, a collection or a document.
     * These resource ids are used when building up SelfLinks, a static addressable Uri for each resource within a database account.
     */
    @JsonProperty("_rid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String resourceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataEncryptionKeyProperties that = (DataEncryptionKeyProperties) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(encryptionAlgorithm, that.encryptionAlgorithm) &&
            Arrays.equals(wrappedDataEncryptionKey, that.wrappedDataEncryptionKey) &&
            Objects.equals(encryptionKeyWrapMetadata, that.encryptionKeyWrapMetadata) &&
            Objects.equals(createdTime, that.createdTime) &&
            Objects.equals(eTag, that.eTag) &&
            Objects.equals(lastModified, that.lastModified) &&
            Objects.equals(selfLink, that.selfLink) &&
            Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, encryptionAlgorithm, encryptionKeyWrapMetadata, createdTime, eTag, lastModified, selfLink, resourceId);
        result = 31 * result + Arrays.hashCode(wrappedDataEncryptionKey);
        return result;
    }

    public static boolean equals(byte[] x, byte[] y) {
        return (x == null && y == null)
            || (x != null && y != null && Arrays.equals(x, y));
    }
}

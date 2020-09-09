// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.azure.cosmos.implementation.encryption.ImplementationBridgeHelpers.EncryptionKeyWrapMetadataHelper;


/**
 * Metadata that a key wrapping provider can use to wrap/unwrap data encryption keys.
 * {@link EncryptionKeyWrapProvider}
 */
public class EncryptionKeyWrapMetadata {

    /**
     * For JSON deserialize
     */
    EncryptionKeyWrapMetadata() {
    }

    /**
     * Creates a new instance of key wrap metadata.
     *
     * @param value Value of the metadata
     */
    public EncryptionKeyWrapMetadata(String value) {
        this("custom", value);
    }

    /**
     * Creates a new instance of key wrap metadata based on an existing instance.
     *
     * @param source Existing instance from which to initialize.
     */
    public EncryptionKeyWrapMetadata(EncryptionKeyWrapMetadata source) {
        this.type = source.type;
        this.algorithm = source.algorithm;
        this.value = source.value;
    }

    EncryptionKeyWrapMetadata(String type, String value) {
        this(type, value, null);
    }

    EncryptionKeyWrapMetadata(String type, String value, String algorithm) {
        Preconditions.checkNotNull(type, "type is null");
        Preconditions.checkNotNull(value, "value is null");
        this.type = type;
        this.value = value;
        this.algorithm = algorithm;
    }

    @JsonProperty("type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String type;

    @JsonProperty("algorithm")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String algorithm;

    /**
     * Serialized form of metadata.
     * Note: This value is saved in the Cosmos DB service.
     * implementors of derived implementations should ensure that this does not have (private) key material or credential information.
     */
    @JsonProperty("value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptionKeyWrapMetadata that = (EncryptionKeyWrapMetadata) o;
        return Objects.equals(type, that.type) &&
            Objects.equals(algorithm, that.algorithm) &&
            Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, algorithm, value);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        EncryptionKeyWrapMetadataHelper.setEncryptionKeyWrapMetadataAccessor(
            new EncryptionKeyWrapMetadataHelper.EncryptionKeyWrapMetadataAccessor() {
                @Override
                public EncryptionKeyWrapMetadata create(String type, String value, String algorithm) {
                    return new EncryptionKeyWrapMetadata(type, value, algorithm);
                }

                @Override
                public EncryptionKeyWrapMetadata create(String type, String value) {
                    return new EncryptionKeyWrapMetadata(type, value);
                }

                @Override
                public String getType(EncryptionKeyWrapMetadata metadata) {
                    return metadata.type;
                }

                @Override
                public String getAlgorithm(EncryptionKeyWrapMetadata metadata) {
                    return metadata.algorithm;
                }
            });
    }
}

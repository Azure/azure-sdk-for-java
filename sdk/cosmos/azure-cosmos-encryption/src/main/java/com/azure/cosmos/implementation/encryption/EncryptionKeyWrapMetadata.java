// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


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
    public String type;

    @JsonProperty("algorithm")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String algorithm;

    /**
     * Serialized form of metadata.
     * Note: This value is saved in the Cosmos DB service.
     * implementors of derived implementations should ensure that this does not have (private) key material or credential information.
     */
    @JsonProperty("value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String value;

    @Override
    public boolean equals(Object obj) {
        EncryptionKeyWrapMetadata other = Utils.as(obj, EncryptionKeyWrapMetadata.class);
        return other != null &&
            StringUtils.equals(this.type, other.type) &&
            StringUtils.equals(this.algorithm, other.algorithm) &&
            StringUtils.equals(this.value, other.value);
    }
}

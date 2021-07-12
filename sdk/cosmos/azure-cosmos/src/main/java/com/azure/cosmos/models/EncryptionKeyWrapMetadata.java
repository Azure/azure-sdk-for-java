// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.guava25.base.Preconditions;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Metadata that a key wrapping provider can use to wrap/unwrap data encryption keys.
 */
@Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class EncryptionKeyWrapMetadata {

    /**
     * For JSON deserialize
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public EncryptionKeyWrapMetadata() {
    }

    /**
     * Creates a new instance of key wrap metadata based on an existing instance.
     *
     * @param source Existing instance from which to initialize.
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public EncryptionKeyWrapMetadata(EncryptionKeyWrapMetadata source) {
        this.type = source.type;
        this.name = source.name;
        this.value = source.value;
    }

    /**
     * Creates a new instance of key wrap metadata based on an existing instance.
     *
     * @param type Type of the metadata.
     * @param name Name of the metadata.
     * @param value Value of the metadata.
     */
    @Beta(value = Beta.SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public EncryptionKeyWrapMetadata(String type, String name, String value) {
        Preconditions.checkNotNull(type, "type is null");
        Preconditions.checkNotNull(value, "value is null");
        Preconditions.checkNotNull(name, "name is null");
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @JsonProperty("type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String type;

    @JsonProperty("value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String value;

    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    /**
     * Serialized form of metadata.
     * Note: This value is saved in the Cosmos DB service.
     * implementors of derived implementations should ensure that this does not have (private) key material or
     * credential information.
     * @return value of metadata
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getValue() {
        return value;
    }

    /**
     * Serialized form of metadata.
     * Note: This value is saved in the Cosmos DB service.
     * implementors of derived implementations should ensure that this does not have (private) key material or
     * credential information.
     * @return name of metadata.
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getName() {
        return name;
    }

    /**
     * Serialized form of metadata.
     * Note: This value is saved in the Cosmos DB service.
     * implementors of derived implementations should ensure that this does not have (private) key material or
     * credential information.
     * @return type of metadata.
     */
    @Beta(value = Beta.SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getType() {
        return type;
    }

    /**
     * Returns whether the properties of the passed in key wrap metadata matches with those in the current instance.
     *
     * @param obj Key wrap metadata to be compared with current instance.
     * @return True if the properties of the key wrap metadata passed in matches with those in the current instance, else false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EncryptionKeyWrapMetadata that = (EncryptionKeyWrapMetadata) obj;
        return Objects.equals(type, that.type) &&
            Objects.equals(name, that.name) &&
            Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }

}

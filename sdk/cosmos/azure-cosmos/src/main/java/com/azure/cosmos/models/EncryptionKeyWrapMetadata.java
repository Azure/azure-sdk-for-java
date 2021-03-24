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
@Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class EncryptionKeyWrapMetadata {

    /**
     * For JSON deserialize
     */
    EncryptionKeyWrapMetadata() {
    }

    /**
     * Creates a new instance of key wrap metadata based on an existing instance.
     *
     * @param source Existing instance from which to initialize.
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public EncryptionKeyWrapMetadata(EncryptionKeyWrapMetadata source) {
        this.type = source.type;
        this.algorithm = source.algorithm;
        this.value = source.value;
    }

    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public EncryptionKeyWrapMetadata(String name, String value) {
        this("custom", name, value, null);
    }

    EncryptionKeyWrapMetadata(String type, String name, String value) {
        this(type, name, value, null);
    }

    EncryptionKeyWrapMetadata(String type, String name, String value, String algorithm) {
        Preconditions.checkNotNull(type, "type is null");
        Preconditions.checkNotNull(value, "value is null");
        this.type = type;
        this.name = name;
        this.value = value;
        this.algorithm = algorithm;
    }

    @JsonProperty("type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String type;

    @JsonProperty("algorithm")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String algorithm;

    @JsonProperty("value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String value;

    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String name;

    /**
     * Serialized form of metadata.
     * Note: This value is saved in the Cosmos DB service.
     * implementors of derived implementations should ensure that this does not have (private) key material or
     * credential information.
     * @return value of metadata
     */
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_13_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getName() {
        return name;
    }

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

}

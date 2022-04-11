// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.security.keyvault.keys.implementation.BinaryDataJsonDeserializer;
import com.azure.security.keyvault.keys.implementation.BinaryDataJsonSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

/**
 * A model that represents the policy rules under which the key can be exported.
 */
@Fluent
public final class KeyReleasePolicy {
    /**
     * The policy rules under which the key can be released. Encoded based on the {@link KeyReleasePolicy#contentType}.
     *
     * For more information regarding the release policy grammar for Azure Key Vault, please refer to:
     * - https://aka.ms/policygrammarkeys for Azure Key Vault release policy grammar.
     * - https://aka.ms/policygrammarmhsm for Azure Managed HSM release policy grammar.
     */
    @JsonProperty(value = "data")
    @JsonSerialize(using = BinaryDataJsonSerializer.class)
    @JsonDeserialize(using = BinaryDataJsonDeserializer.class)
    private BinaryData encodedPolicy;

    /*
     * Content type and version of key release policy.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /*
     * Defines the mutability state of the policy. Once marked immutable on the service side, this flag cannot be reset
     * and the policy cannot be changed under any circumstances.
     */
    @JsonProperty(value = "immutable")
    private Boolean immutable;

    KeyReleasePolicy() {
        // Empty constructor for Jackson Deserialization
    }

    /**
     * Creates an instance of {@link KeyReleasePolicy}.
     *
     * @param encodedPolicy The policy rules under which the key can be released. Encoded based on the
     * {@link KeyReleasePolicy#contentType}.
     *
     * For more information regarding the release policy grammar for Azure Key Vault, please refer to:
     * - https://aka.ms/policygrammarkeys for Azure Key Vault release policy grammar.
     * - https://aka.ms/policygrammarmhsm for Azure Managed HSM release policy grammar.
     */
    public KeyReleasePolicy(BinaryData encodedPolicy) {
        Objects.requireNonNull(encodedPolicy, "'encodedPolicy' cannot be null.");

        this.encodedPolicy = encodedPolicy;
    }

    /**
     * Get a blob encoding the policy rules under which the key can be released.
     *
     * @return encodedPolicy The policy rules under which the key can be released. Encoded based on the
     * {@link KeyReleasePolicy#contentType}.
     *
     * For more information regarding the release policy grammar for Azure Key Vault, please refer to:
     * - https://aka.ms/policygrammarkeys for Azure Key Vault release policy grammar.
     * - https://aka.ms/policygrammarmhsm for Azure Managed HSM release policy grammar.
     */
    public BinaryData getEncodedPolicy() {
        return encodedPolicy;
    }

    /**
     * Get the content type and version of key release policy.
     *
     * @return The content type and version of key release policy.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the content type and version of key release policy.
     *
     * <p>The service default is "application/json; charset=utf-8".</p>
     *
     * @param contentType The content type and version of key release policy to set.
     *
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setContentType(String contentType) {
        this.contentType = contentType;

        return this;
    }

    /**
     * Get a value indicating if the policy is immutable. Once marked immutable on the service side, this flag cannot
     * be reset and the policy cannot be changed under any circumstances.
     *
     * @return If the {@link KeyReleasePolicy} is immutable.
     */
    public Boolean isImmutable() {
        return this.immutable;
    }

    /**
     * Get a value indicating if the policy is immutable. Defines the mutability state of the policy. Once marked
     * immutable on the service side, this flag cannot be reset and the policy cannot be changed under any
     * circumstances.
     *
     * @param immutable The immutable value to set.
     * @return The updated {@link KeyReleasePolicy} object.
     */
    public KeyReleasePolicy setImmutable(Boolean immutable) {
        this.immutable = immutable;

        return this;
    }
}

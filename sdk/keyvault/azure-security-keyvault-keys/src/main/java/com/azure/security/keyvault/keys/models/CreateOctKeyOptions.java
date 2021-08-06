// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents the configurable options to create a symmetric key.
 */
@Fluent
public class CreateOctKeyOptions extends CreateKeyOptions {
    /**
     * The key size.
     */
    private Integer keySize;

    /**
     * The hardware protected indicator for the key.
     */
    private boolean hardwareProtected;

    /**
     * Creates a {@link CreateOctKeyOptions} with {@code name} as name of the key.
     *
     * @param name The name of the key.
     */
    public CreateOctKeyOptions(String name) {
        super(name, KeyType.OCT);
    }

    /**
     * Set the key operations.
     *
     * @param keyOperations The key operations to set.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    @Override
    public CreateOctKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        super.setKeyOperations(keyOperations);

        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    @Override
    public CreateOctKeyOptions setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);

        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set. for the key.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    @Override
    public CreateOctKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        super.setExpiresOn(expiresOn);

        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    @Override
    public CreateOctKeyOptions setTags(Map<String, String> tags) {
        super.setTags(tags);

        return this;
    }

    /**
     * Set a value that indicates if the key is enabled.
     *
     * @param enabled The enabled value to set.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    public CreateOctKeyOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);

        return this;
    }

    /**
     * Set the key size in bits.
     *
     * @param keySize The key size to set.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    public CreateOctKeyOptions setKeySize(Integer keySize) {
        this.keySize = keySize;

        return this;
    }

    /**
     * Get the key size in bits.
     *
     * @return The key size in bits.
     */
    public Integer getKeySize() {
        return this.keySize;
    }

    /**
     * Set whether the key being created is of HSM type or not.
     *
     * @param hardwareProtected The HSM value to set.
     *
     * @return The updated {@link CreateOctKeyOptions} object.
     */
    public CreateOctKeyOptions setHardwareProtected(Boolean hardwareProtected) {
        this.hardwareProtected = hardwareProtected;
        KeyType keyType = hardwareProtected ? KeyType.OCT_HSM : KeyType.OCT;

        setKeyType(keyType);

        return this;
    }

    /**
     * Get the HSM value of the key being created.
     *
     * @return the HSM value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
    }
}

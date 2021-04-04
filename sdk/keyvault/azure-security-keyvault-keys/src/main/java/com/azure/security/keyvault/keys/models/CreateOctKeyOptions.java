// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.models;

import java.time.OffsetDateTime;
import java.util.Map;

public class CreateOctKeyOptions extends CreateKeyOptions {
    /**
     * The hardware protected indicator for the key.
     */
    private boolean hardwareProtected;

    /**
     * Creates a CreateOctKeyOptions with {@code name} as name of the Rsa key.
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
     * Set whether the key being created is of hsm type or not.
     *
     * @param hardwareProtected The hsm value to set.
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
     * Get the hsm value of the key being created.
     *
     * @return the hsm value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
    }
}

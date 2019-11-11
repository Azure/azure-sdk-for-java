// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * Represents the configurable options to create an Ec key.
 */
@Fluent
public class CreateEcKeyOptions extends CreateKeyOptions {

    /**
     * The Ec key curve.
     */
    private KeyCurveName curveName;

    /**
     * The hardware protected indicator for the key.
     */
    private boolean hardwareProtected;

    /**
     * Creates a EcKeyCreateOptions with {@code name} as name of the Ec key.
     * @param name The name of the Ec key.
     */
    public CreateEcKeyOptions(String name) {
        super.name = name;
        this.keyType = KeyType.EC;
    }

    /**
     * Get the curve.
     *
     * @return the curve.
     */
    public KeyCurveName getCurveName() {
        return this.curveName;
    }

    /**
     * Set the curve.
     *
     * @param curveName The curve to set
     * @return the EcKeyCreateOptions object itself.
     */
    public CreateEcKeyOptions setCurveName(KeyCurveName curveName) {
        this.curveName = curveName;
        return this;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public CreateEcKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        this.keyOperations = Arrays.asList(keyOperations);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public CreateEcKeyOptions setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the key.
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public CreateEcKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        super.setExpiresOn(expiresOn);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public CreateEcKeyOptions setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the EcKeyCreateOptions object itself.
     */
    @Override
    public CreateEcKeyOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    /**
     * Set whether the key being created is of hsm type or not.
     * @param hardwareProtected The hsm value to set.
     * @return the EcKeyCreateOptions object itself.
     */
    public CreateEcKeyOptions setHardwareProtected(Boolean hardwareProtected) {
        this.hardwareProtected = hardwareProtected;
        this.keyType = hardwareProtected ? KeyType.EC_HSM : KeyType.EC;
        return this;
    }

    /**
     * Get the hsm value of the key being created.
     * @return the hsm value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents the configurable options to create an EC key.
 */
@Fluent
public class CreateEcKeyOptions extends CreateKeyOptions {
    /**
     * The EC key curve.
     */
    private KeyCurveName curveName;

    /**
     * The hardware protected indicator for the key.
     */
    private boolean hardwareProtected;

    /**
     * Creates a {@link CreateEcKeyOptions} with {@code name} as name of the EC key.
     *
     * @param name The name of the EC key.
     */
    public CreateEcKeyOptions(String name) {
        super(name, KeyType.EC);
    }

    /**
     * Get the curve.
     *
     * @return The curve name.
     */
    public KeyCurveName getCurveName() {
        return this.curveName;
    }

    /**
     * Set the curve name.
     *
     * @param curveName The curve name to set.
     *
     * @return The {@link CreateEcKeyOptions} object.
     */
    public CreateEcKeyOptions setCurveName(KeyCurveName curveName) {
        this.curveName = curveName;

        return this;
    }

    /**
     * Get the HSM value of the key being created.
     *
     * @return The HSM value.
     */
    public Boolean isHardwareProtected() {
        return this.hardwareProtected;
    }

    /**
     * Set whether the key being created is of HSM type or not.
     *
     * @param hardwareProtected The HSM value to set.
     *
     * @return The {@link CreateEcKeyOptions} object.
     */
    public CreateEcKeyOptions setHardwareProtected(Boolean hardwareProtected) {
        this.hardwareProtected = hardwareProtected;
        KeyType keyType = hardwareProtected ? KeyType.EC_HSM : KeyType.EC;

        setKeyType(keyType);

        return this;
    }

    /**
     * Set the key operations.
     *
     * @param keyOperations The key operations to set.
     *
     * @return The {@link CreateEcKeyOptions} object.
     */
    @Override
    public CreateEcKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        super.setKeyOperations(keyOperations);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set.
     *
     * @return The {@link CreateEcKeyOptions} object.
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
     *
     * @return The {@link CreateEcKeyOptions} object.
     */
    @Override
    public CreateEcKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        super.setExpiresOn(expiresOn);

        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The {@link CreateEcKeyOptions} object.
     */
    @Override
    public CreateEcKeyOptions setTags(Map<String, String> tags) {
        super.setTags(tags);

        return this;
    }

    /**
     * Set a value that indicates if the key is enabled.
     *
     * @param enabled The enabled value to set.
     *
     * @return The {@link CreateEcKeyOptions} object.
     */
    @Override
    public CreateEcKeyOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);

        return this;
    }

    /**
     * Set a flag that indicates if the private key can be exported.
     *
     * @param exportable A flag that indicates if the private key can be exported.
     *
     * @return The updated {@link CreateEcKeyOptions} object.
     */
    public CreateEcKeyOptions setExportable(Boolean exportable) {
        super.setExportable(exportable);

        return this;
    }

    /**
     * Set the policy rules under which the key can be exported.
     *
     * @param releasePolicy The policy rules to set.
     *
     * @return The updated {@link CreateEcKeyOptions} object.
     */
    public CreateEcKeyOptions setReleasePolicy(KeyReleasePolicy releasePolicy) {
        super.setReleasePolicy(releasePolicy);

        return this;
    }
}

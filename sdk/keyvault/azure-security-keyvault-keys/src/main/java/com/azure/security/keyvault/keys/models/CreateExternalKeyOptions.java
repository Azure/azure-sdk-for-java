// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents the configurable options to register an external key with Azure Key Vault Managed HSM.
 *
 * <p>An external key references key material that is owned by an external Hardware Security Module (HSM). Unlike other
 * key types, an external key does not have a {@link KeyType key type}; the material is managed by the external HSM and
 * the Managed HSM stores only a reference to it. External keys are mutually exclusive with a key type, so use
 * {@link KeyClient#createExternalKey(CreateExternalKeyOptions)} (or its asynchronous equivalent
 * {@link KeyAsyncClient#createExternalKey(CreateExternalKeyOptions)}) rather than the standard create key operations.
 * </p>
 *
 * <p>External keys are only supported on Managed HSM configured to use External Key Management (EKM), with service
 * version {@code 2026-01-01-preview} or newer. They are not supported on a standard Key Vault.</p>
 *
 * @see KeyClient
 * @see KeyAsyncClient
 */
@Fluent
public class CreateExternalKeyOptions extends CreateKeyOptions {
    /**
     * The reference to the external key material.
     */
    private final ExternalKey externalKey;

    /**
     * Creates an instance of {@link CreateExternalKeyOptions} with {@code name} as the key name and
     * {@code externalKey} as the reference to the external key material.
     *
     * @param name The name of the external key to register.
     * @param externalKey The reference identifying the external key material.
     */
    public CreateExternalKeyOptions(String name, ExternalKey externalKey) {
        super(name, null);
        this.externalKey = externalKey;
    }

    /**
     * Get the reference to the external key material.
     *
     * @return The reference to the external key material.
     */
    public ExternalKey getExternalKey() {
        return this.externalKey;
    }

    /**
     * Set the key operations.
     *
     * @param keyOperations The key operations to set.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        super.setKeyOperations(keyOperations);

        return this;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);

        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the key.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        super.setExpiresOn(expiresOn);

        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setTags(Map<String, String> tags) {
        super.setTags(tags);

        return this;
    }

    /**
     * Set a value that indicates if the key is enabled.
     *
     * @param enabled The enabled value to set.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setEnabled(Boolean enabled) {
        super.setEnabled(enabled);

        return this;
    }

    /**
     * Set a flag that indicates if the private key can be exported.
     *
     * @param exportable A flag that indicates if the private key can be exported.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setExportable(Boolean exportable) {
        super.setExportable(exportable);

        return this;
    }

    /**
     * Set the policy rules under which the key can be exported.
     *
     * @param releasePolicy The policy rules to set.
     *
     * @return The updated {@link CreateExternalKeyOptions} object.
     */
    @Override
    public CreateExternalKeyOptions setReleasePolicy(KeyReleasePolicy releasePolicy) {
        super.setReleasePolicy(releasePolicy);

        return this;
    }
}

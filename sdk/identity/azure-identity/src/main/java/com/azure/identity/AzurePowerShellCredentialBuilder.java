// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link AzurePowerShellCredential}.
 *
 * @see AzurePowerShellCredential
 */
public class AzurePowerShellCredentialBuilder extends CredentialBuilderBase<AzurePowerShellCredentialBuilder> {
    private String tenantId;

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public AzurePowerShellCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(getClass().getSimpleName(), tenantId);
        this.tenantId = tenantId;
        return this;
    }

     /**
     * Creates a new {@link AzurePowerShellCredential} with the current configurations.
     *
     * @return a {@link AzurePowerShellCredential} with the current configurations.
     */
    public AzurePowerShellCredential build() {
        return new AzurePowerShellCredential(tenantId, identityClientOptions);
    }
}

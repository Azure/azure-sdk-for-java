// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link AzureCliCredential}.
 *
 * @see AzureCliCredential
 */
public class AzureCliCredentialBuilder extends CredentialBuilderBase<AzureCliCredentialBuilder> {
    private String tenantId;

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public AzureCliCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(getClass().getSimpleName(), tenantId);
        this.tenantId = tenantId;
        return this;
    }

     /**
     * Creates a new {@link AzureCliCredential} with the current configurations.
     *
     * @return a {@link AzureCliCredential} with the current configurations.
     */
    public AzureCliCredential build() {
        return new AzureCliCredential(identityClientOptions);
    }
}

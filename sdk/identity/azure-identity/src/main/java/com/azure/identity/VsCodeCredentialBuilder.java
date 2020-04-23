// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link VsCodeCredential}.
 *
 * @see VsCodeCredential
 */
public class VsCodeCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {

    private String tenantId;

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public VsCodeCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Creates a new {@link VsCodeCredential} with the current configurations.
     *
     * @return a {@link VsCodeCredential} with the current configurations.
     */
    public VsCodeCredential build() {
        return new VsCodeCredential(tenantId, identityClientOptions);
    }
}

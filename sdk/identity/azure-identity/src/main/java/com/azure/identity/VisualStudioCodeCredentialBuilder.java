// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link VisualStudioCodeCredential}.
 *
 * @see VisualStudioCodeCredential
 */
public class VisualStudioCodeCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {

    private String tenantId;

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public VisualStudioCodeCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Creates a new {@link VisualStudioCodeCredential} with the current configurations.
     *
     * @return a {@link VisualStudioCodeCredential} with the current configurations.
     */
    public VisualStudioCodeCredential build() {
        return new VisualStudioCodeCredential(tenantId, identityClientOptions);
    }
}

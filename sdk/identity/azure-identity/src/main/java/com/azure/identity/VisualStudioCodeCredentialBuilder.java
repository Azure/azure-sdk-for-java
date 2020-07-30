// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link VisualStudioCodeCredential}.
 *
 * @see VisualStudioCodeCredential
 */
public class VisualStudioCodeCredentialBuilder extends CredentialBuilderBase<VisualStudioCodeCredentialBuilder> {
    private String tenantId;

    /**
     * Sets the tenant id of the user to authenticate through the {@link VisualStudioCodeCredential}. The default is
     * the tenant the user originally authenticated to via via the Visual Studio Code Azure Account plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public VisualStudioCodeCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(getClass().getSimpleName(), tenantId);
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

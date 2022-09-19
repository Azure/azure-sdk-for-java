// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link VisualStudioCodeCredential}.
 *
 * @see VisualStudioCodeCredential
 */
public class VisualStudioCodeCredentialBuilder extends CredentialBuilderBase<VisualStudioCodeCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(VisualStudioCodeCredentialBuilder.class);

    private String tenantId;

    /**
     * Sets the tenant id of the user to authenticate through the {@link VisualStudioCodeCredential}. The default is
     * the tenant the user originally authenticated to via via the Visual Studio Code Azure Account plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public VisualStudioCodeCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Specifies tenants in addition to the specified tenantId for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the logged in account can access.
     * If no value is specified for tenantId this option will have no effect, and the credential will acquire tokens
     * for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    public VisualStudioCodeCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        identityClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
        return this;
    }

    /**
     * Specifies tenants in addition to the specified tenantId for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the logged in account can access.
     * If no value is specified for tenantId this option will have no effect, and the credential will acquire tokens
     * for any requested tenant.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the additional tenants configured.
     */
    public VisualStudioCodeCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
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

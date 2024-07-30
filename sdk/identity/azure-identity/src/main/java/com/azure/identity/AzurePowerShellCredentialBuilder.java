// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link AzurePowerShellCredential}.
 *
 * <p>The Azure Powershell is a command-line tool that allows users to manage Azure resources from their local machine
 * or terminal. It allows users to
 * <a href="https://learn.microsoft.com/powershell/azure/authenticate-azureps">authenticate interactively</a>
 * as a user and/or a service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * The {@link AzurePowerShellCredential} authenticates in a development environment and acquires a token on
 * behalf of the logged-in user or service principal in Azure Powershell. It acts as the Azure Powershell logged in
 * user or service principal and executes an Azure Powershell command underneath to authenticate the application
 * against Microsoft Entra ID.</p>
 *
 * <p><strong>Sample: Construct AzurePowershellCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AzurePowerShellCredential},
 * using the {@link com.azure.identity.AzurePowerShellCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.azurepowershellcredential.construct -->
 * <pre>
 * TokenCredential powerShellCredential = new AzurePowerShellCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azurepowershellcredential.construct -->
 *
 * @see AzurePowerShellCredential
 */
public class AzurePowerShellCredentialBuilder extends CredentialBuilderBase<AzurePowerShellCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(AzurePowerShellCredentialBuilder.class);

    private String tenantId;

    /**
     * Constructs an instance of AzurePowerShellCredentialBuilder.
     */
    public AzurePowerShellCredentialBuilder() {
        super();
    }

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public AzurePowerShellCredentialBuilder tenantId(String tenantId) {
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
    @SuppressWarnings("unchecked")
    public AzurePowerShellCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
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
    @SuppressWarnings("unchecked")
    public AzurePowerShellCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
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

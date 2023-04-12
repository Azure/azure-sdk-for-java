// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link AzureDeveloperCliCredential}.
 *
 * <p>Azure Developer CLI is a command-line interface tool that allows developers to create, manage, and deploy
 * resources in Azure. It's built on top of the Azure CLI and provides additional functionality specific
 * to Azure developers. It allows users to authenticate as a user and/or a service principal against
 * <a href="https://learn.microsoft.com/en-us/azure/active-directory/fundamentals/">Azure Active Directory (Azure AD)
 * </a>. The AzureDeveloperCliCredential authenticates in a development environment and acquires a token on behalf of
 * the logged-in user or service principal in Azure Developer CLI. It acts as the Azure Developer CLI logged in user or
 * service principal and executes an Azure CLI command underneath to authenticate the application against
 * Azure Active Directory.</p>
 *
 * <p><strong>Sample: Construct AzureDeveloperCliCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AzureDeveloperCliCredential},
 * using the {@link com.azure.identity.AzureDeveloperCliCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.azuredeveloperclicredential.construct -->
 * <pre>
 * TokenCredential azureDevCliCredential = new AzureDeveloperCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azuredeveloperclicredential.construct -->
 *
 * @see AzureDeveloperCliCredential
 */
public class AzureDeveloperCliCredentialBuilder extends CredentialBuilderBase<AzureDeveloperCliCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(AzureDeveloperCliCredentialBuilder.class);

    private String tenantId;

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public AzureDeveloperCliCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Specifies a {@link Duration} timeout for calling the Azure Developer CLI.
     * @param duration The {@link Duration} to wait.
     * @return An updated instance of this builder with the timeout specified.
     */
    public AzureDeveloperCliCredentialBuilder azureDeveloperCliCredentialTimeout(Duration duration) {
        this.identityClientOptions.setDeveloperCredentialTimeout(duration);
        return this;
    }

    /**
     * Creates a new {@link AzureDeveloperCliCredential} with the current configurations.
     *
     * @return a {@link AzureDeveloperCliCredential} with the current configurations.
     */
    public AzureDeveloperCliCredential build() {
        return new AzureDeveloperCliCredential(tenantId, identityClientOptions);
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
    public AzureDeveloperCliCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
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
    public AzureDeveloperCliCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }
}

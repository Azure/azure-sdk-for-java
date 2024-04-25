// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Fluent credential builder for instantiating a {@link AzureCliCredential}.
 *
 * <p>The Azure CLI is a command-line tool that allows users to manage Azure resources from their local machine or
 * terminal. It allows users to
 * <a href="https://learn.microsoft.com/cli/azure/authenticate-azure-cli">authenticate interactively</a> as a
 * user and/or a service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * The AzureCliCredential authenticates in a development environment and acquires a token on behalf of the
 * logged-in user or service principal in Azure CLI. It acts as the Azure CLI logged in user or service principal
 * and executes an Azure CLI command underneath to authenticate the application against Microsoft Entra ID.</p>
 *
 * <p><strong>Sample: Construct IntelliJCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AzureCliCredential},
 * using the {@link com.azure.identity.AzureCliCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.azureclicredential.construct -->
 * <pre>
 * TokenCredential azureCliCredential = new AzureCliCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.azureclicredential.construct -->
 *
 * @see AzureCliCredential
 */
public class AzureCliCredentialBuilder extends CredentialBuilderBase<AzureCliCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(AzureCliCredentialBuilder.class);

    private String tenantId;

    /**
     * Constructs an instance of AzureCliCredentialBuilder.
     */
    public AzureCliCredentialBuilder() {
        super();
    }
    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public AzureCliCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Specifies a {@link Duration} timeout for calling the Azure CLI. The timeout period is applied on the Azure CLI
     * command execution process invoked by the credential
     * @param processTimeout The {@link Duration} to wait.
     * @return An updated instance of this builder with the timeout specified.
     */
    public AzureCliCredentialBuilder processTimeout(Duration processTimeout) {
        Objects.requireNonNull(processTimeout);
        this.identityClientOptions.setCredentialProcessTimeout(processTimeout);
        return this;
    }

     /**
     * Creates a new {@link AzureCliCredential} with the current configurations.
     *
     * @return a {@link AzureCliCredential} with the current configurations.
     */
    public AzureCliCredential build() {
        return new AzureCliCredential(tenantId, identityClientOptions);
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
    public AzureCliCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
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
    public AzureCliCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }
}

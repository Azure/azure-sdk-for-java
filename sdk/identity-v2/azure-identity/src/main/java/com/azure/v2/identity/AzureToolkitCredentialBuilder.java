// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link AzureToolkitCredential}.
 *
 * <p>IntelliJ IDEA is an integrated development environment (IDE) developed by JetBrains, which provides a variety of
 * features to support software development, such as code completion, debugging, and testing.
 * Azure offers <a href="https://learn.microsoft.com/azure/developer/java/toolkit-for-intellij/">Azure Toolkit
 * for IntelliJ plugin</a> for the IntelliJ IDEA development environment. It enables developers to create, test, and
 * deploy Java applications to the Azure cloud platform. In order to use the plugin authentication as a user or
 * service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> is required.
 * The {@link AzureToolkitCredential} authenticates in a development environment and acquires a token on behalf of the
 * logged-in account in Azure Toolkit for IntelliJ. It uses the logged in user information on the IntelliJ IDE and uses
 * it to authenticate the application against Microsoft Entra ID.</p>
 *
 * <p><strong>Sample: Construct IntelliJCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link AzureToolkitCredential},
 * using the {@link AzureToolkitCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <pre>
 * TokenCredential intelliJCredential = new IntelliJCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see AzureToolkitCredential
 */
public class AzureToolkitCredentialBuilder extends CredentialBuilderBase<AzureToolkitCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(AzureToolkitCredentialBuilder.class);

    private String tenantId;
    private final PublicClientOptions publicClientOptions;

    /**
     * Constructs an instance of IntelliJCredentialBuilder.
     */
    public AzureToolkitCredentialBuilder() {
        super();
        publicClientOptions = new PublicClientOptions();
    }

    /**
     * Sets the tenant id of the user to authenticate through the {@link AzureToolkitCredential}. The default is
     * the tenant the user originally authenticated to via the Azure Toolkit for IntelliJ plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public AzureToolkitCredentialBuilder tenantId(String tenantId) {
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
    public AzureToolkitCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        publicClientOptions.setAdditionallyAllowedTenants(
            IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
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
    public AzureToolkitCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        publicClientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Creates a new {@link AzureToolkitCredential} with the current configurations.
     *
     * @return a {@link AzureToolkitCredential} with the current configurations.
     */
    public AzureToolkitCredential build() {
        return new AzureToolkitCredential(publicClientOptions);
    }

    @Override
    ClientOptions getClientOptions() {
        return publicClientOptions;
    }
}

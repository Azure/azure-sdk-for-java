// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link IntelliJCredential}.
 *
 * <p>IntelliJ IDEA is an integrated development environment (IDE) developed by JetBrains, which provides a variety of
 * features to support software development, such as code completion, debugging, and testing.
 * Azure offers <a href="https://learn.microsoft.com/azure/developer/java/toolkit-for-intellij/">Azure Toolkit
 * for IntelliJ plugin</a> for the IntelliJ IDEA development environment. It enables developers to create, test, and
 * deploy Java applications to the Azure cloud platform. In order to use the plugin authentication as a user or
 * service principal against
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> is required.
 * The {@link IntelliJCredential} authenticates in a development environment and acquires a token on behalf of the
 * logged-in account in Azure Toolkit for IntelliJ. It uses the logged in user information on the IntelliJ IDE and uses
 * it to authenticate the application against Microsoft Entra ID.</p>
 *
 * <p><strong>Sample: Construct IntelliJCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.IntelliJCredential},
 * using the {@link com.azure.identity.IntelliJCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.intellijcredential.construct -->
 * <pre>
 * TokenCredential intelliJCredential = new IntelliJCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.intellijcredential.construct -->
 *
 * @see IntelliJCredential
 */
public class IntelliJCredentialBuilder extends CredentialBuilderBase<VisualStudioCodeCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(IntelliJCredentialBuilder.class);

    private String tenantId;

    /**
     * Constructs an instance of IntelliJCredentialBuilder.
     */
    public IntelliJCredentialBuilder() {
        super();
    }

    /**
     * Sets the tenant id of the user to authenticate through the {@link IntelliJCredential}. The default is
     * the tenant the user originally authenticated to via the Azure Toolkit for IntelliJ plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public IntelliJCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Specifies the KeePass database path to read the cached credentials of Azure toolkit for IntelliJ plugin.
     * The {@code databasePath} is required on Windows platform. For macOS and Linux platform native key chain /
     * key ring will be accessed respectively to retrieve the cached credentials.
     *
     * <p>This path can be located in the IntelliJ IDE.
     * Windows: File -&gt; Settings -&gt; Appearance &amp; Behavior -&gt; System Settings -&gt; Passwords. </p>
     *
     * @param databasePath the path to the KeePass database.
     * @throws IllegalArgumentException if {@code databasePath} is either not specified or is empty.
     * @return An updated instance of this builder with the KeePass database path set as specified.
     */
    public IntelliJCredentialBuilder keePassDatabasePath(String databasePath) {
        if (CoreUtils.isNullOrEmpty(databasePath)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The KeePass database path is either empty or not configured."
                                                 + " Please configure it on the builder."));
        }
        this.identityClientOptions.setIntelliJKeePassDatabasePath(databasePath);
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
    public IntelliJCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
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
    public IntelliJCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        identityClientOptions.setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Creates a new {@link IntelliJCredential} with the current configurations.
     *
     * @return a {@link IntelliJCredential} with the current configurations.
     */
    public IntelliJCredential build() {
        return new IntelliJCredential(tenantId, identityClientOptions);
    }
}

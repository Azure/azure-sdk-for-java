// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link IntelliJCredential}.
 *
 * @see IntelliJCredential
 */
public class IntelliJCredentialBuilder extends CredentialBuilderBase<VisualStudioCodeCredentialBuilder> {
    private String tenantId;
    private final ClientLogger logger = new ClientLogger(IntelliJCredentialBuilder.class);


    /**
     * Sets the tenant id of the user to authenticate through the {@link IntelliJCredential}. The default is
     * the tenant the user originally authenticated to via the Azure Toolkit for IntelliJ plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public IntelliJCredentialBuilder tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(getClass().getSimpleName(), tenantId);
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
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The KeePass database path is either empty or not configured."
                                                 + " Please configure it on the builder."));
        }
        this.identityClientOptions.setIntelliJKeePassDatabasePath(databasePath);
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

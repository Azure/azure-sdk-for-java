package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link IntelliJCredential}.
 *
 * @see IntelliJCredential
 */
public class IntelliJCredentialBuilder extends CredentialBuilderBase<VisualStudioCodeCredentialBuilder> {
    private String tenantId;

    /**
     * Sets the tenant id of the user to authenticate through the {@link IntelliJCredential}. The default is
     * the tenant the user originally authenticated to via the Azure Toolkit for IntelliJ plugin.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public VisualStudioCodeCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
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

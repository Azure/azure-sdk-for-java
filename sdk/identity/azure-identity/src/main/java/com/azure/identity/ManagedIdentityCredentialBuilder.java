// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;

/**
 * <p>Fluent credential builder for instantiating a {@link ManagedIdentityCredential}.</p>
 *
 * <p><a href="https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/">Azure
 * Managed Identity</a> is a feature in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that provides a way for applications running on Azure to authenticate themselves with Azure resources without
 * needing to manage or store any secrets like passwords or keys.
 * The {@link ManagedIdentityCredential} authenticates the configured managed identity (system or user assigned) of an
 * Azure resource. So, if the application is running inside an Azure resource that supports Managed Identity through
 * IDENTITY/MSI, IMDS endpoints, or both, then this credential will get your application authenticated, and offers a
 * great secretless authentication experience. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/managedidentitycredential/docs">managed identity authentication
 * documentation</a>.</p>
 *
 * <p><strong>Sample: Construct a simple ManagedIdentityCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ManagedIdentityCredential},
 * using the ManagedIdentityCredentialBuilder to configure it. Once this credential is created, it may be passed into
 * the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.construct -->
 * <pre>
 * TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.construct -->
 *
 * <p><strong>Sample: Construct a User Assigned ManagedIdentityCredential</strong></p>
 *
 * <p>User-Assigned Managed Identity (UAMI) in Azure is a feature that allows you to create an identity in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that is associated with one or more Azure resources. This identity can then be used to authenticate and
 * authorize access to various Azure services and resources. The following code sample demonstrates the creation of a
 * {@link ManagedIdentityCredential} to target a user assigned managed identity, using the
 * ManagedIdentityCredentialBuilder to configure it. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.userassigned.construct -->
 * <pre>
 * TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41; &#47;&#47; specify client id of user-assigned managed identity.
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.userassigned.construct -->
 *
 * @see ManagedIdentityCredential
 */
public class ManagedIdentityCredentialBuilder extends CredentialBuilderBase<ManagedIdentityCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityCredentialBuilder.class);

    private String clientId;
    private String resourceId;

    /**
     * Constructs an instance of ManagedIdentityCredentialBuilder.
     */
    public ManagedIdentityCredentialBuilder() {
        super();
    }

    /**
     * Specifies the client ID of user assigned or system assigned identity.
     *
     * Only one of clientId and resourceId can be specified.
     *
     * @param clientId the client ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Specifies the resource ID of a user assigned or system assigned identity.
     *
     * Only one of clientId and resourceId can be specified.
     *
     * @param resourceId the resource ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Creates a new {@link ManagedIdentityCredential} with the current configurations.
     *
     * @return a {@link ManagedIdentityCredential} with the current configurations.
     * @throws IllegalStateException if clientId and resourceId are both set.
     */
    public ManagedIdentityCredential build() {
        if (clientId != null && resourceId != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Only one of clientId and resourceId can be specified."));
        }

        return new ManagedIdentityCredential(clientId, resourceId, identityClientOptions);
    }
}

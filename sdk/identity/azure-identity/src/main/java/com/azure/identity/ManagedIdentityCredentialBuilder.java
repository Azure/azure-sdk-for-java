// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.concurrent.ExecutorService;

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
 * <p>Azure Arc-enabled servers support user-assigned managed identities selected by client ID, resource ID, or object
 * ID.</p>
 *
 * <p><strong>Sample: Construct a simple ManagedIdentityCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ManagedIdentityCredential},
 * using the ManagedIdentityCredentialBuilder to configure it. Once this credential is created, it may be passed into
 * the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.construct -->
 * <pre>
 * TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;.build&#40;&#41;;
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
 * TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder&#40;&#41;.clientId&#40;
 *         clientId&#41; &#47;&#47; specify client id of user-assigned managed identity.
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
    private String objectId;

    /**
     * Constructs an instance of ManagedIdentityCredentialBuilder.
     */
    public ManagedIdentityCredentialBuilder() {
        super();
    }

    /**
     * Specifies the client ID of a user-assigned managed identity. If none of clientId, resourceId, or objectId
     * is set, the system-assigned managed identity is used.
     *
     * Only one of clientId, resourceId, or objectId can be specified.
     *
     * @param clientId the client ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Specifies the resource ID of a user-assigned managed identity. If none of clientId, resourceId, or objectId
     * is set, the system-assigned managed identity is used.
     *
     * Only one of clientId, resourceId, or objectId can be specified.
     *
     * @param resourceId the resource ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Specifies the object ID of a user-assigned managed identity. If none of clientId, resourceId, or objectId
     * is set, the system-assigned managed identity is used.
     *
     * Only one of clientId, resourceId, or objectId can be specified.
     *
     * @param objectId the object ID
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder objectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link com.azure.core.util.SharedExecutorService} will be used which is
     * also shared with other SDK libraries. If there are many concurrent SDK tasks occurring, authentication
     * requests might starve and configuring a separate executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
        return this;
    }

    /**
     * Disables mutual TLS (mTLS) proof-of-possession, so that the credential always returns bearer tokens.
     *
     * <p>
     * By default, when a client library asks for a proof-of-possession token, and both the application and the
     * managed identity host support mTLS proof-of-possession, the credential returns a token bound to a TLS client
     * certificate. The client library then only sends the token over TLS connections that present that certificate.
     * mTLS proof-of-possession needs a Windows x64 Azure virtual machine or virtual machine scale set with KeyGuard,
     * the optional {@code com.microsoft.azure:msal4j-key-attestation} dependency, and the
     * {@code MSAL_KEY_ATTESTATION_LIBRARY_PATH} environment variable set to the absolute path of
     * {@code AttestationClientLib.dll}. Otherwise, the credential returns bearer tokens.
     * </p>
     *
     * <p>
     * To disable mTLS proof-of-possession without changing code, set the {@code MSAL_MI_DISABLE_IMDS_V2} environment
     * variable to {@code true}.
     * </p>
     *
     * @return the ManagedIdentityCredentialBuilder itself
     */
    public ManagedIdentityCredentialBuilder disableMtlsProofOfPossession() {
        this.identityClientOptions.disableMtlsProofOfPossession();
        return this;
    }

    /**
     * Creates a new {@link ManagedIdentityCredential} with the current configurations.
     *
     * @return a {@link ManagedIdentityCredential} with the current configurations.
     * @throws IllegalStateException if clientId and resourceId are both set.
     */
    public ManagedIdentityCredential build() {
        ValidationUtil.validateManagedIdentityIdParams(clientId, resourceId, objectId, LOGGER);

        return new ManagedIdentityCredential(clientId, resourceId, objectId, identityClientOptions);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.client.ManagedIdentityClient;
import com.azure.v2.identity.implementation.models.ManagedIdentityClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

/**
 * <p><a href="https://learn.microsoft.com/entra/identity/managed-identities-azure-resources/">Azure
 * Managed Identity</a> is a feature in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that provides a way for applications running on Azure to authenticate themselves with Azure resources without
 * needing to manage or store any secrets like passwords or keys.
 * The ManagedIdentityCredential authenticates the configured managed identity (system or user assigned) of an
 * Azure resource. So, if the application is running inside an Azure resource that supports Managed Identity through
 * IDENTITY/MSI, IMDS endpoints, or both, then this credential will get your application authenticated, and offers a
 * great secretless authentication experience. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/managedidentitycredential/docs">managed identity authentication
 * documentation</a>.</p>
 *
 * <p>The Managed Identity credential supports managed identity authentication for the following Azure Services:</p>
 *
 * <ol>
 *     <li><a href="https://learn.microsoft.com/azure/app-service/">Azure App Service</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/azure-arc/">Azure Arc</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/cloud-shell/overview">Azure Cloud Shell</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/azure-functions/">Azure Functions</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/aks/">Azure Kubernetes Service</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/service-fabric/">Azure Service Fabric</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/virtual-machines/">Azure Virtual Machines</a></li>
 *     <li><a href="https://learn.microsoft.com/azure/virtual-machine-scale-sets/">Azure Virtual Machines Scale
 *     Sets</a></li>
 * </ol>
 *
 * <p><strong>Sample: Construct a simple ManagedIdentityCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a ManagedIdentityCredential,
 * using the {@link ManagedIdentityCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the
 * 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * <p><strong>Sample: Construct a User Assigned ManagedIdentityCredential</strong></p>
 *
 * <p>User-Assigned Managed Identity (UAMI) in Azure is a feature that allows you to create an identity in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that is associated with one or more Azure resources. This identity can then be
 * used to authenticate and authorize access to various Azure services and resources. The following code sample
 * demonstrates the creation of a ManagedIdentityCredential to target a user assigned managed identity, using the
 * {@link ManagedIdentityCredentialBuilder} to configure it. Once this credential is created, it
 * may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder&#40;&#41;.clientId&#40;
 *         clientId&#41; &#47;&#47; specify client id of user-assigned managed identity.
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 */
public final class ManagedIdentityCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityCredential.class);

    private final ManagedIdentityClient managedIdentityClient;
    private final String managedIdentityId;
    private final ManagedIdentityClientOptions clientOptions;

    /**
     * Creates an instance of the ManagedIdentityCredential with the configured options.
     *
     * @param miClientOptions the options for configuring the identity client.
     */
    ManagedIdentityCredential(ManagedIdentityClientOptions miClientOptions) {
        this.managedIdentityId = fetchManagedIdentityId(miClientOptions);
        this.clientOptions = miClientOptions;
        this.managedIdentityClient = new ManagedIdentityClient(miClientOptions);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {

        if (!CoreUtils.isNullOrEmpty(managedIdentityId)) {
            ManagedIdentitySourceType managedIdentitySourceType = ManagedIdentityApplication.getManagedIdentitySource();
            if (ManagedIdentitySourceType.CLOUD_SHELL.equals(managedIdentitySourceType)
                || ManagedIdentitySourceType.AZURE_ARC.equals(managedIdentitySourceType)) {
                throw LOGGER.throwableAtError()
                    .log("ManagedIdentityCredential authentication unavailable. "
                        + "User-assigned managed identity is not supported in " + managedIdentitySourceType
                        + ". To use system-assigned managed identity, remove the configured client ID on "
                        + "the ManagedIdentityCredentialBuilder.", CredentialUnavailableException::new);
            }
        }

        try {
            AccessToken token = managedIdentityClient.authenticate(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, "Managed Identity authentication is not available.", request, e,
                clientOptions.isChained()
                    ? CredentialUnavailableException::new
                    : CredentialAuthenticationException::new);
        }
    }

    String fetchManagedIdentityId(ManagedIdentityClientOptions miClientOptions) {
        String clientId = miClientOptions.getClientId();
        String resourceId = miClientOptions.getResourceId();
        String objectId = miClientOptions.getObjectId();
        if (clientId != null) {
            return clientId;
        } else if (resourceId != null) {
            return resourceId;
        } else if (objectId != null) {
            return objectId;
        } else {
            return null;
        }
    }
}

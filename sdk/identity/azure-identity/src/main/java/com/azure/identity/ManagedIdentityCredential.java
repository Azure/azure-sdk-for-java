// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.ManagedIdentityParameters;
import com.azure.identity.implementation.ManagedIdentityType;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
 * using the {@link com.azure.identity.ManagedIdentityCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the
 * 'credential' parameter.</p>
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
 * that is associated with one or more Azure resources. This identity can then be
 * used to authenticate and authorize access to various Azure services and resources. The following code sample
 * demonstrates the creation of a ManagedIdentityCredential to target a user assigned managed identity, using the
 * {@link com.azure.identity.ManagedIdentityCredentialBuilder} to configure it. Once this credential is created, it
 * may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.managedidentitycredential.userassigned.construct -->
 * <pre>
 * TokenCredential managedIdentityCredentialUserAssigned = new ManagedIdentityCredentialBuilder&#40;&#41;
 *     .clientId&#40;clientId&#41; &#47;&#47; specify client id of user-assigned managed identity.
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.managedidentitycredential.userassigned.construct -->
 *
 * @see com.azure.identity
 * @see ManagedIdentityCredentialBuilder
 */
@Immutable
public final class ManagedIdentityCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityCredential.class);

    final ManagedIdentityServiceCredential managedIdentityServiceCredential;
    private final IdentityClientOptions identityClientOptions;

    static final String PROPERTY_IMDS_ENDPOINT = "IMDS_ENDPOINT";
    static final String PROPERTY_IDENTITY_SERVER_THUMBPRINT = "IDENTITY_SERVER_THUMBPRINT";
    static final String AZURE_FEDERATED_TOKEN_FILE = "AZURE_FEDERATED_TOKEN_FILE";

    static final String USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI = "USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI";


    /**
     * Creates an instance of the ManagedIdentityCredential with the client ID of a
     * user-assigned identity, or app registration (when working with AKS pod-identity).
     * @param clientId the client id of user assigned identity or app registration (when working with AKS pod-identity).
     * @param resourceId the resource id of user assigned identity or registered application
     * @param identityClientOptions the options for configuring the identity client.
     */
    ManagedIdentityCredential(String clientId, String resourceId, IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder clientBuilder = new IdentityClientBuilder()
            .clientId(clientId)
            .resourceId(resourceId)
            .identityClientOptions(identityClientOptions);
        this.identityClientOptions = identityClientOptions;

        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone() : identityClientOptions.getConfiguration();

        /*
         * Choose credential based on available environment variables in this order:
         *
         * Azure Arc: IDENTITY_ENDPOINT, IMDS_ENDPOINT
         * Service Fabric: IDENTITY_ENDPOINT, IDENTITY_HEADER, IDENTITY_SERVER_THUMBPRINT
         * App Service 2019-08-01: IDENTITY_ENDPOINT, IDENTITY_HEADER (MSI_ENDPOINT and MSI_SECRET will also be set.)
         * App Service 2017-09-01: MSI_ENDPOINT, MSI_SECRET
         * Cloud Shell: MSI_ENDPOINT
         * Pod Identity V2 (AksExchangeToken): AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_FEDERATED_TOKEN_FILE
         * IMDS/Pod Identity V1: No variables set.
         */
        if (configuration.contains(Configuration.PROPERTY_AZURE_TENANT_ID)
                && configuration.get(AZURE_FEDERATED_TOKEN_FILE) != null) {
            String clientIdentifier = clientId == null
                ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) : clientId;
            clientBuilder.clientId(clientIdentifier);
            clientBuilder.tenantId(configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID));
            clientBuilder.clientAssertionPath(configuration.get(AZURE_FEDERATED_TOKEN_FILE));
            clientBuilder.clientAssertionTimeout(Duration.ofMinutes(5));
            managedIdentityServiceCredential = new AksExchangeTokenCredential(clientIdentifier, clientBuilder
                .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.AKS,
                    identityClientOptions, configuration))
                .build());
        } else if (configuration.contains(USE_AZURE_IDENTITY_CLIENT_LIBRARY_LEGACY_MI)) {
            if (configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT)) {
                managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, clientBuilder
                    .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.APP_SERVICE,
                        identityClientOptions, configuration))
                    .build());
            } else if (configuration.contains(Configuration.PROPERTY_IDENTITY_ENDPOINT)) {
                if (configuration.contains(Configuration.PROPERTY_IDENTITY_HEADER)) {
                    if (configuration.get(PROPERTY_IDENTITY_SERVER_THUMBPRINT) != null) {
                        managedIdentityServiceCredential = new ServiceFabricMsiCredential(clientId, clientBuilder
                            .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.SERVICE_FABRIC,
                                identityClientOptions, configuration))
                            .build());
                    } else {
                        managedIdentityServiceCredential = new AppServiceMsiCredential(clientId, clientBuilder
                            .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.APP_SERVICE,
                                identityClientOptions, configuration))
                            .build());
                    }
                } else if (configuration.get(PROPERTY_IMDS_ENDPOINT) != null) {
                    managedIdentityServiceCredential = new ArcIdentityCredential(clientId, clientBuilder
                        .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.ARC,
                            identityClientOptions, configuration))
                        .build());
                } else {
                    managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder
                        .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.VM,
                            identityClientOptions, configuration))
                        .build());
                }
            } else {
                managedIdentityServiceCredential = new VirtualMachineMsiCredential(clientId, clientBuilder
                    .identityClientOptions(updateIdentityClientOptions(ManagedIdentityType.VM,
                        identityClientOptions, configuration))
                    .build());
            }
        } else {
            identityClientOptions.setManagedIdentityType(getManagedIdentityEnv(configuration));
            managedIdentityServiceCredential = new ManagedIdentityMsalCredential(clientId, clientBuilder.build());
        }
        LoggingUtil.logAvailableEnvironmentVariables(LOGGER, configuration);
    }

    private IdentityClientOptions updateIdentityClientOptions(ManagedIdentityType managedIdentityType,
                                             IdentityClientOptions clientOptions, Configuration configuration) {
        switch (managedIdentityType) {
            case APP_SERVICE:
                return clientOptions
                    .setManagedIdentityType(ManagedIdentityType.APP_SERVICE)
                    .setManagedIdentityParameters(new ManagedIdentityParameters()
                        .setMsiEndpoint(configuration.get(Configuration.PROPERTY_MSI_ENDPOINT))
                        .setMsiSecret(configuration.get(Configuration.PROPERTY_MSI_SECRET))
                        .setIdentityEndpoint(configuration.get(Configuration.PROPERTY_IDENTITY_ENDPOINT))
                        .setIdentityHeader(configuration.get(Configuration.PROPERTY_IDENTITY_HEADER)));
            case SERVICE_FABRIC:
                return clientOptions
                    .setManagedIdentityType(ManagedIdentityType.SERVICE_FABRIC)
                    .setManagedIdentityParameters(new ManagedIdentityParameters()
                        .setIdentityServerThumbprint(configuration.get(PROPERTY_IDENTITY_SERVER_THUMBPRINT))
                        .setIdentityEndpoint(configuration.get(Configuration.PROPERTY_IDENTITY_ENDPOINT))
                        .setIdentityHeader(configuration.get(Configuration.PROPERTY_IDENTITY_HEADER)));
            case ARC:
                return clientOptions
                    .setManagedIdentityType(ManagedIdentityType.ARC)
                    .setManagedIdentityParameters(new ManagedIdentityParameters()
                        .setIdentityEndpoint(configuration.get(Configuration.PROPERTY_IDENTITY_ENDPOINT)));
            case VM:
                return clientOptions.setManagedIdentityType(ManagedIdentityType.VM);
            case AKS:
                return clientOptions.setManagedIdentityType(ManagedIdentityType.AKS);
            default:
                return clientOptions;
        }
    }

    /**
     * Gets the client ID of user assigned or system assigned identity.
     * @return the client ID of user assigned or system assigned identity.
     */
    public String getClientId() {
        return managedIdentityServiceCredential.getClientId();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (managedIdentityServiceCredential == null) {
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, identityClientOptions,
                new CredentialUnavailableException("ManagedIdentityCredential authentication unavailable. "
                   + "The Target Azure platform could not be determined from environment variables."
                    + "To mitigate this issue, please refer to the troubleshooting guidelines here at"
                    + " https://aka.ms/azsdk/java/identity/managedidentitycredential/troubleshoot")));
        }
        return managedIdentityServiceCredential.authenticate(request)
            .doOnSuccess(t -> LOGGER.info("Azure Identity => Managed Identity environment: {}",
                    managedIdentityServiceCredential.getEnvironment()))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClientOptions, request, error));
    }

    ManagedIdentityType getManagedIdentityEnv(Configuration configuration) {
        if (configuration.contains(Configuration.PROPERTY_MSI_ENDPOINT)) {
            return ManagedIdentityType.APP_SERVICE;
        } else if (configuration.contains(Configuration.PROPERTY_IDENTITY_ENDPOINT)) {
            if (configuration.contains(Configuration.PROPERTY_IDENTITY_HEADER)) {
                if (configuration.get(PROPERTY_IDENTITY_SERVER_THUMBPRINT) != null) {
                    return ManagedIdentityType.SERVICE_FABRIC;
                } else {
                    return ManagedIdentityType.APP_SERVICE;
                }
            } else if (configuration.get(PROPERTY_IMDS_ENDPOINT) != null) {
                return ManagedIdentityType.ARC;
            } else {
                return ManagedIdentityType.VM;
            }
        } else if (configuration.contains(Configuration.PROPERTY_AZURE_TENANT_ID)
            && configuration.get(AZURE_FEDERATED_TOKEN_FILE) != null) {
            return ManagedIdentityType.AKS;
        } else {
            return ManagedIdentityType.VM;
        }
    }
}



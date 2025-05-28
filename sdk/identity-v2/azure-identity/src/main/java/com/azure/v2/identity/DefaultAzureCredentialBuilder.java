// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.client.IdentityLogOptionsImpl;
import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.models.ManagedIdentityClientOptions;
import com.azure.v2.identity.implementation.models.DevToolsClientOptions;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.core.credentials.TokenCredential;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * <p>Fluent credential builder for instantiating {@link DefaultAzureCredential}.</p>
 *
 * <p><strong>Sample: Construct DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link DefaultAzureCredential}, using
 * the DefaultAzureCredentialBuilder to configure it. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * <p><strong>Sample: Construct DefaultAzureCredential with User-Assigned Managed Identity </strong></p>
 *
 * <p>User-Assigned Managed Identity (UAMI) in Azure is a feature that allows you to create an identity in
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> that is
 * associated with one or more Azure resources. This identity can then be used to authenticate and
 * authorize access to various Azure services and resources. The following code sample demonstrates the creation of
 * a {@link DefaultAzureCredential} to target a user-assigned managed identity, using the DefaultAzureCredentialBuilder
 * to configure it. Once this credential is created, it may be passed into the builder of many of the
 * Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential dacWithUserAssignedManagedIdentity
 *     = new DefaultAzureCredentialBuilder&#40;&#41;.managedIdentityClientId&#40;&quot;&lt;Managed-Identity-Client-Id&quot;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see DefaultAzureCredential
 */
public class DefaultAzureCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultAzureCredentialBuilder.class);

    private String managedIdentityClientId;
    private String workloadIdentityClientId;
    private String managedIdentityResourceId;
    private Duration credentialProcessTimeout;
    private final ClientOptions clientOptions;

    /**
     * Creates an instance of a DefaultAzureCredentialBuilder.
     */
    public DefaultAzureCredentialBuilder() {
        clientOptions = new ClientOptions();
        this.clientOptions.setIdentityLogOptions(new IdentityLogOptionsImpl(true));
        this.clientOptions.setChained(true);
        this.clientOptions.setAdditionallyAllowedTenants(
            IdentityUtil.getAdditionalTenantsFromEnvironment(Configuration.getGlobalConfiguration()));
    }

    @Override
    ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Specifies the Microsoft Entra endpoint to acquire tokens.
     * @param authorityHost the Microsoft Entra endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public DefaultAzureCredentialBuilder authorityHost(String authorityHost) {
        this.clientOptions.setAuthorityHost(authorityHost);
        return this;
    }

    /**
     * Specifies the client ID of user assigned or system assigned identity, when this credential is running
     * in an environment with managed identities. If unset, the value in the AZURE_CLIENT_ID environment variable
     * will be used. If neither is set, the default value is null and will only work with system assigned
     * managed identities and not user assigned managed identities.
     *
     * Only one of managedIdentityClientId and managedIdentityResourceId can be specified.
     *
     * @param clientId the client ID
     * @return the DefaultAzureCredentialBuilder itself
     */
    public DefaultAzureCredentialBuilder managedIdentityClientId(String clientId) {
        this.managedIdentityClientId = clientId;
        return this;
    }

    /**
     * Specifies the client ID of Microsoft Entra app to be used for AKS workload identity authentication.
     * if unset, {@link DefaultAzureCredentialBuilder#managedIdentityClientId(String)} will be used.
     * If both values are unset, the value in the AZURE_CLIENT_ID environment variable
     * will be used. If none are set, the default value is null and Workload Identity authentication will not be attempted.
     *
     * @param clientId the client ID
     * @return the DefaultAzureCredentialBuilder itself
     */
    public DefaultAzureCredentialBuilder workloadIdentityClientId(String clientId) {
        this.workloadIdentityClientId = clientId;
        return this;
    }

    /**
     * Specifies the resource ID of user assigned or system assigned identity, when this credential is running
     * in an environment with managed identities. If unset, the value in the AZURE_CLIENT_ID environment variable
     * will be used. If neither is set, the default value is null and will only work with system assigned
     * managed identities and not user assigned managed identities.
     *
     * Only one of managedIdentityResourceId and managedIdentityClientId can be specified.
     *
     * @param resourceId the resource ID
     * @return the DefaultAzureCredentialBuilder itself
     */
    public DefaultAzureCredentialBuilder managedIdentityResourceId(String resourceId) {
        this.managedIdentityResourceId = resourceId;
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link io.clientcore.core.utils.SharedExecutorService} will be used which is
     * also shared with other SDK libraries. If there are many concurrent SDK tasks occurring, authentication
     * requests might starve and configuring a separate executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    public DefaultAzureCredentialBuilder executorService(ExecutorService executorService) {
        this.clientOptions.setExecutorService(executorService);
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the application is installed.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public DefaultAzureCredentialBuilder additionallyAllowedTenants(String... additionallyAllowedTenants) {
        this.clientOptions.setAdditionallyAllowedTenants(
            IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants)));
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the application is installed.
     *
     * @param additionallyAllowedTenants the additionally allowed tenants.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public DefaultAzureCredentialBuilder additionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        this.clientOptions
            .setAdditionallyAllowedTenants(IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants));
        return this;
    }

    /**
     * Specifies a {@link Duration} timeout for developer credentials (such as Azure CLI) that rely on separate process
     * invocations.
     * @param credentialProcessTimeout The {@link Duration} to wait.
     * @return An updated instance of this builder with the timeout specified.
     */
    public DefaultAzureCredentialBuilder credentialProcessTimeout(Duration credentialProcessTimeout) {
        Objects.requireNonNull(credentialProcessTimeout);
        this.credentialProcessTimeout = credentialProcessTimeout;
        return this;
    }

    /**
     * Disables the setting which determines whether or not instance discovery is performed when attempting to
     * authenticate. This will completely disable both instance discovery and authority validation.
     * This functionality is intended for use in scenarios where the metadata endpoint cannot be reached, such as in
     * private clouds or Azure Stack. The process of instance discovery entails retrieving authority metadata from
     * https://login.microsoft.com/ to validate the authority. By utilizing this API, the validation of the authority
     * is disabled. As a result, it is crucial to ensure that the configured authority host is valid and trustworthy.
     *
     * @return An updated instance of this builder with instance discovery disabled.
     */
    public DefaultAzureCredentialBuilder disableInstanceDiscovery() {
        this.clientOptions.disableInstanceDiscovery();
        return this;
    }

    /**
     * Creates new {@link DefaultAzureCredential} with the configured options set.
     *
     * @return a {@link DefaultAzureCredential} with the current configurations.
     * @throws IllegalStateException if clientId and resourceId are both set.
     */
    public DefaultAzureCredential build() {
        loadFallbackValuesFromEnvironment();

        if (managedIdentityClientId != null && managedIdentityResourceId != null) {
            throw LOGGER.throwableAtError()
                .log("Only one of managedIdentityClientId and managedIdentityResourceId can be specified.",
                    IllegalStateException::new);
        }
        return new DefaultAzureCredential(getCredentialsChain());
    }

    private void loadFallbackValuesFromEnvironment() {
        Configuration configuration = clientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration()
            : clientOptions.getConfiguration();
        if (CoreUtils.isNullOrEmpty(clientOptions.getTenantId())) {
            clientOptions.setTenantId(configuration.get(IdentityUtil.PROPERTY_AZURE_TENANT_ID));
        }
        managedIdentityClientId = CoreUtils.isNullOrEmpty(managedIdentityClientId)
            ? configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_ID)
            : managedIdentityClientId;
    }

    private ArrayList<TokenCredential> getCredentialsChain() {
        ArrayList<TokenCredential> output = new ArrayList<TokenCredential>(8);
        output.add(new EnvironmentCredential(new ConfidentialClientOptions(clientOptions)));
        output.add(getWorkloadIdentityCredential());
        output.add(
            new ManagedIdentityCredential((ManagedIdentityClientOptions) new ManagedIdentityClientOptions(clientOptions)
                .setResourceId(managedIdentityResourceId)
                .setClientId(managedIdentityClientId)));
        output.add(new AzureToolkitCredential(new PublicClientOptions(clientOptions)));
        output.add(new AzureCliCredential(
            new DevToolsClientOptions(clientOptions).setProcessTimeout(credentialProcessTimeout)));
        output.add(new AzurePowerShellCredential(
            new DevToolsClientOptions(clientOptions).setProcessTimeout(credentialProcessTimeout)));
        output.add(new AzureDeveloperCliCredential(
            new DevToolsClientOptions(clientOptions).setProcessTimeout(credentialProcessTimeout)));
        return output;
    }

    private WorkloadIdentityCredential getWorkloadIdentityCredential() {
        Configuration configuration = clientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration()
            : clientOptions.getConfiguration();

        String azureAuthorityHost = configuration.get(IdentityUtil.PROPERTY_AZURE_AUTHORITY_HOST);
        clientOptions.setClientId(
            CoreUtils.isNullOrEmpty(workloadIdentityClientId) ? managedIdentityClientId : workloadIdentityClientId);

        if (!CoreUtils.isNullOrEmpty(azureAuthorityHost)) {
            clientOptions.setAuthorityHost(azureAuthorityHost);
        }

        return new WorkloadIdentityCredential((ConfidentialClientOptions) new ConfidentialClientOptions(clientOptions)
            .setClientId(workloadIdentityClientId), null);
    }
}

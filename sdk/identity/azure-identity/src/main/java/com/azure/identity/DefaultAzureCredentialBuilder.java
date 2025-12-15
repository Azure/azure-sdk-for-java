// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityLogOptionsImpl;
import com.azure.identity.implementation.util.IdentityUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.construct -->
 * <pre>
 * TokenCredential defaultAzureCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.construct -->
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
 * <!-- src_embed com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 * <pre>
 * TokenCredential dacWithUserAssignedManagedIdentity
 *     = new DefaultAzureCredentialBuilder&#40;&#41;.managedIdentityClientId&#40;&quot;&lt;Managed-Identity-Client-Id&quot;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.defaultazurecredential.constructwithuserassignedmanagedidentity -->
 *
 * @see DefaultAzureCredential
 */
public class DefaultAzureCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultAzureCredentialBuilder.class);
    private String tenantId;
    private String managedIdentityClientId;
    private String workloadIdentityClientId;
    private String managedIdentityResourceId;
    private List<String> additionallyAllowedTenants
        = IdentityUtil.getAdditionalTenantsFromEnvironment(Configuration.getGlobalConfiguration().clone());
    private AzureIdentityEnvVars[] requiredEnvVars;

    /**
     * Creates an instance of a DefaultAzureCredentialBuilder.
     */
    public DefaultAzureCredentialBuilder() {
        this.identityClientOptions.setIdentityLogOptionsImpl(new IdentityLogOptionsImpl(true));
        this.identityClientOptions.setChained(true);
    }

    /**
     * Sets the tenant id of the user to authenticate through the {@link DefaultAzureCredential}. If unset, the value
     * in the AZURE_TENANT_ID environment variable will be used. If neither is set, the default is null
     * and will authenticate users to their default tenant.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public DefaultAzureCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Specifies the Microsoft Entra endpoint to acquire tokens.
     * @param authorityHost the Microsoft Entra endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public DefaultAzureCredentialBuilder authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
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
     * @deprecated Support for older Azure Toolkit for IntelliJ versions was
     * <a href="https://aka.ms/azsdk/java/identity/intellij-credential-update">removed in 1.14.0.</a>
     */
    @Deprecated
    public DefaultAzureCredentialBuilder intelliJKeePassDatabasePath(String databasePath) {
        if (CoreUtils.isNullOrEmpty(databasePath)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("The KeePass database path is either empty or not configured."
                    + " Please configure it on the builder."));
        }
        this.identityClientOptions.setIntelliJKeePassDatabasePath(databasePath);
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
     * If this is not configured, the {@link com.azure.core.util.SharedExecutorService} will be used which is
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
        this.identityClientOptions.setExecutorService(executorService);
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
        this.additionallyAllowedTenants
            = IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants));
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
        this.additionallyAllowedTenants = IdentityUtil.resolveAdditionalTenants(additionallyAllowedTenants);
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
        this.identityClientOptions.setCredentialProcessTimeout(credentialProcessTimeout);
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
        this.identityClientOptions.disableInstanceDiscovery();
        return this;
    }

    /**
     * Specifies environment variables that must be present when building the credential.
     * If any of the specified environment variables are missing, {@link #build()} will throw an 
     * {@link IllegalStateException}.
     *
     * @param envVars the environment variables that must be present
     * @return An updated instance of this builder with the required environment variables set as specified.
     */
    public DefaultAzureCredentialBuilder requireEnvVars(AzureIdentityEnvVars... envVars) {
        Objects.requireNonNull(envVars, "envVars cannot be null");

        // Check for null elements in the array
        for (int i = 0; i < envVars.length; i++) {
            if (envVars[i] == null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Environment variable at index " + i + " cannot be null"));
            }
        }

        this.requiredEnvVars = envVars.clone();
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
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Only one of managedIdentityClientId and managedIdentityResourceId can be specified."));
        }

        // Check required environment variables
        if (requiredEnvVars != null && requiredEnvVars.length > 0) {
            Configuration configuration = identityClientOptions.getConfiguration() == null
                ? Configuration.getGlobalConfiguration().clone()
                : identityClientOptions.getConfiguration();

            List<String> missingVars = new ArrayList<>();
            for (AzureIdentityEnvVars envVar : requiredEnvVars) {
                if (CoreUtils.isNullOrEmpty(configuration.get(envVar.toString()))) {
                    missingVars.add(envVar.toString());
                }
            }

            if (!missingVars.isEmpty()) {
                String errorMessage;
                if (missingVars.size() == 1) {
                    errorMessage = "Required environment variable is missing: " + missingVars.get(0)
                        + ". Ensure this environment variable is set before creating the DefaultAzureCredential.";
                } else {
                    errorMessage = "Required environment variables are missing: " + String.join(", ", missingVars)
                        + ". Ensure these environment variables are set before creating the DefaultAzureCredential.";
                }

                throw LOGGER.logExceptionAsError(new IllegalStateException(errorMessage
                    + " See https://aka.ms/azsdk/java/identity/defaultazurecredential/troubleshoot for more information."));
            }
        }

        if (!CoreUtils.isNullOrEmpty(additionallyAllowedTenants)) {
            identityClientOptions.setAdditionallyAllowedTenants(additionallyAllowedTenants);
        }
        return new DefaultAzureCredential(getCredentialsChain());
    }

    private void loadFallbackValuesFromEnvironment() {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone()
            : identityClientOptions.getConfiguration();
        tenantId
            = CoreUtils.isNullOrEmpty(tenantId) ? configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID) : tenantId;
        managedIdentityClientId = CoreUtils.isNullOrEmpty(managedIdentityClientId)
            ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID)
            : managedIdentityClientId;
    }

    private ArrayList<TokenCredential> getCredentialsChain() {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone()
            : identityClientOptions.getConfiguration();

        String selectedCredential = configuration.get("AZURE_TOKEN_CREDENTIALS");
        ArrayList<TokenCredential> credentials = new ArrayList<>(8);

        if (!CoreUtils.isNullOrEmpty(selectedCredential)) {
            selectedCredential = selectedCredential.trim().toLowerCase(Locale.ROOT);

            // Use a map to associate credential names to their adders
            java.util.Map<String, Runnable> credentialMap = new java.util.HashMap<>();
            credentialMap.put("prod", () -> addProdCredentials(credentials));
            credentialMap.put("dev", () -> addDevCredentials(credentials));
            credentialMap.put("environmentcredential",
                () -> credentials.add(new EnvironmentCredential(identityClientOptions.clone())));
            credentialMap.put("workloadidentitycredential", () -> credentials.add(getWorkloadIdentityCredential()));
            credentialMap.put("managedidentitycredential",
                () -> credentials.add(new ManagedIdentityCredential(managedIdentityClientId, managedIdentityResourceId,
                    null, identityClientOptions.clone())));
            credentialMap.put("intellijcredential",
                () -> credentials.add(new IntelliJCredential(tenantId, identityClientOptions.clone())));
            credentialMap.put("azureclicredential",
                () -> credentials.add(new AzureCliCredential(tenantId, identityClientOptions.clone())));
            credentialMap.put("azurepowershellcredential",
                () -> credentials.add(new AzurePowerShellCredential(tenantId, identityClientOptions.clone())));
            credentialMap.put("azuredeveloperclicredential",
                () -> credentials.add(new AzureDeveloperCliCredential(tenantId, identityClientOptions.clone())));
            credentialMap.put("visualstudiocodecredential",
                () -> credentials.add(new VisualStudioCodeCredential(tenantId, identityClientOptions.clone())));

            Runnable adder = credentialMap.get(selectedCredential);
            if (adder != null) {
                adder.run();
                return credentials;
            } else {
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Invalid value for AZURE_TOKEN_CREDENTIALS: '"
                        + selectedCredential + "'. " + "Valid values are: 'prod', 'dev', or one of "
                        + "[EnvironmentCredential, WorkloadIdentityCredential, ManagedIdentityCredential, "
                        + "IntelliJCredential, AzureCliCredential, AzurePowerShellCredential, "
                        + "AzureDeveloperCliCredential, VisualStudioCodeCredential] (case-insensitive). "
                        + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                        + "https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot"));
            }
        }

        // Default case: full chain (prod + dev)
        addProdCredentials(credentials);
        addDevCredentials(credentials);
        return credentials;
    }

    // Helper to add prod credentials
    private void addProdCredentials(List<TokenCredential> credentials) {
        credentials.add(new EnvironmentCredential(identityClientOptions.clone()));
        credentials.add(getWorkloadIdentityCredential());
        credentials.add(new ManagedIdentityCredential(managedIdentityClientId, managedIdentityResourceId, null,
            identityClientOptions.clone()));
    }

    // Helper to add dev credentials
    private void addDevCredentials(List<TokenCredential> credentials) {
        credentials.add(new IntelliJCredential(tenantId, identityClientOptions.clone()));
        credentials.add(new VisualStudioCodeCredential(tenantId, identityClientOptions.clone()));
        credentials.add(new AzureCliCredential(tenantId, identityClientOptions.clone()));
        credentials.add(new AzurePowerShellCredential(tenantId, identityClientOptions.clone()));
        credentials.add(new AzureDeveloperCliCredential(tenantId, identityClientOptions.clone()));
        credentials.add(new BrokerCredential(tenantId));
    }

    private WorkloadIdentityCredential getWorkloadIdentityCredential() {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone()
            : identityClientOptions.getConfiguration();
        String azureAuthorityHost = configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
        String clientId
            = CoreUtils.isNullOrEmpty(workloadIdentityClientId) ? managedIdentityClientId : workloadIdentityClientId;

        if (!CoreUtils.isNullOrEmpty(azureAuthorityHost)) {
            identityClientOptions.setAuthorityHost(azureAuthorityHost);
        }
        return new WorkloadIdentityCredential(tenantId, clientId, null, identityClientOptions.clone());
    }
}

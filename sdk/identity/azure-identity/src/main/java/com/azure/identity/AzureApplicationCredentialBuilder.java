package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Fluent credential builder for instantiating a {@link AzureApplicationCredential}.
 *
 * @see AzureApplicationCredential
 */
public class AzureApplicationCredentialBuilder extends CredentialBuilderBase<AzureApplicationCredentialBuilder> {
    private String managedIdentityClientId;

    /**
     * Creates an instance of a  AzureApplicationCredentialBuilder.
     */
    public AzureApplicationCredentialBuilder() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        managedIdentityClientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
    }


    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public AzureApplicationCredentialBuilder authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
        return this;
    }


    /**
     * Specifies the client ID of user assigned or system assigned identity, when this credential is running
     * in an environment with managed identities. If unset, the value in the AZURE_CLIENT_ID environment variable
     * will be used. If neither is set, the default value is null and will only work with system assigned
     * managed identities and not user assigned managed identities.
     *
     * @param clientId the client ID
     * @return An updated instance of this builder with the managed identity client id set as specified.
     */
    public AzureApplicationCredentialBuilder managedIdentityClientId(String clientId) {
        this.managedIdentityClientId = clientId;
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool()} will be used which is
     * also shared with other application tasks. If the common pool is heavily used for other tasks, authentication
     * requests might starve and setting up this executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    public AzureApplicationCredentialBuilder executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
        return this;
    }

    /**
     * Creates new {@link AzureApplicationCredential} with the configured options set.
     *
     * @return a {@link AzureApplicationCredential} with the current configurations.
     */
    public AzureApplicationCredential build() {
        return new AzureApplicationCredential(getCredentialsChain());
    }

    private ArrayList<TokenCredential> getCredentialsChain() {
        ArrayList<TokenCredential> output = new ArrayList<TokenCredential>(2);
        output.add(new EnvironmentCredential(identityClientOptions));
        output.add(new ManagedIdentityCredential(managedIdentityClientId, identityClientOptions));
        return output;
    }
}


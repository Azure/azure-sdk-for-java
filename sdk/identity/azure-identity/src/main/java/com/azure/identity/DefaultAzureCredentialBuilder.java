// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityLogOptionsImpl;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Fluent credential builder for instantiating a {@link DefaultAzureCredential}.
 *
 * @see DefaultAzureCredential
 */
public class DefaultAzureCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultAzureCredentialBuilder.class);

    private String tenantId;
    private String managedIdentityClientId;
    private String managedIdentityResourceId;
    private List<String> additionallyAllowedTenants = IdentityUtil
        .getAdditionalTenantsFromEnvironment(Configuration.getGlobalConfiguration().clone());


    /**
     * Creates an instance of a DefaultAzureCredentialBuilder.
     */
    public DefaultAzureCredentialBuilder() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        managedIdentityClientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        this.identityClientOptions.setIdentityLogOptionsImpl(new IdentityLogOptionsImpl(true));
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
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
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
     */
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
        this.additionallyAllowedTenants = IdentityUtil.resolveAdditionalTenants(Arrays.asList(additionallyAllowedTenants));
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
     * Allows for setting common properties such as application ID, headers, proxy configuration, etc. Note that it is
     * recommended that this method be called with an instance of the {@link HttpClientOptions}
     * class (a subclass of the {@link ClientOptions} base class). The HttpClientOptions subclass provides more
     * configuration options suitable for HTTP clients, which is applicable for any class that implements this HttpTrait
     * interface.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param clientOptions A configured instance of {@link HttpClientOptions}.
     * @see HttpClientOptions
     * @return An updated instance of this builder with the client options configured.
     */
    public DefaultAzureCredentialBuilder clientOptions(ClientOptions clientOptions) {
        identityClientOptions.setClientOptions(clientOptions);
        return this;
    }

    /**
     * Sets the {@link HttpLogOptions logging configuration} to use when sending and receiving requests to and from
     * the service. If a {@code logLevel} is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     *
     * @param logOptions The {@link HttpLogOptions logging configuration} to use when sending and receiving requests to
     * and from the service.
     * @return An updated instance of this builder with the Http log options configured.
     */
    public DefaultAzureCredentialBuilder httpLogOptions(HttpLogOptions logOptions) {
        identityClientOptions.setHttpLogOptions(logOptions);
        return this;
    }

    /**
     * Sets the {@link RetryPolicy} that is used when each request is sent.
     * Setting this is mutually exclusive with using {@link #retryOptions(RetryOptions)}.
     *
     * The default retry policy will be used in the pipeline, if not provided.
     *
     * @param retryPolicy user's retry policy applied to each request.
     *
     * @return An updated instance of this builder with the retry policy configured.
     */
    public DefaultAzureCredentialBuilder retryPolicy(RetryPolicy retryPolicy) {
        identityClientOptions.setRetryPolicy(retryPolicy);
        return this;
    }

    /**
     * Sets the {@link RetryOptions} for all the requests made through the client.
     *
     * <p><strong>Note:</strong> It is important to understand the precedence order of the HttpTrait APIs. In
     * particular, if a {@link HttpPipeline} is specified, this takes precedence over all other APIs in the trait, and
     * they will be ignored. If no {@link HttpPipeline} is specified, a HTTP pipeline will be constructed internally
     * based on the settings provided to this trait. Additionally, there may be other APIs in types that implement this
     * trait that are also ignored if an {@link HttpPipeline} is specified, so please be sure to refer to the
     * documentation of types that implement this trait to understand the full set of implications.</p>
     * <p>
     * Setting this is mutually exclusive with using {@link #retryPolicy(RetryPolicy)}.
     *
     * @param retryOptions The {@link RetryOptions} to use for all the requests made through the client.
     * @return An updated instance of this builder with the retry options configured.
     */
    public DefaultAzureCredentialBuilder retryOptions(RetryOptions retryOptions) {
        identityClientOptions.setRetryOptions(retryOptions);
        return this;
    }

    /**
     * Creates new {@link DefaultAzureCredential} with the configured options set.
     *
     * @return a {@link DefaultAzureCredential} with the current configurations.
     * @throws IllegalStateException if clientId and resourceId are both set.
     */
    public DefaultAzureCredential build() {
        if (managedIdentityClientId != null && managedIdentityResourceId != null) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException("Only one of managedIdentityResourceId and managedIdentityClientId can be specified."));
        }
        if (!CoreUtils.isNullOrEmpty(additionallyAllowedTenants)) {
            identityClientOptions.setAdditionallyAllowedTenants(additionallyAllowedTenants);
        }
        return new DefaultAzureCredential(getCredentialsChain());
    }

    private ArrayList<TokenCredential> getCredentialsChain() {
        ArrayList<TokenCredential> output = new ArrayList<TokenCredential>(6);
        output.add(new EnvironmentCredential(identityClientOptions.clone()));
        output.add(new ManagedIdentityCredential(managedIdentityClientId, managedIdentityResourceId, identityClientOptions.clone()));
        output.add(new SharedTokenCacheCredential(null, IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID,
            tenantId, identityClientOptions.clone()));
        output.add(new IntelliJCredential(tenantId, identityClientOptions.clone()));
        output.add(new AzureCliCredential(tenantId, identityClientOptions.clone()));
        output.add(new AzurePowerShellCredential(tenantId, identityClientOptions.clone()));
        return output;
    }
}

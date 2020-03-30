/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.sql.SqlServers;
import com.azure.management.sql.models.SqlManagementClientBuilder;
import com.azure.management.sql.models.SqlManagementClientImpl;

/**
 * Entry point to Azure SQLServer resource management.
 */
public class SqlServerManager extends Manager<SqlServerManager, SqlManagementClientImpl> {
    private SqlServers sqlServers;

    private final String tenantId;

    protected SqlServerManager(RestClient restClient, String tenantId, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new SqlManagementClientBuilder()
                    .pipeline(restClient.getHttpPipeline())
                    .subscriptionId(subscriptionId)
                    .host(restClient.getBaseUrl().toString())
                    .build(),
                sdkContext);
        this.tenantId = tenantId;
    }

    /**
     * Get a Configurable instance that can be used to create SqlServer with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new SqlServerManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of SqlServer that exposes Compute resource management API entry points.
     *
     * @param credential the credentials to use
     * @param subscriptionId the subscription
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(AzureTokenCredential credential, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), credential.getDomain(), subscriptionId);
    }

    /**
     * Creates an instance of SqlServer that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param tenantId the tenant UUID
     * @param subscriptionId the subscription
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(RestClient restClient, String tenantId, String subscriptionId) {
        return authenticate(restClient, tenantId, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of SqlServer that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param tenantId the tenant UUID
     * @param subscriptionId the subscription
     * @param sdkContext the sdk context
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(RestClient restClient, String tenantId, String subscriptionId, SdkContext sdkContext) {
        return new SqlServerManager(restClient, tenantId, subscriptionId, sdkContext);
    }



    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of SqlServer that exposes Compute resource management API entry points.
         *
         * @param credential the credentials to use
         * @param subscriptionId the subscription
         * @return the SqlServer
         */
        SqlServerManager authenticate(AzureTokenCredential credential, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public SqlServerManager authenticate(AzureTokenCredential credential, String subscriptionId) {
            return SqlServerManager.authenticate(buildRestClient(credential), credential.getDomain(), subscriptionId);
        }
    }

    /**
     * @return the SQL Server management API entry point
     */
    public SqlServers sqlServers() {
        if (sqlServers == null) {
            sqlServers = new SqlServersImpl(this);
        }

        return sqlServers;
    }

    /**
     * Get the tenant ID value.
     *
     * @return the tenant ID value
     */
    public String tenantId() {
        return this.tenantId;
    }


}

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.sql.SqlServers;

import java.util.UUID;

/**
 * Entry point to Azure Batch Account resource management.
 */
public class SqlServerManager extends Manager<SqlServerManager, SqlManagementClientImpl> {
    private SqlServers sqlServers;

    protected SqlServerManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new SqlManagementClientImpl(restClient).withSubscriptionId(UUID.fromString(subscriptionId)));
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
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new SqlServerManager(credentials.getEnvironment().newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of SqlServer that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(RestClient restClient, String subscriptionId) {
        return new SqlServerManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of SqlServer that exposes Compute resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the SqlServer
         */
        SqlServerManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public SqlServerManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return SqlServerManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    /**
     * @return the batch account management API entry point
     */
    public SqlServers sqlServers() {
        if (sqlServers == null) {
            sqlServers = new SqlServersImpl(
                    super.innerManagementClient.servers(),
                    this);
        }

        return sqlServers;
    }
}

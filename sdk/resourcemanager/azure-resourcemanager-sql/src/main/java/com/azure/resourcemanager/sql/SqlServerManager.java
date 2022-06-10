// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.sql.fluent.SqlManagementClient;
import com.azure.resourcemanager.sql.implementation.SqlManagementClientBuilder;
import com.azure.resourcemanager.sql.implementation.SqlServersImpl;
import com.azure.resourcemanager.sql.models.SqlServers;
import com.azure.resourcemanager.storage.StorageManager;

import java.util.Objects;

/** Entry point to Azure SQLServer resource management. */
public class SqlServerManager extends Manager<SqlManagementClient> {
    private SqlServers sqlServers;
    private final String tenantId;

    private final StorageManager storageManager;

    /**
     * Creates a new instance of {@link SqlServerManager}.
     *
     * @param httpPipeline The HttpPipeline to use.
     * @param profile The AzureProfile to use.
     */
    protected SqlServerManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new SqlManagementClientBuilder()
                .pipeline(httpPipeline)
                .subscriptionId(profile.getSubscriptionId())
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .buildClient());
        this.storageManager = StorageManager.authenticate(httpPipeline, profile);
        this.tenantId = profile.getTenantId();
    }

    /** @return the storage manager in sql manager */
    public StorageManager storageManager() {
        return storageManager;
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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(TokenCredential credential, AzureProfile profile) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        Objects.requireNonNull(profile, "'profile' cannot be null.");
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of SqlServer that exposes Compute resource management API entry points.
     *
     * @param httpPipeline the {@link HttpPipeline} configured with Azure authentication credential.
     * @param profile the profile to use
     * @return the SqlServer
     */
    public static SqlServerManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null.");
        Objects.requireNonNull(profile, "'profile' cannot be null.");
        return new SqlServerManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of SqlServer that exposes Compute resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the SqlServer
         */
        SqlServerManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public SqlServerManager authenticate(TokenCredential credential, AzureProfile profile) {
            return SqlServerManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    /** @return the SQL Server management API entry point */
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

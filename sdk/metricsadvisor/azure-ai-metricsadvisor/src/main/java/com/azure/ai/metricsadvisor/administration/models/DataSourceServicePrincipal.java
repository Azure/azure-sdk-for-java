// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.ai.metricsadvisor.implementation.util.DataSourceServicePrincipalAccessor;
import com.azure.core.annotation.Fluent;

/**
 * The service principal credential entity for data source..
 */
@Fluent
public final class DataSourceServicePrincipal extends DataSourceCredentialEntity {
    private String id;
    private String name;
    private String description;
    private String clientId;
    private String tenantId;
    private String clientSecret;

    static {
        DataSourceServicePrincipalAccessor.setAccessor(
            new DataSourceServicePrincipalAccessor.Accessor() {
                @Override
                public void setId(DataSourceServicePrincipal entity, String id) {
                    entity.setId(id);
                }

                @Override
                public String getClientSecret(DataSourceServicePrincipal entity) {
                    return entity.getClientSecret();
                }
            });
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the client id.
     *
     * @return The client id.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Gets the tenant id.
     *
     * @return The tenant id.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Creates DataSourceServicePrincipal.
     *
     * @param name The name.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     * @param tenantId The tenant id.
     */
    public DataSourceServicePrincipal(String name, String clientId, String tenantId, String clientSecret) {
        this.name = name;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.clientSecret = clientSecret;
    }

    /**
     * Sets the name.
     *
     * @param name The name
     * @return an updated object with name set
     */
    public DataSourceServicePrincipal setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the client id.
     *
     * @param clientId The client id
     * @return an updated object with client id set
     */
    public DataSourceServicePrincipal setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret The client secret
     * @return an updated object with client secret set
     */
    public DataSourceServicePrincipal setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Sets the tenant id.
     *
     * @param tenantId The tenant id
     * @return an updated object with client teant id set
     */
    public DataSourceServicePrincipal setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the description.
     *
     * @param description The description
     * @return an updated object with description set
     */
    public DataSourceServicePrincipal setDescription(String description) {
        this.description = description;
        return this;
    }

    private void setId(String id) {
        this.id = id;
    }

    private String getClientSecret() {
        return this.clientSecret;
    }
}

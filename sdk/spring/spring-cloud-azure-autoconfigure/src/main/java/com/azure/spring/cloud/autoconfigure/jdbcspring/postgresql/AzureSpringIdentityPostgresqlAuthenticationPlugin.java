// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbcspring.postgresql;

import com.azure.spring.cloud.autoconfigure.jdbc.extension.postgresql.AzureIdentityPostgresqlAuthenticationPlugin;

import java.util.Properties;


/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzureSpringIdentityPostgresqlAuthenticationPlugin extends AzureIdentityPostgresqlAuthenticationPlugin {

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureSpringIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        super(properties);
    }
}

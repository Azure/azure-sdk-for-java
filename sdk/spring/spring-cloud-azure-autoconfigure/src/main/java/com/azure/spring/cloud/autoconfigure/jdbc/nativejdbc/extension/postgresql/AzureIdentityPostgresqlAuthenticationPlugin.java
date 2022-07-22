package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.extension.postgresql;// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.implementation.JdbcPluginPropertiesUtils;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver.PasswordResolver;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.resolver.NativeJdbcPluginPasswordResolver;
import org.postgresql.plugin.AuthenticationPlugin;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;

import static org.postgresql.util.PSQLState.INVALID_PASSWORD;


/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzureIdentityPostgresqlAuthenticationPlugin implements AuthenticationPlugin {

    private PasswordResolver<String> passwordResolver;

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        Map<String, String> map = new HashMap<>();
        JdbcPluginPropertiesUtils.convertPropertiesToConfigMap(properties, map);
        passwordResolver = new NativeJdbcPluginPasswordResolver(map);
    }

    /**
     * Get the password.
     *
     * @param art the authentication request type.
     * @return the password.
     * @throws PSQLException when an error occurs.
     */
    @Override
    public char[] getPassword(AuthenticationRequestType art) throws PSQLException {
        String password = passwordResolver.getPassword();
        if (password != null) {
            return password.toCharArray();
        }else {
            throw new PSQLException("Unable to acquire access token", INVALID_PASSWORD);
        }
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.postgresql;

import com.azure.spring.cloud.service.implementation.identity.api.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.api.AzureAuthenticationTemplate;
import org.postgresql.plugin.AuthenticationPlugin;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;

import java.util.Properties;

import static org.postgresql.util.PSQLState.INVALID_PASSWORD;


/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzureIdentityPostgresqlAuthenticationPlugin extends AzureAuthenticationTemplate implements AuthenticationPlugin {

    private static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        AuthProperty.SCOPES.setProperty(properties, OSSRDBMS_SCOPE);
        init(properties);
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
        String password = getTokenAsPassword();
        if (password != null) {
            return password.toCharArray();
        } else {
            throw new PSQLException("Unable to acquire access token", INVALID_PASSWORD);
        }
    }

}

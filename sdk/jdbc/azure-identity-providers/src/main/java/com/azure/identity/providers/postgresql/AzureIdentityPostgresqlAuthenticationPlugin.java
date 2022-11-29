// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.postgresql;

import com.azure.identity.providers.jdbc.implementation.template.AzureAuthenticationTemplate;
import org.postgresql.plugin.AuthenticationPlugin;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;

import java.util.Properties;

import static org.postgresql.util.PSQLState.INVALID_PASSWORD;

/**
 * The authentication plugin that enables authentication with Azure AD.
 */
public class AzureIdentityPostgresqlAuthenticationPlugin implements AuthenticationPlugin {

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        this(new AzureAuthenticationTemplate(), properties);
    }

    AzureIdentityPostgresqlAuthenticationPlugin(AzureAuthenticationTemplate azureAuthenticationTemplate, Properties properties) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
        this.azureAuthenticationTemplate.init(properties);
    }

    /**
     * Callback method to provide the password to use for authentication.
     *
     * @param type The authentication method that the server is requesting.<br/>
     *             <br/>
     *             <p>AzureIdentityPostgresqlAuthenticationPlugin is used as an extension to<br/>
     *             perform authentication with Azure AD,the value here is CLEARTEXT_PASSWORD.</p>
     *             <br/>
     *             When PostgreSQL client trying to connect with PostgreSQL server:<br/>
     *             1. Client will send startup packet to server, the server will return the AuthenticationRequestType it accepts,
     *                If the username is used to perform Azure AD authentication, the server will return CLEARTEXT_PASSWORD.<br/>
     *             2. Client will do authentication (until AuthenticationOk).<br/>
     *
     * @return The password to use.
     * @throws PSQLException It will return a PSQLException if the password is null.
     */
    @Override
    public char[] getPassword(AuthenticationRequestType type) throws PSQLException {
        String password = azureAuthenticationTemplate.getTokenAsPassword();
        if (password != null) {
            return password.toCharArray();
        } else {
            throw new PSQLException("Unable to acquire access token", INVALID_PASSWORD);
        }
    }

    AzureAuthenticationTemplate getAzureAuthenticationTemplate() {
        return azureAuthenticationTemplate;
    }
}

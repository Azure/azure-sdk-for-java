// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.postgresql;

import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
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

    private static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        this.azureAuthenticationTemplate = new AzureAuthenticationTemplate();
        AuthProperty.SCOPES.setProperty(properties, OSSRDBMS_SCOPE);
        azureAuthenticationTemplate.init(properties);
    }

    AzureIdentityPostgresqlAuthenticationPlugin(AzureAuthenticationTemplate azureAuthenticationTemplate, Properties properties) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
        AuthProperty.SCOPES.setProperty(properties, OSSRDBMS_SCOPE);
        this.azureAuthenticationTemplate.init(properties);
    }

    /**
     * Get the password.
     *
     * @param art the authentication request type.
     * @return the password.
     * @throws PSQLException when an error occurs.
     */
    // TODO (zhihaoguo): We need to know the usage of AuthenticationRequestType.
    @Override
    public char[] getPassword(AuthenticationRequestType art) throws PSQLException {
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

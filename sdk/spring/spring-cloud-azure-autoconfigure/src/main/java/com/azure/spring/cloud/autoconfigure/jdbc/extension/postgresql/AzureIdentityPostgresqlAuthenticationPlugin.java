// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc.extension.postgresql;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCPropertiesUtils;
import com.azure.spring.cloud.autoconfigure.jdbc.TokenCredentialProvider;
import org.postgresql.plugin.AuthenticationPlugin;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;

import static org.postgresql.util.PSQLState.INVALID_PASSWORD;

/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzureIdentityPostgresqlAuthenticationPlugin implements AuthenticationPlugin {

    private static String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";


    /**
     * Stores the properties.
     */
    private Properties properties;

    private final AzureJDBCProperties azureJDBCProperties;

    private TokenCredentialProvider tokenCredentialProvider;


    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        this.properties = properties;

        //todo check
        this.azureJDBCProperties = new AzureJDBCProperties();
        AzureJDBCPropertiesUtils.convertPropertiesToAzureProperties(properties, azureJDBCProperties);
        this.tokenCredentialProvider = new TokenCredentialProvider(azureJDBCProperties, true);
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
        char[] password;

        AccessToken accessToken = getAccessToken();

        if (accessToken != null) {
            password = accessToken.getToken().toCharArray();
        } else {
            throw new PSQLException("Unable to acquire access token", INVALID_PASSWORD);
        }

        return password;
    }

    private AccessToken getAccessToken() {
        TokenCredential credential = tokenCredentialProvider.getTokenCredential();
        TokenRequestContext request = new TokenRequestContext();
        ArrayList<String> scopes = new ArrayList<>();
        scopes.add(OSSRDBMS_SCOPE);
        request.setScopes(scopes);
        return credential.getToken(request).block(Duration.ofSeconds(30));
    }
}

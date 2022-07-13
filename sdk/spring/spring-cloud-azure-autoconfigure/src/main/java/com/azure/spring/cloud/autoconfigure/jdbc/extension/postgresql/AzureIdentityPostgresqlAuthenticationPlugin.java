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
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
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
     * Stores the access token.
     */
    private AccessToken accessToken;

    private TokenCredential credential;

    /**
     * Stores the properties.
     */
    private Properties properties;

    private final AzureJDBCProperties azureJDBCProperties;

    private final AzureTokenCredentialResolver tokenCredentialResolver;

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzureIdentityPostgresqlAuthenticationPlugin(Properties properties) {
        this.properties = properties;
        this.azureJDBCProperties = new AzureJDBCProperties();
        this.tokenCredentialResolver =  new AzureTokenCredentialResolver();
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

        accessToken = getAccessToken();

        if (accessToken != null) {
            password = accessToken.getToken().toCharArray();
        } else {
            throw new PSQLException("Unable to acquire access token", INVALID_PASSWORD);
        }

        return password;
    }

    private TokenCredential getTokenCredential() {
        if (credential == null) {
            // Resolve the token credential when there is no credential passed from configs.
            AzureJDBCPropertiesUtils.convertPropertiesToAzureProperties(properties, azureJDBCProperties);
            credential = tokenCredentialResolver.resolve(azureJDBCProperties);
            if (credential == null) {
                // Create DefaultAzureCredential when no credential can be resolved from configs.
                credential = new DefaultAzureCredentialBuilderFactory(azureJDBCProperties).build().build();
            }
        }
        return credential;
    }

    private AccessToken getAccessToken() {
        if (accessToken == null || accessToken.isExpired()) {
            TokenCredential credential = getTokenCredential();
            TokenRequestContext request = new TokenRequestContext();
            ArrayList<String> scopes = new ArrayList<>();
            scopes.add(OSSRDBMS_SCOPE);
            request.setScopes(scopes);
            accessToken = credential.getToken(request).block(Duration.ofSeconds(30));
        }
        return accessToken;
    }
}

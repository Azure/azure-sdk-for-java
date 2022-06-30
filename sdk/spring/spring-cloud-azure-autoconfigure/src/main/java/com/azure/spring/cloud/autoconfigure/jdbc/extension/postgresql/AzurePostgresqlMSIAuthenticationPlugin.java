package com.azure.spring.cloud.autoconfigure.jdbc.extension.postgresql;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;
import org.postgresql.plugin.AuthenticationPlugin;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.postgresql.util.PSQLState.INVALID_PASSWORD;

/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzurePostgresqlMSIAuthenticationPlugin implements AuthenticationPlugin {

    DefaultAzureCredential azureCredential;
    Logger logger = LoggerFactory.getLogger(AzurePostgresqlMSIAuthenticationPlugin.class);
    /**
     * Stores the access token.
     */
    private AccessToken accessToken;

    /**
     * Stores the properties.
     */
    private Properties properties;

    /**
     * Constructor.
     */
    public AzurePostgresqlMSIAuthenticationPlugin() {
    }

    /**
     * Constructor with properties.
     *
     * @param properties the properties.
     */
    public AzurePostgresqlMSIAuthenticationPlugin(Properties properties) {
        this.properties = properties;
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

    private String getClientId() {
        String clientId = null;
        if (properties != null && properties.containsKey("clientid")) {
            clientId = properties.getProperty("clientid");
        }
        return clientId;
    }

    private TokenCredential credential;

    private TokenCredential getTokenCredential() {
        if (credential == null) {
            String clientId = getClientId();
            if (clientId != null && !clientId.isEmpty()) {
                credential = new DefaultAzureCredentialBuilder().managedIdentityClientId(clientId).build();
            } else {
                credential = new DefaultAzureCredentialBuilder().build();
            }
        }
        return credential;
    }

    private AccessToken getAccessToken() {
        if (accessToken == null || accessToken.isExpired()) {
            TokenCredential credential = getTokenCredential();
            TokenRequestContext request = new TokenRequestContext();
            ArrayList<String> scopes = new ArrayList<>();
            scopes.add("https://ossrdbms-aad.database.windows.net");
            request.setScopes(scopes);
            accessToken = credential.getToken(request).block(Duration.ofSeconds(30));
        }
        return accessToken;
    }
}

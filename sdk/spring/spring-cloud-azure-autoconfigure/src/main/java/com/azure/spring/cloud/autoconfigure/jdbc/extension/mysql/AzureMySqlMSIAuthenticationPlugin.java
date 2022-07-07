package com.azure.spring.cloud.autoconfigure.jdbc.extension.mysql;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jdbc.AzureJDBCPropertiesUtils;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.factory.credential.DefaultAzureCredentialBuilderFactory;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzureMySqlMSIAuthenticationPlugin implements AuthenticationPlugin<NativePacketPayload> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureMySqlMSIAuthenticationPlugin.class);

    private static String PLUGIN_NAME = "mysql_clear_password";

    private static String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    /**
     * Stores the access token.
     */
    private AccessToken accessToken;

    private TokenCredential credential;

    /**
     * Stores the callback handler.
     */
    private MysqlCallbackHandler callbackHandler;

    private final AzureJDBCProperties azureJDBCProperties;

    private final AzureTokenCredentialResolver tokenCredentialResolver;

    /**
     * Stores the protocol.
     */
    private Protocol<NativePacketPayload> protocol;

    private String sourceOfAuthData;

    public AzureMySqlMSIAuthenticationPlugin() {
        this(new AzureJDBCProperties(), new AzureTokenCredentialResolver());
    }

    public AzureMySqlMSIAuthenticationPlugin(AzureJDBCProperties azureJDBCProperties, AzureTokenCredentialResolver tokenCredentialResolver) {
        this.azureJDBCProperties = azureJDBCProperties;
        this.tokenCredentialResolver = tokenCredentialResolver;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getProtocolPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void init(Protocol<NativePacketPayload> protocol) {
        this.protocol = protocol;
    }

    @Override
    public void init(Protocol<NativePacketPayload> protocol, MysqlCallbackHandler callbackHandler) {
        this.init(protocol);
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean isReusable() {
        return true;
    }

    @Override
    public boolean nextAuthenticationStep(NativePacketPayload fromServer,
                                          List<NativePacketPayload> toServer) {

        /*
         * See com.mysql.cj.protocol.a.authentication.MysqlClearPasswordPlugin
         */
        toServer.clear();
        NativePacketPayload response;

        if (fromServer == null) {
            response = new NativePacketPayload(new byte[0]);
        } else {
            if (protocol.getSocketConnection().isSSLEstablished()) {
                try {
                    String password = getAccessToken().getToken();
                    byte[] content = password.getBytes(
                        protocol.getServerSession()
                                .getCharsetSettings()
                                .getPasswordCharacterEncoding());
                    response = new NativePacketPayload(content);
                    response.setPosition(response.getPayloadLength());
                    response.writeInteger(NativeConstants.IntegerDataType.INT1, 0);
                    response.setPosition(0);
                } catch (Exception uee) {
                    LOGGER.error(uee.getMessage(), uee);
                    response = new NativePacketPayload(new byte[0]);
                }
            } else {
                response = new NativePacketPayload(new byte[0]);
            }
        }

        toServer.add(response);
        return true;
    }

    @Override
    public boolean requiresConfidentiality() {
        return true;
    }

    @Override
    public void reset() {
        accessToken = null;
    }

    @Override
    public void setAuthenticationParameters(String username, String password) {    }

    @Override
    public void setSourceOfAuthData(String sourceOfAuthData) {
        this.sourceOfAuthData = sourceOfAuthData;
    }

    private TokenCredential getTokenCredential() {
        if (credential == null) {
            // Resolve the token credential when there is no credential passed from configs.
            AzureJDBCPropertiesUtils.convertPropertySetToAzureProperties(protocol.getPropertySet(), azureJDBCProperties);
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

package com.azure.spring.cloud.autoconfigure.jdbc.extension.mysql;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.callback.UsernameCallback;
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
    public static String PLUGIN_NAME = "mysql_clear_password";
    // public static String PLUGIN_NAME = "aad_auth";
    // public static String PLUGIN_NAME = "azure_mysql_msi";

    Logger logger = LoggerFactory.getLogger(AzureMySqlMSIAuthenticationPlugin.class);

    /**
     * Stores the access token.
     */
    private AccessToken accessToken;

    /**
     * Stores the callback handler.
     */
    private MysqlCallbackHandler callbackHandler;

    /**
     * Stores the protocol.
     */
    private Protocol<NativePacketPayload> protocol;

    private String sourceOfAuthData;

    @Override
    public void destroy() {

    }

    @Override
    public String getProtocolPluginName() {
        return PLUGIN_NAME;
        // return "mysql_clear_password";
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
                    logger.error(uee.getMessage(), uee);
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

    private String getClientId() {
        String clientId;
        if (this.protocol.getPropertySet().getProperty("clientid") != null) {
            logger.info("clientid=" + this.protocol.getPropertySet().getProperty("clientid"));
            clientId = this.protocol.getPropertySet().getStringProperty("clientid").getStringValue();
        } else {
            clientId = null;
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

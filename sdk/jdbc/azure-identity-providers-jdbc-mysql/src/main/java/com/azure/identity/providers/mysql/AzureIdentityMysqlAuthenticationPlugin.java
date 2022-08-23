// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.mysql;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
import com.azure.identity.providers.jdbc.implementation.template.AzureAuthenticationTemplate;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;

import java.util.List;
import java.util.Properties;

/**
 * The authentication plugin that enables authentication with Azure AD.
 */
public class AzureIdentityMysqlAuthenticationPlugin implements AuthenticationPlugin<NativePacketPayload> {
    private static final ClientLogger LOGGER = new ClientLogger(AzureIdentityMysqlAuthenticationPlugin.class);
    private static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";
    private static final String PLUGIN_NAME = "mysql_clear_password";

    private final AzureAuthenticationTemplate azureAuthenticationTemplate;

    /**
     * Stores the protocol.SharedTokenCacheCredential
     */
    private Protocol<NativePacketPayload> protocol;

    /**
     * Default constructor of AzureIdentityMysqlAuthenticationPlugin.
     */
    public AzureIdentityMysqlAuthenticationPlugin() {
        this(new AzureAuthenticationTemplate());
    }

    AzureIdentityMysqlAuthenticationPlugin(AzureAuthenticationTemplate azureAuthenticationTemplate) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
    }

    AzureIdentityMysqlAuthenticationPlugin(AzureAuthenticationTemplate azureAuthenticationTemplate,
                                           Protocol<NativePacketPayload> protocol) {
        this.azureAuthenticationTemplate = azureAuthenticationTemplate;
        this.protocol = protocol;
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
        Properties properties = protocol.getPropertySet().exposeAsProperties();
        AuthProperty.SCOPES.setProperty(properties, OSSRDBMS_SCOPE);
        azureAuthenticationTemplate.init(properties);
    }

    @Override
    public void init(Protocol<NativePacketPayload> protocol, MysqlCallbackHandler callbackHandler) {
        this.init(protocol);
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
                    String password = azureAuthenticationTemplate.getTokenAsPassword();
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

    /**
     * Does this plugin require the connection itself to be confidential (i.e. tls/ssl)...Highly recommended to return "true" for plugins that return the
     * credentials in the clear.
     *
     * @return true if secure connection is required
     */
    @Override
    public boolean requiresConfidentiality() {
        return true;
    }

    @Override
    public void setAuthenticationParameters(String username, String password) {
    }

}

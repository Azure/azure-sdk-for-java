// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.mysql;

import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import com.mysql.cj.callback.MysqlCallbackHandler;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativePacketPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * The authentication plugin that enables Azure AD managed identity support.
 */
public class AzureIdentityMysqlAuthenticationPlugin implements AuthenticationPlugin<NativePacketPayload> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureIdentityMysqlAuthenticationPlugin.class);
    public static final String OSSRDBMS_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";

    private static final String PLUGIN_NAME = "mysql_clear_password";

    private final AzureAuthenticationTemplate azureAuthenticationTemplate = new AzureAuthenticationTemplate();

    /**
     * Stores the protocol.SharedTokenCacheCredential
     */
    private Protocol<NativePacketPayload> protocol;

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

    // TODO (zhihaoguo): where to use requiresConfidentiality?
    @Override
    public boolean requiresConfidentiality() {
        return true;
    }

    @Override
    public void setAuthenticationParameters(String username, String password) {
    }

}

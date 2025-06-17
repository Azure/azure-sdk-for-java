package com.azure.identity.broker;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AuthenticationRecord;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.broker.implementation.BrokerUtil;
import reactor.core.publisher.Mono;

import static com.azure.identity.broker.implementation.BrokerUtil.loadVSCodeAuthRecord;

public class VisualStudioCodeBrokerCredential implements TokenCredential {
    private static final String VSCODE_CLIENT_ID = "aebc6443-996d-45c2-90f0-388ff96faa56";
    InteractiveBrowserBrokerCredentialBuilder credentialBuilder;

    VisualStudioCodeBrokerCredential(InteractiveBrowserBrokerCredentialBuilder credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        return getBrokerAuthCredential().getToken(tokenRequestContext);
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        return TokenCredential.super.getTokenSync(request);
    }

    TokenCredential getBrokerAuthCredential() {
        if (!BrokerUtil.isVsCodeBrokerAuthAvailable()) {
            throw new CredentialUnavailableException("Visual Studio Code Broker Auth not available.");
        }
        try {
            AuthenticationRecord authenticationRecord = loadVSCodeAuthRecord();
            credentialBuilder.authenticationRecord(authenticationRecord);
            credentialBuilder.clientId(VSCODE_CLIENT_ID);
            credentialBuilder.setWindowHandle(0);
            return credentialBuilder.build();
        } catch (Exception e) {
            throw new CredentialUnavailableException("Failed to create Browser Broker Credential.", e);
        }
    }

}

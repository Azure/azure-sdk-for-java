// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalToken;
import com.azure.identity.implementation.VisualStudioCacheAccessor;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.identity.implementation.util.IdentityUtil.isVsCodeBrokerAuthAvailable;
import static com.azure.identity.implementation.util.IdentityUtil.loadVSCodeAuthRecord;

/**
 * <p>Enables authentication to Microsoft Entra ID as the user signed in to Visual Studio Code via
 * the 'Azure Resources' extension.</p>
 *
 * <p>Pre-requisites:
 *
 * 1. Ensure you have Azure Resources extension installed in VS Code and have complete Azure:Sign In in VS Code IDE.
 * 2. Ensure you have azure-identity-brokers dependency added in your application.
 * </p>
 *
 * @see com.azure.identity
 * @see VisualStudioCodeCredentialBuilder
 */
public class VisualStudioCodeCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(VisualStudioCodeCredential.class);
    private static final String VSCODE_CLIENT_ID = "aebc6443-996d-45c2-90f0-388ff96faa56";
    private static final String BROKER_BUILDER_CLASS
        = "com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder";
    private final IdentityClientOptions clientOptions;
    private final String tenant;

    /**
     * Creates a public class VisualStudioCodeCredential implements TokenCredential with the given tenant and
     * identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param identityClientOptions the options for configuring the identity client
     */
    VisualStudioCodeCredential(String tenantId, IdentityClientOptions identityClientOptions) {
        clientOptions = (identityClientOptions == null ? new IdentityClientOptions() : identityClientOptions);

        if (!CoreUtils.isNullOrEmpty(tenantId)) {
            tenant = tenantId;
        } else {
            tenant = null;
        }
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            TokenCredential brokerAuthCredential = getBrokerAuthCredential(VSCODE_CLIENT_ID);
            if (brokerAuthCredential != null) {
                return brokerAuthCredential.getToken(request);
            } else {
                return Mono
                    .error(new CredentialUnavailableException("Visual Studio Code Authentication is not available."
                        + " Ensure you have azure-identity-broker dependency added to your application."
                        + " Then ensure, you have signed into Azure via VS Code and have Azure Resources Extension installed in VS Code."));
            }
        }).doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request)).doOnError(error -> {
            LoggingUtil.logTokenError(LOGGER, clientOptions, request, error);
        });
    }

    TokenCredential getBrokerAuthCredential(String clientId) {
        if (!isVsCodeBrokerAuthAvailable()) {
            return null;
        }
        try {
            Class<?> builderClass = Class.forName(BROKER_BUILDER_CLASS);
            Object builder = builderClass.getConstructor().newInstance();
            AuthenticationRecord authenticationRecord = loadVSCodeAuthRecord();
            builderClass.getMethod("setWindowHandle", long.class).invoke(builder, 0);
            InteractiveBrowserCredentialBuilder browserCredentialBuilder
                = (InteractiveBrowserCredentialBuilder) builder;
            browserCredentialBuilder.clientId(clientId);
            browserCredentialBuilder.authenticationRecord(authenticationRecord);
            if (CoreUtils.isNullOrEmpty(tenant)) {
                builderClass.getMethod("tenantId", String.class).invoke(builder, authenticationRecord.getTenantId());
            } else {
                builderClass.getMethod("tenantId", String.class).invoke(builder, tenant);
            }
            return (TokenCredential) browserCredentialBuilder.build();
        } catch (ClassNotFoundException e) {
            // Broker not on classpath
            return null;
        } catch (Exception e) {
            throw new CredentialUnavailableException("Failed to create VisualStudioCodeCredential dynamically", e);
        }
    }
}

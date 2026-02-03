// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import static com.azure.identity.implementation.util.IdentityUtil.isVsCodeBrokerAuthAvailable;
import static com.azure.identity.implementation.util.IdentityUtil.loadVSCodeAuthRecord;

/**
 * Enables authentication to Microsoft Entra ID using the user account signed in through the
 * <a href="https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azureresourcegroups">
 * Azure Resources</a> extension in Visual Studio Code.
 *
 * <p><b>Prerequisites:</b></p>
 * <ol>
 *   <li>Install the
 *     <a href="https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azureresourcegroups">
 *     Azure Resources</a> extension in Visual Studio Code and sign in using the <b>Azure: Sign In</b> command.</li>
 *   <li>Add the
 *     <a href="https://central.sonatype.com/artifact/com.azure/azure-identity-broker">
 *     azure-identity-broker</a> dependency to your project's build configuration.</li>
 * </ol>
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
                        + " Then ensure, you have signed into Azure via VS Code and have Azure Resources Extension installed in VS Code."
                        + " For more details, refer to https://aka.ms/azsdk/java/identity/vscodecredential/troubleshoot"));
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
            return browserCredentialBuilder.build();
        } catch (ClassNotFoundException e) {
            // Broker not on classpath
            return null;
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("Failed to create VisualStudioCodeCredential dynamically", e));
        }
    }
}

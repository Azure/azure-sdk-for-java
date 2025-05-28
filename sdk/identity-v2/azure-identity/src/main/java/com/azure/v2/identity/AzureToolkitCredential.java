// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.client.PublicClient;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.IdentityConstants;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

/**
 * <p>
 * The AzureToolkitCredential authenticates in a development environment and acquires a token on behalf of the
 * logged-in account in Azure Toolkit for IntelliJ/Eclipse. It uses the logged in user information on the
 * IntelliJ/Eclipse IDE and uses it to authenticate the application against Microsoft Entra ID.</p>
 *
 * <h2>Configure AzureToolkitCredential</h2>
 *
 * <p>Follow the steps outlined below, if using IntelliJ:</p>
 *
 * <ol>
 *     <li>In your IntelliJ window, open File > Settings > Plugins.</li>
 *     <li>Search for "Azure Toolkit for IntelliJ" in the marketplace. Install and restart IDE.</li>
 *     <li>Find the new menu item Tools > Azure > Azure Sign In.</li>
 *     <li>Device Login will help you log in as a user account. Follow the instructions to log in on the
 *     login.microsoftonline.com website with the device code. IntelliJ will prompt you to select your subscriptions.
 *     Select the subscription with the resources that you want to access.</li>
 * </ol>
 *
 * TODO: Add similar instructions for Eclipse IDE
 *
 * <p> Once the developer has followed the steps above and authenticated successfully with
 * Azure Tools for IntelliJ/Eclipse plugin in the IntelliJ/Eclipse IDE then this credential can be used in the
 * development code to reuse the cached plugin credentials.</p>
 *
 * <p><strong>Sample: Construct AzureToolkitCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link AzureToolkitCredential},
 * using the {@link AzureToolkitCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 *
 * <pre>
 * TokenCredential azureToolkitCredential = new AzureToolkitCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see AzureToolkitCredentialBuilder
 */
public class AzureToolkitCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AzureToolkitCredential.class);
    private static final String AZURE_TOOLKIT_CLIENT_ID = "61d65f5a-6e3b-468b-af73-a033f5098c5c";
    private final PublicClient publicClient;
    private final AtomicReference<MsalToken> cachedToken;

    /**
     * Creates an {@link AzureToolkitCredential} with the given public client options.
     * @param publicClientOptions the options to configure the public client
     */
    AzureToolkitCredential(PublicClientOptions publicClientOptions) {
        String tenant = publicClientOptions.getTenantId();

        if (tenant == null) {
            publicClientOptions.setTenantId("common");
        }

        publicClientOptions.setClientId(IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID);

        publicClient = new PublicClient(publicClientOptions);

        this.cachedToken = new AtomicReference<>();
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            if (cachedToken.get() != null) {
                return publicClient.authenticateWithPublicClientCache(request, cachedToken.get().getAccount());
            }
        } catch (RuntimeException ex) {
        }

        try {
            MsalToken msalToken = publicClient.authenticateWithAzureToolkit(request);
            cachedToken.set(msalToken);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return msalToken;
        } catch (RuntimeException ex) {
            throw logAndThrowTokenError(LOGGER, request, ex,
                publicClient.getClientOptions().isChained()
                    ? CredentialUnavailableException::new
                    : CredentialAuthenticationException::new);
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.ConfidentialClient;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

/**
 * <p>On Behalf of authentication in Azure is a way for a user or application to authenticate to a service or resource
 * using credentials from another identity provider. This type of authentication is typically used when a user or
 * application wants to access a resource in Azure, but their credentials are managed by a different identity provider,
 * such as an on-premises Active Directory or a third-party identity provider.
 * To use "On Behalf of" authentication in Azure, the user must first authenticate to the identity provider using their
 * credentials. The identity provider then issues a security token that contains information about the user and their
 * permissions. This security token is then passed to Azure, which uses it to authenticate the user or application and
 * grant them access to the requested resource.
 * The OnBehalfOfCredential acquires a token with a client secret/certificate and user assertion for a Microsoft Entra application
 * on behalf of a user principal.</p>
 *
 * <p>The following code sample demonstrates the creation of a {@link OnBehalfOfCredential},
 * using the {@link OnBehalfOfCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link OnBehalfOfCredential}. The {@code userAssertion} can be optionally specified on the
 * {@link OnBehalfOfCredentialBuilder}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder&#40;&#41;.clientId&#40;&quot;&lt;app-client-ID&gt;&quot;&#41;
 *     .clientSecret&#40;&quot;&lt;app-Client-Secret&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;app-tenant-ID&gt;&quot;&#41;
 *     .userAssertion&#40;&quot;&lt;user-assertion&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see OnBehalfOfCredentialBuilder
 */
public class OnBehalfOfCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(OnBehalfOfCredential.class);

    private final ConfidentialClient confidentialClient;

    /**
     * Creates OnBehalfOfCredential with the specified Microsoft Entra application details and client options.
     *
     * @param confidentialClientOptions the options for configuring the confidential client
     */
    OnBehalfOfCredential(ConfidentialClientOptions confidentialClientOptions) {
        confidentialClient = new ConfidentialClient(confidentialClientOptions);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken token = confidentialClient.authenticate(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (RuntimeException e) {
        }

        try {
            AccessToken token = confidentialClient.authenticateWithOBO(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, request, e, CredentialAuthenticationException::new);
        }
    }
}

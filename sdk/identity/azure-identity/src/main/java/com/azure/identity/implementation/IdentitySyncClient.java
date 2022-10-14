// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.CoreUtils;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.ScopeUtil;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IdentitySyncClient extends IdentityClientBase {

    private final SynchronousAccessor<PublicClientApplication> publicClientApplicationAccessor;
    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;
    private final SynchronousAccessor<ConfidentialClientApplication> managedIdentityConfidentialClientApplicationAccessor;
    private final SynchronousAccessor<String> clientAssertionAccessor;


    /**
     * Creates an IdentityClient with the given options.
     *
     * @param tenantId the tenant ID of the application.
     * @param clientId the client ID of the application.
     * @param clientSecret the client secret of the application.
     * @param resourceId the resource ID of the application
     * @param certificatePath the path to the PKCS12 or PEM certificate of the application.
     * @param certificate the PKCS12 or PEM certificate of the application.
     * @param certificatePassword the password protecting the PFX certificate.
     * @param isSharedTokenCacheCredential Indicate whether the credential is
     * {@link com.azure.identity.SharedTokenCacheCredential} or not.
     * @param clientAssertionTimeout the timeout to use for the client assertion.
     * @param options the options configuring the client.
     */
    IdentitySyncClient(String tenantId, String clientId, String clientSecret, String certificatePath,
                       String clientAssertionFilePath, String resourceId, Supplier<String> clientAssertionSupplier,
                       InputStream certificate, String certificatePassword, boolean isSharedTokenCacheCredential,
                       Duration clientAssertionTimeout, IdentityClientOptions options) {
        super(tenantId, clientId, clientSecret, certificatePath, clientAssertionFilePath, resourceId, clientAssertionSupplier,
            certificate, certificatePassword, isSharedTokenCacheCredential, clientAssertionTimeout, options);

        this.publicClientApplicationAccessor = new SynchronousAccessor<>(() ->
            this.getPublicClient(isSharedTokenCacheCredential));

        this.confidentialClientApplicationAccessor = new SynchronousAccessor<>(() ->
            this.getConfidentialClient());

        this.managedIdentityConfidentialClientApplicationAccessor = new SynchronousAccessor<>(() ->
            this.getManagedIdentityConfidentialClient());

        this.clientAssertionAccessor = clientAssertionTimeout == null
            ? new SynchronousAccessor<>(() -> parseClientAssertion(), Duration.ofMinutes(5))
            : new SynchronousAccessor<>(() -> parseClientAssertion(), clientAssertionTimeout);
    }

    private String parseClientAssertion() {
        if (clientAssertionFilePath != null) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(clientAssertionFilePath));
                return new String(encoded, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        } else {
            throw LOGGER.logExceptionAsError(new IllegalStateException(
                "Client Assertion File Path is not provided."
                    + " It should be provided to authenticate with client assertion."
            ));
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithConfidentialClient(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient =  confidentialClientApplicationAccessor.getValue();
        ClientCredentialParameters.ClientCredentialParametersBuilder builder =
            ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil
                    .resolveTenantId(tenantId, request, options));
        if (clientAssertionSupplier != null) {
            builder.clientCredential(ClientCredentialFactory
                .createFromClientAssertion(clientAssertionSupplier.get()));
        }
        try {
            return new MsalToken(confidentialClient.acquireToken(builder.build()).get());
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    public AccessToken authenticateWithManagedIdentityConfidentialClient(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient =  managedIdentityConfidentialClientApplicationAccessor.getValue();
        ClientCredentialParameters.ClientCredentialParametersBuilder builder =
            ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil
                    .resolveTenantId(tenantId, request, options));
        try {
            return new MsalToken(confidentialClient.acquireToken(builder.build()).get());
        } catch (Exception e) {
            throw new CredentialUnavailableException("Managed Identity authentication is not available.", e);
        }
    }


    public AccessToken authenticateWithConfidentialClientCache(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClientApplication = confidentialClientApplicationAccessor.getValue();
        SilentParameters.SilentParametersBuilder parametersBuilder = SilentParameters.builder(new HashSet<>(request.getScopes()))
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        try {
            IAuthenticationResult authenticationResult = confidentialClientApplication.acquireTokenSilently(parametersBuilder.build()).get();
            AccessToken accessToken =  new MsalToken(authenticationResult);
            if (OffsetDateTime.now().isBefore(accessToken.getExpiresAt().minus(REFRESH_OFFSET))) {
                return accessToken;
            } else {
                throw new IllegalStateException("Received token is close to expiry.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException(e.getMessage(), null, e));
        }
    }


    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @param account the account used to log in to acquire the last token
     * @return a Publisher that emits an AccessToken
     */
    @SuppressWarnings("deprecation")
    public MsalToken authenticateWithPublicClientCache(TokenRequestContext request, IAccount account) {
        PublicClientApplication pc =  publicClientApplicationAccessor.getValue();
        SilentParameters.SilentParametersBuilder parametersBuilder = SilentParameters.builder(
            new HashSet<>(request.getScopes()));

        if (request.getClaims() != null) {
            ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(customClaimRequest);
            parametersBuilder.forceRefresh(true);
        }

        if (account != null) {
            parametersBuilder = parametersBuilder.account(account);
        }
        parametersBuilder.tenant(
            IdentityUtil.resolveTenantId(tenantId, request, options));
        try {
            MsalToken accessToken = new MsalToken(pc.acquireTokenSilently(parametersBuilder.build()).get());
            if (OffsetDateTime.now().isBefore(accessToken.getExpiresAt().minus(REFRESH_OFFSET))) {
                return accessToken;
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException(e.getMessage(), null, e));
        }

        SilentParameters.SilentParametersBuilder forceParametersBuilder = SilentParameters.builder(
            new HashSet<>(request.getScopes())).forceRefresh(true);

        if (request.getClaims() != null) {
            ClaimsRequest customClaimRequest = CustomClaimRequest
                .formatAsClaimsRequest(request.getClaims());
            forceParametersBuilder.claims(customClaimRequest);
        }

        if (account != null) {
            forceParametersBuilder = forceParametersBuilder.account(account);
        }
        forceParametersBuilder.tenant(
            IdentityUtil.resolveTenantId(tenantId, request, options));
        try {
            return new MsalToken(pc.acquireTokenSilently(forceParametersBuilder.build()).get());
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException(e.getMessage(), null, e));
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a username and a password.
     *
     * @param request the details of the token request
     * @param username the username of the user
     * @param password the password of the user
     * @return a Publisher that emits an AccessToken
     */
    public MsalToken authenticateWithUsernamePassword(TokenRequestContext request,
                                                            String username, String password) {
        PublicClientApplication pc =  publicClientApplicationAccessor.getValue();
        UserNamePasswordParameters.UserNamePasswordParametersBuilder userNamePasswordParametersBuilder =
            buildUsernamePasswordFlowParameters(request, username, password);
        try {
            return new MsalToken(pc.acquireToken(userNamePasswordParametersBuilder.build()).get());
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Failed to acquire token with username and "
                + "password. To mitigate this issue, please refer to the troubleshooting guidelines "
                + "here at https://aka.ms/azsdk/java/identity/usernamepasswordcredential/troubleshoot",
                null, e));
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a device code challenge. Active Directory will provide
     * a device code for login and the user must meet the challenge by authenticating in a browser on the current or a
     * different device.
     *
     * @param request the details of the token request
     * @param deviceCodeConsumer the user provided closure that will consume the device code challenge
     * @return a Publisher that emits an AccessToken when the device challenge is met, or an exception if the device
     *     code expires
     */
    public MsalToken authenticateWithDeviceCode(TokenRequestContext request,
                                                      Consumer<DeviceCodeInfo> deviceCodeConsumer) {
        PublicClientApplication pc =  publicClientApplicationAccessor.getValue();
        DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder = buildDeviceCodeFlowParameters(request, deviceCodeConsumer);

        try {
            return new MsalToken(pc.acquireToken(parametersBuilder.build()).get());
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Failed to acquire token with device code.", null, e));
        }
    }

    /**
     * Synchronously acquire a token from Active Directory by opening a browser and wait for the user to login. The
     * credential will run a minimal local HttpServer at the given port, so {@code http://localhost:{port}} must be
     * listed as a valid reply URL for the application.
     *
     * @param request the details of the token request
     * @param port the port on which the HTTP server is listening
     * @param redirectUrl the redirect URL to listen on and receive security code
     * @param loginHint the username suggestion to pre-fill the login page's username/email address field
     * @return a Publisher that emits an AccessToken
     */
    public MsalToken authenticateWithBrowserInteraction(TokenRequestContext request, Integer port,
                                                              String redirectUrl, String loginHint) {
        URI redirectUri;
        String redirect;

        if (port != null) {
            redirect = HTTP_LOCALHOST + ":" + port;
        } else if (redirectUrl != null) {
            redirect = redirectUrl;
        } else {
            redirect = HTTP_LOCALHOST;
        }

        try {
            redirectUri = new URI(redirect);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }

        InteractiveRequestParameters.InteractiveRequestParametersBuilder builder = buildInteractiveRequestParameters(request, loginHint, redirectUri);

        PublicClientApplication pc =  publicClientApplicationAccessor.getValue();
        try {
            return new MsalToken(pc.acquireToken(builder.build()).get());
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                "Failed to acquire token with Interactive Browser Authentication.", null, e));
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure CLI.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithAzureCli(TokenRequestContext request) {

        StringBuilder azCommand = new StringBuilder("az account get-access-token --output json --resource ");

        String scopes = ScopeUtil.scopesToResource(request.getScopes());

        try {
            ScopeUtil.validateScope(scopes);
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }

        azCommand.append(scopes);

        String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);

        if (!CoreUtils.isNullOrEmpty(tenant)) {
            azCommand.append(" --tenant ").append(tenant);
        }

        try {
            return getTokenFromAzureCLIAuthentication(azCommand);
        } catch (RuntimeException e) {
            throw (e instanceof CredentialUnavailableException
                ? LoggingUtil.logCredentialUnavailableException(LOGGER, options, (CredentialUnavailableException) e)
                : LOGGER.logExceptionAsError(e));
        }

    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure PowerShell.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithOBO(TokenRequestContext request) {
        ConfidentialClientApplication cc = confidentialClientApplicationAccessor.getValue();
        try {
            return new MsalToken(cc.acquireToken(buildOBOFlowParameters(request)).get());
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Failed to acquire token with On Behalf Of Authentication.", null, e));
        }
    }

    /**
     * Get the configured identity client options.
     *
     * @return the client options.
     */
    public IdentityClientOptions getIdentityClientOptions() {
        return options;
    }

    @Override
    Mono<AccessToken> getTokenFromTargetManagedIdentity(TokenRequestContext tokenRequestContext) {
        return null;
    }
}

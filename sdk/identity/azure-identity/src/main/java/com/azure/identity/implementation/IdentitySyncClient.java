// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.CoreUtils;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.ScopeUtil;
import com.azure.identity.implementation.util.ValidationUtil;
import com.microsoft.aad.msal4j.AppTokenProviderParameters;
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
import com.microsoft.aad.msal4j.TokenProviderResult;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IdentitySyncClient extends IdentityClientBase {

    private final SynchronousAccessor<PublicClientApplication> publicClientApplicationAccessor;
    private final SynchronousAccessor<PublicClientApplication> publicClientApplicationAccessorWithCae;
    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;

    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessorWithCae;
    private final SynchronousAccessor<ConfidentialClientApplication> workloadIdentityConfidentialClientApplicationAccessor;
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
        String clientAssertionFilePath, String resourceId, String objectId, Supplier<String> clientAssertionSupplier,
        Function<HttpPipeline, String> clientAssertionSupplierWithHttpPipeline, byte[] certificate,
        String certificatePassword, boolean isSharedTokenCacheCredential, Duration clientAssertionTimeout,
        IdentityClientOptions options) {
        super(tenantId, clientId, clientSecret, certificatePath, clientAssertionFilePath, resourceId, objectId,
            clientAssertionSupplier, clientAssertionSupplierWithHttpPipeline, certificate, certificatePassword,
            isSharedTokenCacheCredential, clientAssertionTimeout, options);

        this.publicClientApplicationAccessor
            = new SynchronousAccessor<>(() -> this.getPublicClient(isSharedTokenCacheCredential, false));

        this.publicClientApplicationAccessorWithCae
            = new SynchronousAccessor<>(() -> this.getPublicClient(isSharedTokenCacheCredential, true));

        this.confidentialClientApplicationAccessor = new SynchronousAccessor<>(() -> this.getConfidentialClient(false));

        this.confidentialClientApplicationAccessorWithCae
            = new SynchronousAccessor<>(() -> this.getConfidentialClient(true));

        this.workloadIdentityConfidentialClientApplicationAccessor
            = new SynchronousAccessor<>(() -> this.getWorkloadIdentityConfidentialClient());

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
            throw LOGGER.logExceptionAsError(new IllegalStateException("Client Assertion File Path is not provided."
                + " It should be provided to authenticate with client assertion."));
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithConfidentialClient(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient = getConfidentialClientInstance(request).getValue();
        ClientCredentialParameters.ClientCredentialParametersBuilder builder
            = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
        if (clientAssertionSupplier != null) {
            builder.clientCredential(ClientCredentialFactory.createFromClientAssertion(clientAssertionSupplier.get()));
        } else if (clientAssertionSupplierWithHttpPipeline != null) {
            builder.clientCredential(ClientCredentialFactory
                .createFromClientAssertion(clientAssertionSupplierWithHttpPipeline.apply(getPipeline())));
        }
        try {
            return new MsalToken(confidentialClient.acquireToken(builder.build()).get());
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private SynchronousAccessor<ConfidentialClientApplication>
        getConfidentialClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled()
            ? confidentialClientApplicationAccessorWithCae
            : confidentialClientApplicationAccessor;
    }

    private SynchronousAccessor<PublicClientApplication> getPublicClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled() ? publicClientApplicationAccessorWithCae : publicClientApplicationAccessor;
    }

    /**
     * Acquire a token from the confidential client.
     *
     * @param request the details of the token request
     * @return An access token, or null if no token exists in the cache.
     */
    @SuppressWarnings("deprecation")
    public AccessToken authenticateWithConfidentialClientCache(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClientApplication = getConfidentialClientInstance(request).getValue();
        SilentParameters.SilentParametersBuilder parametersBuilder
            = SilentParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
            parametersBuilder.forceRefresh(true);
        }

        try {
            IAuthenticationResult authenticationResult
                = confidentialClientApplication.acquireTokenSilently(parametersBuilder.build()).get();
            AccessToken accessToken = new MsalToken(authenticationResult);
            if (OffsetDateTime.now().isBefore(accessToken.getExpiresAt().minus(REFRESH_OFFSET))) {
                return accessToken;
            } else {
                throw new IllegalStateException("Received token is close to expiry.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            // Cache misses should not throw an exception, but should log.
            if (e.getMessage().contains("Token not found in the cache")) {
                LOGGER.verbose("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(e.getMessage(), null, e));
            }
        }
    }

    /**
     * Acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @param account the account used to log in to acquire the last token
     * @return An access token, or null if no token exists in the cache.
     */
    @SuppressWarnings("deprecation")
    public MsalToken authenticateWithPublicClientCache(TokenRequestContext request, IAccount account) {
        PublicClientApplication pc = getPublicClientInstance(request).getValue();
        MsalToken token = acquireTokenFromPublicClientSilently(request, pc, account, false);
        if (OffsetDateTime.now().isAfter(token.getExpiresAt().minus(REFRESH_OFFSET))) {
            token = acquireTokenFromPublicClientSilently(request, pc, account, true);
        }
        return token;
    }

    private MsalToken acquireTokenFromPublicClientSilently(TokenRequestContext request, PublicClientApplication pc,
        IAccount account, boolean forceRefresh) {
        SilentParameters.SilentParametersBuilder parametersBuilder
            = SilentParameters.builder(new HashSet<>(request.getScopes()));

        if (forceRefresh) {
            parametersBuilder.forceRefresh(true);
        }

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
            parametersBuilder.forceRefresh(true);
        }

        if (account != null) {
            parametersBuilder = parametersBuilder.account(account);
        }
        parametersBuilder.tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
        try {
            return new MsalToken(pc.acquireTokenSilently(parametersBuilder.build()).get());
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            // Cache misses should not throw an exception, but should log.
            if (e.getMessage().contains("Token not found in the cache")) {
                LOGGER.verbose("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(e.getMessage(), null, e));
            }
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
    public MsalToken authenticateWithUsernamePassword(TokenRequestContext request, String username, String password) {
        PublicClientApplication pc = getPublicClientInstance(request).getValue();
        UserNamePasswordParameters.UserNamePasswordParametersBuilder userNamePasswordParametersBuilder
            = buildUsernamePasswordFlowParameters(request, username, password);
        try {
            return new MsalToken(pc.acquireToken(userNamePasswordParametersBuilder.build()).get());
        } catch (Exception e) {
            throw LOGGER
                .logExceptionAsError(new ClientAuthenticationException(
                    "Failed to acquire token with username and "
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
        PublicClientApplication pc = getPublicClientInstance(request).getValue();
        DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder
            = buildDeviceCodeFlowParameters(request, deviceCodeConsumer);

        try {
            return new MsalToken(pc.acquireToken(parametersBuilder.build()).get());
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(
                new ClientAuthenticationException("Failed to acquire token with device code.", null, e));
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
    public MsalToken authenticateWithBrowserInteraction(TokenRequestContext request, Integer port, String redirectUrl,
        String loginHint) {
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
        PublicClientApplication pc = getPublicClientInstance(request).getValue();

        // If the broker is enabled, try to get the token for the default account by passing
        // a null account to MSAL. If that fails, show the dialog.
        MsalToken token = null;
        if (options.isBrokerEnabled()
            && (options.useDefaultBrokerAccount() || options.getAuthenticationRecord() != null)) {
            try {
                token = acquireTokenFromPublicClientSilently(request, pc, null, false);
            } catch (Exception e) {
                // The error case here represents the silent acquisition failing. There's nothing actionable and
                // in this case the fallback path of showing the dialog will capture any meaningful error and share it.
            }
        }
        if (token == null) {
            InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
                = buildInteractiveRequestParameters(request, loginHint, redirectUri);

            try {
                return new MsalToken(pc.acquireToken(builder.build()).get());
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                    "Failed to acquire token with Interactive Browser Authentication.", null, e));
            }
        }
        return token;
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure CLI.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithAzureCli(TokenRequestContext request) {
        // Check for claims challenge - if claims are provided, this credential cannot handle them
        if (request.getClaims() != null && !request.getClaims().trim().isEmpty()) {
            String errorMessage = buildClaimsChallengeErrorMessage(request);
            throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                new CredentialUnavailableException(errorMessage));
        }
        StringBuilder azCommand = new StringBuilder("az account get-access-token --output json --resource ");

        String scopes = ScopeUtil.scopesToResource(request.getScopes());

        try {
            ScopeUtil.validateScope(scopes);
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }

        azCommand.append(scopes);

        String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);
        ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

        if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
            azCommand.append(" --tenant ").append(tenant);
        }

        String subscription = options.getSubscription();
        if (!CoreUtils.isNullOrEmpty(subscription)) {
            azCommand.append(" --subscription ").append(subscription);
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
     * Asynchronously acquire a token from Active Directory with Azure Developer CLI.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithAzureDeveloperCli(TokenRequestContext request) {

        StringBuilder azdCommand = new StringBuilder("azd auth token --output json --no-prompt --scope ");

        List<String> scopes = request.getScopes();

        // It's really unlikely that the request comes with no scope, but we want to
        // validate it as we are adding `--scope` arg to the azd command.
        if (scopes.size() == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Missing scope in request"));
        }

        scopes.forEach(scope -> {
            try {
                ScopeUtil.validateScope(scope);
            } catch (IllegalArgumentException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        });

        // At least one scope is appended to the azd command.
        // If there are more than one scope, we add `--scope` before each.
        azdCommand.append(String.join(" --scope ", scopes));

        String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);
        ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

        if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
            azdCommand.append(" --tenant-id ").append(tenant);
        }

        if (request.getClaims() != null && !request.getClaims().trim().isEmpty()) {
            String encodedClaims = IdentityUtil.ensureBase64Encoded(request.getClaims());
            azdCommand.append(" --claims ").append(shellEscape(encodedClaims));
        }

        try {
            return getTokenFromAzureDeveloperCLIAuthentication(azdCommand);
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
        ConfidentialClientApplication cc = getConfidentialClientInstance(request).getValue();
        try {
            return new MsalToken(cc.acquireToken(buildOBOFlowParameters(request)).get());
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                "Failed to acquire token with On Behalf Of Authentication.", null, e));
        }
    }

    public AccessToken authenticateWithExchangeTokenSync(TokenRequestContext request) {

        try {
            String assertionToken = clientAssertionAccessor.getValue();
            return authenticateWithExchangeTokenHelper(request, assertionToken);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> getWorkloadIdentityTokenProvider() {
        return appTokenProviderParameters -> {
            TokenRequestContext trc
                = new TokenRequestContext().setScopes(new ArrayList<>(appTokenProviderParameters.scopes))
                    .setClaims(appTokenProviderParameters.claims)
                    .setTenantId(appTokenProviderParameters.tenantId);

            AccessToken accessToken = authenticateWithExchangeTokenSync(trc);

            TokenProviderResult result = new TokenProviderResult();
            result.setAccessToken(accessToken.getToken());
            result.setTenantId(trc.getTenantId());
            result.setExpiresInSeconds(accessToken.getExpiresAt().toEpochSecond());

            return CompletableFuture.completedFuture(result);
        };
    }

    public AccessToken authenticateWithWorkloadIdentityConfidentialClient(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient
            = workloadIdentityConfidentialClientApplicationAccessor.getValue();

        try {
            ClientCredentialParameters.ClientCredentialParametersBuilder builder
                = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                    .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
            return new MsalToken(confidentialClient.acquireToken(builder.build()).get());
        } catch (Exception e) {
            throw new CredentialUnavailableException("Managed Identity authentication is not available.", e);
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
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.identity.implementation.util.PowerShellUtil;
import com.azure.identity.implementation.util.ScopeUtil;
import com.azure.identity.implementation.util.ValidationUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.microsoft.aad.msal4j.AppTokenProviderParameters;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.MsalJsonParsingException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.TokenProviderResult;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.sun.jna.Platform;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The identity client that contains APIs to retrieve access tokens
 * from various configurations.
 */
public class IdentityClient extends IdentityClientBase {
    private final SynchronizedAccessor<PublicClientApplication> publicClientApplicationAccessor;

    private final SynchronizedAccessor<PublicClientApplication> publicClientApplicationAccessorWithCae;
    private final SynchronizedAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;

    private final SynchronizedAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessorWithCae;
    private final SynchronizedAccessor<ConfidentialClientApplication> aksConfidentialClientApplicationAccessor;
    private final SynchronizedAccessor<ManagedIdentityApplication> managedIdentityMsalApplicationAccessor;
    private final SynchronizedAccessor<ConfidentialClientApplication> workloadIdentityConfidentialClientApplicationAccessor;
    private final SynchronizedAccessor<String> clientAssertionAccessor;

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
    IdentityClient(String tenantId, String clientId, String clientSecret, String certificatePath,
        String clientAssertionFilePath, String resourceId, String objectId, Supplier<String> clientAssertionSupplier,
        Function<HttpPipeline, String> clientAssertionSupplierWithHttpPipeline, byte[] certificate,
        String certificatePassword, boolean isSharedTokenCacheCredential, Duration clientAssertionTimeout,
        IdentityClientOptions options) {
        super(tenantId, clientId, clientSecret, certificatePath, clientAssertionFilePath, resourceId, objectId,
            clientAssertionSupplier, clientAssertionSupplierWithHttpPipeline, certificate, certificatePassword,
            isSharedTokenCacheCredential, clientAssertionTimeout, options);

        this.publicClientApplicationAccessor
            = new SynchronizedAccessor<>(() -> getPublicClientApplication(isSharedTokenCacheCredential, false));

        this.publicClientApplicationAccessorWithCae
            = new SynchronizedAccessor<>(() -> getPublicClientApplication(isSharedTokenCacheCredential, true));

        this.confidentialClientApplicationAccessor
            = new SynchronizedAccessor<>(() -> getConfidentialClientApplication(false));

        this.confidentialClientApplicationAccessorWithCae
            = new SynchronizedAccessor<>(() -> getConfidentialClientApplication(true));

        this.aksConfidentialClientApplicationAccessor
            = new SynchronizedAccessor<>(this::getAksConfidentialClientApplication);

        this.managedIdentityMsalApplicationAccessor = new SynchronizedAccessor<>(this::getManagedIdentityMsalClient);

        this.workloadIdentityConfidentialClientApplicationAccessor
            = new SynchronizedAccessor<>(this::getWorkloadIdentityConfidentialClientApplication);

        Duration cacheTimeout = (clientAssertionTimeout == null) ? Duration.ofMinutes(5) : clientAssertionTimeout;
        this.clientAssertionAccessor = new SynchronizedAccessor<>(this::parseClientAssertion, cacheTimeout);
    }

    public Mono<ManagedIdentityApplication> getManagedIdentityMsalClient() {
        return Mono.defer(() -> {
            try {
                return Mono.just(this.getManagedIdentityMsalApplication());
            } catch (RuntimeException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<ConfidentialClientApplication> getConfidentialClientApplication(boolean enableCae) {
        return Mono.defer(() -> {
            try {
                return Mono.just(this.getConfidentialClient(enableCae));
            } catch (RuntimeException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<ConfidentialClientApplication> getAksConfidentialClientApplication() {
        return Mono.defer(() -> {
            try {
                String authorityUrl
                    = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/" + tenantId;

                // Temporarily pass in Dummy Client secret and Client ID. until MSal removes its requirements.
                IClientCredential credential
                    = ClientCredentialFactory.createFromSecret(clientSecret != null ? clientSecret : "dummy-secret");
                ConfidentialClientApplication.Builder applicationBuilder = ConfidentialClientApplication
                    .builder(clientId == null ? "SYSTEM-ASSIGNED-MANAGED-IDENTITY" : clientId, credential);

                applicationBuilder.instanceDiscovery(false)
                    .validateAuthority(false)
                    .logPii(options.isUnsafeSupportLoggingEnabled());

                try {
                    applicationBuilder = applicationBuilder.authority(authorityUrl);
                } catch (MalformedURLException e) {
                    throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
                }

                applicationBuilder.appTokenProvider(appTokenProviderParameters -> {
                    TokenRequestContext trc
                        = new TokenRequestContext().setScopes(new ArrayList<>(appTokenProviderParameters.scopes))
                            .setClaims(appTokenProviderParameters.claims)
                            .setTenantId(appTokenProviderParameters.tenantId);

                    Mono<AccessToken> accessTokenAsync = authenticateWithExchangeToken(trc);

                    return accessTokenAsync.map(accessToken -> {
                        TokenProviderResult result = new TokenProviderResult();
                        result.setAccessToken(accessToken.getToken());
                        result.setTenantId(trc.getTenantId());
                        result.setExpiresInSeconds(accessToken.getExpiresAt().toEpochSecond());
                        if (accessToken.getRefreshAt() != null) {
                            result.setRefreshInSeconds(accessToken.getRefreshAt().toEpochSecond());
                        }
                        return result;
                    }).toFuture();
                });

                initializeHttpPipelineAdapter();
                if (httpPipelineAdapter != null) {
                    applicationBuilder.httpClient(httpPipelineAdapter);
                } else {
                    applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
                }

                if (options.getExecutorService() != null) {
                    applicationBuilder.executorService(options.getExecutorService());
                } else {
                    applicationBuilder.executorService(SharedExecutorService.getInstance());
                }

                return Mono.just(applicationBuilder.build());
            } catch (RuntimeException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<ConfidentialClientApplication> getWorkloadIdentityConfidentialClientApplication() {
        return Mono.defer(() -> {
            try {
                return Mono.just(super.getWorkloadIdentityConfidentialClient());
            } catch (RuntimeException e) {
                return Mono.error(e);
            }
        });
    }

    private Mono<String> parseClientAssertion() {
        return Mono.fromCallable(() -> {
            if (clientAssertionFilePath != null) {
                byte[] encoded = Files.readAllBytes(Paths.get(clientAssertionFilePath));
                return new String(encoded, StandardCharsets.UTF_8);
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException("Client Assertion File Path is not provided."
                    + " It should be provided to authenticate with client assertion."));
            }
        });
    }

    private Mono<PublicClientApplication> getPublicClientApplication(boolean sharedTokenCacheCredential,
        boolean enableCae) {
        return Mono.defer(() -> {
            try {
                return Mono.just(this.getPublicClient(sharedTokenCacheCredential, enableCae));
            } catch (RuntimeException e) {
                return Mono.error(e);
            }
        });
    }

    public Mono<MsalToken> authenticateWithIntelliJ(TokenRequestContext request) {
        try {
            IntelliJCacheAccessor cacheAccessor = new IntelliJCacheAccessor();
            // Look for cached credential in msal cache first.
            String cachedRefreshToken = cacheAccessor.getIntelliJCredentialsFromIdentityMsalCache();
            if (!CoreUtils.isNullOrEmpty(cachedRefreshToken)) {
                RefreshTokenParameters.RefreshTokenParametersBuilder refreshTokenParametersBuilder
                    = RefreshTokenParameters.builder(new HashSet<>(request.getScopes()), cachedRefreshToken);

                if (request.getClaims() != null) {
                    ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
                    refreshTokenParametersBuilder.claims(claimsRequest);
                }

                return publicClientApplicationAccessor.getValue()
                    .flatMap(pc -> Mono.fromFuture(pc.acquireToken(refreshTokenParametersBuilder.build()))
                        .map(MsalToken::new));
            }

            String exception
                = "IntelliJ authentication not available. Please login with the Azure Toolkit for IntelliJ."
                    + " You may also need to upgrade to a newer version of the Azure Toolkit for IntelliJ. This authentication"
                    + " is supported on version 3.53 and higher. Please see https://aka.ms/azsdk/java/identity/intellijcredential/troubleshoot for more information.";
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                new CredentialUnavailableException(exception)));

        } catch (RuntimeException e) {
            return Mono.error(e);
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure CLI.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithAzureCli(TokenRequestContext request) {
        // Check for claims challenge - if claims are provided, this credential cannot handle them
        if (request.getClaims() != null && !request.getClaims().trim().isEmpty()) {
            String errorMessage = buildClaimsChallengeErrorMessage(request);
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                new CredentialUnavailableException(errorMessage)));
        }
        StringBuilder azCommand = new StringBuilder("az account get-access-token --output json --resource ");

        String scopes = ScopeUtil.scopesToResource(request.getScopes());

        try {
            ScopeUtil.validateScope(scopes);
        } catch (IllegalArgumentException ex) {
            return Mono.error(LOGGER.logExceptionAsError(ex));
        }

        azCommand.append(scopes);

        try {
            String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);
            ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

            // The default is not correct for many cases, such as when the logged in entity is a service principal.
            if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
                azCommand.append(" --tenant ").append(tenant);
            }

            String subscription = options.getSubscription();
            if (!CoreUtils.isNullOrEmpty(subscription)) {
                azCommand.append(" --subscription \"").append(subscription).append("\"");
            }
        } catch (ClientAuthenticationException | IllegalArgumentException e) {
            return Mono.error(e);
        }

        try {
            AccessToken token = getTokenFromAzureCLIAuthentication(azCommand);
            return Mono.just(token);
        } catch (RuntimeException e) {
            return Mono.error(e instanceof CredentialUnavailableException
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
    public Mono<AccessToken> authenticateWithAzureDeveloperCli(TokenRequestContext request) {
        StringBuilder azdCommand = new StringBuilder("azd auth token --output json --no-prompt --scope ");
        List<String> scopes = request.getScopes();

        // It's really unlikely that the request comes with no scope, but we want to
        // validate it as we are adding `--scope` arg to the azd command.
        if (scopes.size() == 0) {
            return Mono.error(LOGGER.logExceptionAsError(new IllegalArgumentException("Missing scope in request")));
        }

        for (String scope : scopes) {
            try {
                ScopeUtil.validateScope(scope);
            } catch (IllegalArgumentException ex) {
                return Mono.error(LOGGER.logExceptionAsError(ex));
            }
        }

        // At least one scope is appended to the azd command.
        // If there are more than one scope, we add `--scope` before each.
        azdCommand.append(String.join(" --scope ", scopes));

        try {
            String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);
            ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

            if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
                azdCommand.append(" --tenant-id ").append(tenant);
            }

            if (request.getClaims() != null && !request.getClaims().trim().isEmpty()) {
                String encodedClaims = IdentityUtil.ensureBase64Encoded(request.getClaims());
                azdCommand.append(" --claims ").append(shellEscape(encodedClaims));
            }
        } catch (ClientAuthenticationException | IllegalArgumentException e) {
            return Mono.error(e);
        }

        try {
            AccessToken token = getTokenFromAzureDeveloperCLIAuthentication(azdCommand);
            return Mono.just(token);
        } catch (RuntimeException e) {
            return Mono.error(e instanceof CredentialUnavailableException
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
    public Mono<AccessToken> authenticateWithAzurePowerShell(TokenRequestContext request) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        List<CredentialUnavailableException> exceptions = new ArrayList<>(2);

        PowershellManager defaultPowerShellManager = new PowershellManager(false);

        PowershellManager legacyPowerShellManager = Platform.isWindows() ? new PowershellManager(true) : null;

        List<PowershellManager> powershellManagers = new ArrayList<>(2);
        powershellManagers.add(defaultPowerShellManager);
        if (legacyPowerShellManager != null) {
            powershellManagers.add(legacyPowerShellManager);
        }
        return Flux.fromIterable(powershellManagers)
            .flatMap(powershellManager -> getAccessTokenFromPowerShell(request, powershellManager).onErrorResume(t -> {
                if (!t.getClass().getSimpleName().equals("CredentialUnavailableException")) {
                    return Mono.error(new ClientAuthenticationException(
                        "Azure Powershell authentication failed. Error Details: " + t.getMessage()
                            + ". To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azsdk/java/identity/powershellcredential/troubleshoot",
                        null, t));
                }
                exceptions.add((CredentialUnavailableException) t);
                return Mono.empty();
            }), 1)
            .next()
            .switchIfEmpty(Mono.defer(() -> {
                // Chain Exceptions.
                CredentialUnavailableException last = exceptions.get(exceptions.size() - 1);
                for (int z = exceptions.size() - 2; z >= 0; z--) {
                    CredentialUnavailableException current = exceptions.get(z);
                    last = new CredentialUnavailableException("Azure PowerShell authentication failed using default"
                        + "powershell(pwsh) with following error: " + current.getMessage() + "\r\n"
                        + "Azure PowerShell authentication failed using powershell-core(powershell)"
                        + " with following error: " + last.getMessage(), last.getCause());
                }
                return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options, (last)));
            }));
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure PowerShell.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithOBO(TokenRequestContext request) {
        return getConfidentialClientInstance(request).getValue()
            .flatMap(confidentialClient -> Mono
                .fromFuture(() -> confidentialClient.acquireToken(buildOBOFlowParameters(request)))
                .map(MsalToken::new));
    }

    private Mono<AccessToken> getAccessTokenFromPowerShell(TokenRequestContext request,
        PowershellManager powershellManager) {
        // Check for claims challenge - if claims are provided, this credential cannot handle them
        if (request.getClaims() != null && !request.getClaims().trim().isEmpty()) {
            String errorMessage = buildPowerShellClaimsChallengeErrorMessage(request);
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                new CredentialUnavailableException(errorMessage)));
        }

        String scope = ScopeUtil.scopesToResource(request.getScopes());
        try {
            ScopeUtil.validateScope(scope);
        } catch (IllegalArgumentException ex) {
            throw LOGGER.logExceptionAsError(ex);
        }

        String resolvedTenant = IdentityUtil.resolveTenantId(tenantId, request, options);
        String tenant = resolvedTenant.equals(IdentityUtil.DEFAULT_TENANT) ? "" : resolvedTenant;
        ValidationUtil.validateTenantIdCharacterRange(tenant, LOGGER);

        return Mono.defer(() -> {
            String sep = System.lineSeparator();

            String command = PowerShellUtil.getPwshCommand(tenant, scope, sep);

            return powershellManager.runCommand(command).flatMap(output -> {
                if (output.contains("VersionTooOld")) {
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException("Az.Account module with version >= 2.2.0 is not installed. "
                            + "It needs to be installed to use Azure PowerShell " + "Credential.")));
                }

                if (output.contains("Run Connect-AzAccount to login")) {
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException(
                            "Run Connect-AzAccount to login to Azure account in PowerShell.")));
                }

                try (JsonReader reader = JsonProviders.createReader(output)) {
                    reader.nextToken();
                    Map<String, String> objectMap = reader.readMap(JsonReader::getString);
                    String accessToken = objectMap.get("Token");
                    String time = objectMap.get("ExpiresOn");
                    OffsetDateTime expiresOn = PowerShellUtil.parseExpiresOn(time);
                    if (expiresOn == null) {
                        return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException(
                                "Encountered error when deserializing ExpiresOn time from PowerShell response.")));
                    }
                    return Mono.just(new AccessToken(accessToken, expiresOn));
                } catch (IOException e) {
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException(
                            "Encountered error when deserializing response from Azure Power Shell.", e)));
                }
            });
        });
    }

    private String buildPowerShellClaimsChallengeErrorMessage(TokenRequestContext request) {
        StringBuilder connectAzCommand = new StringBuilder("Connect-AzAccount -ClaimsChallenge '");

        // Use IdentityUtil.ensureBase64Encoded for the claims
        String encodedClaims = IdentityUtil.ensureBase64Encoded(request.getClaims());
        connectAzCommand.append(encodedClaims.replace("'", "''"))  // Escape single quotes for PowerShell
            .append("'");

        // Add tenant if available
        String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);
        if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
            connectAzCommand.append(" -Tenant ").append(tenant);
        }

        return String.format(
            "Failed to get token. Claims challenges are not supported by AzurePowerShellCredential. Run %s to handle the claims challenge.",
            connectAzCommand.toString());
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithConfidentialClient(TokenRequestContext request) {
        return getConfidentialClientInstance(request).getValue().flatMap(confidentialClient -> Mono.fromFuture(() -> {
            ClientCredentialParameters.ClientCredentialParametersBuilder builder
                = buildConfidentialClientParameters(request);
            return confidentialClient.acquireToken(builder.build());
        })).map(MsalToken::new);
    }

    private SynchronizedAccessor<ConfidentialClientApplication>
        getConfidentialClientInstance(TokenRequestContext requestContext) {
        return requestContext.isCaeEnabled()
            ? confidentialClientApplicationAccessorWithCae
            : confidentialClientApplicationAccessor;
    }

    private ClientCredentialParameters.ClientCredentialParametersBuilder
        buildConfidentialClientParameters(TokenRequestContext request) {
        ClientCredentialParameters.ClientCredentialParametersBuilder builder
            = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
        if (clientAssertionSupplier != null) {
            builder.clientCredential(ClientCredentialFactory.createFromClientAssertion(clientAssertionSupplier.get()));
        } else if (clientAssertionSupplierWithHttpPipeline != null) {
            builder.clientCredential(ClientCredentialFactory
                .createFromClientAssertion(clientAssertionSupplierWithHttpPipeline.apply(getPipeline())));
        }

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            builder.claims(claimsRequest);
        }
        return builder;
    }

    public Mono<AccessToken> authenticateWithAksConfidentialClient(TokenRequestContext request) {
        return aksConfidentialClientApplicationAccessor.getValue().flatMap(confidentialClient -> Mono.fromFuture(() -> {
            ClientCredentialParameters.ClientCredentialParametersBuilder builder
                = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                    .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
            return confidentialClient.acquireToken(builder.build());
        }))
            .onErrorMap(t -> new CredentialUnavailableException("Managed Identity authentication is not available.", t))
            .map(MsalToken::new);
    }

    public Mono<AccessToken> authenticateWithManagedIdentityMsalClient(TokenRequestContext request) {
        String resource = ScopeUtil.scopesToResource(request.getScopes());

        return Mono.fromSupplier(() -> options.isChained()
            && ManagedIdentitySourceType.DEFAULT_TO_IMDS.equals(ManagedIdentityApplication.getManagedIdentitySource()))
            .flatMap(shouldProbe -> shouldProbe ? checkIMDSAvailable(getImdsEndpoint()) : Mono.just(true))
            .flatMap(ignored -> getTokenFromMsalMIClient(resource));
    }

    private Mono<AccessToken> getTokenFromMsalMIClient(String resource) {
        return managedIdentityMsalApplicationAccessor.getValue()
            .flatMap(managedIdentityApplication -> Mono.fromFuture(() -> {
                com.microsoft.aad.msal4j.ManagedIdentityParameters.ManagedIdentityParametersBuilder builder
                    = com.microsoft.aad.msal4j.ManagedIdentityParameters.builder(resource);
                try {
                    return managedIdentityApplication.acquireTokenForManagedIdentity(builder.build());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }))
            .onErrorMap(t -> {
                if (options.isChained() && t instanceof MsalJsonParsingException) {
                    return new CredentialUnavailableException("Managed Identity authentication is not available.", t);
                }
                return new ClientAuthenticationException(
                    "Managed Identity authentication failed, see inner exception" + " for more information.", null, t);
            })
            .map(MsalToken::new);
    }

    public Mono<AccessToken> authenticateWithWorkloadIdentityConfidentialClient(TokenRequestContext request) {
        return workloadIdentityConfidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> {
                ClientCredentialParameters.ClientCredentialParametersBuilder builder
                    = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                        .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
                return confidentialClient.acquireToken(builder.build());
            }))
            .onErrorMap(
                t -> new CredentialUnavailableException("Workload Identity authentication is not available.", t))
            .map(MsalToken::new);
    }

    /**
     * Asynchronously acquire a token from Active Directory with a username and a password.
     *
     * @param request the details of the token request
     * @param username the username of the user
     * @param password the password of the user
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithUsernamePassword(TokenRequestContext request, String username,
        String password) {
        return getPublicClientInstance(request).getValue().flatMap(pc -> Mono.fromFuture(() -> {
            UserNamePasswordParameters.UserNamePasswordParametersBuilder userNamePasswordParametersBuilder
                = buildUsernamePasswordFlowParameters(request, username, password);
            return pc.acquireToken(userNamePasswordParametersBuilder.build());
        }))
            .onErrorMap(
                t -> new ClientAuthenticationException(
                    "Failed to acquire token with username and "
                        + "password. To mitigate this issue, please refer to the troubleshooting guidelines "
                        + "here at https://aka.ms/azsdk/java/identity/usernamepasswordcredential/troubleshoot",
                    null, t))
            .map(MsalToken::new);
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @param account the account used to log in to acquire the last token
     * @return a Publisher that emits an AccessToken
     */
    @SuppressWarnings("deprecation")
    public Mono<MsalToken> authenticateWithPublicClientCache(TokenRequestContext request, IAccount account) {
        return getPublicClientInstance(request).getValue()
            .flatMap(pc -> Mono.fromFuture(() -> acquireTokenFromPublicClientSilently(request, pc, account, false))
                .map(MsalToken::new)
                .filter(t -> OffsetDateTime.now().isBefore(t.getExpiresAt().minus(REFRESH_OFFSET)))
                .switchIfEmpty(Mono.fromFuture(() -> acquireTokenFromPublicClientSilently(request, pc, account, true))
                    .map(MsalToken::new)));
    }

    private CompletableFuture<IAuthenticationResult> acquireTokenFromPublicClientSilently(TokenRequestContext request,
        PublicClientApplication pc, IAccount account, boolean forceRefresh) {
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
            return pc.acquireTokenSilently(parametersBuilder.build());
        } catch (MalformedURLException e) {
            return getFailedCompletableFuture(LOGGER.logExceptionAsError(new RuntimeException(e)));
        }
    }

    private SynchronizedAccessor<PublicClientApplication> getPublicClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled() ? publicClientApplicationAccessorWithCae : publicClientApplicationAccessor;
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    @SuppressWarnings("deprecation")
    public Mono<AccessToken> authenticateWithConfidentialClientCache(TokenRequestContext request) {
        return authenticateWithConfidentialClientCache(request, null);
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @param account the account used to log in to acquire the last token
     *
     * @return a Publisher that emits an AccessToken
     */
    @SuppressWarnings("deprecation")
    public Mono<AccessToken> authenticateWithConfidentialClientCache(TokenRequestContext request, IAccount account) {
        return getConfidentialClientInstance(request).getValue().flatMap(confidentialClient -> Mono.fromFuture(() -> {
            SilentParameters.SilentParametersBuilder parametersBuilder
                = SilentParameters.builder(new HashSet<>(request.getScopes()))
                    .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
            if (account != null) {
                parametersBuilder.account(account);
            }
            try {
                return confidentialClient.acquireTokenSilently(parametersBuilder.build());
            } catch (MalformedURLException e) {
                return getFailedCompletableFuture(LOGGER.logExceptionAsError(new RuntimeException(e)));
            }
        })
            .map(ar -> new MsalToken(ar))
            .filter(t -> OffsetDateTime.now().isBefore(t.getExpiresAt().minus(REFRESH_OFFSET))));
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
    public Mono<MsalToken> authenticateWithDeviceCode(TokenRequestContext request,
        Consumer<DeviceCodeInfo> deviceCodeConsumer) {
        return getPublicClientInstance(request).getValue().flatMap(pc -> Mono.fromFuture(() -> {
            DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder
                = buildDeviceCodeFlowParameters(request, deviceCodeConsumer);
            return pc.acquireToken(parametersBuilder.build());
        })
            .onErrorMap(t -> new ClientAuthenticationException("Failed to acquire token with device code.", null, t))
            .map(MsalToken::new));
    }

    /**
     * Asynchronously acquire a token from Active Directory with Visual Studio cached refresh token.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken.
     */
    public Mono<MsalToken> authenticateWithVsCodeCredential(TokenRequestContext request, String cloud) {

        if (isADFSTenant()) {
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                new CredentialUnavailableException(
                    "VsCodeCredential  " + "authentication unavailable. ADFS tenant/authorities are not supported. "
                        + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                        + "https://aka.ms/azsdk/java/identity/vscodecredential/troubleshoot")));
        }
        VisualStudioCacheAccessor accessor = new VisualStudioCacheAccessor();

        String credential = null;
        try {
            credential = accessor.getCredentials("VS Code Azure", cloud);
        } catch (CredentialUnavailableException e) {
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options, e));
        }

        RefreshTokenParameters.RefreshTokenParametersBuilder parametersBuilder
            = RefreshTokenParameters.builder(new HashSet<>(request.getScopes()), credential);

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
        }

        return getPublicClientInstance(request).getValue()
            .flatMap(pc -> Mono.fromFuture(pc.acquireToken(parametersBuilder.build())).onErrorResume(t -> {
                if (t instanceof MsalInteractionRequiredException) {
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException("Failed to acquire token with" + " VS code credential."
                            + " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azsdk/java/identity/vscodecredential/troubleshoot", t)));
                }
                return Mono.error(
                    new ClientAuthenticationException("Failed to acquire token with" + " VS code credential", null, t));
            }).map(MsalToken::new));
    }

    /**
     * Asynchronously acquire a token from Active Directory with an authorization code from an oauth flow.
     *
     * @param request the details of the token request
     * @param authorizationCode the oauth2 authorization code
     * @param redirectUrl the redirectUrl where the authorization code is sent to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithAuthorizationCode(TokenRequestContext request, String authorizationCode,
        URI redirectUrl) {
        AuthorizationCodeParameters.AuthorizationCodeParametersBuilder parametersBuilder
            = AuthorizationCodeParameters.builder(authorizationCode, redirectUrl)
                .scopes(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
        }

        Mono<IAuthenticationResult> acquireToken;
        if (clientSecret != null) {
            acquireToken = getConfidentialClientInstance(request).getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parametersBuilder.build())));
        } else {
            SynchronizedAccessor<PublicClientApplication> publicClient = getPublicClientInstance(request);
            acquireToken = publicClient.getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parametersBuilder.build())));
        }
        return acquireToken
            .onErrorMap(
                t -> new ClientAuthenticationException("Failed to acquire token with authorization code", null, t))
            .map(MsalToken::new);
    }

    /**
     * Asynchronously acquire a token from Active Directory by opening a browser and wait for the user to login. The
     * credential will run a minimal local HttpServer at the given port, so {@code http://localhost:{port}} must be
     * listed as a valid reply URL for the application.
     *
     * @param request the details of the token request
     * @param port the port on which the HTTP server is listening
     * @param redirectUrl the redirect URL to listen on and receive security code
     * @param loginHint the username suggestion to pre-fill the login page's username/email address field
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithBrowserInteraction(TokenRequestContext request, Integer port,
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
            return Mono.error(LOGGER.logExceptionAsError(new RuntimeException(e)));
        }

        // If the broker is enabled, try to get the token for the default account by passing
        // a null account to MSAL. If that fails, show the dialog.

        return getPublicClientInstance(request).getValue().flatMap(pc -> {
            if (options.isBrokerEnabled()
                && (options.useDefaultBrokerAccount() || options.getAuthenticationRecord() != null)) {
                return Mono.fromFuture(() -> acquireTokenFromPublicClientSilently(request, pc, null, false))
                    // The error case here represents the silent acquisition failing. There's nothing actionable and
                    // in this case the fallback path of showing the dialog will capture any meaningful error and share it.
                    .onErrorResume(e -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> {
            InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
                = buildInteractiveRequestParameters(request, loginHint, redirectUri);

            SynchronizedAccessor<PublicClientApplication> publicClient = getPublicClientInstance(request);

            return publicClient.getValue().flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(builder.build())));

        }))
            // If we're already throwing a ClientAuthenticationException we don't need to wrap it again.
            .onErrorMap(t -> !(t instanceof ClientAuthenticationException), t -> {
                throw new ClientAuthenticationException(
                    "Failed to acquire token with Interactive Browser Authentication.", null, t);
            })
            .map(iAuthenticationResult -> {
                if (options.isBrokerEnabled() && request.getProofOfPossessionOptions() != null) {
                    return new MsalToken(iAuthenticationResult, "PoP");
                } else {
                    return new MsalToken(iAuthenticationResult);
                }
            });
    }

    /**
     * Gets token from shared token cache
     * */
    public Mono<MsalToken> authenticateWithSharedTokenCache(TokenRequestContext request, String username) {
        // find if the Public Client app with the requested username exists
        SynchronizedAccessor<PublicClientApplication> publicClient = getPublicClientInstance(request);
        return publicClient.getValue()
            .flatMap(pc -> Mono.fromFuture(pc::getAccounts))
            .onErrorMap(t -> new CredentialUnavailableException(
                "Cannot get accounts from token cache. Error: " + t.getMessage(), t))
            .flatMap(set -> {
                IAccount requestedAccount;
                Map<String, IAccount> accounts = new HashMap<>(); // home account id -> account

                if (set.isEmpty()) {
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException("SharedTokenCacheCredential "
                            + "authentication unavailable. No accounts were found in the cache.")));
                }

                for (IAccount cached : set) {
                    if (username == null || username.equals(cached.username())) {
                        accounts.putIfAbsent(cached.homeAccountId(), cached); // only put the first one
                    }
                }

                if (accounts.isEmpty()) {
                    // no more accounts after filtering, username must be set
                    return Mono.error(new RuntimeException(String.format("SharedTokenCacheCredential "
                        + "authentication unavailable. No account matching the specified username: %s was "
                        + "found in the cache.", username)));
                } else if (accounts.size() > 1) {
                    if (username == null) {
                        return Mono.error(new RuntimeException("SharedTokenCacheCredential authentication unavailable. "
                            + "Multiple accounts were found in the cache. Use username and tenant id to disambiguate."));
                    } else {
                        return Mono.error(new RuntimeException(String.format("SharedTokenCacheCredential "
                            + "authentication unavailable. Multiple accounts matching the specified username: "
                            + "%s were found in the cache.", username)));
                    }
                } else {
                    requestedAccount = accounts.values().iterator().next();
                }
                return authenticateWithPublicClientCache(request, requestedAccount);
            });
    }

    /**
     * Asynchronously acquire a token from the Azure Arc Managed Service Identity endpoint.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithExchangeToken(TokenRequestContext request) {

        return clientAssertionAccessor.getValue()
            .flatMap(assertionToken -> Mono
                .fromCallable(() -> authenticateWithExchangeTokenHelper(request, assertionToken)));
    }

    private String getImdsEndpoint() {
        return TRAILING_FORWARD_SLASHES.matcher(options.getImdsAuthorityHost()).replaceAll("")
            + IdentityConstants.DEFAULT_IMDS_TOKENPATH;
    }

    int getRetryTimeoutInMs(int retry) {
        return (int) options.getRetryTimeout().apply(Duration.ofSeconds(retry)).toMillis();
    }

    private Mono<Boolean> checkIMDSAvailable(String endpoint) {
        return Mono.fromCallable(() -> {
            HttpURLConnection connection = null;
            URL url = getUrl(endpoint + "?api-version=2018-02-01");

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(1000);
                connection.connect();
            } catch (Exception e) {
                throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                    new CredentialUnavailableException("ManagedIdentityCredential authentication unavailable. "
                        + "Connection to IMDS endpoint cannot be established, " + e.getMessage() + ".", e));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return true;
        });
    }

    private static Proxy proxyOptionsToJavaNetProxy(ProxyOptions options) {
        switch (options.getType()) {
            case SOCKS4:
            case SOCKS5:
                return new Proxy(Type.SOCKS, options.getAddress());

            case HTTP:
            default:
                return new Proxy(Type.HTTP, options.getAddress());
        }
    }

    private CompletableFuture<IAuthenticationResult> getFailedCompletableFuture(Exception e) {
        CompletableFuture<IAuthenticationResult> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(e);
        return completableFuture;
    }

    /**
     * Get the configured identity client options.
     *
     * @return the client options.
     */
    public IdentityClientOptions getIdentityClientOptions() {
        return options;
    }

    private boolean isADFSTenant() {
        return ADFS_TENANT.equals(this.tenantId);
    }

    Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>> getWorkloadIdentityTokenProvider() {
        return appTokenProviderParameters -> {
            TokenRequestContext trc
                = new TokenRequestContext().setScopes(new ArrayList<>(appTokenProviderParameters.scopes))
                    .setClaims(appTokenProviderParameters.claims)
                    .setTenantId(appTokenProviderParameters.tenantId);

            Mono<AccessToken> accessTokenAsync = authenticateWithExchangeToken(trc);

            return accessTokenAsync.map(accessToken -> {
                TokenProviderResult result = new TokenProviderResult();
                result.setAccessToken(accessToken.getToken());
                result.setTenantId(trc.getTenantId());
                result.setExpiresInSeconds(accessToken.getExpiresAt().toEpochSecond());
                return result;
            }).toFuture();
        };
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.implementation.util.CertificateUtil;
import com.azure.identity.implementation.util.ScopeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import com.microsoft.aad.msal4jextensions.persistence.linux.KeyRingAccessException;
import com.sun.jna.Platform;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The identity client that contains APIs to retrieve access tokens
 * from various configurations.
 */
public class IdentityClient {
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final Random RANDOM = new Random();
    private static final String WINDOWS_STARTER = "cmd.exe";
    private static final String LINUX_MAC_STARTER = "/bin/sh";
    private static final String WINDOWS_SWITCHER = "/c";
    private static final String LINUX_MAC_SWITCHER = "-c";
    private static final String WINDOWS_PROCESS_ERROR_MESSAGE = "'az' is not recognized";
    private static final String LINUX_MAC_PROCESS_ERROR_MESSAGE = "(.*)az:(.*)not found";
    private static final String DEFAULT_WINDOWS_SYSTEM_ROOT = System.getenv("SystemRoot");
    private static final String DEFAULT_MAC_LINUX_PATH = "/bin/";
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    private static final String DEFAULT_PUBLIC_CACHE_FILE_NAME = "msal.cache";
    private static final String DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME = "msal.confidential.cache";
    private static final Path DEFAULT_CACHE_FILE_PATH = Platform.isWindows()
        ? Paths.get(System.getProperty("user.home"), "AppData", "Local", ".IdentityService")
        : Paths.get(System.getProperty("user.home"), ".IdentityService");
    private static final String DEFAULT_KEYCHAIN_SERVICE = "Microsoft.Developer.IdentityService";
    private static final String DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT = "MSALCache";
    private static final String DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT = "MSALConfidentialCache";
    private static final String DEFAULT_KEYRING_NAME = "default";
    private static final String DEFAULT_KEYRING_SCHEMA = "msal.cache";
    private static final String DEFAULT_PUBLIC_KEYRING_ITEM_NAME = DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME = DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT;
    private static final String DEFAULT_KEYRING_ATTR_NAME = "MsalClientID";
    private static final String DEFAULT_KEYRING_ATTR_VALUE = "Microsoft.Developer.IdentityService";
    private static final String HTTP_LOCALHOST = "http://localhost";
    private final ClientLogger logger = new ClientLogger(IdentityClient.class);

    private final IdentityClientOptions options;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String certificatePath;
    private final String certificatePassword;
    private HttpPipelineAdapter httpPipelineAdapter;
    private final SynchronizedAccessor<PublicClientApplication> publicClientApplicationAccessor;
    private final SynchronizedAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param tenantId the tenant ID of the application.
     * @param clientId the client ID of the application.
     * @param clientSecret the client secret of the application.
     * @param certificatePath the path to the PKCS12 or PEM certificate of the application.
     * @param certificatePassword the password protecting the PFX certificate.
     * @param isSharedTokenCacheCredential Indicate whether the credential is
     * {@link com.azure.identity.SharedTokenCacheCredential} or not.
     * @param options the options configuring the client.
     */
    IdentityClient(String tenantId, String clientId, String clientSecret,
                   String certificatePath, String certificatePassword, boolean isSharedTokenCacheCredential,
                   IdentityClientOptions options) {
        if (tenantId == null) {
            tenantId = "organizations";
        }
        if (options == null) {
            options = new IdentityClientOptions();
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword;
        this.options = options;

        this.publicClientApplicationAccessor = new SynchronizedAccessor<PublicClientApplication>(() ->
            getPublicClientApplication(isSharedTokenCacheCredential));

        this.confidentialClientApplicationAccessor = new SynchronizedAccessor<ConfidentialClientApplication>(() ->
            getConfidentialClientApplication());
    }

    private ConfidentialClientApplication getConfidentialClientApplication() {
        if (clientId == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        IClientCredential credential;
        if (clientSecret != null) {
            credential = ClientCredentialFactory.createFromSecret(clientSecret);
        } else if (certificatePath != null) {
            try {
                if (certificatePassword == null) {
                    byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(certificatePath));

                    credential = ClientCredentialFactory.createFromCertificate(
                        CertificateUtil.privateKeyFromPem(pemCertificateBytes),
                        CertificateUtil.publicKeyFromPem(pemCertificateBytes));
                } else {
                    credential = ClientCredentialFactory.createFromCertificate(
                        new FileInputStream(certificatePath), certificatePassword);
                }
            } catch (IOException | GeneralSecurityException e) {
                throw logger.logExceptionAsError(new RuntimeException(
                    "Failed to parse the certificate for the credential: " + e.getMessage(), e));
            }
        } else {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Must provide client secret or client certificate path"));
        }
        ConfidentialClientApplication.Builder applicationBuilder =
            ConfidentialClientApplication.builder(clientId, credential);
        try {
            applicationBuilder = applicationBuilder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(new IllegalStateException(e));
        }

        initializeHttpPipelineAdapter();
        if (httpPipelineAdapter != null) {
            applicationBuilder.httpClient(httpPipelineAdapter);
        } else {
            applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
        }

        if (options.getExecutorService() != null) {
            applicationBuilder.executorService(options.getExecutorService());
        }
        if (options.isSharedTokenCacheEnabled()) {
            try {
                PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings.builder(
                    DEFAULT_CONFIDENTIAL_CACHE_FILE_NAME, DEFAULT_CACHE_FILE_PATH);
                if (Platform.isMac()) {
                    persistenceSettingsBuilder.setMacKeychain(
                        DEFAULT_KEYCHAIN_SERVICE, DEFAULT_CONFIDENTIAL_KEYCHAIN_ACCOUNT);
                }
                if (Platform.isLinux()) {
                    try {
                        persistenceSettingsBuilder
                            .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                                DEFAULT_CONFIDENTIAL_KEYRING_ITEM_NAME, DEFAULT_KEYRING_ATTR_NAME,
                                DEFAULT_KEYRING_ATTR_VALUE, null, null);
                        applicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    } catch (KeyRingAccessException e) {
                        if (!options.getAllowUnencryptedCache()) {
                            throw logger.logExceptionAsError(e);
                        }
                        persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                        applicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    }
                }
            } catch (Throwable t) {
                throw logger.logExceptionAsError(new ClientAuthenticationException(
                    "Shared token cache is unavailable in this environment.", null, t));
            }
        }
        return applicationBuilder.build();
    }

    private PublicClientApplication getPublicClientApplication(boolean sharedTokenCacheCredential) {
        if (clientId == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        PublicClientApplication.Builder publicClientApplicationBuilder = PublicClientApplication.builder(clientId);
        try {
            publicClientApplicationBuilder = publicClientApplicationBuilder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsWarning(new IllegalStateException(e));
        }

        initializeHttpPipelineAdapter();
        if (httpPipelineAdapter != null) {
            publicClientApplicationBuilder.httpClient(httpPipelineAdapter);
        } else {
            publicClientApplicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
        }

        if (options.getExecutorService() != null) {
            publicClientApplicationBuilder.executorService(options.getExecutorService());
        }
        if (options.isSharedTokenCacheEnabled()) {
            try {
                PersistenceSettings.Builder persistenceSettingsBuilder = PersistenceSettings.builder(
                        DEFAULT_PUBLIC_CACHE_FILE_NAME, DEFAULT_CACHE_FILE_PATH);
                if (Platform.isWindows()) {
                    publicClientApplicationBuilder.setTokenCacheAccessAspect(
                        new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                } else if (Platform.isMac()) {
                    persistenceSettingsBuilder.setMacKeychain(
                        DEFAULT_KEYCHAIN_SERVICE, DEFAULT_PUBLIC_KEYCHAIN_ACCOUNT);
                    publicClientApplicationBuilder.setTokenCacheAccessAspect(
                        new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                } else if (Platform.isLinux()) {
                    try {
                        persistenceSettingsBuilder
                            .setLinuxKeyring(DEFAULT_KEYRING_NAME, DEFAULT_KEYRING_SCHEMA,
                                DEFAULT_PUBLIC_KEYRING_ITEM_NAME, DEFAULT_KEYRING_ATTR_NAME, DEFAULT_KEYRING_ATTR_VALUE,
                                null, null);
                        publicClientApplicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    } catch (KeyRingAccessException e) {
                        if (!options.getAllowUnencryptedCache()) {
                            throw logger.logExceptionAsError(e);
                        }
                        persistenceSettingsBuilder.setLinuxUseUnprotectedFileAsCacheStorage(true);
                        publicClientApplicationBuilder.setTokenCacheAccessAspect(
                            new PersistenceTokenCacheAccessAspect(persistenceSettingsBuilder.build()));
                    }
                }
            } catch (Throwable t) {
                String message = "Shared token cache is unavailable in this environment.";
                if (sharedTokenCacheCredential) {
                    throw logger.logExceptionAsError(new CredentialUnavailableException(message, t));
                } else {
                    throw logger.logExceptionAsError(new ClientAuthenticationException(message, null, t));
                }
            }
        }
        return publicClientApplicationBuilder.build();
    }

    public Mono<MsalToken> authenticateWithIntelliJ(TokenRequestContext request) {
        try {
            IntelliJCacheAccessor cacheAccessor = new IntelliJCacheAccessor(options.getIntelliJKeePassDatabasePath());
            IntelliJAuthMethodDetails authDetails = cacheAccessor.getAuthDetailsIfAvailable();
            String authType = authDetails.getAuthMethod();
            if (authType.equalsIgnoreCase("SP")) {
                Map<String, String> spDetails = cacheAccessor
                    .getIntellijServicePrincipalDetails(authDetails.getCredFilePath());
                String authorityUrl = spDetails.get("authURL") + spDetails.get("tenant");
                try {
                    ConfidentialClientApplication.Builder applicationBuilder =
                        ConfidentialClientApplication.builder(spDetails.get("client"),
                            ClientCredentialFactory.createFromSecret(spDetails.get("key")))
                            .authority(authorityUrl);

                    // If http pipeline is available, then it should override the proxy options if any configured.
                    if (httpPipelineAdapter != null) {
                        applicationBuilder.httpClient(httpPipelineAdapter);
                    } else if (options.getProxyOptions() != null) {
                        applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
                    }

                    if (options.getExecutorService() != null) {
                        applicationBuilder.executorService(options.getExecutorService());
                    }

                    ConfidentialClientApplication application = applicationBuilder.build();
                    return Mono.fromFuture(application.acquireToken(
                        ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                            .build())).map(MsalToken::new);
                } catch (MalformedURLException e) {
                    return Mono.error(e);
                }
            } else if (authType.equalsIgnoreCase("DC")) {

                JsonNode intelliJCredentials = cacheAccessor.getDeviceCodeCredentials();
                String refreshToken = intelliJCredentials.get("refreshToken").textValue();

                RefreshTokenParameters parameters = RefreshTokenParameters
                                                        .builder(new HashSet<>(request.getScopes()), refreshToken)
                                                            .build();

                return publicClientApplicationAccessor.getValue()
                   .flatMap(pc -> Mono.fromFuture(pc.acquireToken(parameters)).map(MsalToken::new));

            } else {
                throw logger.logExceptionAsError(new CredentialUnavailableException(
                    "IntelliJ Authentication not available."
                    + " Please login with Azure Tools for IntelliJ plugin in the IDE."));
            }
        } catch (IOException e) {
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
        String azCommand = "az account get-access-token --output json --resource ";

        StringBuilder command = new StringBuilder();
        command.append(azCommand);

        String scopes = ScopeUtil.scopesToResource(request.getScopes());

        try {
            ScopeUtil.validateScope(scopes);
        } catch (IllegalArgumentException ex) {
            return Mono.error(logger.logExceptionAsError(ex));
        }

        command.append(scopes);

        AccessToken token = null;
        BufferedReader reader = null;
        try {
            String starter;
            String switcher;
            if (isWindowsPlatform()) {
                starter = WINDOWS_STARTER;
                switcher = WINDOWS_SWITCHER;
            } else {
                starter = LINUX_MAC_STARTER;
                switcher = LINUX_MAC_SWITCHER;
            }

            ProcessBuilder builder = new ProcessBuilder(starter, switcher, command.toString());
            String workingDirectory = getSafeWorkingDirectory();
            if (workingDirectory != null) {
                builder.directory(new File(workingDirectory));
            } else {
                throw logger.logExceptionAsError(new IllegalStateException("A Safe Working directory could not be"
                                                                           + " found to execute CLI command from."));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;
            StringBuilder output = new StringBuilder();
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith(WINDOWS_PROCESS_ERROR_MESSAGE) || line.matches(LINUX_MAC_PROCESS_ERROR_MESSAGE)) {
                    throw logger.logExceptionAsError(
                            new CredentialUnavailableException(
                                    "AzureCliCredential authentication unavailable. Azure CLI not installed"));
                }
                output.append(line);
            }
            String processOutput = output.toString();

            process.waitFor(10, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo("\"accessToken\": \"(.*?)(\"|$)", processOutput);
                    if (redactedOutput.contains("az login") || redactedOutput.contains("az account set")) {
                        throw logger.logExceptionAsError(
                                new CredentialUnavailableException(
                                        "AzureCliCredential authentication unavailable."
                                                + " Please run 'az login' to set up account"));
                    }
                    throw logger.logExceptionAsError(new ClientAuthenticationException(redactedOutput, null));
                } else {
                    throw logger.logExceptionAsError(
                        new ClientAuthenticationException("Failed to invoke Azure CLI ", null));
                }
            }
            Map<String, String> objectMap = SERIALIZER_ADAPTER.deserialize(processOutput, Map.class,
                        SerializerEncoding.JSON);
            String accessToken = objectMap.get("accessToken");
            String time = objectMap.get("expiresOn");
            String timeToSecond = time.substring(0, time.indexOf("."));
            String timeJoinedWithT = String.join("T", timeToSecond.split(" "));
            OffsetDateTime expiresOn = LocalDateTime.parse(timeJoinedWithT, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                           .atZone(ZoneId.systemDefault())
                                           .toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
            token = new AccessToken(accessToken, expiresOn);
        } catch (IOException | InterruptedException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        } catch (RuntimeException e) {
            return Mono.error(logger.logExceptionAsError(e));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                return Mono.error(logger.logExceptionAsError(new IllegalStateException(ex)));
            }
        }
        return Mono.just(token);
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithConfidentialClient(TokenRequestContext request) {
        return confidentialClientApplicationAccessor.getValue()
                .flatMap(confidentialClient -> Mono.fromFuture(() -> confidentialClient.acquireToken(
                    ClientCredentialParameters.builder(new HashSet<>(request.getScopes())).build()))
                    .map(MsalToken::new));
    }

    private HttpPipeline setupPipeline(HttpClient httpClient) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpLogOptions httpLogOptions = new HttpLogOptions();
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new HttpPipelineBuilder().httpClient(httpClient)
                   .policies(policies.toArray(new HttpPipelinePolicy[0])).build();
    }

    /**
     * Asynchronously acquire a token from Active Directory with a username and a password.
     *
     * @param request the details of the token request
     * @param username the username of the user
     * @param password the password of the user
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithUsernamePassword(TokenRequestContext request,
                                                            String username, String password) {
        return publicClientApplicationAccessor.getValue()
               .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(UserNamePasswordParameters.builder(
                            new HashSet<>(request.getScopes()), username, password.toCharArray()).build()))
                    .onErrorMap(t -> new ClientAuthenticationException("Failed to acquire token with username and "
                                                               + "password", null, t))
                    .map(MsalToken::new));
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @param account the account used to login to acquire the last token
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithPublicClientCache(TokenRequestContext request, IAccount account) {
        return publicClientApplicationAccessor.getValue()
            .flatMap(pc -> Mono.fromFuture(() -> {
                SilentParameters.SilentParametersBuilder parametersBuilder = SilentParameters.builder(
                    new HashSet<>(request.getScopes()));
                if (account != null) {
                    parametersBuilder = parametersBuilder.account(account);
                }
                try {
                    return pc.acquireTokenSilently(parametersBuilder.build());
                } catch (MalformedURLException e) {
                    return getFailedCompletableFuture(logger.logExceptionAsError(new RuntimeException(e)));
                }
            }).map(MsalToken::new)
                .filter(t -> OffsetDateTime.now().isBefore(t.getExpiresAt().minus(REFRESH_OFFSET)))
                .switchIfEmpty(Mono.fromFuture(() -> {
                    SilentParameters.SilentParametersBuilder forceParametersBuilder = SilentParameters.builder(
                        new HashSet<>(request.getScopes())).forceRefresh(true);
                    if (account != null) {
                        forceParametersBuilder = forceParametersBuilder.account(account);
                    }
                    try {
                        return pc.acquireTokenSilently(forceParametersBuilder.build());
                    } catch (MalformedURLException e) {
                        return getFailedCompletableFuture(logger.logExceptionAsError(new RuntimeException(e)));
                    }
                }).map(MsalToken::new)));
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithConfidentialClientCache(TokenRequestContext request) {
        return confidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> {
                SilentParameters.SilentParametersBuilder parametersBuilder = SilentParameters.builder(
                        new HashSet<>(request.getScopes()));
                try {
                    return confidentialClient.acquireTokenSilently(parametersBuilder.build());
                } catch (MalformedURLException e) {
                    return getFailedCompletableFuture(logger.logExceptionAsError(new RuntimeException(e)));
                }
            }).map(ar -> (AccessToken) new MsalToken(ar))
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
        return publicClientApplicationAccessor.getValue().flatMap(pc ->
            Mono.fromFuture(() -> {
                DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters.builder(
                    new HashSet<>(request.getScopes()), dc -> deviceCodeConsumer.accept(
                        new DeviceCodeInfo(dc.userCode(), dc.deviceCode(), dc.verificationUri(),
                        OffsetDateTime.now().plusSeconds(dc.expiresIn()), dc.message()))).build();
                return pc.acquireToken(parameters);
            }).onErrorMap(t -> new ClientAuthenticationException("Failed to acquire token with device code", null, t))
                .map(MsalToken::new));
    }

    /**
     * Asynchronously acquire a token from Active Directory with Visual Sutdio cached refresh token.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken.
     */
    public Mono<MsalToken> authenticateWithVsCodeCredential(TokenRequestContext request, String cloud) {

        VisualStudioCacheAccessor accessor = new VisualStudioCacheAccessor();

        String credential = accessor.getCredentials("VS Code Azure", cloud);

        RefreshTokenParameters parameters = RefreshTokenParameters
                                                .builder(new HashSet<>(request.getScopes()), credential)
                                                .build();

        return publicClientApplicationAccessor.getValue()
                .flatMap(pc ->  Mono.fromFuture(pc.acquireToken(parameters)).map(MsalToken::new));
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
        AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(authorizationCode, redirectUrl)
            .scopes(new HashSet<>(request.getScopes()))
            .build();
        Mono<IAuthenticationResult> acquireToken;
        if (clientSecret != null) {
            acquireToken = confidentialClientApplicationAccessor.getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parameters)));
        } else {
            acquireToken = publicClientApplicationAccessor.getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parameters)));
        }
        return acquireToken.onErrorMap(t -> new ClientAuthenticationException(
            "Failed to acquire token with authorization code", null, t)).map(MsalToken::new);
    }


    /**
     * Asynchronously acquire a token from Active Directory by opening a browser and wait for the user to login. The
     * credential will run a minimal local HttpServer at the given port, so {@code http://localhost:{port}} must be
     * listed as a valid reply URL for the application.
     *
     * @param request the details of the token request
     * @param port the port on which the HTTP server is listening
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithBrowserInteraction(TokenRequestContext request, int port) {
        URI redirectUri;
        try {
            redirectUri = new URI(HTTP_LOCALHOST + ":" + port);
        } catch (URISyntaxException e) {
            return Mono.error(logger.logExceptionAsError(new RuntimeException(e)));
        }
        InteractiveRequestParameters parameters = InteractiveRequestParameters.builder(redirectUri)
                                                     .scopes(new HashSet<>(request.getScopes()))
                                                     .build();
        Mono<IAuthenticationResult> acquireToken = publicClientApplicationAccessor.getValue()
                               .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parameters)));

        return acquireToken.onErrorMap(t -> new ClientAuthenticationException(
            "Failed to acquire token with Interactive Browser Authentication.", null, t)).map(MsalToken::new);
    }

    /**
     * Gets token from shared token cache
     * */
    public Mono<MsalToken> authenticateWithSharedTokenCache(TokenRequestContext request, String username) {
        // find if the Public Client app with the requested username exists
        return publicClientApplicationAccessor.getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.getAccounts())
                    .onErrorMap(t -> new CredentialUnavailableException(
                            "Cannot get accounts from token cache. Error: " + t.getMessage(), t))
                    .flatMap(set -> {
                        IAccount requestedAccount;
                        Map<String, IAccount> accounts = new HashMap<>(); // home account id -> account

                        if (set.isEmpty()) {
                            return Mono.error(new CredentialUnavailableException("SharedTokenCacheCredential "
                                    + "authentication unavailable. No accounts were found in the cache."));
                        }

                        for (IAccount cached : set) {
                            if (username == null || username.equals(cached.username())) {
                                if (!accounts.containsKey(cached.homeAccountId())) { // only put the first one
                                    accounts.put(cached.homeAccountId(), cached);
                                }
                            }
                        }

                        if (accounts.isEmpty()) {
                            // no more accounts after filtering, username must be set
                            return Mono.error(new RuntimeException(String.format("SharedTokenCacheCredential "
                                    + "authentication unavailable. No account matching the specified username: %s was "
                                    + "found in the cache.", username)));
                        } else if (accounts.size() > 1) {
                            if (username == null) {
                                return Mono.error(new RuntimeException("SharedTokenCacheCredential authentication "
                                        + "unavailable. Multiple accounts were found in the cache. Use username and "
                                        + "tenant id to disambiguate."));
                            } else {
                                return Mono.error(new RuntimeException(String.format("SharedTokenCacheCredential "
                                    + "authentication unavailable. Multiple accounts matching the specified username: "
                                    + "%s were found in the cache.", username)));
                            }
                        } else {
                            requestedAccount = accounts.values().iterator().next();
                        }


                        return authenticateWithPublicClientCache(request, requestedAccount);
                    }));
    }

    /**
     * Asynchronously acquire a token from the App Service Managed Service Identity endpoint.
     *
     * @param msiEndpoint the endpoint to acquire token from
     * @param msiSecret the secret to acquire token with
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToManagedIdentityEndpoint(String msiEndpoint, String msiSecret,
                                                                   TokenRequestContext request) {
        return Mono.fromCallable(() -> {
            String resource = ScopeUtil.scopesToResource(request.getScopes());
            HttpURLConnection connection = null;
            StringBuilder payload = new StringBuilder();

            payload.append("resource=");
            payload.append(URLEncoder.encode(resource, "UTF-8"));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode("2017-09-01", "UTF-8"));
            if (clientId != null) {
                payload.append("&clientid=");
                payload.append(URLEncoder.encode(clientId, "UTF-8"));
            }
            try {
                URL url = new URL(String.format("%s?%s", msiEndpoint, payload));
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                if (msiSecret != null) {
                    connection.setRequestProperty("Secret", msiSecret);
                }
                connection.setRequestProperty("Metadata", "true");

                connection.connect();

                Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())
                        .useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                return SERIALIZER_ADAPTER.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * Asynchronously acquire a token from the Virtual Machine IMDS endpoint.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToIMDSEndpoint(TokenRequestContext request) {
        String resource = ScopeUtil.scopesToResource(request.getScopes());
        StringBuilder payload = new StringBuilder();
        final int imdsUpgradeTimeInMs = 70 * 1000;

        try {
            payload.append("api-version=");
            payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
            payload.append("&resource=");
            payload.append(URLEncoder.encode(resource, "UTF-8"));
            if (clientId != null) {
                payload.append("&client_id=");
                payload.append(URLEncoder.encode(clientId, "UTF-8"));
            }
        } catch (IOException exception) {
            return Mono.error(exception);
        }

        return checkIMDSAvailable().flatMap(available -> Mono.fromCallable(() -> {
            int retry = 1;
            while (retry <= options.getMaxRetry()) {
                URL url = null;
                HttpURLConnection connection = null;
                try {
                    url =
                            new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s",
                                    payload.toString()));

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Metadata", "true");
                    connection.connect();

                    Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())
                            .useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";

                    return SERIALIZER_ADAPTER.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
                } catch (IOException exception) {
                    if (connection == null) {
                        throw logger.logExceptionAsError(new RuntimeException(
                                String.format("Could not connect to the url: %s.", url), exception));
                    }
                    int responseCode;
                    try {
                        responseCode = connection.getResponseCode();
                    } catch (Exception e) {
                        throw logger.logExceptionAsError(
                            new CredentialUnavailableException(
                                "ManagedIdentityCredential authentication unavailable. "
                                    + "Connection to IMDS endpoint cannot be established, "
                                    + e.getMessage() + ".", e));
                    }
                    if (responseCode == 410
                            || responseCode == 429
                            || responseCode == 404
                            || (responseCode >= 500 && responseCode <= 599)) {
                        int retryTimeoutInMs = options.getRetryTimeout()
                                .apply(Duration.ofSeconds(RANDOM.nextInt(retry))).getNano() / 1000;
                        // Error code 410 indicates IMDS upgrade is in progress, which can take up to 70s
                        //
                        retryTimeoutInMs =
                                (responseCode == 410 && retryTimeoutInMs < imdsUpgradeTimeInMs) ? imdsUpgradeTimeInMs
                                        : retryTimeoutInMs;
                        retry++;
                        if (retry > options.getMaxRetry()) {
                            break;
                        } else {
                            sleep(retryTimeoutInMs);
                        }
                    } else {
                        throw logger.logExceptionAsError(new RuntimeException(
                                "Couldn't acquire access token from IMDS, verify your objectId, "
                                        + "clientId or msiResourceId", exception));
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
            throw logger.logExceptionAsError(new RuntimeException(
                    String.format("MSI: Failed to acquire tokens after retrying %s times",
                    options.getMaxRetry())));
        }));
    }

    private Mono<Boolean> checkIMDSAvailable() {
        StringBuilder payload = new StringBuilder();

        try {
            payload.append("api-version=");
            payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
        } catch (IOException exception) {
            return Mono.error(exception);
        }
        return Mono.fromCallable(() -> {
            HttpURLConnection connection = null;
            URL url = new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s",
                            payload.toString()));

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(500);
                connection.connect();
            } catch (Exception e) {
                throw logger.logExceptionAsError(
                    new CredentialUnavailableException(
                                "ManagedIdentityCredential authentication unavailable. "
                                 + "Connection to IMDS endpoint cannot be established, "
                                 + e.getMessage() + ".", e));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return true;
        });
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
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

    private String getSafeWorkingDirectory() {
        if (isWindowsPlatform()) {
            if (CoreUtils.isNullOrEmpty(DEFAULT_WINDOWS_SYSTEM_ROOT)) {
                return null;
            }
            return DEFAULT_WINDOWS_SYSTEM_ROOT + "\\system32";
        } else {
            return DEFAULT_MAC_LINUX_PATH;
        }
    }

    private boolean isWindowsPlatform() {
        return System.getProperty("os.name").contains("Windows");
    }

    private String redactInfo(String regex, String input) {
        return input.replaceAll(regex, "****");
    }

    void openUrl(String url) throws IOException {
        Runtime rt = Runtime.getRuntime();

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else if (os.contains("mac")) {
            rt.exec("open " + url);
        } else if (os.contains("nix") || os.contains("nux")) {
            rt.exec("xdg-open " + url);
        } else {
            logger.error("Browser could not be opened - please open {} in a browser on this device.", url);
        }
    }

    private CompletableFuture<IAuthenticationResult> getFailedCompletableFuture(Exception e) {
        CompletableFuture<IAuthenticationResult> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(e);
        return completableFuture;
    }

    private void initializeHttpPipelineAdapter() {
        // If user supplies the pipeline, then it should override all other properties
        // as they should directly be set on the pipeline.
        HttpPipeline httpPipeline = options.getHttpPipeline();
        if (httpPipeline != null) {
            httpPipelineAdapter = new HttpPipelineAdapter(httpPipeline);
        } else {
            // If http client is set on the credential, then it should override the proxy options if any configured.
            HttpClient httpClient = options.getHttpClient();
            if (httpClient != null) {
                httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(httpClient));
            } else if (options.getProxyOptions() == null) {
                //Http Client is null, proxy options are not set, use the default client and build the pipeline.
                httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(HttpClient.createDefault()));
            }
        }
    }

    /**
     * Get the configured tenant id.
     *
     * @return the tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get the configured client id.
     *
     * @return the client id.
     */
    public String getClientId() {
        return clientId;
    }
}

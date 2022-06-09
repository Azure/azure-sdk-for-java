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
import com.azure.identity.TokenCachePersistenceOptions;
import com.azure.identity.implementation.util.CertificateUtil;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.IdentitySslUtil;
import com.azure.identity.implementation.util.ScopeUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.sun.jna.Platform;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.InputStream;
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
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
    private static final Pattern LINUX_MAC_PROCESS_ERROR_MESSAGE = Pattern.compile("(.*)az:(.*)not found");
    private static final String DEFAULT_WINDOWS_SYSTEM_ROOT = System.getenv("SystemRoot");
    private static final String DEFAULT_WINDOWS_PS_EXECUTABLE = "pwsh.exe";
    private static final String LEGACY_WINDOWS_PS_EXECUTABLE = "powershell.exe";
    private static final String DEFAULT_LINUX_PS_EXECUTABLE = "pwsh";
    private static final String DEFAULT_MAC_LINUX_PATH = "/bin/";
    private static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    private static final String IDENTITY_ENDPOINT_VERSION = "2019-08-01";
    private static final String MSI_ENDPOINT_VERSION = "2017-09-01";
    private static final String ADFS_TENANT = "adfs";
    private static final String HTTP_LOCALHOST = "http://localhost";
    private static final String SERVICE_FABRIC_MANAGED_IDENTITY_API_VERSION = "2019-07-01-preview";
    private static final ClientLogger LOGGER = new ClientLogger(IdentityClient.class);
    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\": \"(.*?)(\"|$)");
    private static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");

    private final IdentityClientOptions options;
    private final String tenantId;
    private final String clientId;
    private final String resourceId;
    private final String clientSecret;
    private final String clientAssertionFilePath;
    private final InputStream certificate;
    private final String certificatePath;
    private final Supplier<String> clientAssertionSupplier;
    private final String certificatePassword;
    private HttpPipelineAdapter httpPipelineAdapter;
    private final SynchronizedAccessor<PublicClientApplication> publicClientApplicationAccessor;
    private final SynchronizedAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;
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
        String clientAssertionFilePath, String resourceId, Supplier<String> clientAssertionSupplier,
        InputStream certificate, String certificatePassword, boolean isSharedTokenCacheCredential,
        Duration clientAssertionTimeout, IdentityClientOptions options) {
        if (tenantId == null) {
            tenantId = "organizations";
        }
        if (options == null) {
            options = new IdentityClientOptions();
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.resourceId = resourceId;
        this.clientSecret = clientSecret;
        this.clientAssertionFilePath = clientAssertionFilePath;
        this.certificatePath = certificatePath;
        this.certificate = certificate;
        this.certificatePassword = certificatePassword;
        this.clientAssertionSupplier = clientAssertionSupplier;
        this.options = options;

        this.publicClientApplicationAccessor = new SynchronizedAccessor<>(() ->
            getPublicClientApplication(isSharedTokenCacheCredential));

        this.confidentialClientApplicationAccessor = new SynchronizedAccessor<>(() ->
            getConfidentialClientApplication());

        this.clientAssertionAccessor = clientAssertionTimeout == null
            ? new SynchronizedAccessor<>(() -> parseClientAssertion(), Duration.ofMinutes(5))
                : new SynchronizedAccessor<>(() -> parseClientAssertion(), clientAssertionTimeout);
    }

    private Mono<ConfidentialClientApplication> getConfidentialClientApplication() {
        return Mono.defer(() -> {
            if (clientId == null) {
                return Mono.error(LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "A non-null value for client ID must be provided for user authentication.")));
            }
            String authorityUrl = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/"
                + tenantId;
            IClientCredential credential;
            if (clientSecret != null) {
                credential = ClientCredentialFactory.createFromSecret(clientSecret);
            } else if (certificate != null || certificatePath != null) {
                try {
                    if (certificatePassword == null) {
                        byte[] pemCertificateBytes = getCertificateBytes();

                        List<X509Certificate> x509CertificateList = CertificateUtil.publicKeyFromPem(pemCertificateBytes);
                        PrivateKey privateKey = CertificateUtil.privateKeyFromPem(pemCertificateBytes);
                        if (x509CertificateList.size() == 1) {
                            credential = ClientCredentialFactory.createFromCertificate(
                                privateKey, x509CertificateList.get(0));
                        } else {
                            credential = ClientCredentialFactory.createFromCertificateChain(
                                privateKey, x509CertificateList);
                        }
                    } else {
                        try (InputStream pfxCertificateStream = getCertificateInputStream()) {
                            credential = ClientCredentialFactory.createFromCertificate(pfxCertificateStream,
                                certificatePassword);
                        }
                    }
                } catch (IOException | GeneralSecurityException e) {
                    return Mono.error(LOGGER.logExceptionAsError(new RuntimeException(
                        "Failed to parse the certificate for the credential: " + e.getMessage(), e)));
                }
            } else if (clientAssertionSupplier != null) {
                credential = ClientCredentialFactory.createFromClientAssertion(clientAssertionSupplier.get());
            } else {
                return Mono.error(LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Must provide client secret or client certificate path."
                        +  " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                        + "https://aka.ms/azsdk/java/identity/serviceprincipalauthentication/troubleshoot")));
            }

            ConfidentialClientApplication.Builder applicationBuilder =
                ConfidentialClientApplication.builder(clientId, credential);
            try {
                applicationBuilder = applicationBuilder.authority(authorityUrl);
            } catch (MalformedURLException e) {
                return Mono.error(LOGGER.logExceptionAsWarning(new IllegalStateException(e)));
            }

            applicationBuilder.sendX5c(options.isIncludeX5c());

            initializeHttpPipelineAdapter();
            if (httpPipelineAdapter != null) {
                applicationBuilder.httpClient(httpPipelineAdapter);
            } else {
                applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
            }

            if (options.getExecutorService() != null) {
                applicationBuilder.executorService(options.getExecutorService());
            }
            TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getTokenCacheOptions();
            PersistentTokenCacheImpl tokenCache = null;
            if (tokenCachePersistenceOptions != null) {
                try {
                    tokenCache = new PersistentTokenCacheImpl()
                        .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                        .setName(tokenCachePersistenceOptions.getName());
                    applicationBuilder.setTokenCacheAccessAspect(tokenCache);
                } catch (Throwable t) {
                    return  Mono.error(LOGGER.logExceptionAsError(new ClientAuthenticationException(
                        "Shared token cache is unavailable in this environment.", null, t)));
                }
            }
            if (options.getRegionalAuthority() != null) {
                if (options.getRegionalAuthority() == RegionalAuthority.AUTO_DISCOVER_REGION) {
                    applicationBuilder.autoDetectRegion(true);
                } else {
                    applicationBuilder.azureRegion(options.getRegionalAuthority().toString());
                }
            }
            ConfidentialClientApplication confidentialClientApplication = applicationBuilder.build();
            return tokenCache != null ? tokenCache.registerCache()
                .map(ignored -> confidentialClientApplication) : Mono.just(confidentialClientApplication);
        });
    }

    private Mono<String> parseClientAssertion() {
        return Mono.fromCallable(() -> {
            if (clientAssertionFilePath != null) {
                byte[] encoded = Files.readAllBytes(Paths.get(clientAssertionFilePath));
                return new String(encoded, StandardCharsets.UTF_8);
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "Client Assertion File Path is not provided."
                        + " It should be provided to authenticate with client assertion."
                ));
            }

        });
    }

    private Mono<PublicClientApplication> getPublicClientApplication(boolean sharedTokenCacheCredential) {
        return Mono.defer(() -> {
            if (clientId == null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "A non-null value for client ID must be provided for user authentication."));
            }
            String authorityUrl = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/"
                + tenantId;
            PublicClientApplication.Builder publicClientApplicationBuilder = PublicClientApplication.builder(clientId);
            try {
                publicClientApplicationBuilder = publicClientApplicationBuilder.authority(authorityUrl);
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
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

            if (!options.isCp1Disabled()) {
                Set<String> set = new HashSet<>(1);
                set.add("CP1");
                publicClientApplicationBuilder.clientCapabilities(set);
            }
            return Mono.just(publicClientApplicationBuilder);
        }).flatMap(builder -> {
            TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getTokenCacheOptions();
            PersistentTokenCacheImpl tokenCache = null;
            if (tokenCachePersistenceOptions != null) {
                try {
                    tokenCache = new PersistentTokenCacheImpl()
                        .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                        .setName(tokenCachePersistenceOptions.getName());
                    builder.setTokenCacheAccessAspect(tokenCache);
                } catch (Throwable t) {
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                        "Shared token cache is unavailable in this environment.", null, t));
                }
            }
            PublicClientApplication publicClientApplication = builder.build();
            return tokenCache != null ? tokenCache.registerCache()
                .map(ignored -> publicClientApplication) : Mono.just(publicClientApplication);
        });
    }

    public Mono<MsalToken> authenticateWithIntelliJ(TokenRequestContext request) {
        try {
            IntelliJCacheAccessor cacheAccessor = new IntelliJCacheAccessor(options.getIntelliJKeePassDatabasePath());
            IntelliJAuthMethodDetails authDetails;
            try {
                authDetails = cacheAccessor.getAuthDetailsIfAvailable();
            } catch (CredentialUnavailableException e) {
                return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                    new CredentialUnavailableException("IntelliJ Authentication not available.", e)));
            }
            if (authDetails == null) {
                return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                    new CredentialUnavailableException("IntelliJ Authentication not available."
                        + " Please log in with Azure Tools for IntelliJ plugin in the IDE.")));
            }
            String authType = authDetails.getAuthMethod();
            if ("SP".equalsIgnoreCase(authType)) {
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
            } else if ("DC".equalsIgnoreCase(authType)) {
                LOGGER.verbose("IntelliJ Authentication => Device Code Authentication scheme detected in Azure Tools"
                    + " for IntelliJ Plugin.");
                if (isADFSTenant()) {
                    LOGGER.verbose("IntelliJ Authentication => The input tenant is detected to be ADFS and"
                        + " the ADFS tenants are not supported via IntelliJ Authentication currently.");
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException("IntelliJCredential  "
                                         + "authentication unavailable. ADFS tenant/authorities are not supported.")));
                }
                try {
                    JsonNode intelliJCredentials = cacheAccessor.getDeviceCodeCredentials();

                    String refreshToken = intelliJCredentials.get("refreshToken").textValue();

                    RefreshTokenParameters.RefreshTokenParametersBuilder refreshTokenParametersBuilder =
                        RefreshTokenParameters.builder(new HashSet<>(request.getScopes()), refreshToken);

                    if (request.getClaims() != null) {
                        ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
                        refreshTokenParametersBuilder.claims(customClaimRequest);
                    }

                    return publicClientApplicationAccessor.getValue()
                        .flatMap(pc -> Mono.fromFuture(pc.acquireToken(refreshTokenParametersBuilder.build()))
                            .map(MsalToken::new));
                }  catch (CredentialUnavailableException e) {
                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options, e));
                }

            } else {
                LOGGER.verbose("IntelliJ Authentication = > Only Service Principal and Device Code Authentication"
                    + " schemes are currently supported via IntelliJ Credential currently. Please ensure you used one"
                    + " of those schemes from Azure Tools for IntelliJ plugin.");

                return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                    new CredentialUnavailableException("IntelliJ Authentication not available."
                    + " Please login with Azure Tools for IntelliJ plugin in the IDE.")));
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

        StringBuilder azCommand = new StringBuilder("az account get-access-token --output json --resource ");

        String scopes = ScopeUtil.scopesToResource(request.getScopes());

        try {
            ScopeUtil.validateScope(scopes);
        } catch (IllegalArgumentException ex) {
            return Mono.error(LOGGER.logExceptionAsError(ex));
        }

        azCommand.append(scopes);

        String tenant = IdentityUtil.resolveTenantId(null, request, options);
        if (!CoreUtils.isNullOrEmpty(tenant)) {
            azCommand.append("--tenant ").append(tenant);
        }

        AccessToken token;
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

            ProcessBuilder builder = new ProcessBuilder(starter, switcher, azCommand.toString());

            String workingDirectory = getSafeWorkingDirectory();
            if (workingDirectory != null) {
                builder.directory(new File(workingDirectory));
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException("A Safe Working directory could not be"
                    + " found to execute CLI command from. To mitigate this issue, please refer to the troubleshooting "
                    + " guidelines here at https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                StandardCharsets.UTF_8.name()))) {
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (line.startsWith(WINDOWS_PROCESS_ERROR_MESSAGE)
                        || LINUX_MAC_PROCESS_ERROR_MESSAGE.matcher(line).matches()) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException(
                                "AzureCliCredential authentication unavailable. Azure CLI not installed."
                                    + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                                    + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                    }
                    output.append(line);
                }
            }
            String processOutput = output.toString();

            process.waitFor(10, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo(processOutput);
                    if (redactedOutput.contains("az login") || redactedOutput.contains("az account set")) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException(
                                "AzureCliCredential authentication unavailable."
                                    + " Please run 'az login' to set up account. To further mitigate this"
                                    + " issue, please refer to the troubleshooting guidelines here at "
                                    + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                    }
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException(redactedOutput, null));
                } else {
                    throw LOGGER.logExceptionAsError(
                        new ClientAuthenticationException("Failed to invoke Azure CLI ", null));
                }
            }

            LOGGER.verbose("Azure CLI Authentication => A token response was received from Azure CLI, deserializing the"
                + " response into an Access Token.");
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
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        } catch (RuntimeException e) {
            return Mono.error(e instanceof CredentialUnavailableException
                ? LoggingUtil.logCredentialUnavailableException(LOGGER, options, (CredentialUnavailableException) e)
                : LOGGER.logExceptionAsError(e));
        }

        return Mono.just(token);
    }


    /**
     * Asynchronously acquire a token from Active Directory with Azure Power Shell.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithAzurePowerShell(TokenRequestContext request) {

        List<CredentialUnavailableException> exceptions = new ArrayList<>(2);

        PowershellManager defaultPowerShellManager = new PowershellManager(Platform.isWindows()
            ? DEFAULT_WINDOWS_PS_EXECUTABLE : DEFAULT_LINUX_PS_EXECUTABLE);

        PowershellManager legacyPowerShellManager = Platform.isWindows()
            ? new PowershellManager(LEGACY_WINDOWS_PS_EXECUTABLE) : null;

        List<PowershellManager> powershellManagers = new ArrayList<>(2);
        powershellManagers.add(defaultPowerShellManager);
        if (legacyPowerShellManager != null) {
            powershellManagers.add(legacyPowerShellManager);
        }
        return Flux.fromIterable(powershellManagers)
            .flatMap(powershellManager -> getAccessTokenFromPowerShell(request, powershellManager)
                .onErrorResume(t -> {
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
                        + "powershell(pwsh) with following error: " + current.getMessage()
                        + "\r\n" + "Azure PowerShell authentication failed using powershell-core(powershell)"
                        + " with following error: " + last.getMessage(),
                        last.getCause());
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

        return confidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> confidentialClient.acquireToken(OnBehalfOfParameters
                    .builder(new HashSet<>(request.getScopes()), options.getUserAssertion())
                    .tenant(IdentityUtil.resolveTenantId(tenantId, request, options))
                    .build()))
                .map(MsalToken::new));
    }


    private Mono<AccessToken> getAccessTokenFromPowerShell(TokenRequestContext request,
                                                           PowershellManager powershellManager) {
        return powershellManager.initSession()
            .flatMap(manager -> {
                String azAccountsCommand = "Import-Module Az.Accounts -MinimumVersion 2.2.0 -PassThru";
                return manager.runCommand(azAccountsCommand)
                    .flatMap(output -> {
                        if (output.contains("The specified module 'Az.Accounts' with version '2.2.0' was not loaded "
                            + "because no valid module file")) {
                            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                                new CredentialUnavailableException(
                                "Az.Account module with version >= 2.2.0 is not installed. It needs to be installed to"
                                    + " use Azure PowerShell Credential.")));
                        }
                        LOGGER.verbose("Az.accounts module was found installed.");
                        StringBuilder accessTokenCommand = new StringBuilder("Get-AzAccessToken -ResourceUrl ");
                        accessTokenCommand.append(ScopeUtil.scopesToResource(request.getScopes()));
                        accessTokenCommand.append(" | ConvertTo-Json");
                        String command = accessTokenCommand.toString();
                        LOGGER.verbose("Azure Powershell Authentication => Executing the command `%s` in Azure "
                                + "Powershell to retrieve the Access Token.",
                            accessTokenCommand);
                        return manager.runCommand(accessTokenCommand.toString())
                            .flatMap(out -> {
                                if (out.contains("Run Connect-AzAccount to login")) {
                                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                                        new CredentialUnavailableException(
                                        "Run Connect-AzAccount to login to Azure account in PowerShell.")));
                                }
                                try {
                                    LOGGER.verbose("Azure Powershell Authentication => Attempting to deserialize the "
                                        + "received response from Azure Powershell.");
                                    Map<String, String> objectMap = SERIALIZER_ADAPTER.deserialize(out, Map.class,
                                        SerializerEncoding.JSON);
                                    String accessToken = objectMap.get("Token");
                                    String time = objectMap.get("ExpiresOn");
                                    OffsetDateTime expiresOn = OffsetDateTime.parse(time)
                                        .withOffsetSameInstant(ZoneOffset.UTC);
                                    return Mono.just(new AccessToken(accessToken, expiresOn));
                                } catch (IOException e) {
                                    return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                                        new CredentialUnavailableException(
                                            "Encountered error when deserializing response from Azure Power Shell.",
                                            e)));
                                }
                            });
                    });
            }).doFinally(ignored -> powershellManager.close());
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithConfidentialClient(TokenRequestContext request) {
        return confidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> {
                ClientCredentialParameters.ClientCredentialParametersBuilder builder =
                    ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                        .tenant(IdentityUtil
                            .resolveTenantId(tenantId, request, options));
                if (clientAssertionSupplier != null) {
                    builder.clientCredential(ClientCredentialFactory
                        .createFromClientAssertion(clientAssertionSupplier.get()));
                }
                return confidentialClient.acquireToken(builder.build());
            }
        )).map(MsalToken::new);
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
               .flatMap(pc -> Mono.fromFuture(() -> {
                   UserNamePasswordParameters.UserNamePasswordParametersBuilder userNamePasswordParametersBuilder =
                       UserNamePasswordParameters.builder(new HashSet<>(request.getScopes()),
                            username, password.toCharArray());

                   if (request.getClaims() != null) {
                       ClaimsRequest customClaimRequest = CustomClaimRequest
                                                               .formatAsClaimsRequest(request.getClaims());
                       userNamePasswordParametersBuilder.claims(customClaimRequest);
                   }
                   userNamePasswordParametersBuilder.tenant(
                       IdentityUtil.resolveTenantId(tenantId, request, options));
                   return pc.acquireToken(userNamePasswordParametersBuilder.build());
               }
               )).onErrorMap(t -> new ClientAuthenticationException("Failed to acquire token with username and "
                + "password. To mitigate this issue, please refer to the troubleshooting guidelines "
                + "here at https://aka.ms/azsdk/java/identity/usernamepasswordcredential/troubleshoot",
                null, t)).map(MsalToken::new);
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
        return publicClientApplicationAccessor.getValue()
            .flatMap(pc -> Mono.fromFuture(() -> {
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
                    return pc.acquireTokenSilently(parametersBuilder.build());
                } catch (MalformedURLException e) {
                    return getFailedCompletableFuture(LOGGER.logExceptionAsError(new RuntimeException(e)));
                }
            }).map(MsalToken::new)
                .filter(t -> OffsetDateTime.now().isBefore(t.getExpiresAt().minus(REFRESH_OFFSET)))
                .switchIfEmpty(Mono.fromFuture(() -> {
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
                        return pc.acquireTokenSilently(forceParametersBuilder.build());
                    } catch (MalformedURLException e) {
                        return getFailedCompletableFuture(LOGGER.logExceptionAsError(new RuntimeException(e)));
                    }
                }).map(MsalToken::new)));
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    @SuppressWarnings("deprecation")
    public Mono<AccessToken> authenticateWithConfidentialClientCache(TokenRequestContext request) {
        return confidentialClientApplicationAccessor.getValue()
            .flatMap(confidentialClient -> Mono.fromFuture(() -> {
                SilentParameters.SilentParametersBuilder parametersBuilder = SilentParameters.builder(
                        new HashSet<>(request.getScopes()))
                    .tenant(IdentityUtil.resolveTenantId(tenantId, request,
                        options));
                try {
                    return confidentialClient.acquireTokenSilently(parametersBuilder.build());
                } catch (MalformedURLException e) {
                    return getFailedCompletableFuture(LOGGER.logExceptionAsError(new RuntimeException(e)));
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
                DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder =
                    DeviceCodeFlowParameters.builder(
                        new HashSet<>(request.getScopes()), dc -> deviceCodeConsumer.accept(
                            new DeviceCodeInfo(dc.userCode(), dc.deviceCode(), dc.verificationUri(),
                                OffsetDateTime.now().plusSeconds(dc.expiresIn()), dc.message())))
                    .tenant(IdentityUtil
                        .resolveTenantId(tenantId, request, options));

                if (request.getClaims() != null) {
                    ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
                    parametersBuilder.claims(customClaimRequest);
                }
                return pc.acquireToken(parametersBuilder.build());
            }).onErrorMap(t -> new ClientAuthenticationException("Failed to acquire token with device code", null, t))
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
                new CredentialUnavailableException("VsCodeCredential  "
                + "authentication unavailable. ADFS tenant/authorities are not supported. "
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

        RefreshTokenParameters.RefreshTokenParametersBuilder parametersBuilder = RefreshTokenParameters
                                                .builder(new HashSet<>(request.getScopes()), credential);

        if (request.getClaims() != null) {
            ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(customClaimRequest);
        }

        return publicClientApplicationAccessor.getValue()
            .flatMap(pc ->  Mono.fromFuture(pc.acquireToken(parametersBuilder.build()))
                .onErrorResume(t -> {
                    if (t instanceof MsalInteractionRequiredException) {
                        return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException("Failed to acquire token with"
                            + " VS code credential."
                            + " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azsdk/java/identity/vscodecredential/troubleshoot", t)));
                    }
                    return Mono.error(new ClientAuthenticationException("Failed to acquire token with"
                        + " VS code credential", null, t));
                })
                .map(MsalToken::new));    }

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
        AuthorizationCodeParameters.AuthorizationCodeParametersBuilder parametersBuilder =
            AuthorizationCodeParameters.builder(authorizationCode, redirectUrl)
            .scopes(new HashSet<>(request.getScopes()))
            .tenant(IdentityUtil
                .resolveTenantId(tenantId, request, options));

        if (request.getClaims() != null) {
            ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(customClaimRequest);
        }

        Mono<IAuthenticationResult> acquireToken;
        if (clientSecret != null) {
            acquireToken = confidentialClientApplicationAccessor.getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parametersBuilder.build())));
        } else {
            acquireToken = publicClientApplicationAccessor.getValue()
                .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(parametersBuilder.build())));
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
        InteractiveRequestParameters.InteractiveRequestParametersBuilder builder =
            InteractiveRequestParameters.builder(redirectUri)
                .scopes(new HashSet<>(request.getScopes()))
                .prompt(Prompt.SELECT_ACCOUNT)
                .tenant(IdentityUtil
                    .resolveTenantId(tenantId, request, options));

        if (request.getClaims() != null) {
            ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
            builder.claims(customClaimRequest);
        }

        if (loginHint != null) {
            builder.loginHint(loginHint);
        }

        Mono<IAuthenticationResult> acquireToken = publicClientApplicationAccessor.getValue()
                               .flatMap(pc -> Mono.fromFuture(() -> pc.acquireToken(builder.build())));

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
                            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                                new CredentialUnavailableException("SharedTokenCacheCredential "
                                    + "authentication unavailable. No accounts were found in the cache.")));
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
     * Asynchronously acquire a token from the Azure Arc Managed Service Identity endpoint.
     *
     * @param identityEndpoint the Identity endpoint to acquire token from
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToArcManagedIdentityEndpoint(String identityEndpoint,
                                                                      TokenRequestContext request) {
        return Mono.fromCallable(() -> {
            HttpURLConnection connection = null;
            StringBuilder payload = new StringBuilder();
            payload.append("resource=");
            payload.append(URLEncoder.encode(ScopeUtil.scopesToResource(request.getScopes()),
                StandardCharsets.UTF_8.name()));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode("2019-11-01", StandardCharsets.UTF_8.name()));

            URL url = getUrl(String.format("%s?%s", identityEndpoint, payload));


            String secretKey = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Metadata", "true");
                connection.connect();

                new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            } catch (IOException e) {
                if (connection == null) {
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Failed to initialize "
                                                                       + "Http URL connection to the endpoint.",
                        null, e));
                }
                int status = connection.getResponseCode();
                if (status != 401) {
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException(String.format("Expected a 401"
                         + " Unauthorized response from Azure Arc Managed Identity Endpoint, received: %d", status),
                        null, e));
                }

                String realm = connection.getHeaderField("WWW-Authenticate");

                if (realm == null) {
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Did not receive a value"
                           + " for WWW-Authenticate header in the response from Azure Arc Managed Identity Endpoint",
                        null));
                }

                int separatorIndex = realm.indexOf("=");
                if (separatorIndex == -1) {
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Did not receive a correct value"
                           + " for WWW-Authenticate header in the response from Azure Arc Managed Identity Endpoint",
                        null));
                }

                String secretKeyPath = realm.substring(separatorIndex + 1);
                secretKey = new String(Files.readAllBytes(Paths.get(secretKeyPath)), StandardCharsets.UTF_8);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }


            if (secretKey == null) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("Did not receive a secret value"
                     + " in the response from Azure Arc Managed Identity Endpoint",
                    null));
            }


            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", String.format("Basic %s", secretKey));
                connection.setRequestProperty("Metadata", "true");
                connection.connect();

                Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())
                    .useDelimiter("\\A");
                String result = scanner.hasNext() ? scanner.next() : "";

                return SERIALIZER_ADAPTER.deserialize(result, MSIToken.class, SerializerEncoding.JSON);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
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
            .flatMap(assertionToken -> Mono.fromCallable(() -> {
                String authorityUrl = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("")
                    + "/" + tenantId + "/oauth2/v2.0/token";

                StringBuilder urlParametersBuilder  = new StringBuilder();
                urlParametersBuilder.append("client_assertion=");
                urlParametersBuilder.append(assertionToken);
                urlParametersBuilder.append("&client_assertion_type=urn:ietf:params:oauth:client-assertion-type"
                    + ":jwt-bearer");
                urlParametersBuilder.append("&client_id=");
                urlParametersBuilder.append(clientId);
                urlParametersBuilder.append("&grant_type=client_credentials");
                urlParametersBuilder.append("&scope=");
                urlParametersBuilder.append(URLEncoder.encode(request.getScopes().get(0),
                    StandardCharsets.UTF_8.name()));

                String urlParams = urlParametersBuilder.toString();

                byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                HttpURLConnection connection = null;

                URL url = getUrl(authorityUrl);

                try {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                    connection.setDoOutput(true);
                    try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                        outputStream.write(postData);
                    }
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
            }));
    }

    /**
     * Asynchronously acquire a token from the Azure Service Fabric Managed Service Identity endpoint.
     *
     * @param identityEndpoint the Identity endpoint to acquire token from
     * @param identityHeader the identity header to acquire token with
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToServiceFabricManagedIdentityEndpoint(String identityEndpoint,
                                                                                String identityHeader,
                                                                                String thumbprint,
                                                                                TokenRequestContext request) {
        return Mono.fromCallable(() -> {
            HttpsURLConnection connection = null;
            String endpoint = identityEndpoint;
            String headerValue = identityHeader;
            String endpointVersion = SERVICE_FABRIC_MANAGED_IDENTITY_API_VERSION;

            String resource = ScopeUtil.scopesToResource(request.getScopes());
            StringBuilder payload = new StringBuilder();

            payload.append("resource=");
            payload.append(URLEncoder.encode(resource, StandardCharsets.UTF_8.name()));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode(endpointVersion, StandardCharsets.UTF_8.name()));
            if (clientId != null) {
                LOGGER.warning("User assigned managed identities are not supported in the Service Fabric environment.");
                payload.append("&client_id=");
                payload.append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
            }

            if (resourceId != null) {
                LOGGER.warning("User assigned managed identities are not supported in the Service Fabric environment.");
                payload.append("&mi_res_id=");
                payload.append(URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name()));
            }

            try {

                URL url = getUrl(String.format("%s?%s", endpoint, payload));
                connection = (HttpsURLConnection) url.openConnection();

                IdentitySslUtil.addTrustedCertificateThumbprint(connection, thumbprint, LOGGER);
                connection.setRequestMethod("GET");
                if (headerValue != null) {
                    connection.setRequestProperty("Secret", headerValue);
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
     * Asynchronously acquire a token from the App Service Managed Service Identity endpoint.
     *
     * Specifying identity parameters will use the 2019-08-01 endpoint version.
     * Specifying MSI parameters will use the 2017-09-01 endpoint version.
     *
     * @param identityEndpoint the Identity endpoint to acquire token from
     * @param identityHeader the identity header to acquire token with
     * @param msiEndpoint the MSI endpoint to acquire token from
     * @param msiSecret the MSI secret to acquire token with
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToManagedIdentityEndpoint(String identityEndpoint, String identityHeader,
                                                                   String msiEndpoint, String msiSecret,
                                                                   TokenRequestContext request) {
        return Mono.fromCallable(() -> {
            String endpoint;
            String headerValue;
            String endpointVersion;


            if (identityEndpoint != null) {
                endpoint = identityEndpoint;
                headerValue = identityHeader;
                endpointVersion = IDENTITY_ENDPOINT_VERSION;
            } else {
                endpoint = msiEndpoint;
                headerValue = msiSecret;
                endpointVersion = MSI_ENDPOINT_VERSION;
            }


            String resource = ScopeUtil.scopesToResource(request.getScopes());
            HttpURLConnection connection = null;
            StringBuilder payload = new StringBuilder();

            payload.append("resource=");
            payload.append(URLEncoder.encode(resource, StandardCharsets.UTF_8.name()));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode(endpointVersion, StandardCharsets.UTF_8.name()));
            if (clientId != null) {
                if (endpointVersion.equals(IDENTITY_ENDPOINT_VERSION)) {
                    payload.append("&client_id=");
                } else {
                    if (headerValue == null) {
                        // This is the Cloud Shell case. If a clientId is specified, warn the user.
                        LOGGER.warning("User assigned managed identities are not supported in the Cloud Shell environment.");
                    }
                    payload.append("&clientid=");
                }
                payload.append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
            }
            if (resourceId != null) {
                if (endpointVersion.equals(MSI_ENDPOINT_VERSION) && headerValue == null) {
                    // This is the Cloud Shell case. If a clientId is specified, warn the user.
                    LOGGER.warning("User assigned managed identities are not supported in the Cloud Shell environment.");
                }
                payload.append("&mi_res_id=");
                payload.append(URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name()));
            }
            try {
                URL url = getUrl(String.format("%s?%s", endpoint, payload));
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                if (headerValue != null) {
                    if (IDENTITY_ENDPOINT_VERSION.equals(endpointVersion)) {
                        connection.setRequestProperty("X-IDENTITY-HEADER", headerValue);
                    } else {
                        connection.setRequestProperty("Secret", headerValue);
                    }
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

    static URL getUrl(String uri) throws MalformedURLException {
        return new URL(uri);
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
            payload.append(URLEncoder.encode("2018-02-01", StandardCharsets.UTF_8.name()));
            payload.append("&resource=");
            payload.append(URLEncoder.encode(resource, StandardCharsets.UTF_8.name()));
            if (clientId != null) {
                payload.append("&client_id=");
                payload.append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()));
            }
            if (resourceId != null) {
                payload.append("&mi_res_id=");
                payload.append(URLEncoder.encode(resourceId, StandardCharsets.UTF_8.name()));
            }
        } catch (IOException exception) {
            return Mono.error(exception);
        }

        String endpoint = TRAILING_FORWARD_SLASHES.matcher(options.getImdsAuthorityHost()).replaceAll("")
            + IdentityConstants.DEFAULT_IMDS_TOKENPATH;

        return checkIMDSAvailable(endpoint).flatMap(available -> Mono.fromCallable(() -> {
            int retry = 1;
            while (retry <= options.getMaxRetry()) {
                URL url = null;
                HttpURLConnection connection = null;
                try {
                    url = getUrl(String.format("%s?%s", endpoint, payload));

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
                        throw LOGGER.logExceptionAsError(new RuntimeException(
                                String.format("Could not connect to the url: %s.", url), exception));
                    }
                    int responseCode;
                    try {
                        responseCode = connection.getResponseCode();
                    } catch (Exception e) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException(
                                "ManagedIdentityCredential authentication unavailable. "
                                    + "Connection to IMDS endpoint cannot be established, "
                                    + e.getMessage() + ".", e));
                    }
                    if (responseCode == 400) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException(
                                "ManagedIdentityCredential authentication unavailable. "
                                    + "Connection to IMDS endpoint cannot be established.", null));
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
                        throw LOGGER.logExceptionAsError(new RuntimeException(
                                "Couldn't acquire access token from IMDS, verify your objectId, "
                                        + "clientId or msiResourceId", exception));
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
            throw LOGGER.logExceptionAsError(new RuntimeException(
                    String.format("MSI: Failed to acquire tokens after retrying %s times",
                    options.getMaxRetry())));
        }));
    }

    private Mono<Boolean> checkIMDSAvailable(String endpoint) {
        StringBuilder payload = new StringBuilder();

        try {
            payload.append("api-version=");
            payload.append(URLEncoder.encode("2018-02-01", StandardCharsets.UTF_8.name()));
        } catch (IOException exception) {
            return Mono.error(exception);
        }
        return Mono.fromCallable(() -> {
            HttpURLConnection connection = null;
            URL url = getUrl(String.format("%s?%s", endpoint, payload));

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(500);
                connection.connect();
            } catch (Exception e) {
                throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
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

    private String redactInfo(String input) {
        return ACCESS_TOKEN_PATTERN.matcher(input).replaceAll("****");
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
            LOGGER.error("Browser could not be opened - please open {} in a browser on this device.", url);
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
            httpPipelineAdapter = new HttpPipelineAdapter(httpPipeline, options);
        } else {
            // If http client is set on the credential, then it should override the proxy options if any configured.
            HttpClient httpClient = options.getHttpClient();
            if (httpClient != null) {
                httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(httpClient), options);
            } else if (options.getProxyOptions() == null) {
                //Http Client is null, proxy options are not set, use the default client and build the pipeline.
                httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(HttpClient.createDefault()), options);
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

    /**
     * Get the configured identity client options.
     *
     * @return the client options.
     */
    public IdentityClientOptions getIdentityClientOptions() {
        return options;
    }

    private boolean isADFSTenant() {
        return this.tenantId.equals(ADFS_TENANT);
    }

    private byte[] getCertificateBytes() throws IOException {
        if (certificatePath != null) {
            return Files.readAllBytes(Paths.get(certificatePath));
        } else if (certificate != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = certificate.read(buffer, 0, buffer.length);
            while (read != -1) {
                outputStream.write(buffer, 0, read);
                read = certificate.read(buffer, 0, buffer.length);
            }
            return outputStream.toByteArray();
        } else {
            return new byte[0];
        }
    }

    private InputStream getCertificateInputStream() throws IOException {
        if (certificatePath != null) {
            return new BufferedInputStream(new FileInputStream(certificatePath));
        } else {
            return certificate;
        }
    }
}

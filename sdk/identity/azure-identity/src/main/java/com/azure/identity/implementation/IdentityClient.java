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
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.RegionalAuthority;
import com.azure.identity.TokenCachePersistenceOptions;
import com.azure.identity.implementation.util.CertificateUtil;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.IdentitySslUtil;
import com.azure.identity.implementation.util.ScopeUtil;
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
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.sun.jna.Platform;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.Arrays;
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
    private final ClientLogger logger = new ClientLogger(IdentityClient.class);

    private final IdentityClientOptions options;
    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final InputStream certificate;
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
     * @param certificate the PKCS12 or PEM certificate of the application.
     * @param certificatePassword the password protecting the PFX certificate.
     * @param isSharedTokenCacheCredential Indicate whether the credential is
     * {@link com.azure.identity.SharedTokenCacheCredential} or not.
     * @param options the options configuring the client.
     */
    IdentityClient(String tenantId, String clientId, String clientSecret, String certificatePath,
                   InputStream certificate, String certificatePassword, boolean isSharedTokenCacheCredential,
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
        this.certificate = certificate;
        this.certificatePassword = certificatePassword;
        this.options = options;

        this.publicClientApplicationAccessor = new SynchronizedAccessor<PublicClientApplication>(() ->
            getPublicClientApplication(isSharedTokenCacheCredential));

        this.confidentialClientApplicationAccessor = new SynchronizedAccessor<ConfidentialClientApplication>(() ->
            getConfidentialClientApplication());
    }

    private Mono<ConfidentialClientApplication> getConfidentialClientApplication() {
        return Mono.defer(() -> {
            if (clientId == null) {
                return Mono.error(logger.logExceptionAsError(new IllegalArgumentException(
                    "A non-null value for client ID must be provided for user authentication.")));
            }
            String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
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
                        InputStream pfxCertificateStream = getCertificateInputStream();
                        try {
                            credential = ClientCredentialFactory.createFromCertificate(
                                pfxCertificateStream, certificatePassword);
                        } finally {
                            if (pfxCertificateStream != null) {
                                pfxCertificateStream.close();
                            }
                        }
                    }
                } catch (IOException | GeneralSecurityException e) {
                    return Mono.error(logger.logExceptionAsError(new RuntimeException(
                        "Failed to parse the certificate for the credential: " + e.getMessage(), e)));
                }
            } else {
                return Mono.error(logger.logExceptionAsError(
                    new IllegalArgumentException("Must provide client secret or client certificate path")));
            }

            ConfidentialClientApplication.Builder applicationBuilder =
                ConfidentialClientApplication.builder(clientId, credential);

            applicationBuilder.logPii(options.isPiiLoggingAllowed());

            try {
                applicationBuilder = applicationBuilder.authority(authorityUrl);
            } catch (MalformedURLException e) {
                return Mono.error(logger.logExceptionAsWarning(new IllegalStateException(e)));
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
                    return  Mono.error(logger.logExceptionAsError(new ClientAuthenticationException(
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

    private Mono<PublicClientApplication> getPublicClientApplication(boolean sharedTokenCacheCredential) {
        return Mono.defer(() -> {
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
                    throw logger.logExceptionAsError(new ClientAuthenticationException(
                        "Shared token cache is unavailable in this environment.", null, t));
                }
            }
            builder.logPii(options.isPiiLoggingAllowed());
            PublicClientApplication publicClientApplication = builder.build();
            return tokenCache != null ? tokenCache.registerCache()
                .map(ignored -> publicClientApplication) : Mono.just(publicClientApplication);
        });
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

                if (isADFSTenant()) {
                    return Mono.error(new CredentialUnavailableException("IntelliJCredential  "
                                         + "authentication unavailable. ADFS tenant/authorities are not supported."));
                }
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

        List<PowershellManager> powershellManagers = Arrays.asList(defaultPowerShellManager);
        if (legacyPowerShellManager != null) {
            powershellManagers.add(legacyPowerShellManager);
        }
        return Flux.fromIterable(powershellManagers)
            .flatMap(powershellManager -> getAccessTokenFromPowerShell(request, powershellManager)
                .onErrorResume(t -> {
                    if (!t.getClass().getSimpleName().equals("CredentialUnavailableException")) {
                        return Mono.error(new ClientAuthenticationException(
                            "Azure Powershell authentication failed. Error Details: " + t.getMessage(),
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
                    last = new CredentialUnavailableException("Azure Powershell authentication failed using default"
                        + "powershell(pwsh) with following error: " + current.getMessage()
                        + "\r\n" + "Azure Powershell authentication failed using powershell-core(powershell)"
                        + " with following error: " + last.getMessage(),
                        last.getCause());
                }
                return Mono.error(last);
            }));
    }

    private Mono<AccessToken> getAccessTokenFromPowerShell(TokenRequestContext request,
                                                           PowershellManager powershellManager) {
        return powershellManager.initSession()
            .flatMap(manager -> manager.runCommand("Import-Module Az.Accounts -MinimumVersion 2.2.0 -PassThru")
                .flatMap(output -> {
                    if (output.contains("The specified module 'Az.Accounts' with version '2.2.0' was not loaded "
                        + "because no valid module file")) {
                        return Mono.error(new CredentialUnavailableException(
                            "Az.Account module with version >= 2.2.0 is not installed. It needs to be installed to use"
                        + "Azure PowerShell Credential."));
                    }
                    StringBuilder accessTokenCommand = new StringBuilder("Get-AzAccessToken -ResourceUrl ");
                    accessTokenCommand.append(ScopeUtil.scopesToResource(request.getScopes()));
                    accessTokenCommand.append(" | ConvertTo-Json");
                    return manager.runCommand(accessTokenCommand.toString())
                        .flatMap(out -> {
                            if (out.contains("Run Connect-AzAccount to login")) {
                                return Mono.error(new CredentialUnavailableException(
                                    "Run Connect-AzAccount to login to Azure account in PowerShell."));
                            }
                            try {
                                Map<String, String> objectMap = SERIALIZER_ADAPTER.deserialize(out, Map.class,
                                    SerializerEncoding.JSON);
                                String accessToken = objectMap.get("Token");
                                String time = objectMap.get("ExpiresOn");
                                OffsetDateTime expiresOn = OffsetDateTime.parse(time)
                                    .withOffsetSameInstant(ZoneOffset.UTC);
                                return Mono.just(new AccessToken(accessToken, expiresOn));
                            } catch (IOException e) {
                                return Mono.error(logger
                                    .logExceptionAsError(new CredentialUnavailableException(
                                        "Encountered error when deserializing response from Azure Power Shell.", e)));
                            }
                        });
                })).doFinally(ignored -> powershellManager.close());
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
               .flatMap(pc -> Mono.fromFuture(() -> {
                   UserNamePasswordParameters.UserNamePasswordParametersBuilder userNamePasswordParametersBuilder =
                       UserNamePasswordParameters.builder(new HashSet<>(request.getScopes()),
                            username, password.toCharArray());

                   if (request.getClaims() != null) {
                       ClaimsRequest customClaimRequest = CustomClaimRequest
                                                               .formatAsClaimsRequest(request.getClaims());
                       userNamePasswordParametersBuilder.claims(customClaimRequest);
                   }
                   return pc.acquireToken(userNamePasswordParametersBuilder.build());
               }
               )).onErrorMap(t -> new ClientAuthenticationException("Failed to acquire token with username and "
                                + "password", null, t)).map(MsalToken::new);
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

                if (request.getClaims() != null) {
                    ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
                    parametersBuilder.claims(customClaimRequest);
                    parametersBuilder.forceRefresh(true);
                }
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

                    if (request.getClaims() != null) {
                        ClaimsRequest customClaimRequest = CustomClaimRequest
                                                               .formatAsClaimsRequest(request.getClaims());
                        forceParametersBuilder.claims(customClaimRequest);
                    }

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
                DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder =
                    DeviceCodeFlowParameters.builder(
                        new HashSet<>(request.getScopes()), dc -> deviceCodeConsumer.accept(
                            new DeviceCodeInfo(dc.userCode(), dc.deviceCode(), dc.verificationUri(),
                                OffsetDateTime.now().plusSeconds(dc.expiresIn()), dc.message())));

                if (request.getClaims() != null) {
                    ClaimsRequest customClaimRequest = CustomClaimRequest.formatAsClaimsRequest(request.getClaims());
                    parametersBuilder.claims(customClaimRequest);
                }
                return pc.acquireToken(parametersBuilder.build());
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

        if (isADFSTenant()) {
            return Mono.error(new CredentialUnavailableException("VsCodeCredential  "
                                         + "authentication unavailable. ADFS tenant/authorities are not supported."));
        }
        VisualStudioCacheAccessor accessor = new VisualStudioCacheAccessor();

        String credential = accessor.getCredentials("VS Code Azure", cloud);

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
                        return Mono.error(new CredentialUnavailableException("Failed to acquire token with"
                            + " VS code credential", t));
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
            .scopes(new HashSet<>(request.getScopes()));

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
            return Mono.error(logger.logExceptionAsError(new RuntimeException(e)));
        }
        InteractiveRequestParameters.InteractiveRequestParametersBuilder builder =
            InteractiveRequestParameters.builder(redirectUri)
                .scopes(new HashSet<>(request.getScopes()))
                .prompt(Prompt.SELECT_ACCOUNT);

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
                            if (options.isPiiLoggingAllowed()) {
                                logger.verbose(
                                    "Successfully retrieved the access token for the username: %s" + username);
                            }
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
            payload.append(URLEncoder.encode(ScopeUtil.scopesToResource(request.getScopes()), "UTF-8"));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode("2019-11-01", "UTF-8"));

            URL url = new URL(String.format("%s?%s", identityEndpoint, payload));


            String secretKey = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Metadata", "true");
                connection.connect();

                new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
            } catch (IOException e) {
                if (connection == null) {
                    throw logger.logExceptionAsError(new ClientAuthenticationException("Failed to initialize "
                                                                       + "Http URL connection to the endpoint.",
                        null, e));
                }
                int status = connection.getResponseCode();
                if (status != 401) {
                    throw logger.logExceptionAsError(new ClientAuthenticationException(String.format("Expected a 401"
                         + " Unauthorized response from Azure Arc Managed Identity Endpoint, received: %d", status),
                        null, e));
                }

                String realm = connection.getHeaderField("WWW-Authenticate");

                if (realm == null) {
                    throw logger.logExceptionAsError(new ClientAuthenticationException("Did not receive a value"
                           + " for WWW-Authenticate header in the response from Azure Arc Managed Identity Endpoint",
                        null));
                }

                int separatorIndex = realm.indexOf("=");
                if (separatorIndex == -1) {
                    throw logger.logExceptionAsError(new ClientAuthenticationException("Did not receive a correct value"
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
                throw logger.logExceptionAsError(new ClientAuthenticationException("Did not receive a secret value"
                     + " in the response from Azure Arc Managed Identity Endpoint",
                    null));
            }


            try {

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", String.format("Basic %s", secretKey));
                connection.setRequestProperty("Metadata", "true");
                connection.connect();

                Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
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
            payload.append(URLEncoder.encode(resource, "UTF-8"));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode(endpointVersion, "UTF-8"));
            if (clientId != null) {
                payload.append("&client_id=");
                payload.append(URLEncoder.encode(clientId, "UTF-8"));
            }

            try {

                URL url = new URL(String.format("%s?%s", endpoint, payload));
                connection = (HttpsURLConnection) url.openConnection();

                IdentitySslUtil.addTrustedCertificateThumbprint(getClass().getSimpleName(), connection,
                    thumbprint);
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
     * @param identityEndpoint the Identity endpoint to acquire token from
     * @param identityHeader the identity header to acquire token with
     * @param msiEndpoint the MSI endpoint to acquire token from
     * @param msiSecret the msi secret to acquire token with
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
            payload.append(URLEncoder.encode(resource, "UTF-8"));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode(endpointVersion, "UTF-8"));
            if (clientId != null) {
                if (endpointVersion.equals(IDENTITY_ENDPOINT_VERSION)) {
                    payload.append("&client_id=");
                } else {
                    payload.append("&clientid=");
                }
                payload.append(URLEncoder.encode(clientId, "UTF-8"));
            }
            try {
                URL url = new URL(String.format("%s?%s", endpoint, payload));
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                if (headerValue != null) {
                    if (endpointVersion.equals(IDENTITY_ENDPOINT_VERSION)) {
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

        String endpoint = Configuration.getGlobalConfiguration().get(
            Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL,
            IdentityConstants.DEFAULT_IMDS_ENDPOINT);

        return checkIMDSAvailable(endpoint).flatMap(available -> Mono.fromCallable(() -> {
            int retry = 1;
            while (retry <= options.getMaxRetry()) {
                URL url = null;
                HttpURLConnection connection = null;
                try {
                    url =
                            new URL(String.format("%s?%s", endpoint, payload.toString()));

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
                    if (responseCode == 400) {
                        throw logger.logExceptionAsError(
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

    private Mono<Boolean> checkIMDSAvailable(String endpoint) {
        StringBuilder payload = new StringBuilder();

        try {
            payload.append("api-version=");
            payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
        } catch (IOException exception) {
            return Mono.error(exception);
        }
        return Mono.fromCallable(() -> {
            HttpURLConnection connection = null;
            URL url = new URL(String.format("%s?%s", endpoint, payload.toString()));

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
            return new FileInputStream(certificatePath);
        } else if (certificate != null) {
            return certificate;
        } else {
            return null;
        }
    }
}

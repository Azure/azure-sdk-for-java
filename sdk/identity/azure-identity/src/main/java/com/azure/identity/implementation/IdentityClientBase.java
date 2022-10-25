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
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.TokenProviderResult;
import com.microsoft.aad.msal4j.ClaimsRequest;
import reactor.core.publisher.Mono;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class IdentityClientBase {
    static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    static final Random RANDOM = new Random();
    static final String WINDOWS_STARTER = "cmd.exe";
    static final String LINUX_MAC_STARTER = "/bin/sh";
    static final String WINDOWS_SWITCHER = "/c";
    static final String LINUX_MAC_SWITCHER = "-c";
    static final String WINDOWS_PROCESS_ERROR_MESSAGE = "'az' is not recognized";
    static final Pattern LINUX_MAC_PROCESS_ERROR_MESSAGE = Pattern.compile("(.*)az:(.*)not found");
    static final String DEFAULT_WINDOWS_SYSTEM_ROOT = System.getenv("SystemRoot");
    static final String DEFAULT_WINDOWS_PS_EXECUTABLE = "pwsh.exe";
    static final String LEGACY_WINDOWS_PS_EXECUTABLE = "powershell.exe";
    static final String DEFAULT_LINUX_PS_EXECUTABLE = "pwsh";
    static final String DEFAULT_MAC_LINUX_PATH = "/bin/";
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    static final String IDENTITY_ENDPOINT_VERSION = "2019-08-01";
    static final String MSI_ENDPOINT_VERSION = "2017-09-01";
    static final String ADFS_TENANT = "adfs";
    static final String HTTP_LOCALHOST = "http://localhost";
    static final String SERVICE_FABRIC_MANAGED_IDENTITY_API_VERSION = "2019-07-01-preview";
    static final ClientLogger LOGGER = new ClientLogger(IdentityClient.class);
    static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\": \"(.*?)(\"|$)");
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");

    final IdentityClientOptions options;
    final String tenantId;
    final String clientId;
    final String resourceId;
    final String clientSecret;
    final String clientAssertionFilePath;
    final InputStream certificate;
    final String certificatePath;
    final Supplier<String> clientAssertionSupplier;
    final String certificatePassword;
    HttpPipelineAdapter httpPipelineAdapter;

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
    IdentityClientBase(String tenantId, String clientId, String clientSecret, String certificatePath,
                   String clientAssertionFilePath, String resourceId, Supplier<String> clientAssertionSupplier,
                   InputStream certificate, String certificatePassword, boolean isSharedTokenCacheCredential,
                   Duration clientAssertionTimeout, IdentityClientOptions options) {
        if (tenantId == null) {
            tenantId = IdentityUtil.DEFAULT_TENANT;
            options.setAdditionallyAllowedTenants(Arrays.asList(IdentityUtil.ALL_TENANTS));
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
    }

    ConfidentialClientApplication getConfidentialClient() {
        if (clientId == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
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
                throw LOGGER.logExceptionAsError(new RuntimeException(
                    "Failed to parse the certificate for the credential: " + e.getMessage(), e));
            }
        } else if (clientAssertionSupplier != null) {
            credential = ClientCredentialFactory.createFromClientAssertion(clientAssertionSupplier.get());
        } else {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Must provide client secret or client certificate path."
                    +  " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                    + "https://aka.ms/azsdk/java/identity/serviceprincipalauthentication/troubleshoot"));
        }

        ConfidentialClientApplication.Builder applicationBuilder =
            ConfidentialClientApplication.builder(clientId, credential);
        try {
            applicationBuilder = applicationBuilder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
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
                throw  LOGGER.logExceptionAsError(new ClientAuthenticationException(
                    "Shared token cache is unavailable in this environment.", null, t));
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

        if (tokenCache != null) {
            tokenCache.registerCache();
        }
        return confidentialClientApplication;
    }

    PublicClientApplication getPublicClient(boolean sharedTokenCacheCredential) {
        if (clientId == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/"
            + tenantId;
        PublicClientApplication.Builder builder = PublicClientApplication.builder(clientId);
        try {
            builder = builder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
        }

        initializeHttpPipelineAdapter();
        if (httpPipelineAdapter != null) {
            builder.httpClient(httpPipelineAdapter);
        } else {
            builder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
        }

        if (options.getExecutorService() != null) {
            builder.executorService(options.getExecutorService());
        }

        if (!options.isCp1Disabled()) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            builder.clientCapabilities(set);
        }

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

        if (tokenCache != null) {
            tokenCache.registerCache();
        }
        return publicClientApplication;
    }

    ConfidentialClientApplication getManagedIdentityConfidentialClient() {
        String authorityUrl = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("")
            + "/" + tenantId;

        // Temporarily pass in Dummy Client secret and Client ID. until MSal removes its requirements.
        IClientCredential credential = ClientCredentialFactory
            .createFromSecret(clientSecret != null ? clientSecret : "dummy-secret");
        ConfidentialClientApplication.Builder applicationBuilder =
            ConfidentialClientApplication.builder(clientId == null ? "SYSTEM-ASSIGNED-MANAGED-IDENTITY"
                : clientId, credential);
        applicationBuilder.validateAuthority(false);
        try {
            applicationBuilder = applicationBuilder.authority(authorityUrl);
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
        }

        if (options.getManagedIdentityType() == null) {
            throw LOGGER.logExceptionAsError(
                new CredentialUnavailableException("Managed Identity type not configured, authentication not available."));
        }
        applicationBuilder.appTokenProvider(appTokenProviderParameters -> {
            TokenRequestContext trc = new TokenRequestContext()
                .setScopes(new ArrayList<>(appTokenProviderParameters.scopes))
                .setClaims(appTokenProviderParameters.claims)
                .setTenantId(appTokenProviderParameters.tenantId);

            Mono<AccessToken> accessTokenAsync = getTokenFromTargetManagedIdentity(trc);

            return accessTokenAsync.map(accessToken -> {
                TokenProviderResult result =  new TokenProviderResult();
                result.setAccessToken(accessToken.getToken());
                result.setTenantId(trc.getTenantId());
                result.setExpiresInSeconds(accessToken.getExpiresAt().toEpochSecond());
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
        }

        ConfidentialClientApplication confidentialClientApplication = applicationBuilder.build();
        return confidentialClientApplication;
    }

    DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder buildDeviceCodeFlowParameters(TokenRequestContext request, Consumer<DeviceCodeInfo> deviceCodeConsumer) {
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
        return parametersBuilder;
    }

    OnBehalfOfParameters buildOBOFlowParameters(TokenRequestContext request) {
        return OnBehalfOfParameters
            .builder(new HashSet<>(request.getScopes()), options.getUserAssertion())
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, options))
            .build();
    }

    InteractiveRequestParameters.InteractiveRequestParametersBuilder buildInteractiveRequestParameters(TokenRequestContext request, String loginHint, URI redirectUri) {
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
        return builder;
    }

    UserNamePasswordParameters.UserNamePasswordParametersBuilder buildUsernamePasswordFlowParameters(TokenRequestContext request, String username, String password) {
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
        return userNamePasswordParametersBuilder;
    }

    AccessToken getTokenFromAzureCLIAuthentication(StringBuilder azCommand) {
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
        }
        return token;
    }


    String getSafeWorkingDirectory() {
        if (isWindowsPlatform()) {
            if (CoreUtils.isNullOrEmpty(DEFAULT_WINDOWS_SYSTEM_ROOT)) {
                return null;
            }
            return DEFAULT_WINDOWS_SYSTEM_ROOT + "\\system32";
        } else {
            return DEFAULT_MAC_LINUX_PATH;
        }
    }

    boolean isWindowsPlatform() {
        return System.getProperty("os.name").contains("Windows");
    }

    String redactInfo(String input) {
        return ACCESS_TOKEN_PATTERN.matcher(input).replaceAll("****");
    }


    abstract Mono<AccessToken> getTokenFromTargetManagedIdentity(TokenRequestContext tokenRequestContext);


    HttpPipeline setupPipeline(HttpClient httpClient) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpLogOptions httpLogOptions = new HttpLogOptions();
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(new RetryPolicy());
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new HttpPipelineBuilder().httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0])).build();
    }


    void initializeHttpPipelineAdapter() {
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

    private static Proxy proxyOptionsToJavaNetProxy(ProxyOptions options) {
        switch (options.getType()) {
            case SOCKS4:
            case SOCKS5:
                return new Proxy(Proxy.Type.SOCKS, options.getAddress());
            case HTTP:
            default:
                return new Proxy(Proxy.Type.HTTP, options.getAddress());
        }
    }
}

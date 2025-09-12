// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.ProofOfPossessionOptions;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.SharedExecutorService;
import com.azure.core.util.UserAgentUtil;
import com.azure.core.util.builder.ClientBuilderUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.BrowserCustomizationOptions;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.TokenCachePersistenceOptions;
import com.azure.identity.implementation.models.AzureCliToken;
import com.azure.identity.implementation.util.CertificateUtil;
import com.azure.identity.implementation.util.IdentityUtil;
import com.azure.identity.implementation.util.LoggingUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.microsoft.aad.msal4j.AppTokenProviderParameters;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.IBroker;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityId;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SystemBrowserOptions;
import com.microsoft.aad.msal4j.TokenProviderResult;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.azure.identity.implementation.util.IdentityUtil.isWindowsPlatform;

public abstract class IdentityClientBase {
    static final String WINDOWS_STARTER = "cmd.exe";
    static final String LINUX_MAC_STARTER = "/bin/sh";
    static final String WINDOWS_SWITCHER = "/c";
    static final String LINUX_MAC_SWITCHER = "-c";
    static final Pattern WINDOWS_PROCESS_ERROR_MESSAGE = Pattern.compile("'azd?' is not recognized");
    static final Pattern SH_PROCESS_ERROR_MESSAGE = Pattern.compile("azd?:.*not found");
    static final String DEFAULT_MAC_LINUX_PATH = "/bin/";
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    static final String ADFS_TENANT = "adfs";
    static final String HTTP_LOCALHOST = "http://localhost";
    static final ClientLogger LOGGER = new ClientLogger(IdentityClient.class);
    static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\": \"(.*?)(\"|$)");
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");
    private static final String AZURE_IDENTITY_PROPERTIES = "azure-identity.properties";
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    private static final Map<String, HttpMethod> HTTP_METHOD_HASH_MAP = new HashMap<>(8);

    private final Map<String, String> properties = CoreUtils.getProperties(AZURE_IDENTITY_PROPERTIES);

    final IdentityClientOptions options;
    final String tenantId;
    final String clientId;
    final String resourceId;
    final String objectId;
    final String clientSecret;
    final String clientAssertionFilePath;
    final byte[] certificate;
    final String certificatePath;
    final Supplier<String> clientAssertionSupplier;
    final Function<HttpPipeline, String> clientAssertionSupplierWithHttpPipeline;
    final String certificatePassword;
    HttpPipelineAdapter httpPipelineAdapter;
    String userAgent = UserAgentUtil.DEFAULT_USER_AGENT_HEADER;
    private Class<?> interactiveBrowserBroker;
    private Method getMsalRuntimeBroker;
    HttpPipeline httpPipeline;

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
        String clientAssertionFilePath, String resourceId, String objectId, Supplier<String> clientAssertionSupplier,
        Function<HttpPipeline, String> clientAssertionSupplierWithHttpPipeline, byte[] certificate,
        String certificatePassword, boolean isSharedTokenCacheCredential, Duration clientAssertionTimeout,
        IdentityClientOptions options) {
        if (tenantId == null) {
            tenantId = IdentityUtil.DEFAULT_TENANT;
            options.setAdditionallyAllowedTenants(Collections.singletonList(IdentityUtil.ALL_TENANTS));
        }
        if (options == null) {
            options = new IdentityClientOptions();
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.objectId = objectId;
        this.resourceId = resourceId;
        this.clientSecret = clientSecret;
        this.clientAssertionFilePath = clientAssertionFilePath;
        this.certificatePath = certificatePath;
        this.certificate = certificate;
        this.certificatePassword = certificatePassword;
        this.clientAssertionSupplier = clientAssertionSupplier;
        this.clientAssertionSupplierWithHttpPipeline = clientAssertionSupplierWithHttpPipeline;
        this.options = options;

    }

    ConfidentialClientApplication getConfidentialClient(boolean enableCae) {
        if (clientId == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/" + tenantId;
        IClientCredential credential;

        if (clientSecret != null) {
            credential = ClientCredentialFactory.createFromSecret(clientSecret);
        } else if (certificate != null || certificatePath != null) {
            try {

                byte[] certificateBytes = getCertificateBytes();
                if (CertificateUtil.isPem(certificateBytes)) {

                    List<X509Certificate> x509CertificateList = CertificateUtil.publicKeyFromPem(certificateBytes);
                    PrivateKey privateKey = CertificateUtil.privateKeyFromPem(certificateBytes);
                    if (x509CertificateList.size() == 1) {
                        credential
                            = ClientCredentialFactory.createFromCertificate(privateKey, x509CertificateList.get(0));
                    } else {
                        credential
                            = ClientCredentialFactory.createFromCertificateChain(privateKey, x509CertificateList);
                    }
                } else {
                    try (InputStream pfxCertificateStream = getCertificateInputStream()) {
                        credential
                            = ClientCredentialFactory.createFromCertificate(pfxCertificateStream, certificatePassword);
                    }
                }
            } catch (IOException | GeneralSecurityException e) {
                throw LOGGER.logExceptionAsError(
                    new RuntimeException("Failed to parse the certificate for the credential: " + e.getMessage(), e));
            }
        } else if (clientAssertionSupplier != null) {
            credential = ClientCredentialFactory.createFromClientAssertion(clientAssertionSupplier.get());
        } else if (clientAssertionSupplierWithHttpPipeline != null) {
            credential = ClientCredentialFactory
                .createFromClientAssertion(clientAssertionSupplierWithHttpPipeline.apply(getPipeline()));
        } else {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Must provide client secret or client certificate path."
                    + " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                    + "https://aka.ms/azsdk/java/identity/serviceprincipalauthentication/troubleshoot"));
        }

        ConfidentialClientApplication.Builder applicationBuilder
            = ConfidentialClientApplication.builder(clientId, credential);
        try {
            applicationBuilder = applicationBuilder.logPii(options.isUnsafeSupportLoggingEnabled())
                .authority(authorityUrl)
                .instanceDiscovery(options.isInstanceDiscoveryEnabled());

            if (!options.isInstanceDiscoveryEnabled()) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Instance discovery and authority validation is disabled. In this"
                    + " state, the library will not fetch metadata to validate the specified authority host. As a"
                    + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
        }

        if (enableCae) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            applicationBuilder.clientCapabilities(set);
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
        } else {
            applicationBuilder.executorService(SharedExecutorService.getInstance());
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getTokenCacheOptions();
        PersistentTokenCacheImpl tokenCache = null;
        if (tokenCachePersistenceOptions != null) {
            try {
                tokenCache = new PersistentTokenCacheImpl(enableCae)
                    .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                    .setName(tokenCachePersistenceOptions.getName());
                applicationBuilder.setTokenCacheAccessAspect(tokenCache);
            } catch (Throwable t) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
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

    PublicClientApplication getPublicClient(boolean sharedTokenCacheCredential, boolean enableCae) {
        if (clientId == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/" + tenantId;
        PublicClientApplication.Builder builder = PublicClientApplication.builder(clientId);
        try {
            builder = builder.logPii(options.isUnsafeSupportLoggingEnabled())
                .authority(authorityUrl)
                .instanceDiscovery(options.isInstanceDiscoveryEnabled());

            if (!options.isInstanceDiscoveryEnabled()) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Instance discovery and authority validation is disabled. In this"
                    + " state, the library will not fetch metadata to validate the specified authority host. As a"
                    + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }
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
        } else {
            builder.executorService(SharedExecutorService.getInstance());
        }

        if (enableCae) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            builder.clientCapabilities(set);
        }

        if (options.isBrokerEnabled()) {
            if (interactiveBrowserBroker == null) {
                try {
                    interactiveBrowserBroker
                        = Class.forName("com.azure.identity.broker.implementation.InteractiveBrowserBroker");
                } catch (ClassNotFoundException e) {
                    throw LOGGER.logExceptionAsError(
                        new IllegalStateException("Could not load the brokered authentication library. "
                            + "Ensure that the azure-identity-broker library is on the classpath.", e));
                }
                getMsalRuntimeBroker = null;
                try {
                    getMsalRuntimeBroker = interactiveBrowserBroker.getMethod("getMsalRuntimeBroker");
                } catch (NoSuchMethodException e) {
                    throw LOGGER
                        .logExceptionAsError(new IllegalStateException("Could not obtain the InteractiveBrowserBroker. "
                            + "Ensure that the azure-identity-broker library is on the classpath.", e));
                }
            }

            try {
                if (getMsalRuntimeBroker != null) {
                    builder.broker((IBroker) getMsalRuntimeBroker.invoke(null));

                } else {
                    throw LOGGER.logExceptionAsError(new IllegalStateException("Could not obtain the MSAL Broker. "
                        + "Ensure that the azure-identity-broker library is on the classpath.", null));
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException("Could not invoke the MSAL Broker. "
                    + "Ensure that the azure-identity-broker library is on the classpath.", e));
            }
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getTokenCacheOptions();
        PersistentTokenCacheImpl tokenCache = null;
        if (tokenCachePersistenceOptions != null) {
            try {
                tokenCache = new PersistentTokenCacheImpl(enableCae)
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

    ManagedIdentityApplication getManagedIdentityMsalApplication() {

        ManagedIdentityId managedIdentityId;

        if (!CoreUtils.isNullOrEmpty(clientId)) {
            managedIdentityId = ManagedIdentityId.userAssignedClientId(clientId);
        } else if (!CoreUtils.isNullOrEmpty(resourceId)) {
            managedIdentityId = ManagedIdentityId.userAssignedResourceId(resourceId);
        } else if (!CoreUtils.isNullOrEmpty(objectId)) {
            managedIdentityId = ManagedIdentityId.userAssignedObjectId(objectId);
        } else {
            managedIdentityId = ManagedIdentityId.systemAssigned();
        }

        ManagedIdentityApplication.Builder miBuilder
            = ManagedIdentityApplication.builder(managedIdentityId).logPii(options.isUnsafeSupportLoggingEnabled());

        ManagedIdentitySourceType managedIdentitySourceType = ManagedIdentityApplication.getManagedIdentitySource();

        if (ManagedIdentitySourceType.DEFAULT_TO_IMDS.equals(managedIdentitySourceType)) {
            options.setUseImdsRetryStrategy();
        }

        initializeHttpPipelineAdapter();
        if (httpPipelineAdapter != null) {
            miBuilder.httpClient(httpPipelineAdapter);
        } else {
            miBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
        }

        if (options.getExecutorService() != null) {
            miBuilder.executorService(options.getExecutorService());
        } else {
            miBuilder.executorService(SharedExecutorService.getInstance());
        }

        return miBuilder.build();
    }

    ConfidentialClientApplication getWorkloadIdentityConfidentialClient() {
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/" + tenantId;

        // Temporarily pass in Dummy Client secret and Client ID. until MSal removes its requirements.
        IClientCredential credential
            = ClientCredentialFactory.createFromSecret(clientSecret != null ? clientSecret : "dummy-secret");
        ConfidentialClientApplication.Builder applicationBuilder = ConfidentialClientApplication
            .builder(clientId == null ? "SYSTEM-ASSIGNED-MANAGED-IDENTITY" : clientId, credential);

        try {
            applicationBuilder = applicationBuilder.authority(authorityUrl)
                .logPii(options.isUnsafeSupportLoggingEnabled())
                .instanceDiscovery(options.isInstanceDiscoveryEnabled());

            if (!options.isInstanceDiscoveryEnabled()) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Instance discovery and authority validation is disabled. In this"
                    + " state, the library will not fetch metadata to validate the specified authority host. As a"
                    + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }

        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsWarning(new IllegalStateException(e));
        }

        applicationBuilder.appTokenProvider(getWorkloadIdentityTokenProvider());

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

        return applicationBuilder.build();
    }

    abstract Function<AppTokenProviderParameters, CompletableFuture<TokenProviderResult>>
        getWorkloadIdentityTokenProvider();

    DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder buildDeviceCodeFlowParameters(TokenRequestContext request,
        Consumer<DeviceCodeInfo> deviceCodeConsumer) {
        DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder = DeviceCodeFlowParameters
            .builder(new HashSet<>(request.getScopes()),
                dc -> deviceCodeConsumer.accept(new DeviceCodeInfo(dc.userCode(), dc.deviceCode(), dc.verificationUri(),
                    OffsetDateTime.now().plusSeconds(dc.expiresIn()), dc.message())))
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
        }
        return parametersBuilder;
    }

    OnBehalfOfParameters buildOBOFlowParameters(TokenRequestContext request) {
        OnBehalfOfParameters.OnBehalfOfParametersBuilder builder
            = OnBehalfOfParameters.builder(new HashSet<>(request.getScopes()), options.getUserAssertion())
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            builder.claims(claimsRequest);
        }
        return builder.build();
    }

    InteractiveRequestParameters.InteractiveRequestParametersBuilder
        buildInteractiveRequestParameters(TokenRequestContext request, String loginHint, URI redirectUri) {
        InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
            = InteractiveRequestParameters.builder(redirectUri)
                .scopes(new HashSet<>(request.getScopes()))
                .prompt(Prompt.SELECT_ACCOUNT)
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            builder.claims(claimsRequest);
        }

        BrowserCustomizationOptions browserCustomizationOptions = options.getBrowserCustomizationOptions();

        if (IdentityUtil.browserCustomizationOptionsPresent(browserCustomizationOptions)) {
            SystemBrowserOptions.SystemBrowserOptionsBuilder browserOptionsBuilder = SystemBrowserOptions.builder();
            if (!CoreUtils.isNullOrEmpty(browserCustomizationOptions.getSuccessMessage())) {
                browserOptionsBuilder.htmlMessageSuccess(browserCustomizationOptions.getSuccessMessage());
            }

            if (!CoreUtils.isNullOrEmpty(browserCustomizationOptions.getErrorMessage())) {
                browserOptionsBuilder.htmlMessageError(browserCustomizationOptions.getErrorMessage());
            }
            builder.systemBrowserOptions(browserOptionsBuilder.build());
        }

        if (options.isBrokerEnabled()) {
            builder.windowHandle(options.getBrokerWindowHandle());
            if (options.isMsaPassthroughEnabled()) {
                Map<String, String> extraQueryParameters = new HashMap<>();
                extraQueryParameters.put("msal_request_type", "consumer_passthrough");
                builder.extraQueryParameters(extraQueryParameters);
            }

            if (request.getProofOfPossessionOptions() != null) {
                ProofOfPossessionOptions proofOfPossessionOptions = request.getProofOfPossessionOptions();
                try {
                    builder.proofOfPossession(
                        mapToMsalHttpMethod(proofOfPossessionOptions.getRequestMethod().toString()),
                        proofOfPossessionOptions.getRequestUrl().toURI(),
                        proofOfPossessionOptions.getProofOfPossessionNonce());
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        if (loginHint != null) {
            builder.loginHint(loginHint);
        }
        return builder;
    }

    static HttpMethod mapToMsalHttpMethod(String methodName) {
        if (HTTP_METHOD_HASH_MAP.containsKey(methodName)) {
            return HTTP_METHOD_HASH_MAP.get(methodName);
        }

        // Invalidate the cache if it grows too large. This is a simple cache and does not need to be large.
        if (HTTP_METHOD_HASH_MAP.size() > 10) {
            HTTP_METHOD_HASH_MAP.clear();
        }

        for (HttpMethod method : HttpMethod.values()) {
            if (method.methodName.equalsIgnoreCase(methodName)) {
                HTTP_METHOD_HASH_MAP.put(methodName, method);
                return method;
            }
        }
        throw new IllegalArgumentException("No enum constant with method name: " + methodName);
    }

    UserNamePasswordParameters.UserNamePasswordParametersBuilder
        buildUsernamePasswordFlowParameters(TokenRequestContext request, String username, String password) {
        UserNamePasswordParameters.UserNamePasswordParametersBuilder userNamePasswordParametersBuilder
            = UserNamePasswordParameters.builder(new HashSet<>(request.getScopes()), username, password.toCharArray());

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            userNamePasswordParametersBuilder.claims(claimsRequest);
        }
        userNamePasswordParametersBuilder.tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
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
            // Redirects stdin to dev null, helps to avoid messages sent in by the cmd process to upgrade etc.
            builder.redirectInput(ProcessBuilder.Redirect.from(IdentityUtil.NULL_FILE));

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
            try (BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (WINDOWS_PROCESS_ERROR_MESSAGE.matcher(line).find()
                        || SH_PROCESS_ERROR_MESSAGE.matcher(line).find()) {
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

            process.waitFor(this.options.getCredentialProcessTimeout().getSeconds(), TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo(processOutput);
                    if (redactedOutput.contains("az login") || redactedOutput.contains("az account set")) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException("AzureCliCredential authentication unavailable."
                                + " Please run 'az login' to set up account. To further mitigate this"
                                + " issue, please refer to the troubleshooting guidelines here at "
                                + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                    }
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException(redactedOutput, null));
                } else {
                    throw LOGGER
                        .logExceptionAsError(new ClientAuthenticationException("Failed to invoke Azure CLI ", null));
                }
            }

            LOGGER.verbose("Azure CLI Authentication => A token response was received from Azure CLI, deserializing the"
                + " response into an Access Token.");
            try (JsonReader reader = JsonProviders.createReader(processOutput)) {
                AzureCliToken tokenHolder = AzureCliToken.fromJson(reader);
                String accessToken = tokenHolder.getAccessToken();
                OffsetDateTime tokenExpiration = tokenHolder.getTokenExpiration();
                token = new AccessToken(accessToken, tokenExpiration);
            }

        } catch (IOException | InterruptedException e) {
            IllegalStateException ex = new IllegalStateException(redactInfo(e.getMessage()));
            ex.setStackTrace(e.getStackTrace());
            throw LOGGER.logExceptionAsError(ex);
        }
        return token;
    }

    AccessToken getTokenFromAzureDeveloperCLIAuthentication(StringBuilder azdCommand) {
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

            ProcessBuilder builder = new ProcessBuilder(starter, switcher, azdCommand.toString());
            // Redirects stdin to dev null, helps to avoid messages sent in by the cmd process to upgrade etc.
            builder.redirectInput(ProcessBuilder.Redirect.from(IdentityUtil.NULL_FILE));

            String workingDirectory = getSafeWorkingDirectory();
            if (workingDirectory != null) {
                builder.directory(new File(workingDirectory));
            } else {
                throw LOGGER.logExceptionAsError(new IllegalStateException(
                    "A Safe Working directory could not be" + " found to execute Azure Developer CLI command from."));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader
                = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8.name()))) {
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (WINDOWS_PROCESS_ERROR_MESSAGE.matcher(line).find()
                        || SH_PROCESS_ERROR_MESSAGE.matcher(line).find()) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException(
                                "AzureDeveloperCliCredential authentication unavailable. Azure Developer CLI not installed."
                                    + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                                    + "https://aka.ms/azsdk/java/identity/azdevclicredential/troubleshoot"));
                    }
                    output.append(line);
                }
            }
            String processOutput = output.toString();

            // wait until the process completes or the timeout (10 sec) is reached.
            process.waitFor(this.options.getCredentialProcessTimeout().getSeconds(), TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo(processOutput);

                    if (redactedOutput.contains("unknown flag: --claims")
                        || redactedOutput.contains("flag provided but not defined: -claims")) {
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException("Claims challenges are not supported by the "
                                + "currently installed Azure Developer CLI. Please update to azd CLI version 1.18.1 or higher "
                                + "to support claims challenges."));
                    }

                    if (redactedOutput.contains("azd auth login") || redactedOutput.contains("not logged in")) {
                        if (azdCommand.toString().contains("claims")) {
                            String userFriendlyError = extractUserFriendlyErrorFromAzdOutput(redactedOutput);
                            if (userFriendlyError != null) {
                                throw LOGGER
                                    .logExceptionAsError(new ClientAuthenticationException(userFriendlyError, null));
                            }
                        }
                        throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                            new CredentialUnavailableException("AzureDeveloperCliCredential authentication unavailable."
                                + " Please run 'azd auth login' to set up account."));
                    }
                    throw LOGGER.logExceptionAsError(new ClientAuthenticationException(redactedOutput, null));
                } else {
                    throw LOGGER.logExceptionAsError(
                        new ClientAuthenticationException("Failed to invoke Azure Developer CLI ", null));
                }
            }

            LOGGER.verbose(
                "Azure Developer CLI Authentication => A token response was received from Azure Developer CLI, deserializing the"
                    + " response into an Access Token.");
            try (JsonReader reader = JsonProviders.createReader(processOutput)) {
                reader.nextToken();
                Map<String, String> objectMap = reader.readMap(JsonReader::getString);
                String accessToken = objectMap.get("token");
                String time = objectMap.get("expiresOn");
                // az expiresOn format = "2022-11-30 02:38:42.000000" vs
                // azd expiresOn format = "2022-11-30T02:05:08Z"
                String standardTime = time.substring(0, time.indexOf("Z"));
                OffsetDateTime expiresOn = LocalDateTime.parse(standardTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.of("Z"))
                    .toOffsetDateTime()
                    .withOffsetSameInstant(ZoneOffset.UTC);
                token = new AccessToken(accessToken, expiresOn);
            }
        } catch (IOException | InterruptedException e) {
            IllegalStateException ex = new IllegalStateException(redactInfo(e.getMessage()));
            ex.setStackTrace(e.getStackTrace());
            throw LOGGER.logExceptionAsError(ex);
        }

        return token;
    }

    /**
     * Extract a single, user-friendly message from azd consoleMessage JSON output.
     *
     * @param output The output from the Azure Developer CLI command.
     * @return A user-friendly error message if found, otherwise null.
     *
     * Preference order:
     * 1) A message containing "Suggestion" (case-insensitive)
     * 2) The second message if multiple are present
     * 3) The first message if only one exists
     * Returns null if no messages can be parsed.
     */
    String extractUserFriendlyErrorFromAzdOutput(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }

        List<String> messages = new ArrayList<>();

        for (String line : output.split("\\R")) { // split on any line break
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // Handle multiple JSON objects in a single line
            try (JsonReader reader = JsonProviders.createReader(trimmed)) {
                while (reader.nextToken() != null) {
                    if (reader.currentToken() == JsonToken.START_OBJECT) {
                        Map<String, Object> obj = reader.readMap(JsonReader::readUntyped);

                        // check "data.message"
                        Object data = obj.get("data");
                        if (data instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> dataMap = (Map<String, Object>) data;
                            Object message = dataMap.get("message");
                            if (message instanceof String) {
                                String msg = ((String) message).trim();
                                if (!msg.isEmpty()) {
                                    messages.add(msg);
                                    continue;
                                }
                            }
                        }

                        // check "message"
                        Object message = obj.get("message");
                        if (message instanceof String) {
                            String msg = ((String) message).trim();
                            if (!msg.isEmpty()) {
                                messages.add(msg);
                            }
                        }
                    } else {
                        break; // Not a JSON object, stop processing this line
                    }
                }
            } catch (IOException e) {
                // not JSON -> ignore
            }
        }

        if (messages.isEmpty()) {
            return null;
        }

        // Prefer the suggestion line if present
        for (String msg : messages) {
            if (msg.toLowerCase().contains("suggestion")) {
                return redactInfo(msg);
            }
        }

        // If more than one message exists, return the last one
        if (messages.size() > 1) {
            return redactInfo(messages.get(messages.size() - 1));
        }

        return redactInfo(messages.get(0));
    }

    AccessToken authenticateWithExchangeTokenHelper(TokenRequestContext request, String assertionToken)
        throws IOException {
        String authorityUrl = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/"
            + tenantId + "/oauth2/v2.0/token";

        String urlParams = "client_assertion=" + assertionToken
            + "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&client_id=" + clientId
            + "&grant_type=client_credentials&scope=" + urlEncode(request.getScopes().get(0));

        byte[] postData = urlParams.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        HttpURLConnection connection = null;

        URL url = getUrl(authorityUrl);

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.write(postData);
            }
            connection.connect();

            return MSIToken.fromJson(JsonProviders.createReader(connection.getInputStream()));
        } catch (IOException exception) {
            if (connection == null) {
                throw LOGGER.logExceptionAsError(
                    new RuntimeException("Could not connect to the authority host: " + url + ".", exception));
            }
            int responseCode;
            try {
                responseCode = connection.getResponseCode();
            } catch (Exception e) {
                throw LoggingUtil
                    .logCredentialUnavailableException(LOGGER, options,
                        new CredentialUnavailableException(
                            "WorkloadIdentityCredential authentication unavailable. "
                                + "Connection to the authority host cannot be established, " + e.getMessage() + ".",
                            e));
            }
            if (responseCode == 400) {
                throw LoggingUtil.logCredentialUnavailableException(LOGGER, options,
                    new CredentialUnavailableException("WorkloadIdentityCredential authentication unavailable. "
                        + "The request to the authority host was invalid. " + "Additional details: "
                        + exception.getMessage() + ".", exception));
            }

            throw LOGGER.logExceptionAsError(
                new RuntimeException("Couldn't acquire access token from Workload Identity.", exception));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    String getSafeWorkingDirectory() {
        if (isWindowsPlatform()) {
            String windowsSystemRoot = System.getenv("SystemRoot");
            if (CoreUtils.isNullOrEmpty(windowsSystemRoot)) {
                return null;
            }

            return windowsSystemRoot + "\\system32";
        } else {
            return DEFAULT_MAC_LINUX_PATH;
        }
    }

    String redactInfo(String input) {
        return ACCESS_TOKEN_PATTERN.matcher(input).replaceAll("****");
    }

    HttpPipeline setupPipeline() {
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");

        Configuration buildConfiguration = Configuration.getGlobalConfiguration().clone();

        HttpLogOptions httpLogOptions
            = (options.getHttpLogOptions() == null) ? new HttpLogOptions() : options.getHttpLogOptions();

        ClientOptions localClientOptions
            = options.getClientOptions() != null ? options.getClientOptions() : DEFAULT_CLIENT_OPTIONS;

        userAgent = UserAgentUtil.toUserAgentString(CoreUtils.getApplicationId(localClientOptions, httpLogOptions),
            clientName, clientVersion, buildConfiguration);
        policies.add(new UserAgentPolicy(userAgent));

        List<HttpHeader> httpHeaderList = new ArrayList<>();
        localClientOptions.getHeaders()
            .forEach(header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));
        policies.add(new AddHeadersPolicy(new HttpHeaders(httpHeaderList)));

        policies.addAll(options.getPerCallPolicies());
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        // Add retry policy.
        RetryPolicy retryPolicy = options.getRetryPolicy();
        if (retryPolicy == null && options.getUseImdsRetryStrategy()) {
            retryPolicy = new RetryPolicy(new ImdsRetryStrategy());
        }
        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, options.getRetryOptions()));
        policies.addAll(options.getPerRetryPolicies());
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        // if the user has not supplied an httpClient, the builder will create a default using localClientOptions.
        // If the user has supplied an HttpClient, it will be used as is.
        return new HttpPipelineBuilder().httpClient(options.getHttpClient())
            .clientOptions(localClientOptions)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    void initializeHttpPipelineAdapter() {
        if (options.getProxyOptions() == null) {
            httpPipelineAdapter = new HttpPipelineAdapter(getPipeline(), options);
        }
    }

    HttpPipeline getPipeline() {

        // if we've already initialized, return the pipeline
        if (this.httpPipeline != null) {
            return httpPipeline;
        }

        // if the user has supplied a pipeline, use it
        HttpPipeline httpPipeline = options.getHttpPipeline();
        if (httpPipeline != null) {
            this.httpPipeline = httpPipeline;
            return this.httpPipeline;
        }

        // setupPipeline will use the user's HttpClient and HttpClientOptions if they're set
        // otherwise it will use defaults.
        this.httpPipeline = setupPipeline();
        return this.httpPipeline;
    }

    String buildClaimsChallengeErrorMessage(TokenRequestContext request) {
        StringBuilder azLoginCommand = new StringBuilder("az login --claims-challenge ");

        // Use IdentityUtil.ensureBase64Encoded and then escape for shell safety
        String encodedClaims = IdentityUtil.ensureBase64Encoded(request.getClaims());
        String escapedClaims = shellEscape(encodedClaims);
        azLoginCommand.append("\"").append(escapedClaims).append("\"");

        // Add tenant if available
        String tenant = IdentityUtil.resolveTenantId(tenantId, request, options);
        if (!CoreUtils.isNullOrEmpty(tenant) && !tenant.equals(IdentityUtil.DEFAULT_TENANT)) {
            azLoginCommand.append(" --tenant ").append(shellEscape(tenant));
        }

        // Add scopes if available
        if (request.getScopes() != null && !request.getScopes().isEmpty()) {
            azLoginCommand.append(" --scope");
            for (String scope : request.getScopes()) {
                azLoginCommand.append(" ").append(shellEscape(scope));
            }
        }

        return String.format(
            "Failed to get token. Claims challenges are not supported by AzureCliCredential. Run %s to handle the claims challenge.",
            azLoginCommand.toString());
    }

    /**
     * Properly escape a string for shell command usage.
     */
    String shellEscape(String input) {
        if (input == null) {
            return "";
        }

        return input.replace("\\", "\\\\")    // Escape backslashes first
            .replace("\"", "\\\"")     // Escape double quotes
            .replace("'", "\\'")       // Escape single quotes
            .replace("`", "\\`")       // Escape backticks
            .replace("$", "\\$")       // Escape dollar signs
            .replace(";", "\\;")       // Escape semicolons
            .replace("&", "\\&")       // Escape ampersands
            .replace("|", "\\|")       // Escape pipes
            .replace("<", "\\<")       // Escape input redirection
            .replace(">", "\\>");      // Escape output redirection
    }

    private byte[] getCertificateBytes() throws IOException {
        if (certificatePath != null) {
            return Files.readAllBytes(Paths.get(certificatePath));
        } else if (certificate != null) {
            return certificate;
        } else {
            return new byte[0];
        }
    }

    private InputStream getCertificateInputStream() throws IOException {
        if (certificatePath != null) {
            return new BufferedInputStream(new FileInputStream(certificatePath));
        } else {
            return new ByteArrayInputStream(certificate);
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

    static String urlEncode(String value) throws IOException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    static URL getUrl(String uri) throws MalformedURLException {
        return new URL(uri);
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

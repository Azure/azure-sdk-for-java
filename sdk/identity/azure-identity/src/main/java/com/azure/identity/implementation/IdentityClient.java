// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.implementation.util.CertificateUtil;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
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
    private final ClientLogger logger = new ClientLogger(IdentityClient.class);

    private final IdentityClientOptions options;
    private final PublicClientApplication publicClientApplication;
    private final String tenantId;
    private final String clientId;
    private HttpPipelineAdapter httpPipelineAdapter;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param tenantId the tenant ID of the application.
     * @param clientId the client ID of the application.
     * @param options the options configuring the client.
     */
    IdentityClient(String tenantId, String clientId, IdentityClientOptions options) {
        if (tenantId == null) {
            tenantId = "common";
        }
        if (options == null) {
            options = new IdentityClientOptions();
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.options = options;
        if (clientId == null) {
            this.publicClientApplication = null;
        } else {
            String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/organizations/" + tenantId;
            PublicClientApplication.Builder publicClientApplicationBuilder = PublicClientApplication.builder(clientId);
            try {
                publicClientApplicationBuilder = publicClientApplicationBuilder.authority(authorityUrl);
            } catch (MalformedURLException e) {
                throw logger.logExceptionAsWarning(new IllegalStateException(e));
            }

            // If user supplies the pipeline, then it should override all other properties
            // as they should directly be set on the pipeline.
            HttpPipeline httpPipeline = options.getHttpPipeline();
            if (httpPipeline != null) {
                httpPipelineAdapter = new HttpPipelineAdapter(httpPipeline);
                publicClientApplicationBuilder.httpClient(httpPipelineAdapter);
            } else {
                // If http client is set on the credential, then it should override the proxy options if any configured.
                HttpClient httpClient = options.getHttpClient();
                if (httpClient != null) {
                    httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(httpClient));
                    publicClientApplicationBuilder.httpClient(httpPipelineAdapter);
                } else if (options.getProxyOptions() != null) {
                    publicClientApplicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.getProxyOptions()));
                } else {
                    //Http Client is null, proxy options are not set, use the default client and build the pipeline.
                    httpPipelineAdapter = new HttpPipelineAdapter(setupPipeline(HttpClient.createDefault()));
                    publicClientApplicationBuilder.httpClient(httpPipelineAdapter);
                }
            }

            if (options.getExecutorService() != null) {
                publicClientApplicationBuilder.executorService(options.getExecutorService());
            }
            this.publicClientApplication = publicClientApplicationBuilder.build();
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
                        new ClientAuthenticationException("Azure CLI not installed", null));
                }
                output.append(line);
            }
            String processOutput = output.toString();

            process.waitFor(10, TimeUnit.SECONDS);

            if (process.exitValue() != 0) {
                if (processOutput.length() > 0) {
                    String redactedOutput = redactInfo("\"accessToken\": \"(.*?)(\"|$)", processOutput);
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
            token = new IdentityToken(accessToken, expiresOn, options);
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
     * @param clientSecret the client secret of the application
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithClientSecret(String clientSecret, TokenRequestContext request) {
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        try {
            ConfidentialClientApplication.Builder applicationBuilder =
                ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
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
                    .build()))
                .map(ar -> new MsalToken(ar, options));
        } catch (MalformedURLException e) {
            return Mono.error(e);
        }
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
     * Asynchronously acquire a token from Active Directory with a PKCS12 certificate.
     *
     * @param pfxCertificatePath the path to the PKCS12 certificate of the application
     * @param pfxCertificatePassword the password protecting the PFX certificate
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithPfxCertificate(String pfxCertificatePath, String pfxCertificatePassword,
                                                            TokenRequestContext request) {
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        return Mono.fromCallable(() -> {
            ConfidentialClientApplication.Builder applicationBuilder =
                    ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromCertificate(
                                new FileInputStream(pfxCertificatePath), pfxCertificatePassword))
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

            return applicationBuilder.build();
        }).flatMap(application -> Mono.fromFuture(application.acquireToken(
                ClientCredentialParameters.builder(new HashSet<>(request.getScopes())).build())))
        .map(ar -> new MsalToken(ar, options));
    }

    /**
     * Asynchronously acquire a token from Active Directory with a PEM certificate.
     *
     * @param pemCertificatePath the path to the PEM certificate of the application
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithPemCertificate(String pemCertificatePath, TokenRequestContext request) {
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        try {
            byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemCertificatePath));
            ConfidentialClientApplication.Builder applicationBuilder =
                ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromCertificate(
                            CertificateUtil.privateKeyFromPem(pemCertificateBytes),
                            CertificateUtil.publicKeyFromPem(pemCertificateBytes)))
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
                    .build()))
                .map(ar -> new MsalToken(ar, options));
        } catch (IOException e) {
            return Mono.error(e);
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
    public Mono<MsalToken> authenticateWithUsernamePassword(TokenRequestContext request,
                                                            String username, String password) {
        return Mono.fromFuture(publicClientApplication.acquireToken(
            UserNamePasswordParameters.builder(new HashSet<>(request.getScopes()), username, password.toCharArray())
                .build()))
            .map(ar -> new MsalToken(ar, options));
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithUserRefreshToken(TokenRequestContext request, MsalToken msalToken) {
        SilentParameters parameters;
        if (msalToken.getAccount() != null) {
            parameters = SilentParameters.builder(new HashSet<>(request.getScopes()), msalToken.getAccount()).build();
        } else {
            parameters = SilentParameters.builder(new HashSet<>(request.getScopes())).build();
        }
        return Mono.defer(() -> {
            try {
                return Mono.fromFuture(publicClientApplication.acquireTokenSilently(parameters))
                        .map(ar -> new MsalToken(ar, options));
            } catch (MalformedURLException e) {
                return Mono.error(e);
            }
        });
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
        return Mono.fromFuture(() -> {
            DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters.builder(new HashSet<>(request.getScopes()),
                dc -> deviceCodeConsumer.accept(new DeviceCodeInfo(dc.userCode(), dc.deviceCode(),
                    dc.verificationUri(), OffsetDateTime.now().plusSeconds(dc.expiresIn()), dc.message()))).build();
            return publicClientApplication.acquireToken(parameters);
        }).map(ar -> new MsalToken(ar, options));
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
        return Mono.fromFuture(() -> publicClientApplication.acquireToken(
            AuthorizationCodeParameters.builder(authorizationCode, redirectUrl)
                .scopes(new HashSet<>(request.getScopes()))
                .build()))
            .map(ar -> new MsalToken(ar, options));
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
        String authorityUrl = options.getAuthorityHost().replaceAll("/+$", "") + "/" + tenantId;
        return AuthorizationCodeListener.create(port)
            .flatMap(server -> {
                URI redirectUri;
                String browserUri;
                try {
                    redirectUri = new URI(String.format("http://localhost:%s", port));
                    browserUri =
                        String.format("%s/oauth2/v2.0/authorize?response_type=code&response_mode=query&prompt"
                                + "=select_account&client_id=%s&redirect_uri=%s&state=%s&scope=%s",
                            authorityUrl,
                            clientId,
                            redirectUri.toString(),
                            UUID.randomUUID(),
                            String.join(" ", request.getScopes()));
                } catch (URISyntaxException e) {
                    return server.dispose().then(Mono.error(e));
                }

                return server.listen()
                    .mergeWith(Mono.<String>fromRunnable(() -> {
                        try {
                            openUrl(browserUri);
                        } catch (IOException e) {
                            throw logger.logExceptionAsError(new IllegalStateException(e));
                        }
                    }).subscribeOn(Schedulers.newSingle("browser")))
                    .next()
                    .flatMap(code -> authenticateWithAuthorizationCode(request, code, redirectUri))
                    .onErrorResume(t -> server.dispose().then(Mono.error(t)))
                    .flatMap(msalToken -> server.dispose().then(Mono.just(msalToken)));
            });
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

                MSIToken msiToken = SERIALIZER_ADAPTER.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
                return new IdentityToken(msiToken.getToken(), msiToken.getExpiresAt(), options);
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

                    MSIToken msiToken = SERIALIZER_ADAPTER.deserialize(result,
                            MSIToken.class, SerializerEncoding.JSON);
                    return new IdentityToken(msiToken.getToken(), msiToken.getExpiresAt(), options);
                } catch (IOException exception) {
                    if (connection == null) {
                        throw logger.logExceptionAsError(new RuntimeException(
                                String.format("Could not connect to the url: %s.", url), exception));
                    }
                    int responseCode = connection.getResponseCode();
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
}

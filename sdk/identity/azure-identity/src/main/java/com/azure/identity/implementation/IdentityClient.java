// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credentials.AccessToken;
import com.azure.core.http.ProxyOptions;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.implementation.util.ScopeUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DeviceCodeChallenge;
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

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The identity client that contains APIs to retrieve access tokens
 * from various configurations.
 */
public class IdentityClient {
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final Random RANDOM = new Random();
    private static final ClientLogger LOGGER = new ClientLogger(IdentityClient.class);

    private final IdentityClientOptions options;
    private final PublicClientApplication publicClientApplication;
    private final String tenantId;
    private final String clientId;

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
            String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/organizations/" + tenantId;
            PublicClientApplication.Builder publicClientApplicationBuilder = PublicClientApplication.builder(clientId);
            try {
                publicClientApplicationBuilder = publicClientApplicationBuilder.authority(authorityUrl);
            } catch (MalformedURLException e) {
                LOGGER.logAndThrow(new RuntimeException(e));
            }
            if (options.proxyOptions() != null) {
                publicClientApplicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.proxyOptions()));
            }
            this.publicClientApplication = publicClientApplicationBuilder.build();
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param clientSecret the client secret of the application
     * @param scopes the scopes to authenticate to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithClientSecret(String clientSecret, String[] scopes) {
        String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + tenantId;
        try {
            ConfidentialClientApplication.Builder applicationBuilder = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.create(clientSecret)).authority(authorityUrl);
            if (options.proxyOptions() != null) {
                applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.proxyOptions()));
            }
            ConfidentialClientApplication application = applicationBuilder.build();
            return Mono.fromFuture(application.acquireToken(ClientCredentialParameters.builder(new HashSet<>(Arrays.asList(scopes))).build()))
                .map(ar -> new AccessToken(ar.accessToken(), OffsetDateTime.ofInstant(ar.expiresOnDate().toInstant(), ZoneOffset.UTC)));
        } catch (MalformedURLException e) {
            return Mono.error(e);
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a PKCS12 certificate.
     *
     * @param pfxCertificatePath the path to the PKCS12 certificate of the application
     * @param pfxCertificatePassword the password protecting the PFX certificate
     * @param scopes the scopes to authenticate to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithPfxCertificate(String pfxCertificatePath, String pfxCertificatePassword, String[] scopes) {
        String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + tenantId;
        try {
            ConfidentialClientApplication.Builder applicationBuilder = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.create(new FileInputStream(pfxCertificatePath), pfxCertificatePassword)).authority(authorityUrl);
            if (options.proxyOptions() != null) {
                applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.proxyOptions()));
            }
            ConfidentialClientApplication application = applicationBuilder.build();
            return Mono.fromFuture(application.acquireToken(ClientCredentialParameters.builder(new HashSet<>(Arrays.asList(scopes))).build()))
                .map(ar -> new AccessToken(ar.accessToken(), OffsetDateTime.ofInstant(ar.expiresOnDate().toInstant(), ZoneOffset.UTC)));
        } catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | NoSuchProviderException | IOException e) {
            return Mono.error(e);
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a PEM certificate.
     *
     * @param pemCertificatePath the path to the PEM certificate of the application
     * @param scopes the scopes to authenticate to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateWithPemCertificate(String pemCertificatePath, String[] scopes) {
        String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + tenantId;
        try {
            byte[] pemCertificateBytes = Files.readAllBytes(Paths.get(pemCertificatePath));
            ConfidentialClientApplication.Builder applicationBuilder = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.create(CertificateUtil.privateKeyFromPem(pemCertificateBytes), CertificateUtil.publicKeyFromPem(pemCertificateBytes))).authority(authorityUrl);
            if (options.proxyOptions() != null) {
                applicationBuilder.proxy(proxyOptionsToJavaNetProxy(options.proxyOptions()));
            }
            ConfidentialClientApplication application = applicationBuilder.build();
            return Mono.fromFuture(application.acquireToken(ClientCredentialParameters.builder(new HashSet<>(Arrays.asList(scopes))).build()))
                .map(ar -> new AccessToken(ar.accessToken(), OffsetDateTime.ofInstant(ar.expiresOnDate().toInstant(), ZoneOffset.UTC)));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with a username and a password.
     *
     * @param scopes the scopes to authenticate to
     * @param username the username of the user
     * @param password the password of the user
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithUsernamePassword(String[] scopes, String username, String password) {
        return Mono.fromFuture(publicClientApplication.acquireToken(UserNamePasswordParameters.builder(new HashSet<>(Arrays.asList(scopes)), username, password.toCharArray()).build()))
            .map(MsalToken::new);
    }

    /**
     * Asynchronously acquire a token from the currently logged in client.
     * @param scopes the scopes to authenticate to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithUserRefreshToken(String[] scopes, MsalToken msalToken) {
        SilentParameters parameters;
        if (msalToken.account() != null) {
            parameters = SilentParameters.builder(new HashSet<>(Arrays.asList(scopes)), msalToken.account()).build();
        } else {
            parameters = SilentParameters.builder(new HashSet<>(Arrays.asList(scopes))).build();
        }
        return Mono.defer(() -> {
            try {
                return Mono.fromFuture(publicClientApplication.acquireTokenSilently(parameters)).map(MsalToken::new);
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
     * @param scopes the scopes to authenticate to
     * @param deviceCodeConsumer the user provided closure that will consume the device code challenge
     * @return a Publisher that emits an AccessToken when the device challenge is met, or an exception if the device code expires
     */
    public Mono<MsalToken> authenticateWithDeviceCode(String[] scopes, Consumer<DeviceCodeChallenge> deviceCodeConsumer) {
        return Mono.fromFuture(() -> {
            DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters.builder(new HashSet<>(Arrays.asList(scopes)),
                dc -> deviceCodeConsumer.accept(new DeviceCodeChallenge(dc.userCode(), dc.deviceCode(), dc.verificationUri(), dc.expiresIn(), dc.interval(), dc.message()))).build();
            return publicClientApplication.acquireToken(parameters);
        }).map(MsalToken::new);
    }

    /**
     * Asynchronously acquire a token from Active Directory with an authorization code from an oauth flow.
     *
     * @param scopes the scopes to authenticate to
     * @param authorizationCode the oauth2 authorization code
     * @param redirectUri the redirectUri where the authorization code is sent to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithAuthorizationCode(String[] scopes, String authorizationCode, URI redirectUri) {
        return Mono.fromFuture(() -> publicClientApplication.acquireToken(AuthorizationCodeParameters.builder(authorizationCode, redirectUri).scopes(new HashSet<>(Arrays.asList(scopes))).build())).map(MsalToken::new);
    }

    /**
     * Asynchronously acquire a token from Active Directory by opening a browser and wait for the user to login. The
     * credential will run a minimal local HttpServer at the given port, so {@code http://localhost:{port}} must be
     * listed as a valid reply URL for the application.
     *
     * @param scopes the scopes to authenticate to
     * @param port the port on which the HTTP server is listening
     * @return a Publisher that emits an AccessToken
     */
    public Mono<MsalToken> authenticateWithBrowserInteraction(String[] scopes, int port) {
        String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + "common";
        return AuthorizationCodeListener.create(port)
            .flatMap(server -> {
                URI redirectUri;
                URI browserUri;
                try {
                    redirectUri = new URI(String.format("http://localhost:%s", port));
                    browserUri = new URI(String.format("%s/oauth2/v2.0/authorize?response_type=code&response_mode=query&prompt=select_account&client_id=%s&redirect_uri=%s&state=%s&scope=%s",
                        authorityUrl, clientId, redirectUri.toString(), UUID.randomUUID(), String.join(" ", scopes)));
                } catch (URISyntaxException e) {
                    return server.dispose().then(Mono.error(e));
                }

                return server.listen()
                    .mergeWith(Mono.<String>fromRunnable(() -> {
                        try {
                            Desktop.getDesktop().browse(browserUri);
                        } catch (IOException e) {
                            LOGGER.logAndThrow(new RuntimeException(e));
                        }
                    }).subscribeOn(Schedulers.newSingle("browser")))
                    .next()
                    .flatMap(code -> authenticateWithAuthorizationCode(scopes, code, redirectUri))
                    .onErrorResume(t -> server.dispose().then(Mono.error(t)))
                    .flatMap(msalToken -> server.dispose().then(Mono.just(msalToken)));
            });
    }

    /**
     * Asynchronously acquire a token from the App Service Managed Service Identity endpoint.
     *
     * @param msiEndpoint the endpoint to acquire token from
     * @param msiSecret the secret to acquire token with
     * @param scopes the scopes to authenticate to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToManagedIdentityEndpoint(String msiEndpoint, String msiSecret, String[] scopes) {
        String resource = ScopeUtil.scopesToResource(scopes);
        HttpURLConnection connection = null;
        StringBuilder payload = new StringBuilder();

        try {
            payload.append("resource=");
            payload.append(URLEncoder.encode(resource, "UTF-8"));
            payload.append("&api-version=");
            payload.append(URLEncoder.encode("2017-09-01", "UTF-8"));
            if (clientId != null) {
                payload.append("&client_id=");
                payload.append(URLEncoder.encode(clientId, "UTF-8"));
            }
        } catch (IOException exception) {
            return Mono.error(exception);
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

            Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            return Mono.just(SERIALIZER_ADAPTER.deserialize(result, MSIToken.class, SerializerEncoding.JSON));
        } catch (IOException e) {
            return Mono.error(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Asynchronously acquire a token from the Virtual Machine IMDS endpoint.
     *
     * @param scopes the scopes to authenticate to
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticateToIMDSEndpoint(String[] scopes) {
        String resource = ScopeUtil.scopesToResource(scopes);
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

        int retry = 1;
        while (retry <= options.maxRetry()) {
            URL url = null;
            HttpURLConnection connection = null;
            try {
                url = new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s", payload.toString()));

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Metadata", "true");
                connection.connect();

                Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                String result = s.hasNext() ? s.next() : "";

                return Mono.just(SERIALIZER_ADAPTER.deserialize(result, MSIToken.class, SerializerEncoding.JSON));
            } catch (IOException exception) {
                if (connection == null) {
                    return Mono.error(new RuntimeException(String.format("Could not connect to the url: %s.", url), exception));
                }
                int responseCode = 0;
                try {
                    responseCode = connection.getResponseCode();
                } catch (IOException e) {
                    return Mono.error(e);
                }
                if (responseCode == 410 || responseCode == 429 || responseCode == 404 || (responseCode >= 500 && responseCode <= 599)) {
                    int retryTimeoutInMs = options.retryTimeout().apply(RANDOM.nextInt(retry));
                    // Error code 410 indicates IMDS upgrade is in progress, which can take up to 70s
                    //
                    retryTimeoutInMs = (responseCode == 410 && retryTimeoutInMs < imdsUpgradeTimeInMs) ? imdsUpgradeTimeInMs : retryTimeoutInMs;
                    retry++;
                    if (retry > options.maxRetry()) {
                        break;
                    } else {
                        sleep(retryTimeoutInMs);
                    }
                } else {
                    return Mono.error(new RuntimeException("Couldn't acquire access token from IMDS, verify your objectId, clientId or msiResourceId", exception));
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return Mono.error(new RuntimeException(String.format("MSI: Failed to acquire tokens after retrying %s times", options.maxRetry())));
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            LOGGER.logAndThrow(new RuntimeException(ex));
        }
    }

    private static Proxy proxyOptionsToJavaNetProxy(ProxyOptions options) {
        switch (options.type()) {
            case SOCKS4:
            case SOCKS5:
                return new Proxy(Type.SOCKS, options.address());
            case HTTP:
            default:
                return new Proxy(Type.HTTP, options.address());
        }
    }
}

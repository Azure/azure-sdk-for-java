// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.identity.implementation.MSIToken;
import com.azure.identity.implementation.RefreshableTokenCache;
import com.azure.identity.implementation.util.Adal4jUtil;
import com.azure.identity.implementation.util.ScopeUtil;
import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * The identity client that contains APIs to retrieve access tokens
 * from various configurations.
 */
public final class IdentityClient {
    private final IdentityClientOptions options;

    /**
     * Creates an IdentityClient with default options.
     */
    public IdentityClient() {
        this.options = new IdentityClientOptions();
    }

    /**
     * Creates an IdentityClient with the given options.
     * @param options the options configuring the client.
     */
    public IdentityClient(IdentityClientOptions options) {
        this.options = options;
    }

    /**
     * @return the APIs to acquire tokens in Active Directory.
     */
    public ActiveDirectoryClient activeDirectory() {
        return new ActiveDirectoryClient(options);
    }

    /**
     * @return the APIs to acquire tokens with the managed service identity in Azure.
     */
    public ManagedIdentityClient managedIdentityClient() {
        return new ManagedIdentityClient(options);
    }

    /**
     * The identity client that contains APIs to get tokens from Active Directory.
     */
    public static final class ActiveDirectoryClient {
        private IdentityClientOptions options;
        private ActiveDirectoryClient(IdentityClientOptions options) {
            this.options = options;
        }

        /**
         * Asynchronously acquire a token from Active Directory with a client secret.
         * @param tenantId the tenant ID of the application
         * @param clientId the client ID of the application
         * @param clientSecret the client secret of the application
         * @param scopes the scopes to authenticate to
         * @return a Publisher that emits an AccessToken
         */
        public Mono<AccessToken> authenticateWithClientSecret(String tenantId, String clientId, String clientSecret, String[] scopes) {
            String resource = ScopeUtil.scopesToResource(scopes);
            return new RefreshableTokenCache(res -> {
                String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + tenantId;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                AuthenticationContext context = createAuthenticationContext(executor, authorityUrl, options.proxyOptions());
                return Mono.create((Consumer<MonoSink<AuthenticationResult>>) callback -> {
                    context.acquireToken(
                        res,
                        new ClientCredential(clientId, clientSecret),
                        Adal4jUtil.authenticationDelegate(callback));
                }).map(ar -> new AccessToken().token(ar.getAccessToken()).expiresOn(OffsetDateTime.from(ar.getExpiresOnDate().toInstant())))
                    .doFinally(s -> executor.shutdown());
            }).getToken(resource);
        }

        /**
         * Asynchronously acquire a token from Active Directory with a PKCS12 certificate.
         * @param tenantId the tenant ID of the application
         * @param clientId the client ID of the application
         * @param pfxCertificatePath the path to the PKCS12 certificate of the application
         * @param pfxCertificatePassword the password protecting the PFX certificate
         * @param scopes the scopes to authenticate to
         * @return a Publisher that emits an AccessToken
         */
        public Mono<AccessToken> authenticateWithPfxCertificate(String tenantId, String clientId, String pfxCertificatePath, String pfxCertificatePassword, String[] scopes) {
            String resource = ScopeUtil.scopesToResource(scopes);
            return new RefreshableTokenCache(res -> {
                String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + tenantId;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                AuthenticationContext context = createAuthenticationContext(executor, authorityUrl, options.proxyOptions());
                return Mono.create((Consumer<MonoSink<AuthenticationResult>>) callback -> {
                    try {
                        context.acquireToken(
                            res,
                            Adal4jUtil.createAsymmetricKeyCredential(clientId, Files.readAllBytes(Paths.get(pfxCertificatePath)), pfxCertificatePassword),
                            Adal4jUtil.authenticationDelegate(callback));
                    } catch (IOException e) {
                        callback.error(e);
                    }
                }).map(ar -> new AccessToken().token(ar.getAccessToken()).expiresOn(OffsetDateTime.from(ar.getExpiresOnDate().toInstant())))
                    .doFinally(s -> executor.shutdown());
            }).getToken(resource);
        }

        /**
         * Asynchronously acquire a token from Active Directory with a PEM certificate.
         * @param tenantId the tenant ID of the application
         * @param clientId the client ID of the application
         * @param pemCertificatePath the path to the PEM certificate of the application
         * @param scopes the scopes to authenticate to
         * @return a Publisher that emits an AccessToken
         */
        public Mono<AccessToken> authenticateWithPemCertificate(String tenantId, String clientId, String pemCertificatePath, String[] scopes) {
            String resource = ScopeUtil.scopesToResource(scopes);
            return new RefreshableTokenCache(res -> {
                String authorityUrl = options.authorityHost().replaceAll("/+$", "") + "/" + tenantId;
                ExecutorService executor = Executors.newSingleThreadExecutor();
                AuthenticationContext context = createAuthenticationContext(executor, authorityUrl, options.proxyOptions());
                return Mono.create((Consumer<MonoSink<AuthenticationResult>>) callback -> {
                    try {
                        context.acquireToken(
                            res,
                            AsymmetricKeyCredential.create(clientId, Adal4jUtil.privateKeyFromPem(Files.readAllBytes(Paths.get(pemCertificatePath))), Adal4jUtil.publicKeyFromPem(Files.readAllBytes(Paths.get(pemCertificatePath)))),
                            Adal4jUtil.authenticationDelegate(callback));
                    } catch (IOException e) {
                        callback.error(e);
                    }
                }).map(ar -> new AccessToken().token(ar.getAccessToken()).expiresOn(OffsetDateTime.from(ar.getExpiresOnDate().toInstant())))
                    .doFinally(s -> executor.shutdown());
            }).getToken(resource);
        }

        private static AuthenticationContext createAuthenticationContext(ExecutorService executor, String authorityUrl, ProxyOptions proxyOptions) {
            AuthenticationContext context;
            try {
                context = new AuthenticationContext(authorityUrl, false, executor);
            } catch (MalformedURLException mue) {
                throw Exceptions.propagate(mue);
            }
            if (proxyOptions != null) {
                context.setProxy(new Proxy(proxyOptions.type() == Type.HTTP ? Proxy.Type.HTTP : Proxy.Type.SOCKS, proxyOptions.address()));
            }
            return context;
        }
    }

    /**
     * The identity client that contains APIs to get tokens with Managed Service Identity in Azure.
     */
    public static final class ManagedIdentityClient {
        private IdentityClientOptions options;
        private SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();
        private static final Random RANDOM = new Random();

        private ManagedIdentityClient(IdentityClientOptions options) {
            this.options = options;
        }

        /**
         * Asynchronously acquire a token from the App Service Managed Service Identity endpoint.
         * @param msiEndpoint the endpoint to acquire token from
         * @param msiSecret the secret to acquire token with
         * @param scopes the scopes to authenticate to
         * @return a Publisher that emits an AccessToken
         */
        public Mono<AccessToken> authenticateToManagedIdentityEnpoint(String msiEndpoint, String msiSecret, String[] scopes) {
            String resource = ScopeUtil.scopesToResource(scopes);
            return new RefreshableTokenCache(res -> Mono.fromCallable(() -> {
                HttpURLConnection connection = null;
                try {
                    String urlString = String.format("%s?resource=%s&api-version=2017-09-01", msiEndpoint, res);
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Secret", msiSecret);
                    connection.setRequestProperty("Metadata", "true");

                    connection.connect();

                    Scanner s = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";

                    return adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            })).getToken(resource);
        }

        /**
         * Asynchronously acquire a token from the Virtual Machine IMDS endpoint.
         * @param clientId the client ID of the virtual machine
         * @param objectId the object ID of the virtual machine
         * @param identityId the identity ID of the virtual machine
         * @param scopes the scopes to authenticate to
         * @return a Publisher that emits an AccessToken
         */
        public Mono<AccessToken> authenticateToIMDSEndpoint(String clientId, String objectId, String identityId, String[] scopes) {
            String resource = ScopeUtil.scopesToResource(scopes);
            return new RefreshableTokenCache(tokenAudience -> Mono.defer(() -> {
                StringBuilder payload = new StringBuilder();
                final int imdsUpgradeTimeInMs = 70 * 1000;

                try {
                    payload.append("api-version");
                    payload.append("=");
                    payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
                    payload.append("&");
                    payload.append("resource");
                    payload.append("=");
                    payload.append(URLEncoder.encode(tokenAudience, "UTF-8"));
                    if (objectId != null) {
                        payload.append("&");
                        payload.append("object_id");
                        payload.append("=");
                        payload.append(URLEncoder.encode(objectId, "UTF-8"));
                    } else if (clientId != null) {
                        payload.append("&");
                        payload.append("client_id");
                        payload.append("=");
                        payload.append(URLEncoder.encode(clientId, "UTF-8"));
                    } else if (identityId != null) {
                        payload.append("&");
                        payload.append("msi_res_id");
                        payload.append("=");
                        payload.append(URLEncoder.encode(identityId, "UTF-8"));
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

                        return Mono.just(adapter.deserialize(result, MSIToken.class, SerializerEncoding.JSON));
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
                //
                if (retry > options.maxRetry()) {
                    return Mono.error(new RuntimeException(String.format("MSI: Failed to acquire tokens after retrying %s times", options.maxRetry())));
                }
                return null;
            })).getToken(resource);
        }

        private static void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.azure.common.AzureEnvironment;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class ApplicationTokenCredentials extends AzureTokenCredentials {
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AuthenticationResult> tokens;
    /** The active directory application client id. */
    private String clientId;
    /** The authentication secret for the application. */
    private String clientSecret;
    /** The PKCS12 certificate byte array. */
    private byte[] clientCertificate;
    /** The certificate password. */
    private String clientCertificatePassword;

    /**
     * Initializes a new instance of the ApplicationTokenCredentials.
     *
     * @param clientId the active directory application client id.
     * @param domain the domain or tenant id containing this application.
     * @param secret the authentication secret for the application.
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public ApplicationTokenCredentials(String clientId, String domain, String secret, AzureEnvironment environment) {
        super(environment, domain); // defer token acquisition
        this.clientId = clientId;
        this.clientSecret = secret;
        this.tokens = new HashMap<>();
    }

    /**
     * Initializes a new instance of the ApplicationTokenCredentials.
     *
     * @param clientId the active directory application client id.
     * @param domain the domain or tenant id containing this application.
     * @param certificate the PKCS12 certificate file content
     * @param password the password to the certificate file
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public ApplicationTokenCredentials(String clientId, String domain, byte[] certificate, String password, AzureEnvironment environment) {
        super(environment, domain);
        this.clientId = clientId;
        this.clientCertificate = certificate;
        this.clientCertificatePassword = password;
        this.tokens = new HashMap<>();
    }

    /**
     * Initializes the credentials based on the provided credentials file.
     *
     * @param credentialsFile A  file with credentials, using the standard Java properties format.
     * and the following keys:
     *     subscription=&lt;subscription-id&gt;
     *     tenant=&lt;tenant-id&gt;
     *     client=&lt;client-id&gt;
     *     key=&lt;client-key&gt;
     *     managementURI=&lt;management-URI&gt;
     *     baseURL=&lt;base-URL&gt;
     *     authURL=&lt;authentication-URL&gt;
     * or a JSON format and the following keys
     * {
     *     "clientId": "&lt;client-id&gt;",
     *     "clientSecret": "&lt;client-key&gt;",
     *     "subscriptionId": "&lt;subscription-id&gt;",
     *     "tenantId": "&lt;tenant-id&gt;",
     * }
     * and any custom endpoints listed in {@link AzureEnvironment}.
     *
     * @return The credentials based on the file.
     * @throws IOException exception thrown from file access errors.
     */
    public static ApplicationTokenCredentials fromFile(File credentialsFile) throws IOException {
        return AuthFile.parse(credentialsFile).generateCredentials();
    }

    /**
     * Gets the active directory application client id.
     *
     * @return the active directory application client id.
     */
    public String clientId() {
        return clientId;
    }

    String clientSecret() {
        return clientSecret;
    }

    byte[] clientCertificate() {
        return clientCertificate;
    }

    String clientCertificatePassword() {
        return clientCertificatePassword;
    }

    @Override
    public synchronized Mono<String> getToken(String resource) {
        AuthenticationResult authenticationResult = tokens.get(resource);
        if (authenticationResult != null && authenticationResult.getExpiresOnDate().after(new Date())) {
            return Mono.just(authenticationResult.getAccessToken());
        } else {
            return acquireAccessToken(resource)
                    .map(ar -> {
                        tokens.put(resource, ar);
                        return ar.getAccessToken();
                    });
        }
    }

    private Mono<AuthenticationResult> acquireAccessToken(String resource) {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context;
        try {
            context = new AuthenticationContext(authorityUrl, false, executor);
        } catch (MalformedURLException mue) {
            executor.shutdown();
            throw Exceptions.propagate(mue);
        }
        if (proxy() != null) {
            context.setProxy(proxy());
        }
        Mono<AuthenticationResult> authMono;
        if (clientSecret != null) {
            authMono = Mono.create(callback -> {
                context.acquireToken(
                        resource,
                        new ClientCredential(this.clientId(), clientSecret),
                        Util.authenticationDelegate(callback));
            });
        } else if (clientCertificate != null && clientCertificatePassword != null) {
            authMono = Mono.create(callback -> {
                AsymmetricKeyCredential keyCredential = Util.createAsymmetricKeyCredential(clientId, clientCertificate, clientCertificatePassword);
                context.acquireToken(
                        resource,
                        keyCredential,
                        Util.authenticationDelegate(callback));
            });
        } else if (clientCertificate != null) {
            AsymmetricKeyCredential keyCredential = AsymmetricKeyCredential.create(clientId(), Util.privateKeyFromPem(new String(clientCertificate)), Util.publicKeyFromPem(new String(clientCertificate)));
            authMono = Mono.create(callback -> {
                context.acquireToken(
                        resource,
                        keyCredential,
                        Util.authenticationDelegate(callback));
            });
        } else {
            authMono = Mono.error(new AuthenticationException("Please provide either a non-null secret or a non-null certificate."));
        }
        return authMono.doFinally(s -> executor.shutdown());
    }
}

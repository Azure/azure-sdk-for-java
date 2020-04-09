// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * This class describes the credential for service principal.
 */
public class ApplicationTokenCredential extends AzureTokenCredential {

    private final ConcurrentMap<String, SimpleTokenCache> cache = new ConcurrentHashMap<>();

    private final String clientId;
    private final String clientSecret;
    private final byte[] clientCertificate;
    private final String clientCertificatePassword;
    private final ClientSecretCredential clientSecretCredential;

    /**
     * Initializes a new instance of the ApplicationTokenCredentials.
     *
     * @param clientId the active directory application client id. Also known as
     *                 Application Id which Identifies the application that is using the token.
     * @param domain the domain or tenant id containing this application.
     * @param secret the authentication secret for the application.
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public ApplicationTokenCredential(String clientId, String domain, String secret, AzureEnvironment environment) {
        super(environment, domain);
        this.clientId = clientId;
        this.clientSecret = secret;
        this.clientCertificate = null;
        this.clientCertificatePassword = null;
        this.clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(this.getClientId())
                .clientSecret(this.getClientSecret())
                .tenantId(getDomain())
                .build();
    }

    /**
     * Initializes a new instance of the ApplicationTokenCredentials.
     *
     * @param clientId the active directory application client id. Also known as
     *                 Application Id which Identifies the application that is using the token.
     * @param domain the domain or tenant id containing this application.
     * @param certificate the PKCS12 certificate file content
     * @param password the password to the certificate file
     * @param environment the Azure environment to authenticate with.
     *                    If null is provided, AzureEnvironment.AZURE will be used.
     */
    public ApplicationTokenCredential(String clientId, String domain, byte[] certificate,
                                      String password, AzureEnvironment environment) {
        super(environment, domain);
        this.clientId = clientId;
        this.clientSecret = null;
        this.clientCertificate = certificate;
        this.clientCertificatePassword = password;
        this.clientSecretCredential = null;
    }

    /**
     * Initializes the credentials based on the provided credentials file.
     *
     * @param credentialFile A  file with credentials, using the standard Java properties format.
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
    public static ApplicationTokenCredential fromFile(File credentialFile) throws IOException {
        return AuthFile.parse(credentialFile).generateCredential();
    }

    /**
     * Gets the active directory application client id. Also known as
     * Application Id which Identifies the application that is using the token.
     *
     * @return the active directory application client id.
     */
    public String getClientId() {
        return this.clientId;
    }

    String getClientSecret() {
        return this.clientSecret;
    }

    byte[] getClientCertificate() {
        return this.clientCertificate;
    }

    String getClientCertificatePassword() {
        return this.clientCertificatePassword;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        // TODO: Add client certificate token
        List<String> scopes = request.getScopes();
        String digest = String.join(" ", scopes);

        Function<String, SimpleTokenCache> computeSimpleTokenCache = key ->
                new SimpleTokenCache(() -> clientSecretCredential.getToken(request));

        return Mono.just(cache.computeIfAbsent(digest, computeSimpleTokenCache))
                .flatMap(simpleTokenCache -> simpleTokenCache.getToken());
    }
}

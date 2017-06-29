/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.credentials;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.AzureEnvironment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
    private String secret;
    /** The PKCS12 certificate byte array. */
    private byte[] certificate;
    /** The certificate password. */
    private String certPassword;
    /** The default subscription to use, if any. */
    private String defaultSubscription;

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
        this.secret = secret;
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
        this.certificate = certificate;
        this.certPassword = password;
        this.tokens = new HashMap<>();
    }

    /**
     * Contains the keys of the settings in a Properties file to read credentials from.
     */
    private enum CredentialSettings {
        /** The subscription GUID. */
        SUBSCRIPTION_ID("subscription"),
        /** The tenant GUID or domain. */
        TENANT_ID("tenant"),
        /** The client id for the client application. */
        CLIENT_ID("client"),
        /** The client secret for the service principal. */
        CLIENT_KEY("key"),
        /** The client certificate for the service principal. */
        CLIENT_CERT("certificate"),
        /** The password for the client certificate for the service principal. */
        CLIENT_CERT_PASS("certificatePassword"),
        /** The management endpoint. */
        MANAGEMENT_URI("managementURI"),
        /** The base URL to the current Azure environment. */
        BASE_URL("baseURL"),
        /** The URL to Active Directory authentication. */
        AUTH_URL("authURL"),
        /** The URL to Active Directory Graph. */
        GRAPH_URL("graphURL"),
        /** The suffix of Key Vaults. */
        VAULT_SUFFIX("vaultSuffix");

        /** The name of the key in the properties file. */
        private final String name;

        CredentialSettings(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * @return The default subscription ID, if any
     */
    public String defaultSubscriptionId() {
        return defaultSubscription;
    }

    /**
     * Set default subscription ID.
     *
     * @param subscriptionId the default subscription ID.
     * @return the credentials object itself.
     */
    public ApplicationTokenCredentials withDefaultSubscriptionId(String subscriptionId) {
        this.defaultSubscription = subscriptionId;
        return this;
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
     *
     * @return The credentials based on the file.
     * @throws IOException exception thrown from file access errors.
     */
    public static ApplicationTokenCredentials fromFile(File credentialsFile) throws IOException {
        // Set defaults
        Properties authSettings = new Properties();
        authSettings.put(CredentialSettings.AUTH_URL.toString(), AzureEnvironment.AZURE.activeDirectoryEndpoint());
        authSettings.put(CredentialSettings.BASE_URL.toString(), AzureEnvironment.AZURE.resourceManagerEndpoint());
        authSettings.put(CredentialSettings.MANAGEMENT_URI.toString(), AzureEnvironment.AZURE.managementEndpoint());
        authSettings.put(CredentialSettings.GRAPH_URL.toString(), AzureEnvironment.AZURE.graphEndpoint());
        authSettings.put(CredentialSettings.VAULT_SUFFIX.toString(), AzureEnvironment.AZURE.keyVaultDnsSuffix());

        // Load the credentials from the file
        FileInputStream credentialsFileStream = new FileInputStream(credentialsFile);
        authSettings.load(credentialsFileStream);
        credentialsFileStream.close();

        final String clientId = authSettings.getProperty(CredentialSettings.CLIENT_ID.toString());
        final String tenantId = authSettings.getProperty(CredentialSettings.TENANT_ID.toString());
        final String clientKey = authSettings.getProperty(CredentialSettings.CLIENT_KEY.toString());
        final String certificate = authSettings.getProperty(CredentialSettings.CLIENT_CERT.toString());
        final String certPasswrod = authSettings.getProperty(CredentialSettings.CLIENT_CERT_PASS.toString());
        final String mgmtUri = authSettings.getProperty(CredentialSettings.MANAGEMENT_URI.toString());
        final String authUrl = authSettings.getProperty(CredentialSettings.AUTH_URL.toString());
        final String baseUrl = authSettings.getProperty(CredentialSettings.BASE_URL.toString());
        final String graphUrl = authSettings.getProperty(CredentialSettings.GRAPH_URL.toString());
        final String vaultSuffix = authSettings.getProperty(CredentialSettings.VAULT_SUFFIX.toString());
        final String defaultSubscriptionId = authSettings.getProperty(CredentialSettings.SUBSCRIPTION_ID.toString());

        if (clientKey != null) {
            return new ApplicationTokenCredentials(
                    clientId,
                    tenantId,
                    clientKey,
                    new AzureEnvironment(new HashMap<String, String>() {{
                        put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.toString(), authUrl.endsWith("/") ? authUrl : authUrl + "/");
                        put(AzureEnvironment.Endpoint.MANAGEMENT.toString(), mgmtUri);
                        put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.toString(), baseUrl);
                        put(AzureEnvironment.Endpoint.GRAPH.toString(), graphUrl);
                        put(AzureEnvironment.Endpoint.KEYVAULT.toString(), vaultSuffix);
                    }}
                    )).withDefaultSubscriptionId(defaultSubscriptionId);
        } else if (certificate != null) {
            byte[] certs;
            if (new File(certificate).exists()) {
                certs = Files.readAllBytes(Paths.get(certificate));
            } else {
                certs = Files.readAllBytes(Paths.get(credentialsFile.getParent(), certificate));
            }
            return new ApplicationTokenCredentials(
                    clientId,
                    tenantId,
                    certs,
                    certPasswrod,
                    new AzureEnvironment(new HashMap<String, String>() {{
                        put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.toString(), authUrl);
                        put(AzureEnvironment.Endpoint.MANAGEMENT.toString(), mgmtUri);
                        put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.toString(), baseUrl);
                        put(AzureEnvironment.Endpoint.GRAPH.toString(), graphUrl);
                        put(AzureEnvironment.Endpoint.KEYVAULT.toString(), vaultSuffix);
                    }})).withDefaultSubscriptionId(defaultSubscriptionId);
        } else {
            throw new IllegalArgumentException("Please specify either a client key or a client certificate.");
        }
    }

    /**
     * Gets the active directory application client id.
     *
     * @return the active directory application client id.
     */
    public String clientId() {
        return clientId;
    }

    @Override
    public synchronized String getToken(String resource) throws IOException {
        AuthenticationResult authenticationResult = tokens.get(resource);
        if (authenticationResult == null || authenticationResult.getExpiresOnDate().before(new Date())) {
            authenticationResult = acquireAccessToken(resource);
        }
        tokens.put(resource, authenticationResult);
        return authenticationResult.getAccessToken();
    }

    private AuthenticationResult acquireAccessToken(String resource) throws IOException {
        String authorityUrl = this.environment().activeDirectoryEndpoint() + this.domain();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context = new AuthenticationContext(authorityUrl, false, executor);
        if (proxy() != null) {
            context.setProxy(proxy());
        }
        try {
            if (secret != null) {
                return context.acquireToken(
                        resource,
                        new ClientCredential(this.clientId(), secret),
                        null).get();
            } else if (certificate != null) {
                return context.acquireToken(
                        resource,
                        AsymmetricKeyCredential.create(clientId, new ByteArrayInputStream(certificate), certPassword),
                        null).get();
            }
            throw new AuthenticationException("Please provide either a non-null secret or a non-null certificate.");
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            executor.shutdown();
        }
    }
}

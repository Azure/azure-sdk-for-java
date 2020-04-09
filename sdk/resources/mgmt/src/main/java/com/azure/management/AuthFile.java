// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.implementation.TypeUtil;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class describes the information from a .azureauth file.
 */

final class AuthFile {
    private String clientId;
    private String tenantId;
    private String clientSecret;
    private String clientCertificate;
    private String clientCertificatePassword;
    private String subscriptionId;

    @JsonIgnore
    private final AzureEnvironment environment;
    @JsonIgnore
    private static final SerializerAdapter ADAPTER = new AzureJacksonAdapter();
    @JsonIgnore
    private String authFilePath;

    private AuthFile() {
        environment = new AzureEnvironment(new HashMap<String, String>());
        environment.endpoints().putAll(AzureEnvironment.AZURE.endpoints());
    }

    /**
     * Parses an auth file and read into an AuthFile object.
     *
     * @param file the auth file to read
     * @return the AuthFile object created
     * @throws IOException thrown when the auth file or the certificate file cannot be read or parsed
     */
    static AuthFile parse(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
        AuthFile authFile;
        if (isJsonBased(content)) {
            authFile = ADAPTER.deserialize(content, AuthFile.class, SerializerEncoding.JSON);
            Map<String, String> endpoints = ADAPTER.deserialize(content,
                TypeUtil.createParameterizedType(Map.class, String.class, String.class),
                SerializerEncoding.JSON);
            authFile.environment.endpoints().putAll(endpoints);
        } else {
            // Set defaults
            Properties authSettings = new Properties();
            authSettings.put(CredentialSettings.AUTH_URL.toString(),
                AzureEnvironment.AZURE.getActiveDirectoryEndpoint());
            authSettings.put(CredentialSettings.BASE_URL.toString(),
                AzureEnvironment.AZURE.getResourceManagerEndpoint());
            authSettings.put(CredentialSettings.MANAGEMENT_URI.toString(),
                AzureEnvironment.AZURE.getManagementEndpoint());
            authSettings.put(CredentialSettings.GRAPH_URL.toString(),
                AzureEnvironment.AZURE.getGraphEndpoint());
            authSettings.put(CredentialSettings.VAULT_SUFFIX.toString(),
                AzureEnvironment.AZURE.getKeyVaultDnsSuffix());

            // Load the credentials from the file
            StringReader credentialsReader = new StringReader(content);
            authSettings.load(credentialsReader);
            credentialsReader.close();

            authFile = new AuthFile();
            authFile.clientId = authSettings.getProperty(CredentialSettings.CLIENT_ID.toString());
            authFile.tenantId = authSettings.getProperty(CredentialSettings.TENANT_ID.toString());
            authFile.clientSecret = authSettings.getProperty(CredentialSettings.CLIENT_KEY.toString());
            authFile.clientCertificate = authSettings.getProperty(CredentialSettings.CLIENT_CERT.toString());
            authFile.clientCertificatePassword =
                authSettings.getProperty(CredentialSettings.CLIENT_CERT_PASS.toString());
            authFile.subscriptionId = authSettings.getProperty(CredentialSettings.SUBSCRIPTION_ID.toString());

            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.MANAGEMENT.identifier(),
                authSettings.getProperty(CredentialSettings.MANAGEMENT_URI.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.identifier(),
                authSettings.getProperty(CredentialSettings.AUTH_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.identifier(),
                authSettings.getProperty(CredentialSettings.BASE_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.GRAPH.identifier(),
                authSettings.getProperty(CredentialSettings.GRAPH_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.KEYVAULT.identifier(),
                authSettings.getProperty(CredentialSettings.VAULT_SUFFIX.toString()));
        }
        authFile.authFilePath = file.getParent();

        return authFile;
    }

    private static boolean isJsonBased(String content) {
        return content.startsWith("{");
    }

    /**
     * @return an ApplicationTokenCredentials object from the information in this class
     */
    ApplicationTokenCredential generateCredential() throws IOException {
        if (clientSecret != null) {
            return (ApplicationTokenCredential) new ApplicationTokenCredential(
                    clientId,
                    tenantId,
                    clientSecret,
                    environment).defaultSubscriptionId(subscriptionId);
        } else if (clientCertificate != null) {
            byte[] certData;
            File f = new File(clientCertificate);
            if (!f.exists()) {
                f = new File(authFilePath, clientCertificate);
            }
            certData = Files.readAllBytes(f.toPath());

            return (ApplicationTokenCredential) new ApplicationTokenCredential(
                    clientId,
                    tenantId,
                    certData,
                    clientCertificatePassword,
                    environment).defaultSubscriptionId(subscriptionId);
        } else {
            ClientLogger logger = new ClientLogger(this.getClass());
            throw logger.logExceptionAsError(
                    new IllegalArgumentException("Please specify either a client key or a client certificate."));
        }
    }

    /**
     * Contains the keys of the settings in a Properties file to read credentials from.
     */
    private enum CredentialSettings {
        /**
         * The subscription GUID.
         */
        SUBSCRIPTION_ID("subscription"),
        /**
         * The tenant GUID or domain.
         */
        TENANT_ID("tenant"),
        /**
         * The client id for the client application.
         */
        CLIENT_ID("client"),
        /**
         * The client secret for the service principal.
         */
        CLIENT_KEY("key"),
        /**
         * The client certificate for the service principal.
         */
        CLIENT_CERT("certificate"),
        /**
         * The password for the client certificate for the service principal.
         */
        CLIENT_CERT_PASS("certificatePassword"),
        /**
         * The management endpoint.
         */
        MANAGEMENT_URI("managementURI"),
        /**
         * The base URL to the current Azure environment.
         */
        BASE_URL("baseURL"),
        /**
         * The URL to Active Directory authentication.
         */
        AUTH_URL("authURL"),
        /**
         * The URL to Active Directory Graph.
         */
        GRAPH_URL("graphURL"),
        /**
         * The suffix of Key Vaults.
         */
        VAULT_SUFFIX("vaultSuffix");

        /**
         * The name of the key in the properties file.
         */
        private final String name;

        CredentialSettings(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

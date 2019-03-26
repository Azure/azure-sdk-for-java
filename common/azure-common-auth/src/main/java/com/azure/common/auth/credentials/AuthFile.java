/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.azure.common.AzureEnvironment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.reflect.TypeToken;
import com.azure.common.annotations.Beta;
import com.azure.common.implementation.serializer.SerializerEncoding;
import com.azure.common.implementation.serializer.jackson.JacksonAdapter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class describes the information from a .azureauth file.
 */
@Beta(since = "v1.1.0")
final class AuthFile {

    private String clientId;
    private String tenantId;
    private String clientSecret;
    private String clientCertificate;
    private String clientCertificatePassword;
    private String subscriptionId;

    @JsonIgnore
    private AzureEnvironment environment;
    @JsonIgnore
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();
    @JsonIgnore
    private String authFilePath;

    private AuthFile() {
        environment = new AzureEnvironment(new HashMap<String, String>());
        environment.endpoints().putAll(AzureEnvironment.AZURE.endpoints());
    }

    /**
     * Parses an auth file and read into an AuthFile object.
     * @param file the auth file to read
     * @return the AuthFile object created
     * @throws IOException thrown when the auth file or the certificate file cannot be read or parsed
     */
    static AuthFile parse(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        AuthFile authFile;
        if (isJsonBased(content)) {
            authFile = ADAPTER.deserialize(content, AuthFile.class, SerializerEncoding.JSON);
            Map<String, String> endpoints = ADAPTER.deserialize(content, new TypeToken<Map<String, String>>() { }.getType(), SerializerEncoding.JSON);
            authFile.environment.endpoints().putAll(endpoints);
        } else {
            // Set defaults
            Properties authSettings = new Properties();
            authSettings.put(CredentialSettings.AUTH_URL.toString(), AzureEnvironment.AZURE.activeDirectoryEndpoint());
            authSettings.put(CredentialSettings.BASE_URL.toString(), AzureEnvironment.AZURE.resourceManagerEndpoint());
            authSettings.put(CredentialSettings.MANAGEMENT_URI.toString(), AzureEnvironment.AZURE.managementEndpoint());
            authSettings.put(CredentialSettings.GRAPH_URL.toString(), AzureEnvironment.AZURE.graphEndpoint());
            authSettings.put(CredentialSettings.VAULT_SUFFIX.toString(), AzureEnvironment.AZURE.keyVaultDnsSuffix());

            // Load the credentials from the file
            StringReader credentialsReader = new StringReader(content);
            authSettings.load(credentialsReader);
            credentialsReader.close();

            authFile = new AuthFile();
            authFile.clientId = authSettings.getProperty(CredentialSettings.CLIENT_ID.toString());
            authFile.tenantId = authSettings.getProperty(CredentialSettings.TENANT_ID.toString());
            authFile.clientSecret = authSettings.getProperty(CredentialSettings.CLIENT_KEY.toString());
            authFile.clientCertificate = authSettings.getProperty(CredentialSettings.CLIENT_CERT.toString());
            authFile.clientCertificatePassword = authSettings.getProperty(CredentialSettings.CLIENT_CERT_PASS.toString());
            authFile.subscriptionId = authSettings.getProperty(CredentialSettings.SUBSCRIPTION_ID.toString());
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.MANAGEMENT.identifier(), authSettings.getProperty(CredentialSettings.MANAGEMENT_URI.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.identifier(), authSettings.getProperty(CredentialSettings.AUTH_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.identifier(), authSettings.getProperty(CredentialSettings.BASE_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.GRAPH.identifier(), authSettings.getProperty(CredentialSettings.GRAPH_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.KEYVAULT.identifier(), authSettings.getProperty(CredentialSettings.VAULT_SUFFIX.toString()));
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
    ApplicationTokenCredentials generateCredentials() throws IOException {
        if (clientSecret != null) {
            return (ApplicationTokenCredentials) new ApplicationTokenCredentials(
                    clientId,
                    tenantId,
                    clientSecret,
                    environment).withDefaultSubscriptionId(subscriptionId);
        } else if (clientCertificate != null) {
            byte[] certData;
            if (new File(clientCertificate).exists()) {
                certData = Files.readAllBytes(Paths.get(clientCertificate));
            } else {
                certData = Files.readAllBytes(Paths.get(authFilePath, clientCertificate));
            }

            return (ApplicationTokenCredentials) new ApplicationTokenCredentials(
                    clientId,
                    tenantId,
                    certData,
                    clientCertificatePassword,
                    environment).withDefaultSubscriptionId(subscriptionId);
        } else {
            throw new IllegalArgumentException("Please specify either a client key or a client certificate.");
        }
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
}

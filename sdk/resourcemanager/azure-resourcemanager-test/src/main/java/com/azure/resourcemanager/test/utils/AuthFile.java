// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.test.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * AuthFile class to help authenticate in tests.
 */
public final class AuthFile {
    private String clientId;
    private String tenantId;
    private String clientSecret;
    private String clientCertificate;
    private String clientCertificatePassword;
    private String subscriptionId;
    private TokenCredential credential;

    @JsonIgnore
    private final AzureEnvironment environment;
    @JsonIgnore
    private static final SerializerAdapter ADAPTER = new AzureJacksonAdapter();

    private AuthFile() {
        environment = new AzureEnvironment(new HashMap<>());
        environment.endpoints().putAll(AzureEnvironment.AZURE.endpoints());
    }

    /**
     * Create a parameterized type from a raw class and its type arguments.
     *
     * @param rawClass the raw class to construct the parameterized type
     * @param genericTypes the generic arguments
     * @return the parameterized type
     */
    static ParameterizedType createParameterizedType(Class<?> rawClass, Type... genericTypes) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return genericTypes;
            }

            @Override
            public Type getRawType() {
                return rawClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    /**
     * Parses an auth file and read into an AuthFile object.
     *
     * @param file the auth file to read
     * @return the AuthFile object created
     * @throws IOException thrown when the auth file or the certificate file cannot be read or parsed
     */
    public static AuthFile parse(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8);
        AuthFile authFile;
        if (isJsonBased(content)) {
            authFile = ADAPTER.deserialize(content, AuthFile.class, SerializerEncoding.JSON);
            Map<String, String> endpoints = ADAPTER.deserialize(content,
                createParameterizedType(Map.class, String.class, String.class),
                SerializerEncoding.JSON);
            authFile.environment.endpoints().putAll(endpoints);
        } else {
            // Set defaults
            Properties authSettings = new Properties();
            authSettings.put(AuthFile.CredentialSettings.AUTH_URL.toString(),
                AzureEnvironment.AZURE.getActiveDirectoryEndpoint());
            authSettings.put(AuthFile.CredentialSettings.BASE_URL.toString(),
                AzureEnvironment.AZURE.getResourceManagerEndpoint());
            authSettings.put(AuthFile.CredentialSettings.MANAGEMENT_URI.toString(),
                AzureEnvironment.AZURE.getManagementEndpoint());
            authSettings.put(AuthFile.CredentialSettings.GRAPH_URL.toString(),
                AzureEnvironment.AZURE.getGraphEndpoint());
            authSettings.put(AuthFile.CredentialSettings.VAULT_SUFFIX.toString(),
                AzureEnvironment.AZURE.getKeyVaultDnsSuffix());

            // Load the credentials from the file
            StringReader credentialsReader = new StringReader(content);
            authSettings.load(credentialsReader);
            credentialsReader.close();

            authFile = new AuthFile();
            authFile.clientId = authSettings.getProperty(AuthFile.CredentialSettings.CLIENT_ID.toString());
            authFile.tenantId = authSettings.getProperty(AuthFile.CredentialSettings.TENANT_ID.toString());
            authFile.clientSecret = authSettings.getProperty(AuthFile.CredentialSettings.CLIENT_KEY.toString());
            authFile.clientCertificate = authSettings.getProperty(AuthFile.CredentialSettings.CLIENT_CERT.toString());
            authFile.clientCertificatePassword =
                authSettings.getProperty(AuthFile.CredentialSettings.CLIENT_CERT_PASS.toString());
            authFile.subscriptionId = authSettings.getProperty(AuthFile.CredentialSettings.SUBSCRIPTION_ID.toString());

            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.MANAGEMENT.identifier(),
                authSettings.getProperty(AuthFile.CredentialSettings.MANAGEMENT_URI.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.ACTIVE_DIRECTORY.identifier(),
                authSettings.getProperty(AuthFile.CredentialSettings.AUTH_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.RESOURCE_MANAGER.identifier(),
                authSettings.getProperty(AuthFile.CredentialSettings.BASE_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.GRAPH.identifier(),
                authSettings.getProperty(AuthFile.CredentialSettings.GRAPH_URL.toString()));
            authFile.environment.endpoints().put(AzureEnvironment.Endpoint.KEYVAULT.identifier(),
                authSettings.getProperty(AuthFile.CredentialSettings.VAULT_SUFFIX.toString()));
        }
        return authFile;
    }

    private static boolean isJsonBased(String content) {
        return content.startsWith("{");
    }

    /**
     * @return an ApplicationTokenCredentials object from the information in this class
     */
    private TokenCredential generateCredential() {
        if (clientSecret != null) {
            return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorityHost(environment.getActiveDirectoryEndpoint())
                .build();
        } else if (clientCertificate != null) {
            ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .authorityHost(environment.getActiveDirectoryEndpoint());
            if (clientCertificatePassword != null) {
                builder.pfxCertificate(clientCertificate, clientCertificatePassword);
            } else {
                builder.pemCertificate(clientCertificate);
            }
            return builder.build();
        } else {
            ClientLogger logger = new ClientLogger(this.getClass());
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Please specify either a client key or a client certificate."));
        }
    }

    /**
     * @return the subscription ID.
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * @return the tenant ID.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * @return the environment.
     */
    public AzureEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * @return the client ID.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * @return the credential.
     */
    public TokenCredential getCredential() {
        if (this.credential == null) {
            this.credential = generateCredential();
        }
        return credential;
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

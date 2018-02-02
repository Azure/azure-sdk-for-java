/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.v2.AzureEnvironment;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.rest.v2.serializer.JacksonAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token based credentials for use with a REST Service Client.
 */
@Beta
public final class AzureCliCredentials extends AzureTokenCredentials {
    private static final ObjectMapper MAPPER = new JacksonAdapter().serializer().setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ssssss"));
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AzureCliSubscription> subscriptions;
    private File azureProfile;
    private File accessTokens;

    private AzureCliCredentials() {
        super(null, null);
        subscriptions = new ConcurrentHashMap<>();
    }

    private synchronized void loadAccessTokens() throws IOException {
        try {
            AzureCliSubscription.Wrapper wrapper = MAPPER.readValue(azureProfile, AzureCliSubscription.Wrapper.class);
            List<AzureCliToken> tokens = MAPPER.readValue(accessTokens, new TypeReference<List<AzureCliToken>>() { });
            while (wrapper == null || tokens == null || tokens.isEmpty() || wrapper.subscriptions == null || wrapper.subscriptions.isEmpty()) {
                System.err.println("Please login in Azure CLI and press any key to continue after you've successfully logged in.");
                System.in.read();
                wrapper = MAPPER.readValue(azureProfile, AzureCliSubscription.Wrapper.class);
                tokens = MAPPER.readValue(accessTokens, new TypeReference<List<AzureCliToken>>() { });
            }
            for (AzureCliSubscription subscription : wrapper.subscriptions) {
                for (AzureCliToken token : tokens) {
                    // Find match of user and tenant
                    if (subscription.isServicePrincipal() == token.isServicePrincipal()
                            && subscription.userName().equalsIgnoreCase(token.user())
                            && subscription.tenant().equalsIgnoreCase(token.tenant())) {
                        subscriptions.put(subscription.id(), subscription.withToken(token));
                        if (subscription.isDefault()) {
                            withDefaultSubscriptionId(subscription.id());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(String.format("Cannot read files %s and %s. Are you logged in Azure CLI?", azureProfile.getAbsolutePath(), accessTokens.getAbsolutePath()));
            throw e;
        }
    }

    /**
     * Creates an instance of AzureCliCredentials with the default Azure CLI configuration.
     *
     * @return an instance of AzureCliCredentials
     * @throws IOException if the Azure CLI token files are not accessible
     */
    public static AzureCliCredentials create() throws IOException {
        return create(
            Paths.get(System.getProperty("user.home"), ".azure", "azureProfile.json").toFile(),
            Paths.get(System.getProperty("user.home"), ".azure", "accessTokens.json").toFile());
    }

    /**
     * Creates an instance of AzureCliCredentials with custom locations of the token files.
     *
     * @param azureProfile the azureProfile.json file created by Azure CLI
     * @param accessTokens the accessTokens.json file created by Azure CLI
     * @return an instance of AzureCliCredentials
     * @throws IOException if the Azure CLI token files are not accessible
     */
    public static AzureCliCredentials create(File azureProfile, File accessTokens) throws IOException {
        AzureCliCredentials credentials = new AzureCliCredentials();
        credentials.azureProfile = azureProfile;
        credentials.accessTokens = accessTokens;
        credentials.loadAccessTokens();
        return credentials;
    }

    /**
     * @return the active directory application client id
     */
    public String clientId() {
        return subscriptions.get(defaultSubscriptionId()).clientId();
    }

    /**
     * @return the tenant or domain the containing the application
     */
    @Override
    public String domain() {
        return subscriptions.get(defaultSubscriptionId()).tenant();
    }

    /**
     * @return the Azure environment to authenticate with
     */
    public AzureEnvironment environment() {
        return subscriptions.get(defaultSubscriptionId()).environment();
    }

    @Override
    public synchronized String getToken(String resource) throws IOException {
        String token = subscriptions.get(defaultSubscriptionId()).credentialInstance().getToken(resource);
        if (token == null) {
            System.err.println("Please login in Azure CLI and press any key to continue after you've successfully logged in.");
            System.in.read();
            loadAccessTokens();
            token = subscriptions.get(defaultSubscriptionId()).credentialInstance().getToken(resource);
        }
        return token;
    }
}

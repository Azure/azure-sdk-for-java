/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.credentials;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.rest.serializer.JacksonAdapter;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token based credentials for use with a REST Service Client.
 */
public class AzureCliCredentials extends AzureTokenCredentials {
    private static final ObjectMapper _mapper = new JacksonAdapter().serializer().setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ssssss"));
    /** A mapping from resource endpoint to its cached access token. */
    private Map<String, AzureCliSubscription> subscriptions;
    private String defaultSubscriptionId;
    private File azureProfile;
    private File accessTokens;
    private Lock lock = new ReentrantLock();

    private AzureCliCredentials() {
        super(null, null);
        subscriptions = new ConcurrentHashMap<>();
    }

    private synchronized void loadAccessTokens() throws IOException {
        try {
            AzureCliSubscription.Wrapper wrapper = _mapper.readValue(azureProfile, AzureCliSubscription.Wrapper.class);
            List<AzureCliToken> tokens = _mapper.readValue(accessTokens, new TypeReference<List<AzureCliToken>>() { });
            while (wrapper == null || tokens == null || tokens.isEmpty() || wrapper.subscriptions == null || wrapper.subscriptions.isEmpty()) {
                System.err.println("Please login in Azure CLI and press any key to continue after you've successfully logged in.");
                System.in.read();
                wrapper = _mapper.readValue(azureProfile, AzureCliSubscription.Wrapper.class);
                tokens = _mapper.readValue(accessTokens, new TypeReference<List<AzureCliToken>>() { });
            }
            for (AzureCliSubscription subscription : wrapper.subscriptions) {
                for (AzureCliToken token : tokens) {
                    // Find match of user and tenant
                    if (subscription.isServicePrincipal() == token.isServicePrincipal()
                            && subscription.userName().equalsIgnoreCase(token.user())
                            && subscription.tenant().equalsIgnoreCase(token.tenant())) {
                        subscriptions.put(subscription.id(), subscription.withToken(token));
                        if (subscription.isDefault()) {
                            defaultSubscriptionId = subscription.id();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(String.format("Cannot read files %s and %s. Are you logged in Azure CLI?", azureProfile.getAbsolutePath(), accessTokens.getAbsolutePath()));
            throw e;
        }
    }

    public static AzureCliCredentials create() throws IOException {
        return create(
            Paths.get(System.getProperty("user.home"), ".azure", "azureProfile.json").toFile(),
            Paths.get(System.getProperty("user.home"), ".azure", "accessTokens.json").toFile());
    }

    public static AzureCliCredentials create(File azureProfile, File accessTokens) throws IOException {
        AzureCliCredentials credentials = new AzureCliCredentials();
        credentials.azureProfile = azureProfile;
        credentials.accessTokens = accessTokens;
        credentials.loadAccessTokens();
        return credentials;
    }

    /**
     * Gets the active directory application client id.
     *
     * @return the active directory application client id.
     */
    public String clientId() {
        return subscriptions.get(defaultSubscriptionId).clientId();
    }

    /**
     * Gets the tenant or domain the containing the application.
     *
     * @return the tenant or domain the containing the application.
     */
    @Override
    public String domain() {
        return subscriptions.get(defaultSubscriptionId).tenant();
    }

    /**
     * Gets the Azure environment to authenticate with.
     *
     * @return the Azure environment to authenticate with.
     */
    public AzureEnvironment environment() {
        return subscriptions.get(defaultSubscriptionId).environment();
    }

    public String defaultSubscriptionId() {
        return defaultSubscriptionId;
    }

    /**
     * Set default subscription ID.
     *
     * @param subscriptionId the default subscription ID.
     * @return the credentials object itself.
     */
    public AzureCliCredentials withDefaultSubscriptionId(String subscriptionId) {
        this.defaultSubscriptionId = subscriptionId;
        return this;
    }

    @Override
    public synchronized String getToken(String resource) throws IOException {
        String token = subscriptions.get(defaultSubscriptionId).credentialInstance().getToken(resource);
        if (token == null) {
            System.err.println("Please login in Azure CLI and press any key to continue after you've successfully logged in.");
            System.in.read();
            loadAccessTokens();
            token = subscriptions.get(defaultSubscriptionId).credentialInstance().getToken(resource);
        }
        return token;
    }

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        clientBuilder.interceptors().add(new AzureTokenCredentialsInterceptor(this));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage all AAD oauth2 clients configured by property "azure.activedirectory.xxx"
 */
public abstract class AADClientRegistrationRepository implements ClientRegistrationRepository {

    public static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    protected final AzureClientRegistration azureClient;
    protected final List<ClientRegistration> otherClients;
    protected final Map<String, ClientRegistration> allClients;
    protected final AADAuthenticationProperties properties;

    public AADClientRegistrationRepository(AzureClientRegistration azureClient,
                                           List<ClientRegistration> otherClients,
                                           AADAuthenticationProperties properties) {
        this.azureClient = azureClient;
        this.otherClients = new ArrayList<>(otherClients);
        this.properties = properties;

        allClients = new HashMap<>();
        addClientRegistration(azureClient.getClient());
        for (ClientRegistration c : otherClients) {
            addClientRegistration(c);
        }
    }

    private void addClientRegistration(ClientRegistration client) {
        allClients.put(client.getRegistrationId(), client);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return allClients.get(registrationId);
    }

    public AzureClientRegistration getAzureClient() {
        return azureClient;
    }

    public boolean isClientNeedConsentWhenLogin(ClientRegistration client) {
        return otherClients.contains(client)
            && properties.getAuthorizationClients().get(client.getClientName()) != null
            && !properties.getAuthorizationClients().get(client.getClientName()).isOnDemand();
    }

    public boolean isClientNeedConsentWhenLogin(String id) {
        ClientRegistration client = findByRegistrationId(id);
        return client != null && isClientNeedConsentWhenLogin(client);
    }

    public static boolean isDefaultClient(ClientRegistration clientRegistration) {
        return AZURE_CLIENT_REGISTRATION_ID.equals(
            clientRegistration.getClientName());
    }

    public static boolean isDefaultClient(String clientId) {
        return AZURE_CLIENT_REGISTRATION_ID.equals(clientId);
    }
}

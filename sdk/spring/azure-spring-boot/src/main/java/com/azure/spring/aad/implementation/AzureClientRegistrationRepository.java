// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AzureClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final AzureClientRegistration azureClient;
    private final List<ClientRegistration> otherClients;
    private final Map<String, ClientRegistration> allClients;

    public AzureClientRegistrationRepository(AzureClientRegistration azureClient,
                                             List<ClientRegistration> otherClients) {
        this.azureClient = azureClient;
        this.otherClients = new ArrayList<>(otherClients);
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

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return Collections.singleton(azureClient.getClient()).iterator();
    }

    public AzureClientRegistration getAzureClient() {
        return azureClient;
    }

    public boolean isAuthzClient(ClientRegistration client) {
        return otherClients.contains(client);
    }

    public boolean isAuthzClient(String id) {
        ClientRegistration client = findByRegistrationId(id);
        return client != null && isAuthzClient(client);
    }
}

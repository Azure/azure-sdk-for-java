package com.azure.spring.aad.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class AzureClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private DefaultClient defaultClient;
    private List<ClientRegistration> authzClients;

    private Map<String, ClientRegistration> clients;

    public AzureClientRegistrationRepository(DefaultClient defaultClient, List<ClientRegistration> authzClients) {
        this.defaultClient = defaultClient;
        this.authzClients = new ArrayList<>(authzClients);

        clients = new HashMap<>();
        addClientRegistration(defaultClient.client());
        for (ClientRegistration c : authzClients) {
            addClientRegistration(c);
        }
    }

    private void addClientRegistration(ClientRegistration client) {
        clients.put(client.getRegistrationId(), client);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return clients.get(registrationId);
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return Collections.singleton(defaultClient.client()).iterator();
    }

    public DefaultClient defaultClient() {
        return defaultClient;
    }

    public boolean isAuthzClient(ClientRegistration client) {
        return authzClients.contains(client);
    }

    public boolean isAuthzClient(String id) {
        ClientRegistration client = findByRegistrationId(id);
        return client == null ? false : isAuthzClient(client);
    }
}

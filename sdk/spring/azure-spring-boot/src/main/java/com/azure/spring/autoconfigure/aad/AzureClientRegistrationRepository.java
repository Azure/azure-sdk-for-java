// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A ClientRegistrationRepository that manage all AAD's ClientRegistrations.
 */
public class AzureClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final DefaultClient defaultClient;
    private final List<ClientRegistration> authorizedClientRegistrations;

    private final Map<String, ClientRegistration> clientRegistrations;

    public AzureClientRegistrationRepository(DefaultClient defaultClient,
                                             List<ClientRegistration> authorizedClientRegistrations) {
        this.defaultClient = defaultClient;
        this.authorizedClientRegistrations = new ArrayList<>(authorizedClientRegistrations);
        clientRegistrations = new HashMap<>();
        addClientRegistration(defaultClient.getClientRegistration());
        for (ClientRegistration clientRegistration : authorizedClientRegistrations) {
            addClientRegistration(clientRegistration);
        }
    }

    private void addClientRegistration(ClientRegistration clientRegistration) {
        clientRegistrations.put(clientRegistration.getRegistrationId(), clientRegistration);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return clientRegistrations.get(registrationId);
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return Collections.singleton(defaultClient.getClientRegistration()).iterator();
    }

    public DefaultClient defaultClient() {
        return defaultClient;
    }

    public boolean isAuthorizedClient(ClientRegistration clientRegistration) {
        return authorizedClientRegistrations.contains(clientRegistration);
    }

    public boolean isAuthorizedClient(String id) {
        return Optional.of(id)
                       .map(this::findByRegistrationId)
                       .map(this::isAuthorizedClient)
                       .orElse(false);
    }
}

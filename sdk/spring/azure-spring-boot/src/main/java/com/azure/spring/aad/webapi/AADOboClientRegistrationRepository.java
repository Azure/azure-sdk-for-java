// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ClientRegistration Repository for obo flow
 */
public class AADOboClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final Map<String, ClientRegistration> allClients;

    public AADOboClientRegistrationRepository(List<ClientRegistration> resourceClients) {
        allClients = new HashMap<>();
        for (ClientRegistration c : resourceClients) {
            allClients.put(c.getRegistrationId(), c);
        }
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return allClients.get(registrationId);
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return allClients.values().iterator();
    }
}

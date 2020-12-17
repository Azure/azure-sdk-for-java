// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
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
public class OboClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final List<ClientRegistration> resourceClients;

    private final Map<String, ClientRegistration> allClients;

    private final AADAuthenticationProperties properties;

    public OboClientRegistrationRepository(List<ClientRegistration> resourceClients, AADAuthenticationProperties
        properties) {
        this.resourceClients = resourceClients;
        this.properties = properties;
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

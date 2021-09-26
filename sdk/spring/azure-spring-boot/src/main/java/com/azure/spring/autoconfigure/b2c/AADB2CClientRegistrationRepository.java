// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * ClientRegistrationRepository for aad b2c
 * </p>
 */
@Deprecated
public class AADB2CClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final InMemoryClientRegistrationRepository clientRegistrations;
    private final List<ClientRegistration> signUpOrSignInRegistrations;


    AADB2CClientRegistrationRepository(String loginFlow, List<ClientRegistration> clientRegistrations) {
        this.signUpOrSignInRegistrations = clientRegistrations.stream()
                                                              .filter(client -> loginFlow.equals(client.getClientName()))
                                                              .collect(Collectors.toList());
        this.clientRegistrations = new InMemoryClientRegistrationRepository(clientRegistrations);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return this.clientRegistrations.findByRegistrationId(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return this.signUpOrSignInRegistrations.iterator();
    }
}

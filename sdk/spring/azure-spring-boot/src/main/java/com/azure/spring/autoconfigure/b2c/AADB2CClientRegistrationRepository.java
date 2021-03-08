// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * ClientRegistrationRepository for aad b2c
 * </p>
 */
public class AADB2CClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final InMemoryClientRegistrationRepository clientRegistrations;
    private final List<ClientRegistration> signUpOrSignInRegistrations;


    AADB2CClientRegistrationRepository(List<ClientRegistration> signUpOrSignInRegistrations,
                                       List<ClientRegistration> otherRegistrations) {
        this.signUpOrSignInRegistrations = signUpOrSignInRegistrations;
        List<ClientRegistration> allRegistrations = Stream.of(signUpOrSignInRegistrations, otherRegistrations)
                                                          .flatMap(Collection::stream)
                                                          .collect(Collectors.toList());
        this.clientRegistrations = new InMemoryClientRegistrationRepository(allRegistrations);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return this.clientRegistrations.findByRegistrationId(registrationId);
    }

    @NotNull
    @Override
    public Iterator<ClientRegistration> iterator() {
        return this.signUpOrSignInRegistrations.iterator();
    }
}

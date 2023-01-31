// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An AAD B2C {@link ClientRegistrationRepository} implementation, it will manage all the client registrations
 * from user flow instances and native OAuth2 clients.
 */
public class AadB2cClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final InMemoryClientRegistrationRepository delegate;

    private final List<ClientRegistration> signUpOrSignInRegistrations;

    public AadB2cClientRegistrationRepository(List<ClientRegistration> signUpOrSignInRegistrations,
                                              List<ClientRegistration> allClientRegistrations) {
        Assert.noNullElements(signUpOrSignInRegistrations, "signUpOrSignInRegistrations can not be empty.");
        Assert.noNullElements(allClientRegistrations, "allClientRegistrations can not be empty.");
        this.signUpOrSignInRegistrations = Collections.unmodifiableList(signUpOrSignInRegistrations);
        this.delegate = new InMemoryClientRegistrationRepository(allClientRegistrations);
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return this.delegate.findByRegistrationId(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return this.signUpOrSignInRegistrations.iterator();
    }
}

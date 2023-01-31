// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import io.micrometer.core.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * An AAD B2C {@link ClientRegistrationRepository} implementation, it will manage all the client registrations
 * from user flow instances and native OAuth2 clients.
 */
public class AadB2cClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private final InMemoryClientRegistrationRepository delegate;

    private final Set<String> nonSignInClientRegistrationIds;

    public AadB2cClientRegistrationRepository(List<ClientRegistration> clientRegistrations,
                                              @NonNull Set<String> nonSignInClientRegistrationIds) {
        Assert.noNullElements(clientRegistrations, "clientRegistrations can not be empty.");
        this.delegate = new InMemoryClientRegistrationRepository(clientRegistrations);
        this.nonSignInClientRegistrationIds = nonSignInClientRegistrationIds;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return this.delegate.findByRegistrationId(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        final Iterable<ClientRegistration> iterable = () -> this.delegate.iterator();

        return StreamSupport.stream(iterable.spliterator(), false)
            .filter(cr -> !this.nonSignInClientRegistrationIds.contains(cr.getRegistrationId()))
            .iterator();
    }
}

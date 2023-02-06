// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents client registrations and non sign in client registration ids for an
 * AD B2C application, it's built by {@link AadB2cClientRegistrationsBuilder#build()}.
 */
public class AadB2cClientRegistrations {
    private final List<ClientRegistration> clientRegistrations;
    private final Set<String> nonSignInClientRegistrationIds = new HashSet<>();

    /**
     * Create an instance only with client registration list.
     * @param clientRegistrations the client registration list.
     */
    public AadB2cClientRegistrations(Collection<ClientRegistration> clientRegistrations) {
        this(clientRegistrations, null);
    }

    /**
     * Create an instance only with client registration list and non sign in registration id set.
     * @param clientRegistrations the client registration list.
     * @param nonSignInClientRegistrationIds the non sign in registration id set.
     */
    public AadB2cClientRegistrations(Collection<ClientRegistration> clientRegistrations, Set<String> nonSignInClientRegistrationIds) {
        this.clientRegistrations = new ArrayList<>(clientRegistrations);
        if (!CollectionUtils.isEmpty(nonSignInClientRegistrationIds)) {
            this.nonSignInClientRegistrationIds.addAll(nonSignInClientRegistrationIds);
        }
    }

    /**
     * Get the client registration list.
     * @return the client registration list.
     */
    public List<ClientRegistration> getClientRegistrations() {
        return clientRegistrations;
    }

    /**
     * Get the non sign in client registration ids.
     * @return the non sign in client registration ids.
     */
    public Set<String> getNonSignInClientRegistrationIds() {
        return nonSignInClientRegistrationIds;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation.config;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class provides a fluent builder API to help aid the instantiation of
 * {@link AadB2cClientRegistrationRepository} for Azure AD B2C OAuth2 Client repository.
 */
public class AadB2cClientRegistrationRepositoryBuilder {

    private final AtomicBoolean building = new AtomicBoolean();
    private final List<AadB2cClientRegistrationRepositoryBuilderConfigurer> configurers = new ArrayList<>();
    private final List<ClientRegistration> clientRegistrations = new ArrayList<>();
    private final Set<String> nonSignInClientRegistrationIds = new HashSet<>();

    /**
     * Build client registrations of one application registered in Azure AD B2C, the
     * {@link AadB2cClientRegistrationsBuilder} will carry the actual client registration
     * creation and return an {@link AadB2cClientRegistrations} for the repository builder.
     * @param builder the builder to build an {@link AadB2cClientRegistrations}.
     * @return the updated AadB2cClientRegistrationRepositoryBuilder.
     */
    public AadB2cClientRegistrationRepositoryBuilder b2cClientRegistrations(AadB2cClientRegistrationsBuilder builder) {
        final AadB2cClientRegistrations aadB2cClientRegistrations = builder.build();
        this.clientRegistrations(aadB2cClientRegistrations.getClientRegistrations().toArray(new ClientRegistration[0]));
        this.nonSignInClientRegistrationIds(
            aadB2cClientRegistrations.getNonSignInClientRegistrationIds()
                                     .toArray(new String[0]));
        return this;
    }
    public AadB2cClientRegistrationRepositoryBuilder clientRegistrations(ClientRegistration... clientRegistrations) {
        Arrays.stream(clientRegistrations).forEach(this.clientRegistrations::add);
        return this;
    }

    public AadB2cClientRegistrationRepositoryBuilder nonSignInClientRegistrationIds(String... clientRegistrationIds) {
        Arrays.stream(clientRegistrationIds).forEach(this.nonSignInClientRegistrationIds::add);
        return this;
    }

    public AadB2cClientRegistrationRepositoryBuilder addRepositoryBuilderConfigurer(AadB2cClientRegistrationRepositoryBuilderConfigurer configurer) {
        this.configurers.add(configurer);
        return this;
    }

    /**
     * Build client registration repository for Azure AD B2C.
     * @return an {@link AadB2cClientRegistrationRepository} created from the configuration in this builder.
     */
    public AadB2cClientRegistrationRepository build() {
        if (this.building.compareAndSet(false, true)) {
            return doBuild();
        }
        throw new IllegalStateException("This AadB2cClientRegistrationRepository has already been built");
    }

    private AadB2cClientRegistrationRepository doBuild() {
        synchronized (this.configurers) {
            for (AadB2cClientRegistrationRepositoryBuilderConfigurer configurer : configurers) {
                configurer.configure(this);
            }
            return new AadB2cClientRegistrationRepository(this.clientRegistrations, this.nonSignInClientRegistrationIds);
        }
    }
}

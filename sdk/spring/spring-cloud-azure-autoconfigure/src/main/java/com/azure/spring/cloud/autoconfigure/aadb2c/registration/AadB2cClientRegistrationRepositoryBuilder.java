// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

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
    private final List<AadB2cClientRegistrationRepositoryBuilderCustomizer> customizers = new ArrayList<>();
    private final List<ClientRegistration> clientRegistrations = new ArrayList<>();
    private final Set<String> nonSignInClientRegistrationIds = new HashSet<>();

    /**
     * Add the client registrations of one application registered in Azure AD B2C, the
     * {@link AadB2cClientRegistrationsBuilder} will carry the actual client registration
     * creation and return an {@link AadB2cClientRegistrations} for the repository builder.
     * @param aadB2cClientRegistrations the {@link AadB2cClientRegistrations}.
     * @return the updated AadB2cClientRegistrationRepositoryBuilder.
     */
    public AadB2cClientRegistrationRepositoryBuilder aadB2cClientRegistrations(AadB2cClientRegistrations aadB2cClientRegistrations) {
        this.clientRegistrations(aadB2cClientRegistrations.getClientRegistrations().toArray(new ClientRegistration[0]));
        this.nonSignInClientRegistrationIds(
            aadB2cClientRegistrations.getNonSignInClientRegistrationIds()
                                        .toArray(new String[0]));
        return this;
    }

    /**
     * Add the client registrations for the repository builder.
     * @param clientRegistrations the array of {@link ClientRegistration}.
     * @return the updated AadB2cClientRegistrationRepositoryBuilder.
     */
    public AadB2cClientRegistrationRepositoryBuilder clientRegistrations(ClientRegistration... clientRegistrations) {
        Arrays.stream(clientRegistrations).forEach(this.clientRegistrations::add);
        return this;
    }

    /**
     * Add the non sign-in registration array, which are not the primary OAuth2
     * login clients and will not be shown by default on the login page.
     * @param nonSignInClientRegistrationIds the non sign-in registration ids.
     * @return the updated AadB2cClientRegistrationRepositoryBuilder.
     */
    public AadB2cClientRegistrationRepositoryBuilder nonSignInClientRegistrationIds(String... nonSignInClientRegistrationIds) {
        Arrays.stream(nonSignInClientRegistrationIds).forEach(this.nonSignInClientRegistrationIds::add);
        return this;
    }

    /**
     * Add an {@link AadB2cClientRegistrationRepositoryBuilderCustomizer} to customize extra client registrationS of an application registered in Azure AD B2C.
     * @param customizer the repository builder customizer.
     * @return the updated AadB2cClientRegistrationRepositoryBuilder.
     */
    public AadB2cClientRegistrationRepositoryBuilder addBuilderCustomizer(AadB2cClientRegistrationRepositoryBuilderCustomizer customizer) {
        this.customizers.add(customizer);
        return this;
    }

    /**
     * Build client registration repository for Azure AD B2C.
     * @return an {@link AadB2cClientRegistrationRepository} created from the configuration in this builder.
     * @throws IllegalStateException If the method {@link #build} is called a second time.
     */
    public ClientRegistrationRepository build() {
        if (this.building.compareAndSet(false, true)) {
            return doBuild();
        }
        throw new IllegalStateException("This AadB2cClientRegistrationRepository has already been built.");
    }

    private ClientRegistrationRepository doBuild() {
        synchronized (this.customizers) {
            for (AadB2cClientRegistrationRepositoryBuilderCustomizer customizer : customizers) {
                customizer.customize(this);
            }
            return new AadB2cClientRegistrationRepository(this.clientRegistrations, this.nonSignInClientRegistrationIds);
        }
    }
}

package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.List;

public interface ClientRegistrationRepositoryBuilder<O> {

    void addClientRegistrations(List<ClientRegistration> clientRegistrations);

    void addSignInClientRegistrations(List<ClientRegistration> clientRegistrations);

    O build() throws Exception;
}

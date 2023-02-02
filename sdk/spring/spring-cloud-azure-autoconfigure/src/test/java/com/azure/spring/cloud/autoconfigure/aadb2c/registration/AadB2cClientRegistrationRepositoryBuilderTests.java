// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.registration;

import com.azure.spring.cloud.autoconfigure.aadb2c.implementation.AadB2cClientRegistrationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AadB2cClientRegistrationRepositoryBuilderTests {

    private AadB2cClientRegistrationRepositoryBuilder repositoryBuilder;
    private static AadB2cClientRegistrationsBuilder registrationBuilder;
    private static final String SIGN_IN_USER_FLOW_ID = "B2C_1_signuporsignin";
    private static final String SIGN_IN_CLIENT_SECRET = "xxx";
    private static final String PASSWORD_RESET_USER_FLOW_ID = "B2C_1_passwordreset";
    private static final String AUTHORIZATION_CLIENT_REGISTRATION_ID = "fly";

    @BeforeAll
    static void setupClass() {
        registrationBuilder = new AadB2cClientRegistrationsBuilder();
        registrationBuilder.baseUri("https://xxx.b2clogin.com/xxx.onmicrosoft.com/")
                           .clientId("xxx")
                           .clientSecret(SIGN_IN_CLIENT_SECRET)
                           .tenantId("xxx")
                           .signInUserFlow(SIGN_IN_USER_FLOW_ID)
                           .userFlows(PASSWORD_RESET_USER_FLOW_ID)
                           .authorizationClient(AUTHORIZATION_CLIENT_REGISTRATION_ID, CLIENT_CREDENTIALS, "http://localhost/xxx/.default")
                           .build();
    }

    @BeforeEach
    void init() {
        repositoryBuilder = new AadB2cClientRegistrationRepositoryBuilder();
    }

    @Test
    @SuppressWarnings("unchecked")
    void setClientRegistrations() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("xxx")
                                                            .clientId("xxx")
                                                            .authorizationGrantType(CLIENT_CREDENTIALS)
                                                            .tokenUri("https://xxx.com/oauth2/v2.0/token")
                                                            .build();
        repositoryBuilder.clientRegistrations(registration);
        List<ClientRegistration> clientRegistrations = (List<ClientRegistration>) ReflectionTestUtils.getField(repositoryBuilder, "clientRegistrations");
        Assertions.assertNotNull(clientRegistrations);
        Assertions.assertEquals(registration, clientRegistrations.get(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void setNonSignInClientRegistrationIds() {
        String registrationId = "test";
        repositoryBuilder.nonSignInClientRegistrationIds(registrationId);
        Set<String> nonSignInClientRegistrationIds = (Set<String>) ReflectionTestUtils.getField(repositoryBuilder, "nonSignInClientRegistrationIds");
        Assertions.assertNotNull(nonSignInClientRegistrationIds);
        Assertions.assertTrue(nonSignInClientRegistrationIds.contains(registrationId));
    }

    @Test
    @SuppressWarnings("unchecked")
    void setB2cClientRegistrations() {
        repositoryBuilder.b2cClientRegistrations(registrationBuilder);
        List<ClientRegistration> clientRegistrations = (List<ClientRegistration>) ReflectionTestUtils.getField(repositoryBuilder, "clientRegistrations");
        Set<String> nonSignInClientRegistrationIds = (Set<String>) ReflectionTestUtils.getField(repositoryBuilder, "nonSignInClientRegistrationIds");
        Assertions.assertNotNull(clientRegistrations);
        Assertions.assertNotNull(nonSignInClientRegistrationIds);
        Assertions.assertTrue(nonSignInClientRegistrationIds.contains(PASSWORD_RESET_USER_FLOW_ID)
            && nonSignInClientRegistrationIds.contains(AUTHORIZATION_CLIENT_REGISTRATION_ID));
        Assertions.assertEquals(3, clientRegistrations.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void setAddRepositoryBuilderConfigurer() {
        AadB2cClientRegistrationRepositoryBuilderConfigurer configurer =
            (repositoryBuilder) -> repositoryBuilder.b2cClientRegistrations(registrationBuilder);
        repositoryBuilder.addRepositoryBuilderConfigurer(configurer).build();
        List<ClientRegistration> clientRegistrations = (List<ClientRegistration>) ReflectionTestUtils.getField(repositoryBuilder, "clientRegistrations");
        Set<String> nonSignInClientRegistrationIds = (Set<String>) ReflectionTestUtils.getField(repositoryBuilder, "nonSignInClientRegistrationIds");
        Assertions.assertNotNull(clientRegistrations);
        Assertions.assertNotNull(nonSignInClientRegistrationIds);
        Assertions.assertTrue(nonSignInClientRegistrationIds.contains(PASSWORD_RESET_USER_FLOW_ID)
            && nonSignInClientRegistrationIds.contains(AUTHORIZATION_CLIENT_REGISTRATION_ID));
        Assertions.assertEquals(3, clientRegistrations.size());
    }

    @Test
    void buildRepository() {
        AadB2cClientRegistrationRepository repository =
            (AadB2cClientRegistrationRepository) repositoryBuilder.b2cClientRegistrations(registrationBuilder)
                                                                  .build();
        ClientRegistration signInClient = repository.iterator().next();
        Assertions.assertEquals(SIGN_IN_USER_FLOW_ID, signInClient.getRegistrationId());
        Assertions.assertEquals(SIGN_IN_CLIENT_SECRET, signInClient.getClientSecret());
        Assertions.assertEquals(PASSWORD_RESET_USER_FLOW_ID,
            repository.findByRegistrationId(PASSWORD_RESET_USER_FLOW_ID).getRegistrationId());
        Assertions.assertEquals(AUTHORIZATION_CLIENT_REGISTRATION_ID,
            repository.findByRegistrationId(AUTHORIZATION_CLIENT_REGISTRATION_ID).getRegistrationId());
    }

    @Test
    void repeatBuildRepository() {
        repositoryBuilder.b2cClientRegistrations(registrationBuilder).build();
        Assertions.assertThrows(IllegalStateException.class, () -> repositoryBuilder.build());
    }
}

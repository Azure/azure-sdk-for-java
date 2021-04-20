// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

public class AADOAuth2AuthorizedClientCredentialRepositoryTest {

    private static final String AAD_PROPERTY_PREFIX = "azure.activedirectory.";
    private static final String CLIENT_CREDENTIAL_ACCESS_TOKEN =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Im5PbzNaRHJPRFhFSzFqS1doWHNsSFJfS1hFZyIsImtpZCI6Im5PbzNaRHJPRFhFSz"
            + "FqS1doWHNsSFJfS1hFZyJ9.eyJhdWQiOiJhcGk6Ly85MjdiYTU0Mi05YWRmLTQxMzktOWI3Yy03ZDA0MmM0YWVhMTUiLCJpc3MiOiJod"
            + "HRwczovL3N0cy53aW5kb3dzLm5ldC8xYzA1N2U3Yy01NGQ5LTRiNTItODkwMi05ZTE2Yjc0ODVkMzUvIiwiaWF0IjoxNjE1NTE1OTY2L"
            + "CJuYmYiOjE2MTU1MTU5NjYsImV4cCI6MTYxNTUxOTg2NiwiYWNyIjoiMSIsImFpbyI6IkFUUUF5LzhUQUFBQWJyNGk5SnBvYlNjdUltN"
            + "2ZSQjFuNzUzVHg2VzZrVkFzZVZtOXV0ODVIT1BGTk9WRllkSWt3eUpNeTVuMlByTGciLCJhbXIiOlsicHdkIl0sImFwcGlkIjoiOTExN"
            + "2QxZjEtNDE2Ny00MWE3LWFiMDUtNzNlZDRiZmUwOTg4IiwiYXBwaWRhY3IiOiIxIiwiaXBhZGRyIjoiMTY3LjIyMC4yNTUuOCIsIm5hb"
            + "WUiOiJhcHBfdXNlciIsIm9pZCI6IjUzNTcwM2EyLTY0MGMtNDM0NS04YmE4LTNjMGU1ZTQ0N2RhYyIsInB3ZF9leHAiOiIxMDM0ODYyI"
            + "iwicHdkX3VybCI6Imh0dHBzOi8vcG9ydGFsLm1pY3Jvc29mdG9ubGluZS5jb20vQ2hhbmdlUGFzc3dvcmQuYXNweCIsInJoIjoiMC5BQ"
            + "UFBZkg0RkhObFVVa3VKQXA0V3QwaGROZkhSRjVGblFhZEJxd1Z6N1V2LUNZaDRBSncuIiwic2NwIjoiRmlsZS5yZWFkIiwic3ViIjoiS"
            + "DNnV1YyLVJrODJBZHF0a3VyVzlvdVFPZmVRRDJ1NXZ3MkdIcWZRSWlfOCIsInRpZCI6IjFjMDU3ZTdjLTU0ZDktNGI1Mi04OTAyLTllM"
            + "TZiNzQ4NWQzNSIsInVuaXF1ZV9uYW1lIjoiYXBwX3VzZXJAeGlhb3podXRlc3RzYW1wbGUub25taWNyb3NvZnQuY29tIiwidXBuIjoiY"
            + "XBwX3VzZXJAeGlhb3podXRlc3RzYW1wbGUub25taWNyb3NvZnQuY29tIiwidXRpIjoiNTcyZERTQ2Vta2VETkxOUVRXRUVBQSIsInZlc"
            + "iI6IjEuMCJ9.TLE8oRV-6h3MKVyUDgRdNw-VuPjuZMIbe9QxQMUwMDLFPlnPBZizYaM9tcGGUvIWuBV_rQkayHPHRbVhOUqFTwQyiv1Z"
            + "hBUbXgltBzDt5rybd2rh0O6HsNqZGTYD65lMMnd_TvpFf2hBpvf1q9C-Txa2HRx8Q4i6Q3gAKCjcxbEMITwLlCMz3JtTQ785lME9o4fZ"
            + "R-s_LOz1eBiMboKWIPiQZwWQ-peGNsalNriss4_x4pwOwYlMqeZJBk1tIyON0nYNLTU169_KSGHIlTtVtaAlNnt9C2Ajg1PTvJvj3fsg"
            + "FhZpRbO4XBs6nEjFSwPC0RII36raH9wjgveNn63LPg";

    private ClientRegistration fakeClientRegistration;
    private Authentication mockPrincipal;
    private OAuth2AuthorizedClient oAuth2AuthorizedClient;
    private InMemoryClientRegistrationRepository clientRegistrationsRepo;
    private InMemoryOAuth2AuthorizedClientService inMemoryOAuth2AuthorizedClientService;

    @BeforeEach
    public void setup() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        addInlinedPropertiesToEnvironment(
            context,
            AAD_PROPERTY_PREFIX + "tenant-id = fake-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id = fake-client-id",
            AAD_PROPERTY_PREFIX + "client-secret = fake-client-secret",
            AAD_PROPERTY_PREFIX + "authorization-clients.fake.scopes = https://sample.onmicrosoft"
                + ".com/xxxx-xxxxx-xxxx/.default",
            AAD_PROPERTY_PREFIX + "authorization-clients.fake.authorization-grant-type = client_credentials"
        );
        context.register(AADResourceServerClientConfiguration.class);
        context.refresh();
        clientRegistrationsRepo = context.getBean(InMemoryClientRegistrationRepository.class);
        fakeClientRegistration = ClientRegistration
            .withRegistrationId("fake")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("https://login.microsoftonline.com/xxxx-xxxxx-xxxx/oauth2/v2.0/token")
            .scope("https://xiaozhusampleb2c.onmicrosoft.com/xxxx-xxxxx-xxxx/.default")
            .clientId("xxxx-xxxxx-xxxx").build();
        OAuth2AccessToken mockOAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(mockOAuth2AccessToken.getTokenValue()).thenReturn(CLIENT_CREDENTIAL_ACCESS_TOKEN);
        oAuth2AuthorizedClient = new OAuth2AuthorizedClient(fakeClientRegistration, "fake-name", mockOAuth2AccessToken);
        mockPrincipal = mock(JwtAuthenticationToken.class);
        when(mockPrincipal.getName()).thenReturn("fake-name");
        inMemoryOAuth2AuthorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationsRepo);
        inMemoryOAuth2AuthorizedClientService.saveAuthorizedClient(oAuth2AuthorizedClient, mockPrincipal);
    }

    @Test
    public void testAuthorizedClientCache() {
        OAuth2AuthorizedClient authorizedClient = inMemoryOAuth2AuthorizedClientService
            .loadAuthorizedClient(fakeClientRegistration.getRegistrationId(), "fake-name");
        Assertions.assertNotNull(authorizedClient);
        Assertions.assertEquals(CLIENT_CREDENTIAL_ACCESS_TOKEN, authorizedClient.getAccessToken().getTokenValue());
    }

    @Test
    public void testLoadAuthorizedClient() {
        OAuth2AuthorizedClient authorizedClient = inMemoryOAuth2AuthorizedClientService.loadAuthorizedClient(
            "fake", mockPrincipal.getName());
        Assertions.assertNotNull(authorizedClient);
        Assertions.assertEquals(CLIENT_CREDENTIAL_ACCESS_TOKEN, authorizedClient.getAccessToken().getTokenValue());
    }

    @Test
    public void testLoadNotExistAuthorizedClient() {
        OAuth2AuthorizedClient authorizedClient = inMemoryOAuth2AuthorizedClientService.loadAuthorizedClient(
            "fake-2", mockPrincipal.getName());
        Assertions.assertNull(authorizedClient);
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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

    private InMemoryClientRegistrationRepository clientRegistrationsRepo;
    private OAuth2AuthorizedClient authorizedClient;
    private AADResourceServerOAuth2AuthorizedClientRepository authorizedRepo;
    private JwtAuthenticationToken jwtAuthenticationToken;
    private MockHttpServletRequest mockHttpServletRequest;

    @BeforeEach
    public void setup() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        addInlinedPropertiesToEnvironment(
            context,
            AAD_PROPERTY_PREFIX + "tenant-id = fake-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id = fake-client-id",
            AAD_PROPERTY_PREFIX + "client-secret = fake-client-secret",
            AAD_PROPERTY_PREFIX + "authorization-clients.fake.scopes = https://xiaozhusampleb2c.onmicrosoft"
                + ".com/0fd938bc-e2e1-4c22-a298-65c50cb7b81a/.default",
            AAD_PROPERTY_PREFIX + "authorization-clients.fake.authorization-grant-type = client_credentials"
        );
        context.register(AADResourceServerClientConfiguration.class);
        context.refresh();

        clientRegistrationsRepo = context.getBean(InMemoryClientRegistrationRepository.class);
    }

    public void setupForAzureAuthorizedClient() {
        InMemoryClientRegistrationRepository clientRegistrationsRepo = mock(InMemoryClientRegistrationRepository.class);
        OAuth2AuthorizedClientService oAuth2AuthorizedClientService = mock(InMemoryOAuth2AuthorizedClientService.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);

        final Jwt mockJwt = mock(Jwt.class);
        jwtAuthenticationToken = new JwtAuthenticationToken(mockJwt);
        mockHttpServletRequest = new MockHttpServletRequest();

        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        when(authorizedClient.getAccessToken()).thenReturn(oAuth2AccessToken);
        when(oAuth2AccessToken.getTokenValue()).thenReturn(CLIENT_CREDENTIAL_ACCESS_TOKEN);

        when(clientRegistrationsRepo.findByRegistrationId(any())).thenReturn(ClientRegistration
            .withRegistrationId("fake")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("https://login.microsoftonline.com/43572109-471e-4c28-adc8-b9d264903c4e/oauth2/v2.0/token")
            .scope("https://xiaozhusampleb2c.onmicrosoft.com/0fd938bc-e2e1-4c22-a298-65c50cb7b81a/.default")
            .clientId("0fd938bc-e2e1-4c22-a298-65c50cb7b81a").build());

        doReturn(authorizedClient).when(oAuth2AuthorizedClientService).loadAuthorizedClient("fake",
            jwtAuthenticationToken.getName());

        authorizedRepo = new AADResourceServerOAuth2AuthorizedClientRepository(oAuth2AuthorizedClientService,
            clientRegistrationsRepo) {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String registrationId,
                                                                             Authentication principal,
                                                                             HttpServletRequest request) {
                ClientRegistration clientRegistration = clientRegistrationsRepo.findByRegistrationId(registrationId);
                if (clientRegistration.getAuthorizationGrantType().getValue().equals
                    (AADAuthorizationGrantType.CLIENT_CREDENTIALS.getValue())) {
                    return oAuth2AuthorizedClientService.loadAuthorizedClient("fake",
                        jwtAuthenticationToken.getName());
                }
                return null;
            }
        };

        this.authorizedClient = authorizedRepo.loadAuthorizedClient("fake",
            jwtAuthenticationToken,
            mockHttpServletRequest
        );
    }

    @Test
    public void testLoadClientCredentialFlow() {
        setupForAzureAuthorizedClient();
        Assertions.assertEquals(CLIENT_CREDENTIAL_ACCESS_TOKEN,
            authorizedClient.getAccessToken().getTokenValue());
    }
}

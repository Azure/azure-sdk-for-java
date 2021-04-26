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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

public class AADOAuth2AuthorizedOboClientRepositoryTest {

    private static final String OBO_ACCESS_TOKEN_1 =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJhcGk6Ly9zYW1wbGUtY2xpZW50LWlkIiwiaXNzIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvMWQxYTA2YTktYjIwYS00NTEzLThhNjQtZGFiMDhkMzJjOGI2LyIsImlhdCI6MTYwNzA3NTc1MiwibmJmIjoxNjA3MDc1NzUyLCJleHAiOjE2MDcwNzk2NTIsImFjciI6IjEiLCJhaW8iOiJBVFFBeS84UkFBQUFkSllKZkluaHhoWHBQTStVUVR0TmsrcnJnWG1FQmRpL0JhQWJUOGtQT2t1amJhQ2pBSTNBeUZWcnE0NGZHdHNOIiwiYW1yIjpbInB3ZCJdLCJhcHBpZCI6ImZmMzhjYjg2LTljMzgtNGUyMS1iZTY4LWM1ODFhNTVmYjVjMCIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiY2hlbiIsImdpdmVuX25hbWUiOiJhbXkiLCJpcGFkZHIiOiIxNjcuMjIwLjI1NS42OCIsIm5hbWUiOiJhbXkgY2hlbiIsIm9pZCI6ImFiZDI4ZGUxLTljMzctNDg5ZC04ZWVjLWZlZWVmNGQyNzRhMyIsInJoIjoiMC5BQUFBcVFZYUhRcXlFMFdLWk5xd2pUTEl0b2JMT1A4NG5DRk92bWpGZ2FWZnRjQjRBQUkuIiwic2NwIjoiUmVzb3VyY2VBY2Nlc3NDdXN0b21SZXNvdXJjZXMucmVhZCBSZXNvdXJjZUFjY2Vzc0dyYXBoLnJlYWQgUmVzb3VyY2VBY2Nlc3NPdGhlclJlc291cmNlcy5yZWFkIiwic3ViIjoiS0xyMXZFQTN3Wk1MdWFFZU1IUl80ZmdTdVVVVnNJWDhHREVlOWU5M1BPYyIsInRpZCI6IjFkMWEwNmE5LWIyMGEtNDUxMy04YTY0LWRhYjA4ZDMyYzhiNiIsInVuaXF1ZV9uYW1lIjoiYW15QG1vYXJ5Lm9ubWljcm9zb2Z0LmNvbSIsInVwbiI6ImFteUBtb2FyeS5vbm1pY3Jvc29mdC5jb20iLCJ1dGkiOiJFTG1xXzZVUkJFS19kN3I4ZlFJR0FBIiwidmVyIjoiMS4wIn0.fM_huHrr5M243oM3rMagGGckoxkLanFkurMJz4EBthrdQlFJzl6eo13pmU0Taq2ognAzsxUka0yihImrvhqzub9IGxRtCdQ3NAvD1fAiVdSUt_aBetIFCi5Pdc6I7KJDiGMQh8RTmduM7IOdxV_3-rug6dZXhW5TTmeq5PfLGYlrKOkC2za7M5G7gn7li1D5osh98HorFBWZoCDhe1iJPd_p_m0EffwTbKFwyvOGN-PKxyzOnoCOma_VYvRABUtBa8rNBFTaH5R9EAvsOmIZ_mI98Irl_8QNr9No-R0nXOrqKCFx5sMYkUuT7mvSaVPAlNr2X8eJjY3Wi-6ishufWQ";

    private static final String AAD_PROPERTY_PREFIX = "azure.activedirectory.";
    public static final String FAKE_GRAPH = "fake-graph";
    public static final String FAKE_PRINCIPAL_NAME = "fake-principal-name";
    public static final String FAKE_TOKEN_VALUE = "fake-token-value";

    private InMemoryClientRegistrationRepository clientRegistrationsRepo;
    private OAuth2AuthorizedClient client;
    private InMemoryOAuth2AuthorizedClientService inMemoryOAuth2AuthorizedClientService;
    private JwtAuthenticationToken jwtAuthenticationToken;
    private OAuth2AuthorizedClient mockOAuth2AuthorizedClient;

    @BeforeEach
    public void setup() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        addInlinedPropertiesToEnvironment(
            context,
            AAD_PROPERTY_PREFIX + "tenant-id = fake-tenant-id",
            AAD_PROPERTY_PREFIX + "client-id = fake-client-id",
            AAD_PROPERTY_PREFIX + "client-secret = fake-client-secret",
            AAD_PROPERTY_PREFIX + "authorization-clients.fake-graph.scopes = https://graph.microsoft.com/.default"
        );
        context.register(AADResourceServerClientConfiguration.class);
        context.refresh();

        clientRegistrationsRepo = context.getBean(InMemoryClientRegistrationRepository.class);
        setupForAzureAuthorizedClient();
    }

    @SuppressWarnings("unchecked")
    public void setupForAzureAuthorizedClient() {

        OAuth2AccessToken mockAccessToken = mock(OAuth2AccessToken.class);
        when(mockAccessToken.getTokenValue()).thenReturn(OBO_ACCESS_TOKEN_1);

        InMemoryClientRegistrationRepository mockClientRegistrationsRepo = mock(InMemoryClientRegistrationRepository.class);

        when(mockClientRegistrationsRepo.findByRegistrationId(any())).thenReturn(ClientRegistration
            .withRegistrationId(FAKE_GRAPH)
            .authorizationGrantType(new AuthorizationGrantType("on-behalf-of"))
            .redirectUri("{baseUrl}/login/oauth2/code/")
            .tokenUri("https://login.microsoftonline.com/308df08a-1332-4a15-bb06-2ad7e8b71bcf/oauth2/v2.0/token")
            .jwkSetUri("https://login.microsoftonline.com/308df08a-1332-4a15-bb06-2ad7e8b71bcf/discovery/v2.0/keys")
            .authorizationUri("https://login.microsoftonline.com/308df08a-1332-4a15-bb06-2ad7e8b71bcf/oauth2/v2"
                + ".0/authorize")
            .scope("User.read")
            .clientId("2c47b831-d838-464f-a684-fa79cbd64f20").build());

        ClientRegistration mockClientRegistration = mock(ClientRegistration.class);
        when(mockClientRegistration.getRegistrationId()).thenReturn(FAKE_GRAPH);

        mockOAuth2AuthorizedClient = mock(OAuth2AuthorizedClient.class);
        when(mockOAuth2AuthorizedClient.getClientRegistration()).thenReturn(mockClientRegistration);
        when(mockOAuth2AuthorizedClient.getAccessToken()).thenReturn(mockAccessToken);

        Authentication mockPrincipal = mock(Authentication.class);
        when(mockPrincipal.getName()).thenReturn(FAKE_PRINCIPAL_NAME);

        inMemoryOAuth2AuthorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationsRepo);
        inMemoryOAuth2AuthorizedClientService.saveAuthorizedClient(mockOAuth2AuthorizedClient, mockPrincipal);

        final Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn(FAKE_TOKEN_VALUE);
        when(mockJwt.getSubject()).thenReturn(FAKE_PRINCIPAL_NAME);

        jwtAuthenticationToken = new JwtAuthenticationToken(mockJwt);
        client = inMemoryOAuth2AuthorizedClientService.loadAuthorizedClient(FAKE_GRAPH,
            jwtAuthenticationToken.getName()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadAzureAuthorizedClient() {
        Assertions.assertEquals(OBO_ACCESS_TOKEN_1, client.getAccessToken().getTokenValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAuthorizedClientCache() {
        Assertions.assertEquals(OBO_ACCESS_TOKEN_1, client.getAccessToken().getTokenValue());
        client = inMemoryOAuth2AuthorizedClientService.loadAuthorizedClient(FAKE_GRAPH,
            jwtAuthenticationToken.getName()
        );
        Assertions.assertEquals(OBO_ACCESS_TOKEN_1, client.getAccessToken().getTokenValue());
        Assertions.assertEquals(mockOAuth2AuthorizedClient, client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadNotExistClientRegistration() {
        final Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getSubject()).thenReturn(FAKE_PRINCIPAL_NAME);
        OAuth2AuthorizedClient client = inMemoryOAuth2AuthorizedClientService.loadAuthorizedClient("fake-graph-fake", new
            JwtAuthenticationToken(mockJwt).getName());
        Assertions.assertNull(client);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNotExistClientApplication() {
        final Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn(FAKE_TOKEN_VALUE);
        when(mockJwt.getSubject()).thenReturn("not-exist-client");
        OAuth2AuthorizedClient client = inMemoryOAuth2AuthorizedClientService.loadAuthorizedClient(FAKE_GRAPH, new
            JwtAuthenticationToken(mockJwt).getName());
        Assertions.assertNull(client);
    }

}

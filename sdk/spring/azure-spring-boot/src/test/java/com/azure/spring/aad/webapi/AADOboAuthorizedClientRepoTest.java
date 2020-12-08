// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.webapp.AzureClientRegistrationRepository;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AADOboAuthorizedClientRepoTest {

    private AADOAuth2OboAuthorizedClientRepository authorizedRepo;
    private MockHttpServletRequest request;

    private static final String ACCESS_TOKEN = "fake-access-token";
    private static final String OBO_ACCESS_TOKEN =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCIsImtpZCI6ImtnMkxZczJUMENUaklmajRydDZKSXluZW4zOCJ9.eyJhdWQiOiJhcGk6Ly9zYW1wbGUtY2xpZW50LWlkIiwiaXNzIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvMWQxYTA2YTktYjIwYS00NTEzLThhNjQtZGFiMDhkMzJjOGI2LyIsImlhdCI6MTYwNzA3NTc1MiwibmJmIjoxNjA3MDc1NzUyLCJleHAiOjE2MDcwNzk2NTIsImFjciI6IjEiLCJhaW8iOiJBVFFBeS84UkFBQUFkSllKZkluaHhoWHBQTStVUVR0TmsrcnJnWG1FQmRpL0JhQWJUOGtQT2t1amJhQ2pBSTNBeUZWcnE0NGZHdHNOIiwiYW1yIjpbInB3ZCJdLCJhcHBpZCI6ImZmMzhjYjg2LTljMzgtNGUyMS1iZTY4LWM1ODFhNTVmYjVjMCIsImFwcGlkYWNyIjoiMSIsImZhbWlseV9uYW1lIjoiY2hlbiIsImdpdmVuX25hbWUiOiJhbXkiLCJpcGFkZHIiOiIxNjcuMjIwLjI1NS42OCIsIm5hbWUiOiJhbXkgY2hlbiIsIm9pZCI6ImFiZDI4ZGUxLTljMzctNDg5ZC04ZWVjLWZlZWVmNGQyNzRhMyIsInJoIjoiMC5BQUFBcVFZYUhRcXlFMFdLWk5xd2pUTEl0b2JMT1A4NG5DRk92bWpGZ2FWZnRjQjRBQUkuIiwic2NwIjoiUmVzb3VyY2VBY2Nlc3NDdXN0b21SZXNvdXJjZXMucmVhZCBSZXNvdXJjZUFjY2Vzc0dyYXBoLnJlYWQgUmVzb3VyY2VBY2Nlc3NPdGhlclJlc291cmNlcy5yZWFkIiwic3ViIjoiS0xyMXZFQTN3Wk1MdWFFZU1IUl80ZmdTdVVVVnNJWDhHREVlOWU5M1BPYyIsInRpZCI6IjFkMWEwNmE5LWIyMGEtNDUxMy04YTY0LWRhYjA4ZDMyYzhiNiIsInVuaXF1ZV9uYW1lIjoiYW15QG1vYXJ5Lm9ubWljcm9zb2Z0LmNvbSIsInVwbiI6ImFteUBtb2FyeS5vbm1pY3Jvc29mdC5jb20iLCJ1dGkiOiJFTG1xXzZVUkJFS19kN3I4ZlFJR0FBIiwidmVyIjoiMS4wIn0.fM_huHrr5M243oM3rMagGGckoxkLanFkurMJz4EBthrdQlFJzl6eo13pmU0Taq2ognAzsxUka0yihImrvhqzub9IGxRtCdQ3NAvD1fAiVdSUt_aBetIFCi5Pdc6I7KJDiGMQh8RTmduM7IOdxV_3-rug6dZXhW5TTmeq5PfLGYlrKOkC2za7M5G7gn7li1D5osh98HorFBWZoCDhe1iJPd_p_m0EffwTbKFwyvOGN-PKxyzOnoCOma_VYvRABUtBa8rNBFTaH5R9EAvsOmIZ_mI98Irl_8QNr9No-R0nXOrqKCFx5sMYkUuT7mvSaVPAlNr2X8eJjY3Wi-6ishufWQ";

    private Jwt jwt;
    private BearerTokenAuthentication bearerTokenAuthentication;
    private UserAssertion userAssertion;

    private ConfidentialClientApplication confidentialClientApplication;
    private OAuth2AccessToken oAuth2AccessToken;
    private CompletableFuture acquireTokenFuture;
    private IAuthenticationResult authenticationResult;

    private Map<String, Object> claims = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();

    private Set<String> otherScopes;

    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            context,
            "azure.activedirectory.user-group.allowed-groups = group1",
            "azure.activedirectory.authorization-server-uri = https://login.microsoftonline.com",
            "azure.activedirectory.tenant-id = fake-tenant-id",
            "azure.activedirectory.client-id = fake-client-id",
            "azure.activedirectory.client-secret = fake-client-secret",
            "azure.activedirectory.authorization.fake-client-id.scopes = https://graph.microsoft.com/.default",
            "azure.activedirectory.app-id-uri = fake-client-id"
        );
        context.register(AzureActiveDirectoryResourceServerClientConfiguration.class);
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);

        authorizedRepo = spy(new AADOAuth2OboAuthorizedClientRepository(clientRepo));

        request = new MockHttpServletRequest();
        jwt = mock(Jwt.class);

        otherScopes = new HashSet<>(Arrays.asList("https://graph.microsoft.com/.default"));

        claims.put("iss", "fake-issuer");
        claims.put("tid", "fake-tid");
        claims.put("aud", "fake-aud");
        headers.put("kid", "kg2LYs2T0CTjIfj4rt6JIynen38");
        when(jwt.getClaim("scp")).thenReturn("access_as_user");
        when(jwt.getId()).thenReturn("fake_principal_name");
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getHeaders()).thenReturn(headers);
        when(jwt.getExpiresAt()).thenReturn(Instant.MAX);
        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.containsClaim("scp")).thenReturn(true);

        bearerTokenAuthentication = mock(BearerTokenAuthentication.class);
        userAssertion = new UserAssertion(ACCESS_TOKEN);

        confidentialClientApplication = mock(ConfidentialClientApplication.class);
        oAuth2AccessToken = mock(OAuth2AccessToken.class);
        acquireTokenFuture = mock(CompletableFuture.class);
        authenticationResult = mock(IAuthenticationResult.class);
        AzureOAuth2AuthenticatedPrincipal azureOAuth2AuthenticatedPrincipal = mock(AzureOAuth2AuthenticatedPrincipal
            .class);

        when(bearerTokenAuthentication.getToken()).thenReturn(oAuth2AccessToken);

        when(oAuth2AccessToken.getTokenValue()).thenReturn(ACCESS_TOKEN);

        when(authorizedRepo.getClientApplication("fake-client-id")).thenReturn(confidentialClientApplication);

        when(confidentialClientApplication.acquireToken(any(OnBehalfOfParameters.class))).thenReturn
            (acquireTokenFuture);
        when(acquireTokenFuture.get()).thenReturn(authenticationResult);
        when(authenticationResult.accessToken()).thenReturn(OBO_ACCESS_TOKEN);

        //TODO we can't set principalName here .
        when(bearerTokenAuthentication.getPrincipal()).thenReturn(azureOAuth2AuthenticatedPrincipal);
        when(azureOAuth2AuthenticatedPrincipal.getName()).thenReturn("fake_prinpal_name");

    }

    @Test
    public void loadAzureAuthorizedClient() {

        OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient("fake-client-id",
            new JwtAuthenticationToken(jwt), request);

        Assertions.assertNotNull(client);
        Assertions.assertNotNull(client.getAccessToken());

    }

}

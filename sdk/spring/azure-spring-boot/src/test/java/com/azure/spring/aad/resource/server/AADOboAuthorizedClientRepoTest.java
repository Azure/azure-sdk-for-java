package com.azure.spring.aad.resource.server;

import com.azure.spring.aad.implementation.AzureActiveDirectoryConfiguration;
import com.azure.spring.aad.implementation.AzureClientRegistrationRepository;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AADOboAuthorizedClientRepoTest {

    private OAuth2AuthorizedClientRepository authorizedRepo;
    private MockHttpServletRequest request;

    private Jwt jwt = mock(Jwt.class);

    private Map<String, Object> claims = new HashMap<>();
    private Map<String, Object> headers = new HashMap<>();


    @BeforeEach
    public void setup() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            context,
            "azure.activedirectory.user-group.allowed-groups = group1",
            "azure.activedirectory.authorization-server-uri = https://login.microsoftonline.com",
            "azure.activedirectory.tenant-id = fake-tenant-id",
            "azure.activedirectory.client-id = fake-client-id",
            "azure.activedirectory.client-secret = fake-client-secret",
            "azure.activedirectory.authorization.graph.scopes = https://graph.microsoft.com/.default"
        );
        context.register(AzureActiveDirectoryConfiguration.class);
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);

        authorizedRepo = new AADOAuth2OboAuthorizedClientRepository(clientRepo);

        request = new MockHttpServletRequest();


    }

    @Before
    public void init() {
        claims.put("iss", "fake-issuer");
        claims.put("tid", "fake-tid");
        claims.put("aud", "fake-aud");
        headers.put("kid", "kg2LYs2T0CTjIfj4rt6JIynen38");
        when(jwt.getClaim("scp")).thenReturn("access_as_user");
        when(jwt.getTokenValue()).thenReturn("fake-token-value");
        when(jwt.getIssuedAt()).thenReturn(Instant.now());
        when(jwt.getHeaders()).thenReturn(headers);
        when(jwt.getExpiresAt()).thenReturn(Instant.MAX);
        when(jwt.getClaims()).thenReturn(claims);
        when(jwt.containsClaim("scp")).thenReturn(true);

    }

    @Test
    public void loadAzureAuthorizedClient() {

        OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient("graph",
            new JwtAuthenticationToken(jwt), request);

        Assertions.assertNotNull(client);
        Assertions.assertNotNull(client.getAccessToken());

    }

}

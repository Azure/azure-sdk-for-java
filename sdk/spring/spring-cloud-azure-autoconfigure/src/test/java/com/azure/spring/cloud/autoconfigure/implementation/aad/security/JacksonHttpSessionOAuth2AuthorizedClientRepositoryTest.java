// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson.SerializerUtils.deserializeOAuth2AuthorizedClientMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

class JacksonHttpSessionOAuth2AuthorizedClientRepositoryTest {
    private final String principalName1 = "principalName-1";
    private final String principalName2 = "principalName-2";

    private final ClientRegistration registration1 = ClientRegistration
            .withRegistrationId("registration-id-1")
            .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope("scope-1")
            .authorizationUri("https://example1.com/login/oauth/authorize")
            .tokenUri("https://example1.com/login/oauth/access_token")
            .jwkSetUri("https://example1.com/oauth2/jwk")
            .issuerUri("https://example1.com")
            .userInfoUri("https://api.example1.com/user")
            .userNameAttributeName("id-1")
            .clientName("Client Name 1")
            .clientId("client-id-1")
            .clientSecret("client-secret-1")
            .build();

    private final ClientRegistration registration2 = ClientRegistration
            .withRegistrationId("registration-id-2")
            .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .scope("scope-2")
            .authorizationUri("https://example2.com/login/oauth/authorize")
            .tokenUri("https://example2.com/login/oauth/access_token")
            .userInfoUri("https://api.example2.com/user")
            .userNameAttributeName("id-2")
            .clientName("Client Name 2")
            .clientId("client-id-2")
            .clientSecret("client-secret-2")
            .build();

    private final String registrationId1 = this.registration1.getRegistrationId();

    private final String registrationId2 = this.registration2.getRegistrationId();

    private final OAuth2AccessToken oAuth2AccessToken1 = new OAuth2AccessToken(BEARER, "tokenValue1", Instant.now(), Instant.now().plusMillis(3_600_000));
    private final OAuth2AccessToken oAuth2AccessToken2 = new OAuth2AccessToken(BEARER, "tokenValue2", Instant.now(), Instant.now().plusMillis(3_600_000));

    private final OAuth2AuthorizedClient authorizedClient1 = new OAuth2AuthorizedClient(this.registration1, this.principalName1, oAuth2AccessToken1);

    private final OAuth2AuthorizedClient authorizedClient2 = new OAuth2AuthorizedClient(this.registration2, this.principalName2, oAuth2AccessToken2);

    private final JacksonHttpSessionOAuth2AuthorizedClientRepository authorizedClientRepository =
            new JacksonHttpSessionOAuth2AuthorizedClientRepository();
    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
    }

    @Test
    void loadAuthorizedClientWhenClientRegistrationIdIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.loadAuthorizedClient(null, null, this.request));
    }

    @Test
    void loadAuthorizedClientWhenPrincipalNameIsNullThenExceptionNotThrown() {
        this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, this.request);
    }

    @Test
    void loadAuthorizedClientWhenRequestIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, null));
    }

    @Test
    void loadAuthorizedClientWhenClientRegistrationNotFoundThenReturnNull() {
        OAuth2AuthorizedClient authorizedClient =
                this.authorizedClientRepository.loadAuthorizedClient("registration-not-found", null, this.request);
        assertThat(authorizedClient).isNull();
    }

    @Test
    void loadAuthorizedClientWhenSavedThenReturnAuthorizedClient() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);
        OAuth2AuthorizedClient loadedAuthorizedClient =
                this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, this.request);
        assertSame(authorizedClient1, loadedAuthorizedClient);
    }

    @Test
    void saveAuthorizedClientWhenAuthorizedClientIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.saveAuthorizedClient(null, null, this.request, this.response));
    }

    @Test
    void saveAuthorizedClientWhenAuthenticationIsNullThenExceptionNotThrown() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);
    }

    @Test
    void saveAuthorizedClientWhenRequestIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, null, this.response));
    }

    @Test
    void saveAuthorizedClientWhenResponseIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, null));
    }

    @Test
    void saveAuthorizedClientWhenSavedThenSavedToSession() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);

        HttpSession session = this.request.getSession(false);
        assertThat(session).isNotNull();
        String authorizedClientsString = (String) session.getAttribute(
                JacksonHttpSessionOAuth2AuthorizedClientRepository.class.getName() + ".AUTHORIZED_CLIENTS");
        Map<String, OAuth2AuthorizedClient> authorizedClients = deserializeOAuth2AuthorizedClientMap(authorizedClientsString);
        assertThat(authorizedClients).isNotEmpty();
        assertThat(authorizedClients).hasSize(1);
        OAuth2AuthorizedClient loadedAuthorizedClient = authorizedClients.values().iterator().next();
        assertSame(authorizedClient1, loadedAuthorizedClient);
    }

    @Test
    void removeAuthorizedClientWhenClientRegistrationIdIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.removeAuthorizedClient(null, null, this.request, this.response));
    }

    @Test
    void removeAuthorizedClientWhenPrincipalNameIsNullThenExceptionNotThrown() {
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId1, null, this.request, this.response);
    }

    @Test
    void removeAuthorizedClientWhenRequestIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                this.authorizedClientRepository.removeAuthorizedClient(this.registrationId1, null, null, this.response));
    }

    @Test
    void removeAuthorizedClientWhenResponseIsNullThenExceptionNotThrown() {
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId1, null, this.request, null);
    }

    @Test
    void removeAuthorizedClientWhenNotSavedThenSessionNotCreated() {
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId2, null, this.request, this.response);
        assertThat(this.request.getSession(false)).isNull();
    }

    @Test
    void removeAuthorizedClientWhenClient1SavedAndClient2RemovedThenClient1NotRemoved() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);
        // Remove registrationId2 (never added so is not removed either)
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId2, null, this.request, this.response);
        OAuth2AuthorizedClient loadedAuthorizedClient1 =
                this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, this.request);
        assertThat(loadedAuthorizedClient1).isNotNull();
        assertSame(authorizedClient1, loadedAuthorizedClient1);
    }

    @Test
    void removeAuthorizedClientWhenSavedThenRemoved() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);
        OAuth2AuthorizedClient loadedAuthorizedClient =
                this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, this.request);
        assertSame(authorizedClient1, loadedAuthorizedClient);
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId1, null, this.request, this.response);
        loadedAuthorizedClient = this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, this.request);
        assertThat(loadedAuthorizedClient).isNull();
    }

    @Test
    void removeAuthorizedClientWhenSavedThenRemovedFromSession() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);
        OAuth2AuthorizedClient loadedAuthorizedClient =
                this.authorizedClientRepository.loadAuthorizedClient(this.registrationId1, null, this.request);
        assertSame(authorizedClient1, loadedAuthorizedClient);
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId1, null, this.request, this.response);
        HttpSession session = this.request.getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(JacksonHttpSessionOAuth2AuthorizedClientRepository.class.getName() + ".AUTHORIZED_CLIENTS")).isNull();
    }

    @Test
    void removeAuthorizedClientWhenClient1Client2SavedAndClient1RemovedThenClient2NotRemoved() {
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient1, null, this.request, this.response);
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient2, null, this.request, this.response);
        this.authorizedClientRepository.removeAuthorizedClient(this.registrationId1, null, this.request, this.response);
        OAuth2AuthorizedClient loadedAuthorizedClient2 =
                this.authorizedClientRepository.loadAuthorizedClient(this.registrationId2, null, this.request);
        assertThat(loadedAuthorizedClient2).isNotNull();
        assertSame(authorizedClient2, loadedAuthorizedClient2);
    }

    private void assertSame(OAuth2AuthorizedClient client1, OAuth2AuthorizedClient client2) {
        assertEquals(client1.getClientRegistration().getClientId(), client2.getClientRegistration().getClientId());
        assertEquals(client1.getClientRegistration().getRegistrationId(), client2.getClientRegistration().getRegistrationId());
        assertEquals(client1.getClientRegistration().getClientName(), client2.getClientRegistration().getClientName());
        assertEquals(client1.getClientRegistration().getClientSecret(), client2.getClientRegistration().getClientSecret());
        assertEquals(client1.getClientRegistration().getClientAuthenticationMethod(), client2.getClientRegistration().getClientAuthenticationMethod());
        assertEquals(client1.getClientRegistration().getAuthorizationGrantType(), client2.getClientRegistration().getAuthorizationGrantType());
        assertEquals(client1.getPrincipalName(), client2.getPrincipalName());
        assertEquals(client1.getAccessToken().getTokenType(), client2.getAccessToken().getTokenType());
        assertEquals(client1.getAccessToken().getTokenValue(), client2.getAccessToken().getTokenValue());
        assertEquals(client1.getAccessToken().getScopes(), client2.getAccessToken().getScopes());
    }
}

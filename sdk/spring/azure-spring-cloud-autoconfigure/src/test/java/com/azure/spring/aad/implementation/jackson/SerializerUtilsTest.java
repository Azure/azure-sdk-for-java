// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.implementation.jackson;

import com.azure.spring.aad.AADAuthorizationGrantType;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.aad.implementation.jackson.SerializerUtils.deserializeOAuth2AuthorizedClientMap;
import static com.azure.spring.aad.implementation.jackson.SerializerUtils.serializeOAuth2AuthorizedClientMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializerUtilsTest {

    private static final Logger LOGGER = Logger.getLogger(SerializerUtilsTest.class.getName());

    @Test
    public void serializeAndDeserializeTest() {
        Map<String, OAuth2AuthorizedClient> authorizedClients = new HashMap<>();
        authorizedClients.put(AZURE_CLIENT_REGISTRATION_ID,
            createOAuth2AuthorizedClient(AZURE_CLIENT_REGISTRATION_ID,
                AADAuthorizationGrantType.AUTHORIZATION_CODE.getValue()));
        authorizedClients.put("graph",
            createOAuth2AuthorizedClient("graph",
                AADAuthorizationGrantType.AZURE_DELEGATED.getValue()));
        authorizedClients.put("arm",
            createOAuth2AuthorizedClient("arm",
                AADAuthorizationGrantType.CLIENT_CREDENTIALS.getValue()));
        authorizedClients.put("office",
            createOAuth2AuthorizedClient("office",
                AADAuthorizationGrantType.ON_BEHALF_OF.getValue()));
        String serializedOAuth2AuthorizedClients = serializeOAuth2AuthorizedClientMap(authorizedClients);
        LOGGER.info(serializedOAuth2AuthorizedClients);
        Map<String, OAuth2AuthorizedClient> deserializedOAuth2AuthorizedClients =
            deserializeOAuth2AuthorizedClientMap(serializedOAuth2AuthorizedClients);
        String serializedOAuth2AuthorizedClients1 = serializeOAuth2AuthorizedClientMap(deserializedOAuth2AuthorizedClients);
        assertEquals(serializedOAuth2AuthorizedClients, serializedOAuth2AuthorizedClients1);
    }

    private OAuth2AuthorizedClient createOAuth2AuthorizedClient(String registrationId, String authorizationGrantType) {
        ClientRegistration clientRegistration = createClientRegistration(registrationId, authorizationGrantType);
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
            "tokenValue " + registrationId, Instant.now(), Instant.now().plusSeconds(60 * 60));
        return new OAuth2AuthorizedClient(clientRegistration, "principalName " + registrationId, oAuth2AccessToken);
    }

    private ClientRegistration createClientRegistration(String registrationId, String authorizationGrantType) {
        return ClientRegistration.withRegistrationId(registrationId)
                                 .clientName(registrationId)
                                 .authorizationGrantType(new AuthorizationGrantType(authorizationGrantType))
                                 .scope("scope" + registrationId)
                                 .redirectUri("redirectUri " + registrationId)
                                 .userNameAttributeName("userNameAttributeName " + registrationId)
                                 .clientId("clientId " + registrationId)
                                 .clientSecret("clientSecret " + registrationId)
                                 .authorizationUri("authorizationUri " + registrationId)
                                 .tokenUri("tokenUri " + registrationId)
                                 .jwkSetUri("jwkSetUri " + registrationId)
                                 .providerConfigurationMetadata(new HashMap<>())
                                 .build();
    }
}

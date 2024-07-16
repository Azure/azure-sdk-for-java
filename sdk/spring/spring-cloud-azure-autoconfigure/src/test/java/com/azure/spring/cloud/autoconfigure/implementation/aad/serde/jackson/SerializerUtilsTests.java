// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson.SerializerUtils.deserializeOAuth2AuthorizedClientMap;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.serde.jackson.SerializerUtils.serializeOAuth2AuthorizedClientMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.JWT_BEARER;

class SerializerUtilsTests {

    private static final Logger LOGGER = Logger.getLogger(SerializerUtilsTests.class.getName());

    @Test
    void serializeAndDeserializeTest() {
        Map<String, OAuth2AuthorizedClient> authorizedClients = new HashMap<>();
        authorizedClients.put(AZURE_CLIENT_REGISTRATION_ID,
            createOAuth2AuthorizedClient(AZURE_CLIENT_REGISTRATION_ID, AUTHORIZATION_CODE.getValue()));
        authorizedClients.put("graph",
            createOAuth2AuthorizedClient("graph",
                Constants.AZURE_DELEGATED.getValue()));
        authorizedClients.put("arm",
            createOAuth2AuthorizedClient("arm", CLIENT_CREDENTIALS.getValue()));
        authorizedClients.put("office",
            createOAuth2AuthorizedClient("office", JWT_BEARER.getValue()));
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

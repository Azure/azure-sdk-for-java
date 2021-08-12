// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.implementation.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.io.IOException;

public class AADClientRegistrationDeserializer extends JsonDeserializer<ClientRegistration> {

    private static final StdConverter<JsonNode, ClientAuthenticationMethod> CLIENT_AUTHENTICATION_METHOD_CONVERTER =
        new StdConverters.ClientAuthenticationMethodConverter();

    private static final StdConverter<JsonNode, AuthorizationGrantType> AUTHORIZATION_GRANT_TYPE_CONVERTER =
        new StdConverters.AuthorizationGrantTypeConverter();

    private static final StdConverter<JsonNode, AuthenticationMethod> AUTHENTICATION_METHOD_CONVERTER =
        new StdConverters.AuthenticationMethodConverter();

    @Override
    public ClientRegistration deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode clientRegistrationNode = mapper.readTree(parser);
        JsonNode providerDetailsNode = JsonNodeUtils.findObjectNode(clientRegistrationNode, "providerDetails");
        JsonNode userInfoEndpointNode = JsonNodeUtils.findObjectNode(providerDetailsNode, "userInfoEndpoint");
        return ClientRegistration
            .withRegistrationId(JsonNodeUtils.findStringValue(clientRegistrationNode, "registrationId"))
            .clientId(JsonNodeUtils.findStringValue(clientRegistrationNode, "clientId"))
            .clientSecret(JsonNodeUtils.findStringValue(clientRegistrationNode, "clientSecret"))
            .clientAuthenticationMethod(CLIENT_AUTHENTICATION_METHOD_CONVERTER
                .convert(JsonNodeUtils.findObjectNode(clientRegistrationNode, "clientAuthenticationMethod")))
            .authorizationGrantType(AUTHORIZATION_GRANT_TYPE_CONVERTER
                .convert(JsonNodeUtils.findObjectNode(clientRegistrationNode, "authorizationGrantType")))
            .redirectUri(JsonNodeUtils.findStringValue(clientRegistrationNode, "redirectUri"))
            .scope(JsonNodeUtils.findValue(clientRegistrationNode, "scopes", JsonNodeUtils.STRING_SET, mapper))
            .clientName(JsonNodeUtils.findStringValue(clientRegistrationNode, "clientName"))
            .authorizationUri(JsonNodeUtils.findStringValue(providerDetailsNode, "authorizationUri"))
            .tokenUri(JsonNodeUtils.findStringValue(providerDetailsNode, "tokenUri"))
            .userInfoUri(JsonNodeUtils.findStringValue(userInfoEndpointNode, "uri"))
            .userInfoAuthenticationMethod(AUTHENTICATION_METHOD_CONVERTER
                .convert(JsonNodeUtils.findObjectNode(userInfoEndpointNode, "authenticationMethod")))
            .userNameAttributeName(JsonNodeUtils.findStringValue(userInfoEndpointNode, "userNameAttributeName"))
            .jwkSetUri(JsonNodeUtils.findStringValue(providerDetailsNode, "jwkSetUri"))
            .issuerUri(JsonNodeUtils.findStringValue(providerDetailsNode, "issuerUri"))
            .providerConfigurationMetadata(JsonNodeUtils.findValue(providerDetailsNode, "configurationMetadata",
                JsonNodeUtils.STRING_OBJECT_MAP, mapper))
            .build();
    }
}

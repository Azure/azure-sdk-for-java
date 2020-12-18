// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.io.IOException;

import static com.azure.spring.aad.webapp.jackson.JsonNodeUtils.MAP_TYPE_REFERENCE;
import static com.azure.spring.aad.webapp.jackson.JsonNodeUtils.SET_TYPE_REFERENCE;
import static com.azure.spring.aad.webapp.jackson.JsonNodeUtils.findObjectNode;
import static com.azure.spring.aad.webapp.jackson.JsonNodeUtils.findStringValue;
import static com.azure.spring.aad.webapp.jackson.JsonNodeUtils.findValue;

/**
 * A {@code JsonDeserializer} for {@link OAuth2AuthorizationRequest}.
 *
 * @see OAuth2AuthorizationRequest
 * @see OAuth2AuthorizationRequestMixin
 */
final class OAuth2AuthorizationRequestDeserializer extends JsonDeserializer<OAuth2AuthorizationRequest> {
    private static final StdConverter<JsonNode, AuthorizationGrantType> AUTHORIZATION_GRANT_TYPE_CONVERTER =
        new StdConverters.AuthorizationGrantTypeConverter();

    @Override
    public OAuth2AuthorizationRequest deserialize(JsonParser parser,
                                                  DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode authorizationRequestNode = mapper.readTree(parser);

        AuthorizationGrantType authorizationGrantType = AUTHORIZATION_GRANT_TYPE_CONVERTER.convert(
            findObjectNode(authorizationRequestNode, "authorizationGrantType"));

        OAuth2AuthorizationRequest.Builder builder;
        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType)) {
            builder = OAuth2AuthorizationRequest.authorizationCode();
        } else if (AuthorizationGrantType.IMPLICIT.equals(authorizationGrantType)) {
            builder = OAuth2AuthorizationRequest.implicit();
        } else {
            throw new JsonParseException(parser, "Invalid authorizationGrantType");
        }

        return builder
            .authorizationUri(findStringValue(authorizationRequestNode, "authorizationUri"))
            .clientId(findStringValue(authorizationRequestNode, "clientId"))
            .redirectUri(findStringValue(authorizationRequestNode, "redirectUri"))
            .scopes(findValue(authorizationRequestNode, "scopes", SET_TYPE_REFERENCE, mapper))
            .state(findStringValue(authorizationRequestNode, "state"))
            .additionalParameters(findValue(authorizationRequestNode, "additionalParameters", MAP_TYPE_REFERENCE,
                mapper))
            .authorizationRequestUri(findStringValue(authorizationRequestNode, "authorizationRequestUri"))
            .attributes(findValue(authorizationRequestNode, "attributes", MAP_TYPE_REFERENCE, mapper))
            .build();
    }
}

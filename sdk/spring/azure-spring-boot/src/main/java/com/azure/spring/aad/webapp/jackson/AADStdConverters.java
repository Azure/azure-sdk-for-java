// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import static com.azure.spring.aad.webapp.jackson.AADJsonNodeUtil.findStringValue;

/**
 * StdConverters used for AAD
 */
class AADStdConverters {

    static final class AccessTokenTypeConverter extends StdConverter<JsonNode, OAuth2AccessToken.TokenType> {
        @Override
        public OAuth2AccessToken.TokenType convert(JsonNode jsonNode) {
            String value = findStringValue(jsonNode, "value");
            if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(value)) {
                return OAuth2AccessToken.TokenType.BEARER;
            }
            return null;
        }
    }

    static final class ClientAuthenticationMethodConverter extends StdConverter<JsonNode, ClientAuthenticationMethod> {
        @Override
        public ClientAuthenticationMethod convert(JsonNode jsonNode) {
            String value = findStringValue(jsonNode, "value");
            if (ClientAuthenticationMethod.BASIC.getValue().equalsIgnoreCase(value)) {
                return ClientAuthenticationMethod.BASIC;
            } else if (ClientAuthenticationMethod.POST.getValue().equalsIgnoreCase(value)) {
                return ClientAuthenticationMethod.POST;
            } else if (ClientAuthenticationMethod.NONE.getValue().equalsIgnoreCase(value)) {
                return ClientAuthenticationMethod.NONE;
            }
            return null;
        }
    }

    static final class AuthorizationGrantTypeConverter extends StdConverter<JsonNode, AuthorizationGrantType> {
        @Override
        public AuthorizationGrantType convert(JsonNode jsonNode) {
            String value = findStringValue(jsonNode, "value");
            if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.AUTHORIZATION_CODE;
            } else if (AuthorizationGrantType.IMPLICIT.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.IMPLICIT;
            } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.CLIENT_CREDENTIALS;
            } else if (AuthorizationGrantType.PASSWORD.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.PASSWORD;
            }
            return null;
        }
    }

    static final class AuthenticationMethodConverter extends StdConverter<JsonNode, AuthenticationMethod> {
        @Override
        public AuthenticationMethod convert(JsonNode jsonNode) {
            String value = findStringValue(jsonNode, "value");
            if (AuthenticationMethod.HEADER.getValue().equalsIgnoreCase(value)) {
                return AuthenticationMethod.HEADER;
            } else if (AuthenticationMethod.FORM.getValue().equalsIgnoreCase(value)) {
                return AuthenticationMethod.FORM;
            } else if (AuthenticationMethod.QUERY.getValue().equalsIgnoreCase(value)) {
                return AuthenticationMethod.QUERY;
            }
            return null;
        }
    }
}

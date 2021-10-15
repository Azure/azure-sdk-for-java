// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.implementation.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

abstract class StdConverters {

    static final class ClientAuthenticationMethodConverter extends StdConverter<JsonNode, ClientAuthenticationMethod> {

        @Override
        public ClientAuthenticationMethod convert(JsonNode jsonNode) {
            String value = JsonNodeUtils.findStringValue(jsonNode, "value");
            if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equalsIgnoreCase(value)
                || ClientAuthenticationMethod.BASIC.getValue().equalsIgnoreCase(value)) {
                return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
            }
            if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equalsIgnoreCase(value)
                || ClientAuthenticationMethod.POST.getValue().equalsIgnoreCase(value)) {
                return ClientAuthenticationMethod.CLIENT_SECRET_POST;
            }
            if (ClientAuthenticationMethod.NONE.getValue().equalsIgnoreCase(value)) {
                return ClientAuthenticationMethod.NONE;
            }
            return null;
        }

    }

    static final class AuthorizationGrantTypeConverter extends StdConverter<JsonNode, AuthorizationGrantType> {

        @Override
        public AuthorizationGrantType convert(JsonNode jsonNode) {
            String value = JsonNodeUtils.findStringValue(jsonNode, "value");
            if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.AUTHORIZATION_CODE;
            }
            if (AuthorizationGrantType.IMPLICIT.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.IMPLICIT;
            }
            if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.CLIENT_CREDENTIALS;
            }
            if (AuthorizationGrantType.PASSWORD.getValue().equalsIgnoreCase(value)) {
                return AuthorizationGrantType.PASSWORD;
            }
            return new AuthorizationGrantType(value);
        }

    }

    static final class AuthenticationMethodConverter extends StdConverter<JsonNode, AuthenticationMethod> {

        @Override
        public AuthenticationMethod convert(JsonNode jsonNode) {
            String value = JsonNodeUtils.findStringValue(jsonNode, "value");
            if (AuthenticationMethod.HEADER.getValue().equalsIgnoreCase(value)) {
                return AuthenticationMethod.HEADER;
            }
            if (AuthenticationMethod.FORM.getValue().equalsIgnoreCase(value)) {
                return AuthenticationMethod.FORM;
            }
            if (AuthenticationMethod.QUERY.getValue().equalsIgnoreCase(value)) {
                return AuthenticationMethod.QUERY;
            }
            return null;
        }

    }

}

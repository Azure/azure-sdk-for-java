// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Set;

/**
 * This mixin class is used to serialize/deserialize {@link OAuth2AccessToken}.
 *
 * @see OAuth2AccessToken
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class OAuth2AccessTokenMixin {

    @JsonCreator
    OAuth2AccessTokenMixin(
        @JsonProperty("tokenType") @JsonDeserialize(converter = StdConverters.AccessTokenTypeConverter.class)
            OAuth2AccessToken.TokenType tokenType,
        @JsonProperty("tokenValue") String tokenValue,
        @JsonProperty("issuedAt") Instant issuedAt,
        @JsonProperty("expiresAt") Instant expiresAt,
        @JsonProperty("scopes") Set<String> scopes) {
    }
}

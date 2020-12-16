// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.time.Instant;

/**
 * This mixin class is used to serialize/deserialize {@link OAuth2RefreshToken}.
 *
 * @see OAuth2RefreshToken
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class OAuth2RefreshTokenMixin {

    @JsonCreator
    OAuth2RefreshTokenMixin(
        @JsonProperty("tokenValue") String tokenValue,
        @JsonProperty("issuedAt") Instant issuedAt) {
    }
}

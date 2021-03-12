// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * This mixin class is used to serialize/deserialize OAuth2RefreshToken.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class AADOAuth2RefreshTokenMixin {

    @JsonCreator
    AADOAuth2RefreshTokenMixin(
        @JsonProperty("tokenValue") String tokenValue,
        @JsonProperty("issuedAt") Instant issuedAt) {
    }
}

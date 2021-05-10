// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

/**
 * This mixin class is used to serialize/deserialize OAuth2AuthorizedClient.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class AADOAuth2AuthorizedClientMixin {

    @JsonCreator
    AADOAuth2AuthorizedClientMixin(
        @JsonProperty("clientRegistration") ClientRegistration clientRegistration,
        @JsonProperty("principalName") String principalName,
        @JsonProperty("accessToken") OAuth2AccessToken accessToken,
        @JsonProperty("refreshToken") OAuth2RefreshToken refreshToken) {
    }
}

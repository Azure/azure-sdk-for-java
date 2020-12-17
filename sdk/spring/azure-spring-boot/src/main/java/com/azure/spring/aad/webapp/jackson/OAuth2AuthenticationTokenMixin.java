// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

/**
 * This mixin class is used to serialize/deserialize {@link OAuth2AuthenticationToken}.
 *
 * @see OAuth2AuthenticationToken
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(value = { "authenticated" }, ignoreUnknown = true)
abstract class OAuth2AuthenticationTokenMixin {

    @JsonCreator
    OAuth2AuthenticationTokenMixin(
        @JsonProperty("principal") OAuth2User principal,
        @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
        @JsonProperty("authorizedClientRegistrationId") String authorizedClientRegistrationId) {
    }
}

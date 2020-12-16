// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.util.Map;

/**
 * This mixin class is used to serialize/deserialize {@link OAuth2UserAuthority}.
 *
 * @see OAuth2UserAuthority
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class OAuth2UserAuthorityMixin {

    @JsonCreator
    OAuth2UserAuthorityMixin(
        @JsonProperty("authority") String authority,
        @JsonProperty("attributes") Map<String, Object> attributes) {
    }
}

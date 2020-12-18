// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * This mixin class is used to serialize/deserialize {@link OAuth2AuthenticationException}.
 *
 * @see OAuth2AuthenticationException
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "cause", "stackTrace", "suppressedExceptions" })
abstract class OAuth2AuthenticationExceptionMixin {

    @JsonCreator
    OAuth2AuthenticationExceptionMixin(
        @JsonProperty("error") OAuth2Error error,
        @JsonProperty("detailMessage") String message) {
    }
}

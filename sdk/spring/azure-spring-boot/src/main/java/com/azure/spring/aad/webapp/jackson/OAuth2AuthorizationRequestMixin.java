// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * This mixin class is used to serialize/deserialize {@link OAuth2AuthorizationRequest}. It also registers a custom
 * deserializer {@link OAuth2AuthorizationRequestDeserializer}.
 *
 * @see OAuth2AuthorizationRequest
 * @see OAuth2AuthorizationRequestDeserializer
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonDeserialize(using = OAuth2AuthorizationRequestDeserializer.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class OAuth2AuthorizationRequestMixin {
}

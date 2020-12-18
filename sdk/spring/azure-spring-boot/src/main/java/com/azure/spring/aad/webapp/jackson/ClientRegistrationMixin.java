// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * This mixin class is used to serialize/deserialize {@link ClientRegistration}. It also registers a custom deserializer
 * {@link ClientRegistrationDeserializer}.
 *
 * @see ClientRegistration
 * @see ClientRegistrationDeserializer
 * @see OAuth2ClientJackson2Module
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonDeserialize(using = ClientRegistrationDeserializer.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class ClientRegistrationMixin {
}

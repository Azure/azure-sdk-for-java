// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This mixin class is used to serialize/deserialize ClientRegistration
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AADClientRegistrationDeserializer.class)
class AADClientRegistrationMixin {
}

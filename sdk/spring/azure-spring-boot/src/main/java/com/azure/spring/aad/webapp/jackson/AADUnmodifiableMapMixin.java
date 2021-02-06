// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.Map;

/**
 * This mixin class is used to serialize/deserialize {@link Collections#unmodifiableMap(Map)}. It also registers a
 * custom deserializer {@link AADUnmodifiableMapDeserializer}.
 */
@JsonDeserialize(using = AADUnmodifiableMapDeserializer.class)
abstract class AADUnmodifiableMapMixin {

    @JsonCreator
    AADUnmodifiableMapMixin(Map<?, ?> map) {
    }
}

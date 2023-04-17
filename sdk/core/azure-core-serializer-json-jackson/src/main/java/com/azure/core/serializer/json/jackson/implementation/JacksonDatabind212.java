// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;

/**
 * Utility methods for Jackson Databind types when it's known that the version is 2.12+.
 */
final class JacksonDatabind212 {
    /**
     * Mutates the passed {@link ObjectMapper} to coerce empty strings as null.
     * <p>
     * Before Jackson Databind 2.12 this was the default behavior for XML but this change in Jackson 2.12.
     *
     * @param mapper The {@link ObjectMapper} being mutated.
     * @return The updated {@link ObjectMapper}.
     */
    static ObjectMapper mutateXmlCoercions(ObjectMapper mapper) {
        mapper.coercionConfigDefaults().setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        return mapper;
    }

    static String removePrefix(MapperConfig<?> config, AnnotatedClass annotatedClass,
        AnnotatedMethod method, String methodName) {
        AccessorNamingStrategy namingStrategy = config.getAccessorNaming().forPOJO(config, annotatedClass);
        String name = namingStrategy.findNameForIsGetter(method, methodName);
        if (name == null) {
            name = namingStrategy.findNameForRegularGetter(method, methodName);
        }

        return name;
    }
}

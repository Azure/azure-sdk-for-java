// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.type.LogicalType;

/**
 * Utility methods for Jackson Databind types when it's known that the version is 2.12+.
 */
final class JacksonDatabind212 {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonDatabind212.class);

    /**
     * Mutates the passed {@link ObjectMapper} to coerce empty strings as null.
     * <p>
     * Before Jackson Databind 2.12 this was the default behavior for XML but this change in Jackson 2.12.
     *
     * @param mapper The {@link ObjectMapper} being mutated.
     * @return The updated {@link ObjectMapper}.
     */
    static ObjectMapper mutateXmlCoercions(ObjectMapper mapper) {
        // https://github.com/FasterXML/jackson-dataformat-xml/pull/585/files fixed array and collection elements
        // with coercion to be handled by the coercion config below which is a backwards compatibility breaking
        // change for us. Handle empty string items within an array or collection as empty string.
        mapper.coercionConfigFor(LogicalType.Array)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
        mapper.coercionConfigFor(LogicalType.Collection)
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);

        mapper.coercionConfigDefaults().setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
        return mapper;
    }

    static String removePrefix(MapperConfig<?> config, AnnotatedClass annotatedClass, AnnotatedMethod method,
        String methodName) {
        AccessorNamingStrategy namingStrategy = config.getAccessorNaming().forPOJO(config, annotatedClass);
        String name = namingStrategy.findNameForIsGetter(method, methodName);
        if (name == null) {
            name = namingStrategy.findNameForRegularGetter(method, methodName);
        }

        return name;
    }
}

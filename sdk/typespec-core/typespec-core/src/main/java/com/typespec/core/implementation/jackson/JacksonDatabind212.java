// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.AccessPattern;

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
        // https://github.com/FasterXML/jackson-dataformat-xml/pull/585/files fixed array and collection elements
        // with coercion to be handled by the coercion config below which is a backwards compatibility breaking
        // change for us. Handle empty string items within an array or collection as empty string.
        mapper.registerModule(new SimpleModule().setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
                if (String.class.isAssignableFrom(beanDesc.getBeanClass())) {
                    return new DelegatingDeserializer(deserializer) {
                        @Override
                        protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
                            return this;
                        }

                        @Override
                        public AccessPattern getNullAccessPattern() {
                            return AccessPattern.DYNAMIC;
                        }

                        @Override
                        public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
                            return (ctxt.getParser().getParsingContext().inArray()) ? "" : super.getNullValue(ctxt);
                        }
                    };
                } else {
                    return deserializer;
                }
            }
        }));

        mapper.coercionConfigDefaults()
            .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
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

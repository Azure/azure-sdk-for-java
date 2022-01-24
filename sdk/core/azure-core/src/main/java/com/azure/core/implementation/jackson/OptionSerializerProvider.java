/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.Option;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;

/**
 * The provider that provide the serializer for {@link Option} type.
 */
final class OptionSerializerProvider extends Serializers.Base implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public JsonSerializer<?> findReferenceSerializer(SerializationConfig config,
        ReferenceType refType,
        BeanDescription beanDesc,
        TypeSerializer contentTypeSerializer,
        JsonSerializer<Object> contentValueSerializer) {
        if (Option.class.isAssignableFrom(refType.getRawClass())) {
            // The standard Serializers.Base implementations for reference types honor
            // USE_STATIC_TYPING flag (e.g. Optional in Jdk8Module), do the same for
            // custom Option reference type.
            boolean staticTyping = (contentTypeSerializer == null)
                && config.isEnabled(MapperFeature.USE_STATIC_TYPING);
            return new OptionSerializer(refType, staticTyping,
                contentTypeSerializer, contentValueSerializer);
        }
        return null;
    }
}

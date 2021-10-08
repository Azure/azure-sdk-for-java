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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ReferenceTypeSerializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Specialization of jackson {@code ReferenceTypeSerializer} for {@link Option} type.
 */
final class OptionSerializer extends ReferenceTypeSerializer<Option<?>> {
    private static final long serialVersionUID = 1L;

    OptionSerializer(ReferenceType fullType,
        boolean staticTyping,
        TypeSerializer typeSerializer,
        JsonSerializer<Object> valueSerializer) {
        super(fullType, staticTyping, typeSerializer, valueSerializer);
    }

    private OptionSerializer(OptionSerializer base,
        BeanProperty property,
        TypeSerializer typeSerializer,
        JsonSerializer<?> valueSerializer,
        NameTransformer transformer,
        Object suppressableValue,
        boolean suppressNulls) {
        super(base, property, typeSerializer,
            valueSerializer, transformer,
            suppressableValue, suppressNulls);
    }

    @Override
    protected ReferenceTypeSerializer<Option<?>> withResolved(BeanProperty property,
        TypeSerializer typeSerializer,
        JsonSerializer<?> valueSerializer,
        NameTransformer transformer) {
        return new OptionSerializer(this, property, typeSerializer,
            valueSerializer, transformer,
            super._suppressableValue, super._suppressNulls);
    }

    @Override
    public ReferenceTypeSerializer<Option<?>> withContentInclusion(Object suppressableValue,
        boolean suppressNulls) {
        return new OptionSerializer(this, super._property, super._valueTypeSerializer,
            super._valueSerializer, super._unwrapper,
            suppressableValue, suppressNulls);
    }

    @Override
    protected boolean _isValuePresent(Option<?> option) {
        // Option type is to represent tri-state (no-value, null-value, non-null-value).
        // null-value is one of the valid values that Option can hold hence it
        // represents presence of value.
        // When Option is in uninitialized state, then there is no value present.
        return option.isInitialized();
    }

    @Override
    protected Object _getReferenced(Option<?> option) {
        return option.getValue();
    }

    @Override
    protected Object _getReferencedIfPresent(Option<?> option) {
        return option.isInitialized() ? option.getValue() : null;
    }
}

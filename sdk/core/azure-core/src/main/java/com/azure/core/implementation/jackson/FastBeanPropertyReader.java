// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

import java.io.IOException;
import java.util.function.BiFunction;

public class FastBeanPropertyReader extends SettableBeanProperty.Delegating {
    private final Class<?> propertyType;
    private final BiFunction<Object, Object, Object> setter;

    protected FastBeanPropertyReader(Class<?> propertyType, BiFunction<Object, Object, Object> setter,
        SettableBeanProperty d) {
        super(d);
        this.propertyType = propertyType;
        this.setter = setter;
    }

    @Override
    protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
        return new FastBeanPropertyReader(propertyType, setter, d);
    }

    @Override
    public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
        deserializeSetAndReturn(p, ctxt, instance);
    }

    @Override
    public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance)
        throws IOException {
        return setAndReturn(instance, delegate.deserialize(p, ctxt));
    }

    @Override
    public void set(Object instance, Object value) throws IOException {
        setAndReturn(instance, value);
    }

    @Override
    public Object setAndReturn(Object instance, Object value) throws IOException {
        return propertyType.cast(setter.apply(instance, value));
    }
}

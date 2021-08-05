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

package com.azure.core.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * A writer used by the serializer for the property of type {@link Option}.
 * The main purpose of this writer is to ensure {@link Option} property
 * is ignored when it has no-value i.e. when it is in uninitialized state.
 */
final class OptionBeanPropertyWriter extends BeanPropertyWriter {
    private static final long serialVersionUID = 1L;

    OptionBeanPropertyWriter(BeanPropertyWriter base) {
        super(base);
    }

    private OptionBeanPropertyWriter(OptionBeanPropertyWriter base,
                                     PropertyName newName) {
        super(base, newName);
    }

    @Override
    protected BeanPropertyWriter _new(PropertyName newName) {
        return new OptionBeanPropertyWriter(this, newName);
    }

    @Override
    public BeanPropertyWriter unwrappingWriter(NameTransformer transformer) {
        return new UnwrappingOptionBeanPropertyWriter(this, transformer);
    }

    @Override
    public void serializeAsField(Object bean,
                                 JsonGenerator jsonGenerator,
                                 SerializerProvider provider) throws Exception {
        // Follow the same pattern that standard Optional serializer (Jdk8Module) follows.
        if (super._nullSerializer == null) {
            Object option = super.get(bean);
            if (option == null || option.equals(Option.uninitialized())) {
                return;
            }
        }
        super.serializeAsField(bean, jsonGenerator, provider);
    }

    private static final class UnwrappingOptionBeanPropertyWriter extends UnwrappingBeanPropertyWriter {
        private static final long serialVersionUID = 1L;

        UnwrappingOptionBeanPropertyWriter(BeanPropertyWriter base,
                                           NameTransformer transformer) {
            super(base, transformer);
        }

        private UnwrappingOptionBeanPropertyWriter(UnwrappingOptionBeanPropertyWriter base,
                                                   NameTransformer transformer,
                                                   SerializedString name) {
            super(base, transformer, name);
        }

        @Override
        protected UnwrappingBeanPropertyWriter _new(NameTransformer transformer,
                                                    SerializedString newName) {
            return new UnwrappingOptionBeanPropertyWriter(this, transformer, newName);
        }

        @Override
        public void serializeAsField(Object bean,
                                     JsonGenerator jsonGenerator,
                                     SerializerProvider provider) throws Exception {
            // Follow the same pattern that standard Optional serializer (Jdk8Module) follows.
            if (super._nullSerializer == null) {
                Object option = super.get(bean);
                if (option == null || option.equals(Option.uninitialized())) {
                    return;
                }
            }
            super.serializeAsField(bean, jsonGenerator, provider);
        }
    }
}

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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;

/**
 * The Serializer modifier for {@link Option} properties to not serialize them when
 * those properties are in uninitialized state.
 */
final class OptionPropertiesModifier extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanPropertyWriters) {
        for (int i = 0; i < beanPropertyWriters.size(); i++) {
            final BeanPropertyWriter writer = beanPropertyWriters.get(i);
            if (writer.getType().isTypeOrSubTypeOf(Option.class)) {
                beanPropertyWriters.set(i, new OptionBeanPropertyWriter(writer));
            }
        }
        return beanPropertyWriters;
    }
}

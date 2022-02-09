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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;

import java.lang.reflect.Type;

/**
 * The modifier to inspect an {@link Option} type and upgrade the type to be serialized from Option&lt;T&gt; to T.
 */
final class OptionTypeModifier extends TypeModifier implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public JavaType modifyType(JavaType type, Type jdkType,
                               TypeBindings bindings,
                               TypeFactory typeFactory) {
        if (type.isReferenceType() || type.isContainerType()) {
            return type;
        } else if (type.getRawClass() == Option.class) {
            // When serializing Option type, the actual type to serialize is the contained-type,
            // do the type upgrade Option<T> to T.
            return ReferenceType.upgradeFrom(type, type.containedTypeOrUnknown(0));
        } else {
            return type;
        }
    }
}

/**
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.core.utils;

import java.util.EnumSet;

/**
 * Provides functionality for parsing enums case insensitively.
 */
public final class EnumUtility {
    /**
     * Parses an Enum of type {@link T} case insensitively.
     *
     * @param enumClass the class of the Enum type
     * @param name the string to parse
     * @param <T> the type of the Enum class, should be inferred by enumClass
     * @return the parse Enum value of type {@link T}
     */
    public static <T extends Enum<T>> T fromString(Class<T> enumClass, String name) {
        if (name == null) {
            return null;
        }

        EnumSet<T> values = EnumSet.allOf(enumClass);
        for (T value : values) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException(
                "No enum constant " + enumClass.getCanonicalName() + "." + name);
    }
}

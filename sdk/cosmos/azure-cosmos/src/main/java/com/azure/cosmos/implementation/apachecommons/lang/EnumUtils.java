/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.lang;

public class EnumUtils {
    /**
     * This constructor is public to permit tools that require a JavaBean
     * instance to operate.
     */
    private EnumUtils() {
    }

    /**
     * <p>Gets the enum for the class, returning {@code null} if not found.</p>
     *
     * <p>This method differs from {@link Enum#valueOf} in that it does not throw an exception
     * for an invalid enum name and performs case insensitive matching of the name.</p>
     *
     * @param <E>         the type of the enumeration
     * @param enumClass   the class of the enum to query, not null
     * @param enumName    the enum name, null returns null
     * @return the enum, null if not found
     */
    public static <E extends Enum<E>> E getEnumIgnoreCase(final Class<E> enumClass, final String enumName) {
        if (enumName == null || !enumClass.isEnum()) {
            return null;
        }
        for (final E each : enumClass.getEnumConstants()) {
            if (each.name().equalsIgnoreCase(enumName)) {
                return each;
            }
        }
        return null;
    }
}

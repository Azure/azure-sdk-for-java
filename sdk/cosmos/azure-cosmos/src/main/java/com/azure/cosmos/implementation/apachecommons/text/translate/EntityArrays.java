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

package com.azure.cosmos.implementation.apachecommons.text.translate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntityArrays {
    /**
     * A Map&lt;CharSequence, CharSequence&gt; to escape the Java
     * control characters.
     *
     * Namely: {@code \b \n \t \f \r}
     */
    public static final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_ESCAPE;
    static {
        final Map<CharSequence, CharSequence> initialMap = new HashMap<>();
        initialMap.put("\b", "\\b");
        initialMap.put("\n", "\\n");
        initialMap.put("\t", "\\t");
        initialMap.put("\f", "\\f");
        initialMap.put("\r", "\\r");
        JAVA_CTRL_CHARS_ESCAPE = Collections.unmodifiableMap(initialMap);
    }

    /**
     * Reverse of {@link #JAVA_CTRL_CHARS_ESCAPE} for unescaping purposes.
     */
    public static final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_UNESCAPE;
    static {
        JAVA_CTRL_CHARS_UNESCAPE = Collections.unmodifiableMap(invert(JAVA_CTRL_CHARS_ESCAPE));
    }

    /**
     * Used to invert an escape Map into an unescape Map.
     * @param map Map&lt;String, String&gt; to be inverted
     * @return Map&lt;String, String&gt; inverted array
     */
    public static Map<CharSequence, CharSequence> invert(final Map<CharSequence, CharSequence> map) {
        final Map<CharSequence, CharSequence> newMap = new HashMap<>();
        for (final Map.Entry<CharSequence, CharSequence> pair : map.entrySet()) {
            newMap.put(pair.getValue(), pair.getKey());
        }
        return newMap;
    }
}

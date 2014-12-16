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

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class CollectionStringBuilder {
    private static final String DEFAULT_SEPARATOR = ",";
    private final StringBuilder sb;
    private static String separator;

    public CollectionStringBuilder() {
        sb = new StringBuilder();
        separator = DEFAULT_SEPARATOR;
    }

    public CollectionStringBuilder(String separator) {
        sb = new StringBuilder();
        CollectionStringBuilder.separator = separator;
    }

    public void add(String representation) {
        if (sb.length() > 0) {
            sb.append(separator);
        }
        sb.append(representation);
    }

    public void addValue(boolean value, String representation) {
        if (value) {
            add(representation);
        }
    }

    public static String join(List<String> values) {
        return StringUtils.join(values, separator);
    }

    public static String join(List<String> values, String separator) {
        return StringUtils.join(values, separator);
    }

    public static String join(String... values) {
        CollectionStringBuilder sb = new CollectionStringBuilder();

        for (String value : values) {
            sb.add(value);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }
}

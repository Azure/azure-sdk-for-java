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

import java.util.List;

public class CommaStringBuilder {
    private final StringBuilder sb = new StringBuilder();

    public void add(String representation) {
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(representation);
    }

    public void addValue(boolean value, String representation) {
        if (value) {
            add(representation);
        }
    }

    public static String join(List<String> values) {
        CommaStringBuilder sb = new CommaStringBuilder();

        for (String value : values) {
            sb.add(value);
        }

        return sb.toString();
    }

    public static String join(String... values) {
        CommaStringBuilder sb = new CommaStringBuilder();

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

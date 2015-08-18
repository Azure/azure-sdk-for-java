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

import java.util.Map;

/**
 * Helper functions useful when creating Exports classses.
 * 
 */
public abstract class ExportUtils {

    private static String normalizeProfile(String profile) {
        if (profile == null || profile.equals("")) {
            return "";
        }

        if (profile.endsWith(".")) {
            return profile;
        }

        return profile + ".";
    }

    /**
     * Check if the given property exists under the given profile. If so, return
     * the value, otherwise return null.
     * 
     * @param profile
     *            profile to search
     * @param properties
     *            the set of property values
     * @param propertyName
     *            name of desired property
     * @return the property value, or null if it is not set.
     */
    public static Object getPropertyIfExists(String profile,
            Map<String, Object> properties, String propertyName) {
        String fullPropertyName = normalizeProfile(profile) + propertyName;

        if (properties.containsKey(fullPropertyName)) {
            return properties.get(fullPropertyName);
        }
        return null;
    }
}

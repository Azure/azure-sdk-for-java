/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.extensions;

/**
 * String handlers.
 */
public class Strings {

    /**
     * Determines whether the parameter string is either null or empty.
     * @param arg the string to verify
     * @return true if the string is empty or null and false otherwise.
     */
    public static boolean isNullOrEmpty(String arg) {

        if (arg == null || arg.length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether the parameter string is null, empty or whitespace.
     * @param arg the string to verify
     * @return true if the string is empty, contains only whitespace or is null and false otherwise
     */
    public static boolean isNullOrWhiteSpace(String arg) {

        if (Strings.isNullOrEmpty(arg) || arg.trim().isEmpty()) {
            return true;
        }

        return false;
    }
}

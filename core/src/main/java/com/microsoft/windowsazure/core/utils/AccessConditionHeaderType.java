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

/**
 * Specifies the kinds of conditional headers that may be set for a request.
 */
public enum AccessConditionHeaderType {
    /**
     * Specifies that no conditional headers are set.
     */
    NONE,

    /**
     * Specifies the <code>If-Unmodified-Since</code> conditional header is set.
     */
    IF_UNMODIFIED_SINCE,

    /**
     * Specifies the <code>If-Match</code> conditional header is set.
     */
    IF_MATCH,

    /**
     * Specifies the <code>If-Modified-Since</code> conditional header is set.
     */
    IF_MODIFIED_SINCE,

    /**
     * Specifies the <code>If-None-Match</code> conditional header is set.
     */
    IF_NONE_MATCH;

    /**
     * Returns a string representation of the current value, or an empty string
     * if no value is assigned.
     * 
     * @return A <code>String</code> that represents the currently assigned
     *         value.
     */
    @Override
    public String toString() {
        switch (this) {
        case IF_MATCH:
            return "If-Match";
        case IF_UNMODIFIED_SINCE:
            return "If-Unmodified-Since";
        case IF_MODIFIED_SINCE:
            return "If-Modified-Since";
        case IF_NONE_MATCH:
            return "If-None-Match";
        default:
            return "";
        }
    }
}

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
 * Provides Base64 encoding, decoding, and validation functionality.
 */
public final class Base64 {
    /**
     * Decodes a given Base64 string into its corresponding byte array.
     *
     * @param data
     *            the Base64 string, as a <code>String</code> object, to decode
     *
     * @return the corresponding decoded byte array
     * @throws IllegalArgumentException
     *             If the string is not a valid base64 encoded string
     */
    public static byte[] decode(final String data) {
        return org.apache.commons.codec.binary.Base64.decodeBase64(data);
    }

    /**
     * Encodes a byte array as a Base64 string.
     *
     * @param data
     *            the byte array to encode
     * @return the Base64-encoded string, as a <code>String</code> object
     */
    public static String encode(final byte[] data) {
        return org.apache.commons.codec.binary.Base64.encodeBase64String(data);
    }

    /**
     * Private Default Ctor.
     */
    private Base64() {
        // No op
    }
}

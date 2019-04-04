/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.serializer;


import com.azure.common.http.HttpHeaders;

/**
 * Supported serialization encoding formats.
 */
public enum SerializerEncoding {
    /**
     * JavaScript Object Notation.
     */
    JSON,

    /**
     * Extensible Markup Language.
     */
    XML;

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to check the encoding for
     * @return the serializer encoding to use for the body
     */
    public static SerializerEncoding fromHeaders(HttpHeaders headers) {
        String mimeContentType = headers.value("Content-Type");
        if (mimeContentType != null) {
            String[] parts = mimeContentType.split(";");
            if (parts[0].equalsIgnoreCase("application/xml") || parts[0].equalsIgnoreCase("text/xml")) {
                return XML;
            }
        }

        return JSON;
    }
}

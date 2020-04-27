// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;


import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;

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
    XML,

    /**
     * Plaintext.
     */
    TEXT;

    /**
     * Determines the serializer encoding to use based on the {@code Content-Type} header.
     * <p>
     * If the {@code Content-Type} is unknown {@link #JSON} will be returned.
     *
     * @param headers The {@link HttpHeaders} that will be used to retrieve the {@code Content-Type} header.
     * @return The serializer encoding to use when handling the body.
     */
    public static SerializerEncoding fromHeaders(HttpHeaders headers) {
        String mimeContentType = headers.getValue("Content-Type");

        if (mimeContentType != null) {
            String[] parts = mimeContentType.split(";");
            if (ContentType.APPLICATION_XML.equalsIgnoreCase(parts[0])
                || ContentType.TEXT_XML.equalsIgnoreCase(parts[0])) {
                return XML;
            } else if (ContentType.TEXT_PLAIN.equalsIgnoreCase(parts[0])) {
                return TEXT;
            }
        }

        return JSON;
    }
}

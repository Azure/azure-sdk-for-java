// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;


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

    private static final String APPLICATION_XML = "application/xml";
    private static final String TEXT_XML = "text/xml";

    private static final String TEXT_PLAIN = "text/plain";

    /**
     * Determines the serializer encoding to use based on the {@code Content-Type} header.
     *
     * @param headers The {@link HttpHeaders} that will be used to retrieve the {@code Content-Type} header.
     * @return The serializer encoding to use when handling the body.
     */
    public static SerializerEncoding fromHeaders(HttpHeaders headers) {
        String mimeContentType = headers.getValue("Content-Type");

        if (mimeContentType != null) {
            String[] parts = mimeContentType.split(";");
            if (APPLICATION_XML.equalsIgnoreCase(parts[0]) || TEXT_XML.equalsIgnoreCase(parts[0])) {
                return XML;
            } else if (TEXT_PLAIN.equalsIgnoreCase(parts[0])) {
                return TEXT;
            }
        }

        return JSON;
    }
}

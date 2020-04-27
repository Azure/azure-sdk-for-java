// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

/**
 * The different values that commonly used for Content-Type header.
 */
public final class ContentType {
    /**
     * A JSON {@code Content-Type} header.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * A binary {@code Content-Type} header.
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * A URL encoded form {@code Content-Type} header.
     */
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * An XML {@code Content-Type} header.
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * A plaintext {@code Content-Type} header.
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * An XML {@code Content-Type} header.
     */
    public static final String TEXT_XML = "text/xml";

    /**
     * Private ctr.
     */
    private ContentType() {
    }
}

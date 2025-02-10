// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

/**
 * <p>This class provides constants for commonly used Content-Type header values in HTTP requests and responses.</p>
 *
 * <p>This class is useful when you need to specify the Content-Type header in an HTTP request or check the
 * Content-Type header in an HTTP response.</p>
 */
public final class ContentType {
    /**
     * the default JSON Content-Type header.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * the default binary Content-Type header.
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * The default form data Content-Type header.
     */
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * Private ctr.
     */
    private ContentType() {
    }
}

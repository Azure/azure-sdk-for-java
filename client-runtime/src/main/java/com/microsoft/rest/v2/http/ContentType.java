/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

/**
 * The different values that we commonly use for Content-Type headers.
 */
public final class ContentType {
    /**
     * Private constructor that prevents creation of ContentType objects.
     */
    private ContentType() {
    }

    /**
     * The default JSON Content-Type header.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * The default binary Content-Type header.
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
}

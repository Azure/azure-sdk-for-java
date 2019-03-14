/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.http;

/**
 * The different values that commonly used for Content-Type header.
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
     * Private ctr.
     */
    private ContentType() {
    }
}

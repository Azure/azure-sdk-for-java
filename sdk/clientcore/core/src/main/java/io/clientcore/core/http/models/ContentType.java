// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

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
     * The default form data Content-Type header.
     */
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * The default text/event-stream Content-Type header.
     */
    public static final String TEXT_EVENT_STREAM = "text/event-stream";

    /**
     * Private ctr.
     */
    private ContentType() {
    }
}

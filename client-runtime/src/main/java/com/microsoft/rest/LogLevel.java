/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.rest;

public enum LogLevel {
    NONE,
    BASIC,
    HEADERS,
    BODY,
    BODY_AND_HEADERS;

    private boolean prettyJson = false;

    public boolean isPrettyJson() {
        return prettyJson;
    }

    public LogLevel withPrettyJson(boolean prettyJson) {
        this.prettyJson = prettyJson;
        return this;
    }
}

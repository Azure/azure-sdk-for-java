// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * This class used to redact the sensitive hostname information from URL when recording
 */
public class UrlRegexSanitizer {
    private final String regex;

    public UrlRegexSanitizer(String regex) {
        this.regex = regex;
    }
}

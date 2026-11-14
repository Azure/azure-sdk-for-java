// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

/**
 * Whitespace to render before and after a token.
 */
public enum Spacing {
    /** Adds space after the token (suffix is true), but no space before the token (prefix is false). */
    DEFAULT,

    SPACE_AFTER,

    SPACE_BEFORE,

    SPACE_BEFORE_AND_AFTER,

    NO_SPACE;
}

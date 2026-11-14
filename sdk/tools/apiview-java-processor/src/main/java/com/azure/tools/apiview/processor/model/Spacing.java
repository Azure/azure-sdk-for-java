package com.azure.tools.apiview.processor.model;

public enum Spacing {
    /** Adds space after the token (suffix is true), but no space before the token (prefix is false). */
    DEFAULT,

    SPACE_AFTER,

    SPACE_BEFORE,

    SPACE_BEFORE_AND_AFTER,

    NO_SPACE;
}

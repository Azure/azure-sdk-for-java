package com.microsoft.azure.services.blob;


/**
 * TODO: Unify this with client layer
 *
 * Specifies the kinds of conditional headers that may be set for a request.
 *
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public enum AccessConditionHeaderType {
    /**
     * Specifies that no conditional headers are set.
     */
    NONE,

    /**
     * Specifies the <i>If-Unmodified-Since</i> conditional header is set.
     */
    IF_UNMODIFIED_SINCE,

    /**
     * Specifies the <i>If-Match</i> conditional header is set.
     */
    IF_MATCH,

    /**
     * Specifies the <i>If-Modified-Since</i> conditional header is set.
     */
    IF_MODIFIED_SINCE,

    /**
     * Specifies the <i>If-None-Match</i> conditional header is set.
     */
    IF_NONE_MATCH;

    /**
     * TODO: Should this be move somewhere else?
     *
     * Returns a string representation of the current value, or an empty string if no value is assigned.
     *
     * @return A <code>String</code> that represents the currently assigned value.
     */
    @Override
    public String toString() {
        switch (this) {
            case IF_MATCH:
                return "If-Match";
            case IF_UNMODIFIED_SINCE:
                return "If-Unmodified-Since";
            case IF_MODIFIED_SINCE:
                return "If-Modified-Since";
            case IF_NONE_MATCH:
                return "If-None-Match";
            default:
                return "";
        }
    }
}

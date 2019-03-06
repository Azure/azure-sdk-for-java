// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

/**
 * Fields to return for Get* queries.
 */
public class KeyValueFields {
    /**
     * None.
     */
    public static final int NONE = 0x0000;
    /**
     * Key field.
     */
    public static final int KEY = 0x0001;
    /**
     * Label field.
     */
    public static final int LABEL = 0x0002;
    /**
     * Value field.
     */
    public static final int VALUE = 0x0004;
    /**
     * Content type field.
     */
    public static final int CONTENT_TYPE = 0x0008;
    /**
     * Etag field.
     */
    public static final int ETAG = 0x0010;
    /**
     * Last modified field.
     */
    public static final int LAST_MODIFIED = 0x0020;
    /**
     * Locked field.
     */
    public static final int LOCKED = 0x0040;
    /**
     * Tags field.
     */
    public static final int TAGS = 0x0080;

    /**
     * Include all the fields.
     */
    public static final int ALL = KEY | LABEL | VALUE | CONTENT_TYPE | ETAG | LAST_MODIFIED | LOCKED | TAGS;
}

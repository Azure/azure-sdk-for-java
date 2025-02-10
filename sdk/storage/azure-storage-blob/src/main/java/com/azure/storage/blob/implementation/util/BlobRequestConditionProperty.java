// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

public enum BlobRequestConditionProperty {
    LEASE_ID("LeaseId"),
    TAGS_CONDITIONS("TagsConditions"),
    IF_MODIFIED_SINCE("IfModifiedSince"),
    IF_UNMODIFIED_SINCE("IfUnmodifiedSince"),
    IF_MATCH("IfMatch"),
    IF_NONE_MATCH("IfNoneMatch");

    /** The actual serialized value for a BlobRequestConditionProperty instance. */
    private final String value;

    BlobRequestConditionProperty(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a BlobRequestConditionProperty instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed BlobRequestConditionProperty object, or null if unable to parse.
     */
    public static BlobRequestConditionProperty fromString(String value) {
        BlobRequestConditionProperty[] items = BlobRequestConditionProperty.values();
        for (BlobRequestConditionProperty item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

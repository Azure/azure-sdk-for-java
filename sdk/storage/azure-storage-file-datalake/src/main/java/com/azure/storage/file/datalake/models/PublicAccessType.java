// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for PublicAccessType.
 */
public final class PublicAccessType extends ExpandableStringEnum<PublicAccessType> {
    /**
     * Static value file system for PublicAccessType.
     */
    public static final PublicAccessType CONTAINER = fromString("container");

    /**
     * Static value path for PublicAccessType.
     */
    public static final PublicAccessType BLOB = fromString("blob");

    /**
     * Creates a new instance of {@link PublicAccessType} with no string value.
     *
     * @deprecated Please use {@link #fromString(String)} to create an instance of PublicAccessType.
     */
    @Deprecated
    public PublicAccessType() {
    }

    /**
     * Creates or finds a PublicAccessType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding PublicAccessType.
     */
    public static PublicAccessType fromString(String name) {
        return fromString(name, PublicAccessType.class);
    }

    /**
     * Gets known PublicAccessType values.
     *
     * @return known PublicAccessType values.
     */
    public static Collection<PublicAccessType> values() {
        return values(PublicAccessType.class);
    }
}

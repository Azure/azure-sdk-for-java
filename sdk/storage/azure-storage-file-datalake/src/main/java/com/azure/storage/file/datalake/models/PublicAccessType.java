// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

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
     * Creates or finds a PublicAccessType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding PublicAccessType.
     */
    @JsonCreator
    public static PublicAccessType fromString(String name) {
        return fromString(name, PublicAccessType.class);
    }

    /**
     * @return known PublicAccessType values.
     */
    public static Collection<PublicAccessType> values() {
        return values(PublicAccessType.class);
    }
}

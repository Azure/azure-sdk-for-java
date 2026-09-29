// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// This file is not generated. The Blob TypeSpec does not describe it, so it is maintained by hand;
// edits here are preserved across regeneration.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Generated;
import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * Defines values for BlobCopySourceTagsMode.
 */
public final class BlobCopySourceTagsMode extends ExpandableStringEnum<BlobCopySourceTagsMode> {
    /**
     * Static value REPLACE for BlobCopySourceTagsMode.
     */
    @Generated
    public static final BlobCopySourceTagsMode REPLACE = fromString("REPLACE");

    /**
     * Static value COPY for BlobCopySourceTagsMode.
     */
    @Generated
    public static final BlobCopySourceTagsMode COPY = fromString("COPY");

    /**
     * Creates a new instance of BlobCopySourceTagsMode value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Generated
    @Deprecated
    public BlobCopySourceTagsMode() {
    }

    /**
     * Creates or finds a BlobCopySourceTagsMode from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding BlobCopySourceTagsMode.
     */
    @Generated
    public static BlobCopySourceTagsMode fromString(String name) {
        return fromString(name, BlobCopySourceTagsMode.class);
    }

    /**
     * Gets known BlobCopySourceTagsMode values.
     * 
     * @return known BlobCopySourceTagsMode values.
     */
    @Generated
    public static Collection<BlobCopySourceTagsMode> values() {
        return values(BlobCopySourceTagsMode.class);
    }
}

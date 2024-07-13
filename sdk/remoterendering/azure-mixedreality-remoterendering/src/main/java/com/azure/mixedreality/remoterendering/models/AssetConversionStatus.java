// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** The status of a conversion. */
public final class AssetConversionStatus extends ExpandableStringEnum<AssetConversionStatus> {
    /** Static value NotStarted for AssetConversionStatus. */
    public static final AssetConversionStatus NOT_STARTED = fromString("NotStarted");

    /** Static value Running for AssetConversionStatus. */
    public static final AssetConversionStatus RUNNING = fromString("Running");

    /** Static value Cancelled for AssetConversionStatus. */
    public static final AssetConversionStatus CANCELLED = fromString("Cancelled");

    /** Static value Failed for AssetConversionStatus. */
    public static final AssetConversionStatus FAILED = fromString("Failed");

    /** Static value Succeeded for AssetConversionStatus. */
    public static final AssetConversionStatus SUCCEEDED = fromString("Succeeded");

    /**
     * Creates a new instance of {@link AssetConversionStatus} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link AssetConversionStatus} which doesn't
     * have a String enum value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AssetConversionStatus() {

    }

    /**
     * Creates or finds a AssetConversionStatus from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AssetConversionStatus.
     */
    public static AssetConversionStatus fromString(String name) {
        return fromString(name, AssetConversionStatus.class);
    }

    /**
     * Gets known AssetConversionStatus values.
     *
     * @return known AssetConversionStatus values.
     */
    public static Collection<AssetConversionStatus> values() {
        return values(AssetConversionStatus.class);
    }
}

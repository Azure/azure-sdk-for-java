// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values returned by the service on a download to hint the client towards a more optimal download strategy.
 */
public final class DownloadHint extends ExpandableStringEnum<DownloadHint> {
    /**
     * Indicates that the client should call
     * {@link com.azure.storage.blob.specialized.BlobClientBase#getLayout(
     *     com.azure.storage.blob.options.BlobGetLayoutOptions)} (or the async
     * equivalent) to obtain the blob's layout and route subsequent range downloads to the optimal endpoint for
     * each range.
     */
    public static final DownloadHint LAYOUT = fromString("layout");

    /**
     * Creates a new instance of {@link DownloadHint} without a {@link #toString()} value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DownloadHint() {
    }

    /**
     * Creates or finds a {@link DownloadHint} from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding {@link DownloadHint}.
     */
    public static DownloadHint fromString(String name) {
        return fromString(name, DownloadHint.class);
    }

    /**
     * Gets known {@link DownloadHint} values.
     *
     * @return known {@link DownloadHint} values.
     */
    public static Collection<DownloadHint> values() {
        return values(DownloadHint.class);
    }
}

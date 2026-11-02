// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values returned by the service on a download to hint the client towards a more optimal download strategy.
 */
public final class DownloadHint extends ExpandableStringEnum<DownloadHint> {
    // TODO: Restore manual getLayout guidance when getLayout is made public.
    /**
     * Indicates that the service has data-locality layout information for this blob. When
     * {@link com.azure.storage.common.LayoutAwareRouting#ENABLED} is set on supported download options, the SDK uses
     * the layout to route range downloads to the optimal endpoint automatically.
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

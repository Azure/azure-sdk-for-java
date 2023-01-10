// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.VolumeResolution;
import com.azure.ai.textanalytics.models.VolumeUnit;

public final class VolumeResolutionPropertiesHelper {
    private static VolumeResolutionAccessor accessor;

    private VolumeResolutionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link VolumeResolution} instance.
     */
    public interface VolumeResolutionAccessor {
        void setUnit(VolumeResolution volumeResolution, VolumeUnit unit);
        void setValue(VolumeResolution volumeResolution, double value);
    }

    /**
     * The method called from {@link VolumeResolution} to set it's accessor.
     *
     * @param volumeResolutionAccessor The accessor.
     */
    public static void setAccessor(final VolumeResolutionAccessor volumeResolutionAccessor) {
        accessor = volumeResolutionAccessor;
    }

    public static void setUnit(VolumeResolution volumeResolution, VolumeUnit unit) {
        accessor.setUnit(volumeResolution, unit);
    }

    public static void setValue(VolumeResolution volumeResolution, double value) {
        accessor.setValue(volumeResolution, value);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.models.BoundingRegion;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link BoundingRegion} instance.
 */
public final class BoundingRegionHelper {
    private static BoundingRegionAccessor accessor;

    private BoundingRegionHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link BoundingRegion} instance.
     */
    public interface BoundingRegionAccessor {
        void setPageNumber(BoundingRegion boundingRegion, int pageNumber);
        void setBoundingPolygon(BoundingRegion boundingRegion, List<Point> boundingPolygon);
    }

    /**
     * The method called from {@link BoundingRegion} to set it's accessor.
     *
     * @param boundingRegionAccessor The accessor.
     */
    public static void setAccessor(final BoundingRegionHelper.BoundingRegionAccessor boundingRegionAccessor) {
        accessor = boundingRegionAccessor;
    }

    static void setPageNumber(BoundingRegion boundingRegion, int pageNumber) {
        accessor.setPageNumber(boundingRegion, pageNumber);
    }

    static void setBoundingPolygon(BoundingRegion boundingRegion, List<Point> boundingRegionHelper) {
        accessor.setBoundingPolygon(boundingRegion, boundingRegionHelper);
    }
}

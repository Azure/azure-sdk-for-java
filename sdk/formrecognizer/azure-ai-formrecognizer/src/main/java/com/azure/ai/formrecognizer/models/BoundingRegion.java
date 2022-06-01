// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.BoundingRegionHelper;

import java.util.List;

/**
 * Bounding polygon on a specific page of the input.
 */
public final class BoundingRegion {
    /*
     * 1-based page number of page containing the bounding region.
     */
    private int pageNumber;

    /*
     * The list of coordinates of bounding polygon, or the entire page if not specified.
     */
    private List<Point> boundingPolygon;

    /**
     * Get the pageNumber property: 1-based page number of page containing the bounding region.
     *
     * @return the pageNumber value.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Set the pageNumber property: 1-based page number of page containing the bounding region.
     *
     * @param pageNumber the pageNumber value to set.
     * @return the BoundingRegion object itself.
     */
    void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Get the list of coordinates of bounding polygon, or the entire page if not specified.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @return the boundingPolygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.boundingPolygon;
    }

    /**
     * Set the list of coordinates of bounding polygon, or the entire page if not specified.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @param boundingPolygon the boundingPolygon value to set.
     * @return the BoundingRegion object itself.
     */
    void setBoundingPolygon(List<Point> boundingPolygon) {
        this.boundingPolygon = boundingPolygon;
    }

    static {
        BoundingRegionHelper.setAccessor(new BoundingRegionHelper.BoundingRegionAccessor() {
            @Override
            public void setPageNumber(BoundingRegion boundingRegion, int pageNumber) {
                boundingRegion.setPageNumber(pageNumber);
            }

            @Override
            public void setBoundingPolygon(BoundingRegion boundingRegion, List<Point> boundingPolygon) {
                boundingRegion.setBoundingPolygon(boundingPolygon);
            }
        });
    }
}

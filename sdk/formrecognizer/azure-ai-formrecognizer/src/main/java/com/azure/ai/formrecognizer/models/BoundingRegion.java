// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.BoundingRegionHelper;

import java.util.List;

/**
 * Bounding box on a specific page of the input.
 */
public final class BoundingRegion {
    /*
     * 1-based page number of page containing the bounding region.
     */
    private int pageNumber;

    /*
     * Bounding box on the page, or the entire page if not specified.
     */
    private List<Float> boundingBox;

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
     * Get the boundingBox property: Bounding box on the page, or the entire page if not specified.
     *
     * @return the boundingBox value.
     */
    public List<Float> getBoundingBox() {
        return this.boundingBox;
    }

    /**
     * Set the boundingBox property: Bounding box on the page, or the entire page if not specified.
     *
     * @param boundingBox the boundingBox value to set.
     * @return the BoundingRegion object itself.
     */
    void setBoundingBox(List<Float> boundingBox) {
        this.boundingBox = boundingBox;
    }

    static {
        BoundingRegionHelper.setAccessor(new BoundingRegionHelper.BoundingRegionAccessor() {
            @Override
            public void setPageNumber(BoundingRegion boundingRegion, int pageNumber) {
                boundingRegion.setPageNumber(pageNumber);
            }

            @Override
            public void setBoundingBox(BoundingRegion boundingRegion, List<Float> boundingBox) {
                boundingRegion.setBoundingBox(boundingBox);
            }
        });
    }
}

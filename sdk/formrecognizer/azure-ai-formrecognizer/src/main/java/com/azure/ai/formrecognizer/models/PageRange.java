// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The PageRange model.
 */
@Immutable
public final class PageRange {

    /*
     * The start page number property.
     */
    private final int startPageNumber;

    /*
     * The end page number property.
     */
    private final int endPageNumber;


    /**
     * Construct a PageRange object.
     *
     * @param startPageNumber The start page number property.
     * @param endPageNumber The end page number property.
     */
    public PageRange(final int startPageNumber, final int endPageNumber) {
        this.startPageNumber = startPageNumber;
        this.endPageNumber = endPageNumber;
    }

    /**
     * Get the start page number.
     *
     * @return the start value of the page number .
     */
    public int getStartPageNumber() {
        return this.startPageNumber;
    }

    /**
     * Get the end  page number.
     *
     * @return the end value of the page number .
     */
    public int getEndPageNumber() {
        return this.endPageNumber;
    }
}

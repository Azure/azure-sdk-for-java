// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents a page interval from the input document. Page numbers are 1-based..
 */
@Immutable
public final class FormPageRange {

    /*
     * The first page number property.
     */
    private final int firstPageNumber;

    /*
     * The last page number property.
     */
    private final int lastPageNumber;


    /**
     * Construct a FormPageRange object.
     *
     * @param firstPageNumber The first page number of the range.
     * @param lastPageNumber The first page number of the range..
     */
    public FormPageRange(final int firstPageNumber, final int lastPageNumber) {
        this.firstPageNumber = firstPageNumber;
        this.lastPageNumber = lastPageNumber;
    }

    /**
     * Get the first page number of the range.
     *
     * @return the first page number of the range.
     */
    public int getFirstPageNumber() {
        return this.firstPageNumber;
    }

    /**
     * Get the last page number.
     *
     * @return the last page number of the range.
     */
    public int getLastPageNumber() {
        return this.lastPageNumber;
    }
}

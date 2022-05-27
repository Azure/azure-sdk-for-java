// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The class is to test unsupported two-dimensional type.
 */
public class HotelTwoDimensional {
    private List<List<String>> matrix;

    /**
     * Gets the matrix
     *
     * @return The matrix of hotel.
     */
    public List<List<String>> getMatrix() {
        return (matrix == null) ? null : new ArrayList<>(matrix);
    }

    /**
     * Sets the matrix.
     *
     * @param matrix The matrix of hotel.
     * @return The {@link HotelTwoDimensional} object itself.
     */
    public HotelTwoDimensional setMatrix(List<List<String>> matrix) {
        this.matrix = (matrix == null) ? null : new ArrayList<>(matrix);
        return this;
    }

}

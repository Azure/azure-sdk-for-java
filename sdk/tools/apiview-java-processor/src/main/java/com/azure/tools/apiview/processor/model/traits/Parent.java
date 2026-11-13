// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model.traits;

import com.azure.tools.apiview.processor.model.ReviewLine;

import java.util.List;

/**
 * A review node that owns child lines.
 */
public interface Parent {
    /**
     * Adds child line.
     *
     * @return the added child line
     */
    ReviewLine addChildLine();

    /**
     * Adds child line.
     *
     * @param lineId the line id
     * @return the added child line
     */
    ReviewLine addChildLine(String lineId);

    /**
     * Adds child line.
     *
     * @param child the child
     * @return the added child line
     */
    ReviewLine addChildLine(ReviewLine child);

    /**
     * Returns the children.
     *
     * @return the children
     */
    List<ReviewLine> getChildren();
}

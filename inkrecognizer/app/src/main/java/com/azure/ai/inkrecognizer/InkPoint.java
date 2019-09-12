// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

/**
 * The InkPoint interface represents a single position on the path of an ink stroke.
 * Clients of the InkRecognizer services are expected to implement this interface on the data store
 * used to store ink data so the InkRecognizer class can use it to translate the ink to JSON for delivery
 * to the Ink Recognizer service.
 * @author Microsoft
 * @version 1.0
 */
public interface InkPoint {

    /**
     * Retrieves the x coordinate of the point
     * @return The x coordinate for the point
     */
    float getX();

    /**
     * Retrieves the y coordinate of the point.
     * @return The y coordinate for the point
     */
    float getY();

}

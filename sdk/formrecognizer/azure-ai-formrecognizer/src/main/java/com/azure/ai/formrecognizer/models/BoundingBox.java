// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

public class BoundingBox {
    // TODO: improve to make a better coordinates class
    private List<Float> coordinates;

    public BoundingBox(final List<Float> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Float> getCoordinates() {
        return coordinates;
    }
}

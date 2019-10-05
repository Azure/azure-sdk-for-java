// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

public class InkPointImplementor implements InkPoint {

    private float x;
    private float y;

    InkPointImplementor() {
    }

    InkPointImplementor setX(double x) {
        this.x = (float) x;
        return this;
    }

    InkPointImplementor setY(double y) {
        this.y = (float) y;
        return this;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

}

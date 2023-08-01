// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import java.text.DecimalFormat;
import java.time.Instant;

public class CpuLoad {
    public Instant timestamp;
    public float value;
    private String cachedToString;

    public CpuLoad(Instant timestamp, float value) {
        if ((double) value < 0.0 || (double) value > 100.0) {
            throw new IllegalArgumentException("Valid CPU load values must be between 0.0 and 100.0, but it is " + value);
        }

        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public String toString() {
        if (cachedToString == null) {
            cachedToString = toStringInternal();
        }

        return cachedToString;
    }

    private String toStringInternal() {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return "(" + timestamp.toString() + " " + decimalFormat.format(this.value) + "%)";
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.ai.inkrecognizer.model;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.InkPointUnit;

class Utils {

    private static final float INCH_TO_MM = 25.4f;

    // Translates MM to PIXEL if user chose PIXEL as unit
    static float translatePoints(double milliValue, InkPointUnit inkPointUnit, DisplayMetrics displayMetrics) {

        if (inkPointUnit == InkPointUnit.PIXEL) {

            final float DOT_PER_INCH = displayMetrics.xdpi;
            return ((float)milliValue / INCH_TO_MM) * DOT_PER_INCH;

        } else {

            return (float)milliValue;

        }

    }

}

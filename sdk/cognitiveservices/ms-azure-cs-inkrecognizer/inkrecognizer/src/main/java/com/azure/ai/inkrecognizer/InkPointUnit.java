/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.ai.inkrecognizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The InkPointUnit is used to specify the physical units of the ink points. If a value isn't specified,
 * it is assumed that the points are in pixels
 * @author Microsoft
 * @version 1.0
 */
public enum InkPointUnit {
    /**
     * The values haven't been converted to a physical unit.
     */
    PIXEL("pixel"),

    /**
     * Millimeters
     */
    MM("mm"),

    /**
     * Centimeters
     */
    CM("cm"),

    /**
     * Inches
     */
    INCH("in");

    private String inkPointUnitString;
    private static final Map<String, InkPointUnit> map = new HashMap<>();

    static {
        for (InkPointUnit inkPointUnit : InkPointUnit.values()) {
            map.put(inkPointUnit.toString().toLowerCase(Locale.getDefault()), inkPointUnit);
        }
    }

    InkPointUnit(String inkPointUnitString) {
        this.inkPointUnitString = inkPointUnitString;
    }

    static InkPointUnit getInkPointUnitOrDefault(String inkPointUnitString) {
        if (inkPointUnitString != null && map.containsKey(inkPointUnitString.toLowerCase(Locale.getDefault()))) {
            return map.get(inkPointUnitString.toLowerCase(Locale.getDefault()));
        } else {
            return InkPointUnit.PIXEL;
        }
    }

    @Override
    public String toString() {
        return inkPointUnitString;
    }
}


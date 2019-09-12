// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import java.util.HashMap;
import java.util.Map;

/**
 * The InkPointUnit is used to specified the physical units of the ink points. If a value isn't specified,
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
        for(InkPointUnit inkPointUnit : InkPointUnit.values()) {
            map.put(inkPointUnit.toString(), inkPointUnit);
        }
    }

    InkPointUnit(String inkPointUnitString) {
        this.inkPointUnitString = inkPointUnitString;
    }

    static InkPointUnit getInkPointUnitOrDefault(String inkPointUnitString) {
        if(map.containsKey(inkPointUnitString)) {
            return map.get(inkPointUnitString);
        } else {
            return InkPointUnit.PIXEL;
        }
    }

    @Override
    public String toString() {
        return inkPointUnitString;
    }
}


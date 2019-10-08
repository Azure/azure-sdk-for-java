// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The InkPointUnit is used to specify the physical units of the ink points. If a value isn't specified, it is assumed
 * that the points are in mm
 * @author Microsoft
 * @version 1.0
 */
public enum InkPointUnit {

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
    private static final Map<String, InkPointUnit> MAP = new HashMap<>();

    static {
        for (InkPointUnit inkPointUnit : InkPointUnit.values()) {
            MAP.put(inkPointUnit.toString().toLowerCase(Locale.getDefault()), inkPointUnit);
        }
    }

    InkPointUnit(String inkPointUnitString) {
        this.inkPointUnitString = inkPointUnitString;
    }

    static InkPointUnit getInkPointUnitOrDefault(String inkPointUnitString) {
        if (inkPointUnitString != null && MAP.containsKey(inkPointUnitString.toLowerCase(Locale.getDefault()))) {
            return MAP.get(inkPointUnitString.toLowerCase(Locale.getDefault()));
        } else {
            return InkPointUnit.MM;
        }
    }

    @Override
    public String toString() {
        return inkPointUnitString;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

/**
 * Diagnostic severity levels supported by APIView.
 */
public enum DiagnosticKind {
    FATAL(4),       // block release!
    ERROR(3),       // red
    WARNING(2),     // yellow
    INFO(1);        // green

    private final int level;

    DiagnosticKind(int level) {
        this.level = level;
    }

    /**
     * Finds the diagnostic severity for its APIView numeric level.
     *
     * @param level the level
     * @return the matching severity, or null if the level is unknown
     */
    public static DiagnosticKind fromInt(int level) {
        switch (level) {
            case 4:
                return FATAL;

            case 3:
                return ERROR;

            case 2:
                return WARNING;

            case 1:
                return INFO;

            default:
                return null;
        }
    }

    /**
     * Returns the level.
     *
     * @return the level
     */
    public int getLevel() {
        return level;
    }
}

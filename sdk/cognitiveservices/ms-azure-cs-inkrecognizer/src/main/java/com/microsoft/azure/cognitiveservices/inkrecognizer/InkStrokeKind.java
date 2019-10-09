// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The StrokeKind enum represents the class a stroke belongs to. The user of the Ink recognizer service is expected to
 * set this value when it is known with absolute certainty. The default value is InkStrokeKind.UNKNOWN
 * @author Microsoft
 * @version 1.0
 */
public enum InkStrokeKind {
    /**
     * The stroke kind is unknown
     */
    UNKNOWN("unknown"),

    /**
     * The stroke is part of a drawing
     */
    DRAWING("inkDrawing"),

    /**
     * The stroke is a word or part of a word
     */
    WRITING("inkWriting");

    private String inkStrokeKindString;
    private static final Map<String, InkStrokeKind> MAP = new HashMap<>();

    static {
        for (InkStrokeKind inkStrokeKind : InkStrokeKind.values()) {
            MAP.put(inkStrokeKind.toString().toLowerCase(Locale.getDefault()), inkStrokeKind);
        }
    }

    InkStrokeKind(String inkStrokeKindString) {
        this.inkStrokeKindString = inkStrokeKindString;
    }

    static InkStrokeKind getInkStrokeKindOrDefault(String inkStrokeKindString) {
        if (inkStrokeKindString != null && MAP.containsKey(inkStrokeKindString.toLowerCase(Locale.getDefault()))) {
            return MAP.get(inkStrokeKindString.toLowerCase(Locale.getDefault()));
        } else {
            return InkStrokeKind.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return inkStrokeKindString;
    }

}

// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import java.util.HashMap;
import java.util.Map;

/**
 * The StrokeKind enum represents the class a stroke belongs to.
 * The user of the Ink recognizer service is expected to set this value
 * when it is known with absolute certainly. The default value is "UNKNOWN"
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
    private static final Map<String, InkStrokeKind> map = new HashMap<>();

    static {
        for(InkStrokeKind inkStrokeKind : InkStrokeKind.values()) {
            map.put(inkStrokeKind.toString(), inkStrokeKind);
        }
    }

    InkStrokeKind(String inkStrokeKindString) {
        this.inkStrokeKindString = inkStrokeKindString;
    }

    static InkStrokeKind getInkStrokeKindOrDefault(String inkStrokeKindString) {
        if(map.containsKey(inkStrokeKindString)) {
            return map.get(inkStrokeKindString);
        } else {
            return InkStrokeKind.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return inkStrokeKindString;
    }

}

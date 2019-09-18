// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The InkRecognitionUnitKind has all the different categories of model units available from the service.
 * @author Microsoft
 * @version 1.0
 */
public enum InkRecognitionUnitKind {

    UNKNOWN("unknown"),

    /**
     * A bullet on a line of text. The bullet can be associated with more than one line.
     */
    INK_BULLET("inkBullet"),

    /**
     * A word.
     */
    INK_WORD("inkWord"),

    /**
     * A drawing.
     */
    INK_DRAWING("inkDrawing"),

    /**
     * A paragraph.
     */
    PARAGRAPH("paragraph"),

    /**
     * A list item.
     */
    INK_LIST("listItem"),

    /**
     * A line.
     */
    LINE("line"),

    /**
     * A writing region is a part of the writing surface that contains words.
     */
    WRITING_REGION("writingRegion");

    private String inkRecognitionKindString;
    private static final Map<String, InkRecognitionUnitKind> map = new HashMap<>();

    static {
        for (InkRecognitionUnitKind inkRecognitionUnitKind : InkRecognitionUnitKind.values()) {
            map.put(inkRecognitionUnitKind.toString().toLowerCase(), inkRecognitionUnitKind);
        }
    }

    InkRecognitionUnitKind(String inkRecognitionKindString) {
        this.inkRecognitionKindString = inkRecognitionKindString;
    }

    static InkRecognitionUnitKind getInkRecognitionUnitKindOrDefault(String inkRecognitionKindString) {
        if (inkRecognitionKindString != null && map.containsKey(inkRecognitionKindString.toLowerCase())) {
            return map.get(inkRecognitionKindString.toLowerCase());
        } else {
            return InkRecognitionUnitKind.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return inkRecognitionKindString;
    }

}

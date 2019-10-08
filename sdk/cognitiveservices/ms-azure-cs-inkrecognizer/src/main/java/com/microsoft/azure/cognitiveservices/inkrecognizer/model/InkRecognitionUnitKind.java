// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer.model;

import java.util.HashMap;
import java.util.Locale;
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
    private static final Map<String, InkRecognitionUnitKind> MAP = new HashMap<>();

    static {
        for (InkRecognitionUnitKind inkRecognitionUnitKind : InkRecognitionUnitKind.values()) {
            MAP.put(inkRecognitionUnitKind.toString().toLowerCase(Locale.getDefault()), inkRecognitionUnitKind);
        }
    }

    InkRecognitionUnitKind(String inkRecognitionKindString) {
        this.inkRecognitionKindString = inkRecognitionKindString;
    }

    static InkRecognitionUnitKind getInkRecognitionUnitKindOrDefault(String inkRecognitionKindString) {
        if (inkRecognitionKindString != null && MAP.containsKey(inkRecognitionKindString.toLowerCase(Locale.getDefault()))) {
            return MAP.get(inkRecognitionKindString.toLowerCase(Locale.getDefault()));
        } else {
            return InkRecognitionUnitKind.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return inkRecognitionKindString;
    }

}

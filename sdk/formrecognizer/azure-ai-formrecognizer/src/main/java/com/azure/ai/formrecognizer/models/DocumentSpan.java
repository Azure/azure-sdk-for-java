// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentSpanHelper;

/**
 * Contiguous region of the concatenated content property, specified as an offset and length.
 */
public final class DocumentSpan {
    /*
     * Zero-based index of the content represented by the span.
     */
    private int offset;

    /*
     * Number of characters in the content represented by the span.
     */
    private int length;

    /**
     * Get the offset property: Zero-based index of the content represented by the span.
     *
     * @return the offset value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Set the offset property: Zero-based index of the content represented by the span.
     *
     * @param offset the offset value to set.
     * @return the DocumentSpan object itself.
     */
    void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Get the length property: Number of characters in the content represented by the span.
     *
     * @return the length value.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Set the length property: Number of characters in the content represented by the span.
     *
     * @param length the length value to set.
     * @return the DocumentSpan object itself.
     */
    void setLength(int length) {
        this.length = length;
    }

    static {
        DocumentSpanHelper.setAccessor(new DocumentSpanHelper.DocumentSpanAccessor() {
            @Override
            public void setOffset(DocumentSpan documentSpan, int offset) {
                documentSpan.setOffset(offset);
            }

            @Override
            public void setLength(DocumentSpan documentSpan, int length) {
                documentSpan.setLength(length);
            }
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentSpanHelper;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Contiguous region of the concatenated content property, specified as an offset and length.
 */
@Immutable
public final class DocumentSpan implements JsonSerializable<DocumentSpan> {
    /**
     * Creates a DocumentSpan object.
     */
    public DocumentSpan() {
    }

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
     */
    private void setOffset(int offset) {
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
     */
    private void setLength(int length) {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("offset", this.offset);
        jsonWriter.writeIntField("length", this.length);
        return jsonWriter.writeEndObject();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentSpan;

/**
 * The helper class to set the non-public properties of an {@link DocumentSpan} instance.
 */
public final class DocumentSpanHelper {
    private static DocumentSpanAccessor accessor;

    private DocumentSpanHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentSpan} instance.
     */
    public interface DocumentSpanAccessor {
        void setOffset(DocumentSpan documentSpan, int offset);
        void setLength(DocumentSpan documentSpan, int length);
    }

    /**
     * The method called from {@link DocumentSpan} to set it's accessor.
     *
     * @param documentSpanAccessor The accessor.
     */
    public static void setAccessor(final DocumentSpanHelper.DocumentSpanAccessor documentSpanAccessor) {
        accessor = documentSpanAccessor;
    }

    static void setOffset(DocumentSpan documentSpan, int offset) {
        accessor.setOffset(documentSpan, offset);
    }

    static void setLength(DocumentSpan documentSpan, int length) {
        accessor.setLength(documentSpan, length);
    }
}

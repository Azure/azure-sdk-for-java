// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentLanguage;
import com.azure.ai.formrecognizer.models.DocumentSpan;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link DocumentLanguage} instance.
 */
public final class DocumentLanguageHelper {
    private static DocumentLanguageAccessor accessor;

    private DocumentLanguageHelper() {
    }

    /**
     * The method called from {@link DocumentLanguage} to set it's accessor.
     *
     * @param documentLanguageAccessor The accessor.
     */
    public static void setAccessor(final DocumentLanguageHelper.DocumentLanguageAccessor documentLanguageAccessor) {
        accessor = documentLanguageAccessor;
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentLanguage} instance.
     */
    public interface DocumentLanguageAccessor {
        void setLocale(DocumentLanguage documentLanguage, String locale);
        void setSpans(DocumentLanguage documentLanguage, List<DocumentSpan> spans);
        void setConfidence(DocumentLanguage documentLanguage, Float confidence);
    }

    static void setLocale(DocumentLanguage documentLanguage, String locale) {
        accessor.setLocale(documentLanguage, locale);
    }

    static void setConfidence(DocumentLanguage documentLanguage, Float confidence) {
        accessor.setConfidence(documentLanguage, confidence);
    }

    static void setSpans(DocumentLanguage documentLanguage, List<DocumentSpan> spans) {
        accessor.setSpans(documentLanguage, spans);
    }
}

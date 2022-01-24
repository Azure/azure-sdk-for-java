// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentFieldSchema;

import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocTypeInfo} instance.
 */
public final class DocTypeInfoHelper {
    private static DocTypeInfoAccessor accessor;

    private DocTypeInfoHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocTypeInfo} instance.
     */
    public interface DocTypeInfoAccessor {
        void setDescription(DocTypeInfo docTypeInfo, String description);
        void setFieldSchema(DocTypeInfo docTypeInfo, Map<String, DocumentFieldSchema> fieldSchema);
        void setFieldConfidence(DocTypeInfo docTypeInfo, Map<String, Float> fieldConfidence);
    }

    /**
     * The method called from {@link DocTypeInfo} to set it's accessor.
     *
     * @param docInfoAccessor The accessor.
     */
    public static void setAccessor(final DocTypeInfoHelper.DocTypeInfoAccessor docInfoAccessor) {
        accessor = docInfoAccessor;
    }

    static void setDescription(DocTypeInfo docTypeInfo, String description) {
        accessor.setDescription(docTypeInfo, description);
    }

    static void setFieldSchema(DocTypeInfo docTypeInfo, Map<String, DocumentFieldSchema> fieldSchema) {
        accessor.setFieldSchema(docTypeInfo, fieldSchema);
    }

    static void setFieldConfidence(DocTypeInfo docTypeInfo, Map<String, Float> fieldConfidence) {
        accessor.setFieldConfidence(docTypeInfo, fieldConfidence);
    }
}

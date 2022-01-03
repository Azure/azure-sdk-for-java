// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.administration.models.DocumentFieldSchema;

import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link DocumentFieldSchema} instance.
 */
public final class DocumentFieldSchemaHelper {
    private static DocumentFieldSchemaAccessor accessor;

    private DocumentFieldSchemaHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an {@link DocumentFieldSchema} instance.
     */
    public interface DocumentFieldSchemaAccessor {
        void setType(DocumentFieldSchema documentFieldSchema, DocumentFieldType type);
        void setDescription(DocumentFieldSchema documentFieldSchema, String description);
        void setExample(DocumentFieldSchema documentFieldSchema, String example);
        void setItems(DocumentFieldSchema documentFieldSchema, DocumentFieldSchema items);
        void setProperties(DocumentFieldSchema documentFieldSchema, Map<String, DocumentFieldSchema> properties);
    }

    /**
     * The method called from {@link DocumentFieldSchema} to set it's accessor.
     *
     * @param documentFieldSchemaAccessor The accessor.
     */
    public static void setAccessor(final DocumentFieldSchemaHelper.DocumentFieldSchemaAccessor documentFieldSchemaAccessor) {
        accessor = documentFieldSchemaAccessor;
    }

    static void setType(DocumentFieldSchema documentFieldSchema, DocumentFieldType type) {
        accessor.setType(documentFieldSchema, type);
    }

    static void setDescription(DocumentFieldSchema documentFieldSchema, String description) {
        accessor.setDescription(documentFieldSchema, description);
    }

    static void setExample(DocumentFieldSchema documentFieldSchema, String example) {
        accessor.setExample(documentFieldSchema, example);
    }

    static void setItems(DocumentFieldSchema documentFieldSchema, DocumentFieldSchema items) {
        accessor.setItems(documentFieldSchema, items);
    }

    static void setProperties(DocumentFieldSchema documentFieldSchema, Map<String, DocumentFieldSchema> properties) {
        accessor.setProperties(documentFieldSchema, properties);
    }
}

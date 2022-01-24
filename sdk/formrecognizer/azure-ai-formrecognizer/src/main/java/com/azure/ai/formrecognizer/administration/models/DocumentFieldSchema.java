// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentFieldSchemaHelper;
import com.azure.ai.formrecognizer.models.DocumentFieldType;

import java.util.Map;

/**
 * Description of the field semantic schema using a JSON Schema style syntax.
 */
public final class DocumentFieldSchema {
    /*
     * Semantic data type of the field value.
     */
    private DocumentFieldType type;

    /*
     * Field description.
     */
    private String description;

    /*
     * Example field content.
     */
    private String example;

    /*
     * Field type schema of each array element.
     */
    private DocumentFieldSchema items;

    /*
     * Named sub-fields of the object field.
     */
    private Map<String, DocumentFieldSchema> properties;

    /**
     * Get the type property: Semantic data type of the field value.
     *
     * @return the type value.
     */
    public DocumentFieldType getType() {
        return this.type;
    }

    /**
     * Set the type property: Semantic data type of the field value.
     *
     * @param type the type value to set.
     * @return the DocumentFieldSchema object itself.
     */
    void setType(DocumentFieldType type) {
        this.type = type;
    }

    /**
     * Get the description property: Field description.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: Field description.
     *
     * @param description the description value to set.
     * @return the DocumentFieldSchema object itself.
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the example property: Example field content.
     *
     * @return the example value.
     */
    public String getExample() {
        return this.example;
    }

    /**
     * Set the example property: Example field content.
     *
     * @param example the example value to set.
     * @return the DocumentFieldSchema object itself.
     */
    void setExample(String example) {
        this.example = example;
    }

    /**
     * Get the items property: Field type schema of each array element.
     *
     * @return the items value.
     */
    public DocumentFieldSchema getItems() {
        return this.items;
    }

    /**
     * Set the items property: Field type schema of each array element.
     *
     * @param items the items value to set.
     * @return the DocumentFieldSchema object itself.
     */
    void setItems(DocumentFieldSchema items) {
        this.items = items;
    }

    /**
     * Get the properties property: Named sub-fields of the object field.
     *
     * @return the properties value.
     */
    public Map<String, DocumentFieldSchema> getProperties() {
        return this.properties;
    }

    /**
     * Set the properties property: Named sub-fields of the object field.
     *
     * @param properties the properties value to set.
     * @return the DocumentFieldSchema object itself.
     */
    void setProperties(Map<String, DocumentFieldSchema> properties) {
        this.properties = properties;
    }

    static {
        DocumentFieldSchemaHelper.setAccessor(new DocumentFieldSchemaHelper.DocumentFieldSchemaAccessor() {
            @Override
            public void setType(DocumentFieldSchema documentFieldSchema, DocumentFieldType type) {
                documentFieldSchema.setType(type);
            }

            @Override
            public void setDescription(DocumentFieldSchema documentFieldSchema, String description) {
                documentFieldSchema.setDescription(description);
            }

            @Override
            public void setExample(DocumentFieldSchema documentFieldSchema, String example) {
                documentFieldSchema.setExample(example);
            }

            @Override
            public void setItems(DocumentFieldSchema documentFieldSchema, DocumentFieldSchema items) {
                documentFieldSchema.setItems(items);
            }

            @Override
            public void setProperties(DocumentFieldSchema documentFieldSchema, Map<String, DocumentFieldSchema> properties) {
                documentFieldSchema.setProperties(properties);
            }
        });
    }
}

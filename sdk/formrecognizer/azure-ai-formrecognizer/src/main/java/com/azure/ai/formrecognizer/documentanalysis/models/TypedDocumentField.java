// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.TypedDocumentFieldHelper;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * Strongly typed version of {@link DocumentField}
 * @param <T> the type of value.
 */
public class TypedDocumentField<T> {
    private T value;
    private DocumentFieldType type;
    private String content;
    private List<BoundingRegion> boundingRegions;
    private List<DocumentSpan> spans;
    private Float confidence;

    /**
     * Create a TypedDocumentField instance.
     */
    public TypedDocumentField() {
    }

    /**
     * Get value of the field.
     * @return the value of the field
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the data type of the field value.
     *
     * @return the type value.
     */
    public DocumentFieldType getType() {
        return this.type;
    }

    /**
     * Get the field content.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Get the bounding regions covering the field.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Get the location of the field in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Get the confidence of correctly extracting the field.
     *
     * @return the confidence value.
     */
    public Float getConfidence() {
        return this.confidence;
    }

    void setValue(T value) {
        this.value = value;
    }

    void setType(DocumentFieldType type) {
        this.type = type;
    }

    void setContent(String content) {
        this.content = content;
    }

    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    static {
        TypedDocumentFieldHelper.setAccessor(new TypedDocumentFieldHelper.TypedDocumentFieldAccessor() {

            @Override
            public <T> void setValue(TypedDocumentField<T> typedDocumentField, T value) {
                typedDocumentField.setValue(value);
            }

            @Override
            public <T> void setType(TypedDocumentField<T> typedDocumentField, DocumentFieldType type) {
                typedDocumentField.setType(type);
            }

            @Override
            public <T> void setContent(TypedDocumentField<T> typedDocumentField, String content) {
                typedDocumentField.setContent(content);
            }

            @Override
            public <T> void setBoundingRegions(TypedDocumentField<T> typedDocumentField,
                                               List<BoundingRegion> boundingRegions) {
                typedDocumentField.setBoundingRegions(boundingRegions);
            }

            @Override
            public <T> void setSpans(TypedDocumentField<T> typedDocumentField, List<DocumentSpan> spans) {
                typedDocumentField.setSpans(spans);

            }

            @Override
            public <T> void setConfidence(TypedDocumentField<T> typedDocumentField, Float confidence) {
                typedDocumentField.setConfidence(confidence);
            }
        });
    }

    /**
     * Reads an instance of TypedDocumentField from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of IndexAction if the JsonReader was pointing to an instance of it, or null if it was
     *     pointing to JSON null.
     * @throws IOException If an error occurs while reading the IndexAction.
     */
    public static TypedDocumentField fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(
            reader -> {
                TypedDocumentField deserializedValue = new TypedDocumentField();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("value".equals(fieldName)) {
                        deserializedValue.setValue(reader.getFieldName());
                    } else if ("type".equals(fieldName)) {
                        deserializedValue.setType(DocumentFieldType.fromString(reader.getString()));
                    } else if ("content".equals(fieldName)) {
                        deserializedValue.setContent(reader.getString());
                    } else if ("boundingRegions".equals(fieldName)) {
                        deserializedValue.setBoundingRegions(
                            reader.readArray(// BoundingRegionHelper::fromJson));
                    } else if ("spans".equals(fieldName)) {
                        deserializedValue.setSpans(reader.readArray(//DocumentSpanHelper::fromJson));
                    } else if ("confidence".equals(fieldName)) {
                        deserializedValue.setConfidence(reader.getFloat());
                    } else {
                        reader.skipValue();
                    }
                }
                return deserializedValue;
            });
    }
}

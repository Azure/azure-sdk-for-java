// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentFieldHelper;
import com.azure.core.annotation.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * An object representing the content and location of a field value.
 */
@Immutable
public final class DocumentField extends TypedDocumentField<Object> {
    /*
     * Data type of the field value.
     */
    private DocumentFieldType type;

    /*
     * Field content.
     */
    private String content;

    /*
     * Bounding regions covering the field.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the field in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /*
     * Confidence of correctly extracting the field.
     */
    private Float confidence;

    private Object value;

    /**
     * Get the type property: Data type of the field value.
     *
     * @return the type value.
     */
    public DocumentFieldType getType() {
        return this.type;
    }

    /**
     * Set the type property: Data type of the field value.
     *
     * @param type the type value to set.
     */
    private void setType(DocumentFieldType type) {
        this.type = type;
    }

    /**
     * Get the string value of the field.
     *
     * @return the value.
     */
    public String getValueAsString() {
        return (String) this.value;
    }

    /**
     * Get the date value in YYYY-MM-DD format (ISO 8601).
     *
     * @return the value.
     */
    public LocalDate getValueAsDate() {
        return (LocalDate) this.value;
    }

    /**
     * Get the time value in hh:mm:ss format (ISO 8601).
     *
     * @return the value.
     */
    public LocalTime getValueAsTime() {
        return (LocalTime) this.value;
    }

    /**
     * Get the phone number value in E.164 format (ex. +19876543210).
     *
     * @return the value.
     */
    public String getValueAsPhoneNumber() {
        return (String) this.value;
    }

    /**
     * Get the floating point value of the field.
     *
     * @return the value.
     */
    public Float getValueAsFloat() {
        return (Float) this.value;
    }

    /**
     * Get the integer value of the field.
     *
     * @return the value.
     */
    public Long getValueAsInteger() {
        return (Long) this.value;
    }

    /**
     * Get the selection mark value.
     *
     * @return the value.
     */
    public SelectionMarkState getValueAsSelectionMark() {
        return (SelectionMarkState) this.value;
    }

    /**
     * Get the presence of signature type.
     *
     * @return the value.
     */
    public DocumentSignatureType getValueAsSignature() {
        return (DocumentSignatureType) this.value;
    }

    /**
     * Get the 3-letter country code value (ISO 3166-1 alpha-3).
     *
     * @return the value.
     */
    public String getValueAsCountry() {
        return (String) this.value;
    }

    /**
     * Get the array of field values.
     *
     * @return the value.
     */
    @SuppressWarnings("unchecked")
    public List<DocumentField> getValueAsList() {
        return (List<DocumentField>) this.value;
    }

    /**
     * Get the map of named field values.
     *
     * @return the value.
     */
    @SuppressWarnings("unchecked")
    public Map<String, DocumentField> getValueAsMap() {
        return (Map<String, DocumentField>) this.value;
    }

    /**
     * Get the Currency value.
     *
     * @return the value.
     */
    public CurrencyValue getValueAsCurrency() {
        return (CurrencyValue) this.value;
    }

    /**
     * Get address value of the field.
     *
     * @return the value.
     */
    public AddressValue getValueAsAddress() {
        return (AddressValue) this.value;
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
     * Set the content property: Field content.
     *
     * @param content the content value to set.
     */
    private void setContent(String content) {
        this.content = content;
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
     * Set the boundingRegions property: Bounding regions covering the field.
     *
     * @param boundingRegions the boundingRegions value to set.
     */
    private void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
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
     * Set the location of the field in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     */
    private void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the confidence of correctly extracting the field.
     *
     * @return the confidence value.
     */
    public Float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the field.
     *
     * @param confidence the confidence value to set.
     */
    private void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    /**
     * Set the field value.
     *
     * @param value the value to set.
     */
    private void setValue(Object value) {
        this.value = value;
    }

    static {
        DocumentFieldHelper.setAccessor(new DocumentFieldHelper.DocumentFieldAccessor() {
            @Override
            public void setType(DocumentField documentField, DocumentFieldType type) {
                documentField.setType(type);
            }

            @Override
            public void setContent(DocumentField documentField, String content) {
                documentField.setContent(content);
            }

            @Override
            public void setBoundingRegions(DocumentField documentField, List<BoundingRegion> boundingRegions) {
                documentField.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(DocumentField documentField, List<DocumentSpan> spans) {
                documentField.setSpans(spans);
            }

            @Override
            public void setConfidence(DocumentField documentField, Float confidence) {
                documentField.setConfidence(confidence);
            }

            @Override
            public void setValue(DocumentField documentField, Object value) {
                documentField.setValue(value);
            }
        });
    }
}

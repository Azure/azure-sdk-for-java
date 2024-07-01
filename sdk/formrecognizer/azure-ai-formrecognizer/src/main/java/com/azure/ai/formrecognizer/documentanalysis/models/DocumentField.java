// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentFieldHelper;
import com.azure.ai.formrecognizer.documentanalysis.implementation.util.TypedDocumentFieldHelper;
import com.azure.core.annotation.Immutable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Model representing the content and location of a field value.
 */
@Immutable
public final class DocumentField extends TypedDocumentField<Object> {
    /**
     * Constructs a DocumentField object.
     */
    public DocumentField() {
        super();
    }

    // Ignore custom getters in the class to prevent serialization and deserialization issues

    /**
     * Get the string value of the field.
     *
     * @return the value.
     */

    public String getValueAsString() {
        return (String) super.getValue();
    }

    /**
     * Get the date value in YYYY-MM-DD format (ISO 8601).
     *
     * @return the value.
     */
    public LocalDate getValueAsDate() {
        return (LocalDate) super.getValue();
    }

    /**
     * Get the time value in hh:mm:ss format (ISO 8601).
     *
     * @return the value.
     */
    public LocalTime getValueAsTime() {
        return (LocalTime) super.getValue();
    }

    /**
     * Get the phone number value in E.164 format (ex. +19876543210).
     *
     * @return the value.
     */
    public String getValueAsPhoneNumber() {
        return (String) super.getValue();
    }

    /**
     * Get the double/floating point value of the field.
     *
     * @return the value.
     */
    public Double getValueAsDouble() {
        return (Double) super.getValue();
    }

    /**
     * Get the long value of the field.
     *
     * @return the value.
     */
    public Long getValueAsLong() {
        return (Long) super.getValue();
    }

    /**
     * Get the selection mark value.
     *
     * @return the value.
     */
    public DocumentSelectionMarkState getValueAsSelectionMark() {
        return (DocumentSelectionMarkState) super.getValue();
    }

    /**
     * Get the presence of signature type.
     *
     * @return the value.
     */
    public DocumentSignatureType getValueAsSignature() {
        return (DocumentSignatureType) super.getValue();
    }

    /**
     * Get the 3-letter country code value (ISO 3166-1 alpha-3).
     *
     * @return the value.
     */
    public String getValueAsCountry() {
        return (String) super.getValue();
    }

    /**
     * Get the array of field values.
     *
     * @return the value.
     */
    @SuppressWarnings("unchecked")
    public List<DocumentField> getValueAsList() {
        return (List<DocumentField>) super.getValue();
    }

    /**
     * Get the map of named field values.
     *
     * @return the value.
     */
    @SuppressWarnings("unchecked")
    public Map<String, DocumentField> getValueAsMap() {
        return (Map<String, DocumentField>) super.getValue();
    }

    /**
     * Get the Currency value.
     *
     * @return the value.
     */
    public CurrencyValue getValueAsCurrency() {
        return (CurrencyValue) super.getValue();
    }

    /**
     * Get address value of the field.
     *
     * @return the value.
     */
    public AddressValue getValueAsAddress() {
        return (AddressValue) super.getValue();
    }

    /**
     * Get boolean value of the field.
     *
     * @return the value.
     */
    public Boolean getValueAsBoolean() {
        return (Boolean) super.getValue();
    }

    static {
        DocumentFieldHelper.setAccessor(new TypedDocumentFieldHelper.TypedDocumentFieldAccessor() {

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
}

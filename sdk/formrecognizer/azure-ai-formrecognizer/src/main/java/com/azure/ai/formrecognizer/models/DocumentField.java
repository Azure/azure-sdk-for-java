// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentFieldHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** An object representing the content and location of a field value. */
public final class DocumentField {
    /*
     * Data type of the field value.
     */
    private DocumentFieldType type;

    /*
     * String value.
     */
    private String valueString;

    /*
     * Date value in YYYY-MM-DD format (ISO 8601).
     */
    private LocalDate valueDate;

    /*
     * Time value in hh:mm:ss format (ISO 8601).
     */
    private LocalTime valueTime;

    /*
     * Phone number value in E.164 format (ex. +19876543210).
     */
    private String valuePhoneNumber;

    /*
     * Floating point value.
     */
    private Float valueFloat;

    /*
     * Integer value.
     */
    private Long valueInteger;

    /*
     * Selection mark value.
     */
    private SelectionMarkState valueSelectionMark;

    /*
     * Presence of signature.
     */
    private DocumentSignatureType valueSignature;

    /*
     * 3-letter country code value (ISO 3166-1 alpha-3).
     */
    private String valueCountryRegion;

    /*
     * Array of field values.
     */
    private List<DocumentField> valueList;

    /*
     * Dictionary of named field values.
     */
    private Map<String, DocumentField> valueMap;

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
     * @return the DocumentField object itself.
     */
    void setType(DocumentFieldType type) {
        this.type = type;
    }

    /**
     * Get the valueString property: String value.
     *
     * @return the valueString value.
     */
    public String getValueString() {
        return this.valueString;
    }

    /**
     * Set the valueString property: String value.
     *
     * @param valueString the valueString value to set.
     * @return the DocumentField object itself.
     */
    void setValueString(String valueString) {
        this.valueString = valueString;
    }

    /**
     * Get the valueDate property: Date value in YYYY-MM-DD format (ISO 8601).
     *
     * @return the valueDate value.
     */
    public LocalDate getValueDate() {
        return this.valueDate;
    }

    /**
     * Set the valueDate property: Date value in YYYY-MM-DD format (ISO 8601).
     *
     * @param valueDate the valueDate value to set.
     * @return the DocumentField object itself.
     */
    void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    /**
     * Get the valueTime property: Time value in hh:mm:ss format (ISO 8601).
     *
     * @return the valueTime value.
     */
    public LocalTime getValueTime() {
        return this.valueTime;
    }

    /**
     * Set the valueTime property: Time value in hh:mm:ss format (ISO 8601).
     *
     * @param valueTime the valueTime value to set.
     * @return the DocumentField object itself.
     */
    void setValueTime(LocalTime valueTime) {
        this.valueTime = valueTime;
    }

    /**
     * Get the valuePhoneNumber property: Phone number value in E.164 format (ex. +19876543210).
     *
     * @return the valuePhoneNumber value.
     */
    public String getValuePhoneNumber() {
        return this.valuePhoneNumber;
    }

    /**
     * Set the valuePhoneNumber property: Phone number value in E.164 format (ex. +19876543210).
     *
     * @param valuePhoneNumber the valuePhoneNumber value to set.
     * @return the DocumentField object itself.
     */
    void setValuePhoneNumber(String valuePhoneNumber) {
        this.valuePhoneNumber = valuePhoneNumber;
    }

    /**
     * Get the valueFloat property: Floating point value.
     *
     * @return the valueFloat value.
     */
    public Float getValueFloat() {
        return this.valueFloat;
    }

    /**
     * Set the valueFloat property: Floating point value.
     *
     * @param valueFloat the valueFloat value to set.
     * @return the DocumentField object itself.
     */
    void setValueFloat(Float valueFloat) {
        this.valueFloat = valueFloat;
    }

    /**
     * Get the valueInteger property: Integer value.
     *
     * @return the valueInteger value.
     */
    public Long getValueInteger() {
        return this.valueInteger;
    }

    /**
     * Set the valueInteger property: Integer value.
     *
     * @param valueInteger the valueInteger value to set.
     * @return the DocumentField object itself.
     */
    void setValueInteger(Long valueInteger) {
        this.valueInteger = valueInteger;
    }

    /**
     * Get the valueSelectionMark property: Selection mark value.
     *
     * @return the valueSelectionMark value.
     */
    public SelectionMarkState getValueSelectionMark() {
        return this.valueSelectionMark;
    }

    /**
     * Set the valueSelectionMark property: Selection mark value.
     *
     * @param valueSelectionMark the valueSelectionMark value to set.
     * @return the DocumentField object itself.
     */
    void setValueSelectionMark(SelectionMarkState valueSelectionMark) {
        this.valueSelectionMark = valueSelectionMark;
    }

    /**
     * Get the valueSignature property: Presence of signature.
     *
     * @return the valueSignature value.
     */
    public DocumentSignatureType getValueSignature() {
        return this.valueSignature;
    }

    /**
     * Set the valueSignature property: Presence of signature.
     *
     * @param valueSignature the valueSignature value to set.
     * @return the DocumentField object itself.
     */
    void setValueSignature(DocumentSignatureType valueSignature) {
        this.valueSignature = valueSignature;
    }

    /**
     * Get the valueCountryRegion property: 3-letter country code value (ISO 3166-1 alpha-3).
     *
     * @return the valueCountryRegion value.
     */
    public String getValueCountryRegion() {
        return this.valueCountryRegion;
    }

    /**
     * Set the valueCountryRegion property: 3-letter country code value (ISO 3166-1 alpha-3).
     *
     * @param valueCountryRegion the valueCountryRegion value to set.
     * @return the DocumentField object itself.
     */
    void setValueCountryRegion(String valueCountryRegion) {
        this.valueCountryRegion = valueCountryRegion;
    }

    /**
     * Set the valueCurrency property: 3-letter currency code value (ISO 4217).
     *
     * @param valueCurrency the valueCurrency value to set.
     * @return the DocumentField object itself.
     */
    /**
     * Get the valueList property: Array of field values.
     *
     * @return the valueList value.
     */
    public List<DocumentField> getValueList() {
        return this.valueList;
    }

    /**
     * Set the valueList property: Array of field values.
     *
     * @param valueList the valueList value to set.
     * @return the DocumentField object itself.
     */
    void setValueList(List<DocumentField> valueList) {
        this.valueList = valueList;
    }

    /**
     * Get the valueMap property: Dictionary of named field values.
     *
     * @return the valueMap value.
     */
    public Map<String, DocumentField> getValueMap() {
        return this.valueMap;
    }

    /**
     * Set the valueMap property: Dictionary of named field values.
     *
     * @param valueMap the valueMap value to set.
     * @return the DocumentField object itself.
     */
    void setValueMap(Map<String, DocumentField> valueMap) {
        this.valueMap = valueMap;
    }

    /**
     * Get the content property: Field content.
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
     * @return the DocumentField object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the field.
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
     * @return the DocumentField object itself.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the field in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the field in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentField object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the field.
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
     * @return the DocumentField object itself.
     */
    void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentFieldHelper.setAccessor(new DocumentFieldHelper.DocumentFieldAccessor() {
            @Override
            public void setType(DocumentField documentField, DocumentFieldType type) {
                documentField.setType(type);
            }

            @Override
            public void setValueString(DocumentField documentField, String valueString) {
                documentField.setValueString(valueString);
            }

            @Override
            public void setValueDate(DocumentField documentField, LocalDate valueDate) {
                documentField.setValueDate(valueDate);
            }

            @Override
            public void setValueTime(DocumentField documentField, LocalTime valueTime) {
                documentField.setValueTime(valueTime);
            }

            @Override
            public void setValuePhoneNumber(DocumentField documentField, String valuePhoneNumber) {
                documentField.setValuePhoneNumber(valuePhoneNumber);
            }

            @Override
            public void setValueNumber(DocumentField documentField, Float valueFloat) {
                documentField.setValueFloat(valueFloat);
            }

            @Override
            public void setValueInteger(DocumentField documentField, Long valueInteger) {
                documentField.setValueInteger(valueInteger);
            }

            @Override
            public void setValueSelectionMark(DocumentField documentField,
                                              SelectionMarkState valueSelectionMark) {
                documentField.setValueSelectionMark(valueSelectionMark);
            }

            @Override
            public void setValueSignature(DocumentField documentField,
                                          DocumentSignatureType valueSignature) {
                documentField.setValueSignature(valueSignature);
            }

            @Override
            public void setValueCountryRegion(DocumentField documentField, String valueCountryRegion) {
                documentField.setValueCountryRegion(valueCountryRegion);
            }

            @Override
            public void setValueArray(DocumentField documentField,
                                      List<DocumentField> valueList) {
                documentField.setValueList(valueList);
            }

            @Override
            public void setValueObject(DocumentField documentField,
                                       Map<String, DocumentField> valueMap) {
                documentField.setValueMap(valueMap);
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
        });
    }
}

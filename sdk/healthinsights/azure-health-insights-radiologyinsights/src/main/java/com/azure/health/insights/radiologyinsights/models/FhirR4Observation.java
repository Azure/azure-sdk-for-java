// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.health.insights.radiologyinsights.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Detailed information about observations
 * Based on [FHIR Observation](https://www.hl7.org/fhir/R4/observation.html).
 */
@Immutable
public final class FhirR4Observation extends FhirR4DomainResource {

    /*
     * Discriminator property for Fhir_R4_DomainResource.
     */
    @Generated
    private String resourceType = "Observation";

    /*
     * Business Identifier for observation
     */
    @Generated
    private List<FhirR4Identifier> identifier;

    /*
     * registered | preliminary | final | amended +
     */
    @Generated
    private final ObservationStatusCodeType status;

    /*
     * Classification of type of observation
     */
    @Generated
    private List<FhirR4CodeableConcept> category;

    /*
     * Type of observation (code / type)
     */
    @Generated
    private final FhirR4CodeableConcept code;

    /*
     * Who and/or what the observation is about
     */
    @Generated
    private FhirR4Reference subject;

    /*
     * Healthcare event during which this observation is made
     */
    @Generated
    private FhirR4Reference encounter;

    /*
     * Clinically relevant time/time-period for observation
     */
    @Generated
    private String effectiveDateTime;

    /*
     * Clinically relevant time/time-period for observation
     */
    @Generated
    private FhirR4Period effectivePeriod;

    /*
     * Clinically relevant time/time-period for observation
     */
    @Generated
    private String effectiveInstant;

    /*
     * Date/Time this version was made available
     */
    @Generated
    private String issued;

    /*
     * Actual result
     */
    @Generated
    private FhirR4Quantity valueQuantity;

    /*
     * Actual result
     */
    @Generated
    private FhirR4CodeableConcept valueCodeableConcept;

    /*
     * Actual result
     */
    @Generated
    private String valueString;

    /*
     * Actual result
     */
    @Generated
    private Boolean valueBoolean;

    /*
     * Actual result
     */
    @Generated
    private Integer valueInteger;

    /*
     * Actual result
     */
    @Generated
    private FhirR4Range valueRange;

    /*
     * Actual result
     */
    @Generated
    private FhirR4Ratio valueRatio;

    /*
     * Actual result
     */
    @Generated
    private FhirR4SampledData valueSampledData;

    /*
     * Actual result
     */
    @Generated
    private String valueTime;

    /*
     * Actual result
     */
    @Generated
    private String valueDateTime;

    /*
     * Actual result
     */
    @Generated
    private FhirR4Period valuePeriod;

    /*
     * Why the result is missing
     */
    @Generated
    private FhirR4CodeableConcept dataAbsentReason;

    /*
     * High, low, normal, etc.
     */
    @Generated
    private List<FhirR4CodeableConcept> interpretation;

    /*
     * Comments about the observation
     */
    @Generated
    private List<FhirR4Annotation> note;

    /*
     * Observed body part
     */
    @Generated
    private FhirR4CodeableConcept bodySite;

    /*
     * How it was done
     */
    @Generated
    private FhirR4CodeableConcept method;

    /*
     * Provides guide for interpretation
     */
    @Generated
    private List<FhirR4ObservationReferenceRange> referenceRange;

    /*
     * Related resource that belongs to the Observation group
     */
    @Generated
    private List<FhirR4Reference> hasMember;

    /*
     * Related measurements the observation is made from
     */
    @Generated
    private List<FhirR4Reference> derivedFrom;

    /*
     * Component results
     */
    @Generated
    private List<FhirR4ObservationComponent> component;

    /**
     * Creates an instance of FhirR4Observation class.
     *
     * @param resourceType the resourceType value to set.
     * @param status the status value to set.
     * @param code the code value to set.
     */
    @Generated
    private FhirR4Observation(String resourceType, ObservationStatusCodeType status, FhirR4CodeableConcept code) {
        super(resourceType);
        this.status = status;
        this.code = code;
    }

    /**
     * Get the resourceType property: Discriminator property for Fhir_R4_DomainResource.
     *
     * @return the resourceType value.
     */
    @Generated
    @Override
    public String getResourceType() {
        return this.resourceType;
    }

    /**
     * Get the identifier property: Business Identifier for observation.
     *
     * @return the identifier value.
     */
    @Generated
    public List<FhirR4Identifier> getIdentifier() {
        return this.identifier;
    }

    /**
     * Get the status property: registered | preliminary | final | amended +.
     *
     * @return the status value.
     */
    @Generated
    public ObservationStatusCodeType getStatus() {
        return this.status;
    }

    /**
     * Get the category property: Classification of type of observation.
     *
     * @return the category value.
     */
    @Generated
    public List<FhirR4CodeableConcept> getCategory() {
        return this.category;
    }

    /**
     * Get the code property: Type of observation (code / type).
     *
     * @return the code value.
     */
    @Generated
    public FhirR4CodeableConcept getCode() {
        return this.code;
    }

    /**
     * Get the subject property: Who and/or what the observation is about.
     *
     * @return the subject value.
     */
    @Generated
    public FhirR4Reference getSubject() {
        return this.subject;
    }

    /**
     * Get the encounter property: Healthcare event during which this observation is made.
     *
     * @return the encounter value.
     */
    @Generated
    public FhirR4Reference getEncounter() {
        return this.encounter;
    }

    /**
     * Get the effectiveDateTime property: Clinically relevant time/time-period for observation.
     *
     * @return the effectiveDateTime value.
     */
    @Generated
    public String getEffectiveDateTime() {
        return this.effectiveDateTime;
    }

    /**
     * Get the effectivePeriod property: Clinically relevant time/time-period for observation.
     *
     * @return the effectivePeriod value.
     */
    @Generated
    public FhirR4Period getEffectivePeriod() {
        return this.effectivePeriod;
    }

    /**
     * Get the effectiveInstant property: Clinically relevant time/time-period for observation.
     *
     * @return the effectiveInstant value.
     */
    @Generated
    public String getEffectiveInstant() {
        return this.effectiveInstant;
    }

    /**
     * Get the issued property: Date/Time this version was made available.
     *
     * @return the issued value.
     */
    @Generated
    public String getIssued() {
        return this.issued;
    }

    /**
     * Get the valueQuantity property: Actual result.
     *
     * @return the valueQuantity value.
     */
    @Generated
    public FhirR4Quantity getValueQuantity() {
        return this.valueQuantity;
    }

    /**
     * Get the valueCodeableConcept property: Actual result.
     *
     * @return the valueCodeableConcept value.
     */
    @Generated
    public FhirR4CodeableConcept getValueCodeableConcept() {
        return this.valueCodeableConcept;
    }

    /**
     * Get the valueString property: Actual result.
     *
     * @return the valueString value.
     */
    @Generated
    public String getValueString() {
        return this.valueString;
    }

    /**
     * Get the valueBoolean property: Actual result.
     *
     * @return the valueBoolean value.
     */
    @Generated
    public Boolean isValueBoolean() {
        return this.valueBoolean;
    }

    /**
     * Get the valueInteger property: Actual result.
     *
     * @return the valueInteger value.
     */
    @Generated
    public Integer getValueInteger() {
        return this.valueInteger;
    }

    /**
     * Get the valueRange property: Actual result.
     *
     * @return the valueRange value.
     */
    @Generated
    public FhirR4Range getValueRange() {
        return this.valueRange;
    }

    /**
     * Get the valueRatio property: Actual result.
     *
     * @return the valueRatio value.
     */
    @Generated
    public FhirR4Ratio getValueRatio() {
        return this.valueRatio;
    }

    /**
     * Get the valueSampledData property: Actual result.
     *
     * @return the valueSampledData value.
     */
    @Generated
    public FhirR4SampledData getValueSampledData() {
        return this.valueSampledData;
    }

    /**
     * Get the valueTime property: Actual result.
     *
     * @return the valueTime value.
     */
    @Generated
    public String getValueTime() {
        return this.valueTime;
    }

    /**
     * Get the valueDateTime property: Actual result.
     *
     * @return the valueDateTime value.
     */
    @Generated
    public String getValueDateTime() {
        return this.valueDateTime;
    }

    /**
     * Get the valuePeriod property: Actual result.
     *
     * @return the valuePeriod value.
     */
    @Generated
    public FhirR4Period getValuePeriod() {
        return this.valuePeriod;
    }

    /**
     * Get the dataAbsentReason property: Why the result is missing.
     *
     * @return the dataAbsentReason value.
     */
    @Generated
    public FhirR4CodeableConcept getDataAbsentReason() {
        return this.dataAbsentReason;
    }

    /**
     * Get the interpretation property: High, low, normal, etc.
     *
     * @return the interpretation value.
     */
    @Generated
    public List<FhirR4CodeableConcept> getInterpretation() {
        return this.interpretation;
    }

    /**
     * Get the note property: Comments about the observation.
     *
     * @return the note value.
     */
    @Generated
    public List<FhirR4Annotation> getNote() {
        return this.note;
    }

    /**
     * Get the bodySite property: Observed body part.
     *
     * @return the bodySite value.
     */
    @Generated
    public FhirR4CodeableConcept getBodySite() {
        return this.bodySite;
    }

    /**
     * Get the method property: How it was done.
     *
     * @return the method value.
     */
    @Generated
    public FhirR4CodeableConcept getMethod() {
        return this.method;
    }

    /**
     * Get the referenceRange property: Provides guide for interpretation.
     *
     * @return the referenceRange value.
     */
    @Generated
    public List<FhirR4ObservationReferenceRange> getReferenceRange() {
        return this.referenceRange;
    }

    /**
     * Get the hasMember property: Related resource that belongs to the Observation group.
     *
     * @return the hasMember value.
     */
    @Generated
    public List<FhirR4Reference> getHasMember() {
        return this.hasMember;
    }

    /**
     * Get the derivedFrom property: Related measurements the observation is made from.
     *
     * @return the derivedFrom value.
     */
    @Generated
    public List<FhirR4Reference> getDerivedFrom() {
        return this.derivedFrom;
    }

    /**
     * Get the component property: Component results.
     *
     * @return the component value.
     */
    @Generated
    public List<FhirR4ObservationComponent> getComponent() {
        return this.component;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", getId());
        jsonWriter.writeJsonField("meta", getMeta());
        jsonWriter.writeStringField("implicitRules", getImplicitRules());
        jsonWriter.writeStringField("language", getLanguage());
        jsonWriter.writeJsonField("text", getText());
        jsonWriter.writeArrayField("contained", getContained(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("extension", getExtension(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("modifierExtension", getModifierExtension(),
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("status", this.status == null ? null : this.status.toString());
        jsonWriter.writeJsonField("code", this.code);
        jsonWriter.writeStringField("resourceType", this.resourceType);
        jsonWriter.writeArrayField("identifier", this.identifier, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("category", this.category, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("subject", this.subject);
        jsonWriter.writeJsonField("encounter", this.encounter);
        jsonWriter.writeStringField("effectiveDateTime", this.effectiveDateTime);
        jsonWriter.writeJsonField("effectivePeriod", this.effectivePeriod);
        jsonWriter.writeStringField("effectiveInstant", this.effectiveInstant);
        jsonWriter.writeStringField("issued", this.issued);
        jsonWriter.writeJsonField("valueQuantity", this.valueQuantity);
        jsonWriter.writeJsonField("valueCodeableConcept", this.valueCodeableConcept);
        jsonWriter.writeStringField("valueString", this.valueString);
        jsonWriter.writeBooleanField("valueBoolean", this.valueBoolean);
        jsonWriter.writeNumberField("valueInteger", this.valueInteger);
        jsonWriter.writeJsonField("valueRange", this.valueRange);
        jsonWriter.writeJsonField("valueRatio", this.valueRatio);
        jsonWriter.writeJsonField("valueSampledData", this.valueSampledData);
        jsonWriter.writeStringField("valueTime", this.valueTime);
        jsonWriter.writeStringField("valueDateTime", this.valueDateTime);
        jsonWriter.writeJsonField("valuePeriod", this.valuePeriod);
        jsonWriter.writeJsonField("dataAbsentReason", this.dataAbsentReason);
        jsonWriter.writeArrayField("interpretation", this.interpretation,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("note", this.note, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("bodySite", this.bodySite);
        jsonWriter.writeJsonField("method", this.method);
        jsonWriter.writeArrayField("referenceRange", this.referenceRange,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("hasMember", this.hasMember, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("derivedFrom", this.derivedFrom, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("component", this.component, (writer, element) -> writer.writeJson(element));
        if (getAdditionalProperties() != null) {
            for (Map.Entry<String, Object> additionalProperty : getAdditionalProperties().entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of FhirR4Observation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of FhirR4Observation if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the FhirR4Observation.
     */
    @Generated
    public static FhirR4Observation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            FhirR4Meta meta = null;
            String implicitRules = null;
            String language = null;
            FhirR4Narrative text = null;
            List<FhirR4Resource> contained = null;
            List<FhirR4Extension> extension = null;
            List<FhirR4Extension> modifierExtension = null;
            ObservationStatusCodeType status = null;
            FhirR4CodeableConcept code = null;
            String resourceType = "Observation";
            List<FhirR4Identifier> identifier = null;
            List<FhirR4CodeableConcept> category = null;
            FhirR4Reference subject = null;
            FhirR4Reference encounter = null;
            String effectiveDateTime = null;
            FhirR4Period effectivePeriod = null;
            String effectiveInstant = null;
            String issued = null;
            FhirR4Quantity valueQuantity = null;
            FhirR4CodeableConcept valueCodeableConcept = null;
            String valueString = null;
            Boolean valueBoolean = null;
            Integer valueInteger = null;
            FhirR4Range valueRange = null;
            FhirR4Ratio valueRatio = null;
            FhirR4SampledData valueSampledData = null;
            String valueTime = null;
            String valueDateTime = null;
            FhirR4Period valuePeriod = null;
            FhirR4CodeableConcept dataAbsentReason = null;
            List<FhirR4CodeableConcept> interpretation = null;
            List<FhirR4Annotation> note = null;
            FhirR4CodeableConcept bodySite = null;
            FhirR4CodeableConcept method = null;
            List<FhirR4ObservationReferenceRange> referenceRange = null;
            List<FhirR4Reference> hasMember = null;
            List<FhirR4Reference> derivedFrom = null;
            List<FhirR4ObservationComponent> component = null;
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("meta".equals(fieldName)) {
                    meta = FhirR4Meta.fromJson(reader);
                } else if ("implicitRules".equals(fieldName)) {
                    implicitRules = reader.getString();
                } else if ("language".equals(fieldName)) {
                    language = reader.getString();
                } else if ("text".equals(fieldName)) {
                    text = FhirR4Narrative.fromJson(reader);
                } else if ("contained".equals(fieldName)) {
                    contained = reader.readArray(reader1 -> FhirR4Resource.fromJson(reader1));
                } else if ("extension".equals(fieldName)) {
                    extension = reader.readArray(reader1 -> FhirR4Extension.fromJson(reader1));
                } else if ("modifierExtension".equals(fieldName)) {
                    modifierExtension = reader.readArray(reader1 -> FhirR4Extension.fromJson(reader1));
                } else if ("status".equals(fieldName)) {
                    status = ObservationStatusCodeType.fromString(reader.getString());
                } else if ("code".equals(fieldName)) {
                    code = FhirR4CodeableConcept.fromJson(reader);
                } else if ("resourceType".equals(fieldName)) {
                    resourceType = reader.getString();
                } else if ("identifier".equals(fieldName)) {
                    identifier = reader.readArray(reader1 -> FhirR4Identifier.fromJson(reader1));
                } else if ("category".equals(fieldName)) {
                    category = reader.readArray(reader1 -> FhirR4CodeableConcept.fromJson(reader1));
                } else if ("subject".equals(fieldName)) {
                    subject = FhirR4Reference.fromJson(reader);
                } else if ("encounter".equals(fieldName)) {
                    encounter = FhirR4Reference.fromJson(reader);
                } else if ("effectiveDateTime".equals(fieldName)) {
                    effectiveDateTime = reader.getString();
                } else if ("effectivePeriod".equals(fieldName)) {
                    effectivePeriod = FhirR4Period.fromJson(reader);
                } else if ("effectiveInstant".equals(fieldName)) {
                    effectiveInstant = reader.getString();
                } else if ("issued".equals(fieldName)) {
                    issued = reader.getString();
                } else if ("valueQuantity".equals(fieldName)) {
                    valueQuantity = FhirR4Quantity.fromJson(reader);
                } else if ("valueCodeableConcept".equals(fieldName)) {
                    valueCodeableConcept = FhirR4CodeableConcept.fromJson(reader);
                } else if ("valueString".equals(fieldName)) {
                    valueString = reader.getString();
                } else if ("valueBoolean".equals(fieldName)) {
                    valueBoolean = reader.getNullable(JsonReader::getBoolean);
                } else if ("valueInteger".equals(fieldName)) {
                    valueInteger = reader.getNullable(JsonReader::getInt);
                } else if ("valueRange".equals(fieldName)) {
                    valueRange = FhirR4Range.fromJson(reader);
                } else if ("valueRatio".equals(fieldName)) {
                    valueRatio = FhirR4Ratio.fromJson(reader);
                } else if ("valueSampledData".equals(fieldName)) {
                    valueSampledData = FhirR4SampledData.fromJson(reader);
                } else if ("valueTime".equals(fieldName)) {
                    valueTime = reader.getString();
                } else if ("valueDateTime".equals(fieldName)) {
                    valueDateTime = reader.getString();
                } else if ("valuePeriod".equals(fieldName)) {
                    valuePeriod = FhirR4Period.fromJson(reader);
                } else if ("dataAbsentReason".equals(fieldName)) {
                    dataAbsentReason = FhirR4CodeableConcept.fromJson(reader);
                } else if ("interpretation".equals(fieldName)) {
                    interpretation = reader.readArray(reader1 -> FhirR4CodeableConcept.fromJson(reader1));
                } else if ("note".equals(fieldName)) {
                    note = reader.readArray(reader1 -> FhirR4Annotation.fromJson(reader1));
                } else if ("bodySite".equals(fieldName)) {
                    bodySite = FhirR4CodeableConcept.fromJson(reader);
                } else if ("method".equals(fieldName)) {
                    method = FhirR4CodeableConcept.fromJson(reader);
                } else if ("referenceRange".equals(fieldName)) {
                    referenceRange = reader.readArray(reader1 -> FhirR4ObservationReferenceRange.fromJson(reader1));
                } else if ("hasMember".equals(fieldName)) {
                    hasMember = reader.readArray(reader1 -> FhirR4Reference.fromJson(reader1));
                } else if ("derivedFrom".equals(fieldName)) {
                    derivedFrom = reader.readArray(reader1 -> FhirR4Reference.fromJson(reader1));
                } else if ("component".equals(fieldName)) {
                    component = reader.readArray(reader1 -> FhirR4ObservationComponent.fromJson(reader1));
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }
                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            FhirR4Observation deserializedFhirR4Observation = new FhirR4Observation(resourceType, status, code);
            deserializedFhirR4Observation.setId(id);
            deserializedFhirR4Observation.setMeta(meta);
            deserializedFhirR4Observation.setImplicitRules(implicitRules);
            deserializedFhirR4Observation.setLanguage(language);
            deserializedFhirR4Observation.setText(text);
            deserializedFhirR4Observation.setContained(contained);
            deserializedFhirR4Observation.setExtension(extension);
            deserializedFhirR4Observation.setModifierExtension(modifierExtension);
            deserializedFhirR4Observation.resourceType = resourceType;
            deserializedFhirR4Observation.identifier = identifier;
            deserializedFhirR4Observation.category = category;
            deserializedFhirR4Observation.subject = subject;
            deserializedFhirR4Observation.encounter = encounter;
            deserializedFhirR4Observation.effectiveDateTime = effectiveDateTime;
            deserializedFhirR4Observation.effectivePeriod = effectivePeriod;
            deserializedFhirR4Observation.effectiveInstant = effectiveInstant;
            deserializedFhirR4Observation.issued = issued;
            deserializedFhirR4Observation.valueQuantity = valueQuantity;
            deserializedFhirR4Observation.valueCodeableConcept = valueCodeableConcept;
            deserializedFhirR4Observation.valueString = valueString;
            deserializedFhirR4Observation.valueBoolean = valueBoolean;
            deserializedFhirR4Observation.valueInteger = valueInteger;
            deserializedFhirR4Observation.valueRange = valueRange;
            deserializedFhirR4Observation.valueRatio = valueRatio;
            deserializedFhirR4Observation.valueSampledData = valueSampledData;
            deserializedFhirR4Observation.valueTime = valueTime;
            deserializedFhirR4Observation.valueDateTime = valueDateTime;
            deserializedFhirR4Observation.valuePeriod = valuePeriod;
            deserializedFhirR4Observation.dataAbsentReason = dataAbsentReason;
            deserializedFhirR4Observation.interpretation = interpretation;
            deserializedFhirR4Observation.note = note;
            deserializedFhirR4Observation.bodySite = bodySite;
            deserializedFhirR4Observation.method = method;
            deserializedFhirR4Observation.referenceRange = referenceRange;
            deserializedFhirR4Observation.hasMember = hasMember;
            deserializedFhirR4Observation.derivedFrom = derivedFrom;
            deserializedFhirR4Observation.component = component;
            deserializedFhirR4Observation.setAdditionalProperties(additionalProperties);
            return deserializedFhirR4Observation;
        });
    }
}

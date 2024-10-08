// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.health.insights.radiologyinsights.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * visit/encounter information.
 */
@Fluent
public final class PatientEncounter implements JsonSerializable<PatientEncounter> {

    /*
     * The id of the visit.
     */
    @Generated
    private final String id;

    /*
     * Time period of the visit.
     * In case of admission, use timePeriod.start to indicate the admission time and timePeriod.end to indicate the
     * discharge time.
     */
    @Generated
    private TimePeriod period;

    /*
     * The class of the encounter.
     */
    @Generated
    private EncounterClass classProperty;

    /**
     * Creates an instance of PatientEncounter class.
     *
     * @param id the id value to set.
     */
    @Generated
    public PatientEncounter(String id) {
        this.id = id;
    }

    /**
     * Get the id property: The id of the visit.
     *
     * @return the id value.
     */
    @Generated
    public String getId() {
        return this.id;
    }

    /**
     * Get the period property: Time period of the visit.
     * In case of admission, use timePeriod.start to indicate the admission time and timePeriod.end to indicate the
     * discharge time.
     *
     * @return the period value.
     */
    @Generated
    public TimePeriod getPeriod() {
        return this.period;
    }

    /**
     * Set the period property: Time period of the visit.
     * In case of admission, use timePeriod.start to indicate the admission time and timePeriod.end to indicate the
     * discharge time.
     *
     * @param period the period value to set.
     * @return the PatientEncounter object itself.
     */
    @Generated
    public PatientEncounter setPeriod(TimePeriod period) {
        this.period = period;
        return this;
    }

    /**
     * Get the classProperty property: The class of the encounter.
     *
     * @return the classProperty value.
     */
    @Generated
    public EncounterClass getClassProperty() {
        return this.classProperty;
    }

    /**
     * Set the classProperty property: The class of the encounter.
     *
     * @param classProperty the classProperty value to set.
     * @return the PatientEncounter object itself.
     */
    @Generated
    public PatientEncounter setClassProperty(EncounterClass classProperty) {
        this.classProperty = classProperty;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeJsonField("period", this.period);
        jsonWriter.writeStringField("class", this.classProperty == null ? null : this.classProperty.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PatientEncounter from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of PatientEncounter if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the PatientEncounter.
     */
    @Generated
    public static PatientEncounter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            TimePeriod period = null;
            EncounterClass classProperty = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("period".equals(fieldName)) {
                    period = TimePeriod.fromJson(reader);
                } else if ("class".equals(fieldName)) {
                    classProperty = EncounterClass.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            PatientEncounter deserializedPatientEncounter = new PatientEncounter(id);
            deserializedPatientEncounter.period = period;
            deserializedPatientEncounter.classProperty = classProperty;
            return deserializedPatientEncounter;
        });
    }
}

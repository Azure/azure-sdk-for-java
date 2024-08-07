// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * presence.
 */
@Fluent
public final class MicrosoftGraphPresence extends MicrosoftGraphEntity {
    /*
     * The supplemental information to a user's availability. Possible values are Available, Away, BeRightBack,Busy,
     * DoNotDisturb, InACall, InAConferenceCall, Inactive,InAMeeting, Offline, OffWork,OutOfOffice,
     * PresenceUnknown,Presenting, UrgentInterruptionsOnly.
     */
    private String activity;

    /*
     * The base presence information for a user. Possible values are Available, AvailableIdle, Away, BeRightBack, Busy,
     * BusyIdle, DoNotDisturb, Offline, PresenceUnknown
     */
    private String availability;

    /*
     * presence
     */
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance of MicrosoftGraphPresence class.
     */
    public MicrosoftGraphPresence() {
    }

    /**
     * Get the activity property: The supplemental information to a user's availability. Possible values are Available,
     * Away, BeRightBack,Busy, DoNotDisturb, InACall, InAConferenceCall, Inactive,InAMeeting, Offline,
     * OffWork,OutOfOffice, PresenceUnknown,Presenting, UrgentInterruptionsOnly.
     * 
     * @return the activity value.
     */
    public String activity() {
        return this.activity;
    }

    /**
     * Set the activity property: The supplemental information to a user's availability. Possible values are Available,
     * Away, BeRightBack,Busy, DoNotDisturb, InACall, InAConferenceCall, Inactive,InAMeeting, Offline,
     * OffWork,OutOfOffice, PresenceUnknown,Presenting, UrgentInterruptionsOnly.
     * 
     * @param activity the activity value to set.
     * @return the MicrosoftGraphPresence object itself.
     */
    public MicrosoftGraphPresence withActivity(String activity) {
        this.activity = activity;
        return this;
    }

    /**
     * Get the availability property: The base presence information for a user. Possible values are Available,
     * AvailableIdle, Away, BeRightBack, Busy, BusyIdle, DoNotDisturb, Offline, PresenceUnknown.
     * 
     * @return the availability value.
     */
    public String availability() {
        return this.availability;
    }

    /**
     * Set the availability property: The base presence information for a user. Possible values are Available,
     * AvailableIdle, Away, BeRightBack, Busy, BusyIdle, DoNotDisturb, Offline, PresenceUnknown.
     * 
     * @param availability the availability value to set.
     * @return the MicrosoftGraphPresence object itself.
     */
    public MicrosoftGraphPresence withAvailability(String availability) {
        this.availability = availability;
        return this;
    }

    /**
     * Get the additionalProperties property: presence.
     * 
     * @return the additionalProperties value.
     */
    public Map<String, Object> additionalProperties() {
        return this.additionalProperties;
    }

    /**
     * Set the additionalProperties property: presence.
     * 
     * @param additionalProperties the additionalProperties value to set.
     * @return the MicrosoftGraphPresence object itself.
     */
    public MicrosoftGraphPresence withAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MicrosoftGraphPresence withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id());
        jsonWriter.writeStringField("activity", this.activity);
        jsonWriter.writeStringField("availability", this.availability);
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MicrosoftGraphPresence from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MicrosoftGraphPresence if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MicrosoftGraphPresence.
     */
    public static MicrosoftGraphPresence fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MicrosoftGraphPresence deserializedMicrosoftGraphPresence = new MicrosoftGraphPresence();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedMicrosoftGraphPresence.withId(reader.getString());
                } else if ("activity".equals(fieldName)) {
                    deserializedMicrosoftGraphPresence.activity = reader.getString();
                } else if ("availability".equals(fieldName)) {
                    deserializedMicrosoftGraphPresence.availability = reader.getString();
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedMicrosoftGraphPresence.additionalProperties = additionalProperties;

            return deserializedMicrosoftGraphPresence;
        });
    }
}

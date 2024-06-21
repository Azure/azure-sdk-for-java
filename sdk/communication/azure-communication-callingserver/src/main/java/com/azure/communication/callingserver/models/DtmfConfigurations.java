// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/** Options for DTMF recognition. */
@Fluent
public final class DtmfConfigurations implements JsonSerializable<DtmfConfigurations> {
    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    @JsonProperty(value = "interToneTimeoutInSeconds")
    private Duration interToneTimeoutInSeconds;

    /*
     * Maximum number of DTMFs to be collected.
     */
    @JsonProperty(value = "maxTonesToCollect")
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    @JsonProperty(value = "stopTones")
    private List<StopTones> stopTones;

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("interToneTimeoutInSeconds", CoreUtils.durationToStringWithDays(this.interToneTimeoutInSeconds));
        jsonWriter.writeNumberField("maxTonesToCollect", this.maxTonesToCollect);
        jsonWriter.writeArrayField("stopTones", this.stopTones,
            (writer, element) -> writer.writeString(element == null ? null : element.toString()));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DtmfOptionsInternal from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DtmfOptionsInternal if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the DtmfOptionsInternal.
     */
    public static DtmfConfigurations fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DtmfConfigurations configurations = new DtmfConfigurations();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("interToneTimeoutInSeconds".equals(fieldName)) {
                    final String value = reader.getString();
                    configurations.interToneTimeoutInSeconds = Duration.parse(value);
                } else if ("maxTonesToCollect".equals(fieldName)) {
                    configurations.maxTonesToCollect = reader.getNullable(JsonReader::getInt);
                } else if ("stopTones".equals(fieldName)) {
                    configurations.stopTones = reader.readArray(r -> StopTones.fromString(r.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return configurations;
        });
    }

    /**
     * Get the interToneTimeoutInSeconds property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @return the interToneTimeoutInSeconds value.
     */
    public Duration getInterToneTimeoutInSeconds() {
        return this.interToneTimeoutInSeconds;
    }

    /**
     * Set the interToneTimeoutInSeconds property: Time to wait between DTMF inputs to stop recognizing.
     *
     * @param interToneTimeoutInSeconds the interToneTimeoutInSeconds value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setInterToneTimeoutInSeconds(Duration interToneTimeoutInSeconds) {
        this.interToneTimeoutInSeconds = interToneTimeoutInSeconds;
        return this;
    }

    /**
     * Get the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @return the maxTonesToCollect value.
     */
    public Integer getMaxTonesToCollect() {
        return this.maxTonesToCollect;
    }

    /**
     * Set the maxTonesToCollect property: Maximum number of DTMFs to be collected.
     *
     * @param maxTonesToCollect the maxTonesToCollect value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setMaxTonesToCollect(Integer maxTonesToCollect) {
        this.maxTonesToCollect = maxTonesToCollect;
        return this;
    }

    /**
     * Get the stopTones property: List of tones that will stop recognizing.
     *
     * @return the stopTones value.
     */
    public List<StopTones> getStopTones() {
        return this.stopTones;
    }

    /**
     * Set the stopTones property: List of tones that will stop recognizing.
     *
     * @param stopTones the stopTones value to set.
     * @return the DtmfConfigurationsInternal object itself.
     */
    public DtmfConfigurations setStopTones(List<StopTones> stopTones) {
        this.stopTones = stopTones;
        return this;
    }
}

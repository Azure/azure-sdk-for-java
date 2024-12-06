// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Options for DTMF recognition. */
@Fluent
public final class DtmfConfigurations implements JsonSerializable<DtmfConfigurations> {
    /*
     * Time to wait between DTMF inputs to stop recognizing.
     */
    private Duration interToneTimeoutInSeconds;

    /*
     * Maximum number of DTMFs to be collected.
     */
    private Integer maxTonesToCollect;

    /*
     * List of tones that will stop recognizing.
     */
    private List<StopTones> stopTones;

    /**
     * Creates a new instance of {@link DtmfConfigurations}.
     */
    public DtmfConfigurations() {
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("interToneTimeoutInSeconds",
                CoreUtils.durationToStringWithDays(interToneTimeoutInSeconds))
            .writeNumberField("maxTonesToCollect", maxTonesToCollect)
            .writeArrayField("stopTones", stopTones, (writer, tone) -> writer.writeString(Objects.toString(tone, null)))
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link DtmfConfigurations} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link DtmfConfigurations}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static DtmfConfigurations fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DtmfConfigurations configurations = new DtmfConfigurations();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("interToneTimeoutInSeconds".equals(fieldName)) {
                    configurations.interToneTimeoutInSeconds
                        = reader.getNullable(nonNull -> Duration.parse(nonNull.getString()));
                } else if ("maxTonesToCollect".equals(fieldName)) {
                    configurations.maxTonesToCollect = reader.getNullable(JsonReader::getInt);
                } else if ("stopTones".equals(fieldName)) {
                    configurations.stopTones = reader.readArray(elem -> StopTones.fromString(elem.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return configurations;
        });
    }
}

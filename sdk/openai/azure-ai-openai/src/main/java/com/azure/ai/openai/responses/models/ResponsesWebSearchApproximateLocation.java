// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesWebSearchApproximateLocation model.
 */
@Fluent
public final class ResponsesWebSearchApproximateLocation extends ResponsesWebSearchLocation {

    /*
     * The type property.
     */
    @Generated
    private String type = "approximate";

    /*
     * The two-letter [ISO country code](https://en.wikipedia.org/wiki/ISO_3166-1) of the user, e.g. `US`.
     */
    @Generated
    private String country;

    /*
     * Free text input for the region of the user, e.g. `California`.
     */
    @Generated
    private String region;

    /*
     * Free text input for the city of the user, e.g. `San Francisco`.
     */
    @Generated
    private String city;

    /*
     * The [IANA timezone](https://timeapi.io/documentation/iana-timezones) of the user, e.g. `America/Los_Angeles`.
     */
    @Generated
    private String timezone;

    /**
     * Creates an instance of ResponsesWebSearchApproximateLocation class.
     */
    @Generated
    public ResponsesWebSearchApproximateLocation() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Get the country property: The two-letter [ISO country code](https://en.wikipedia.org/wiki/ISO_3166-1) of the
     * user, e.g. `US`.
     *
     * @return the country value.
     */
    @Generated
    public String getCountry() {
        return this.country;
    }

    /**
     * Set the country property: The two-letter [ISO country code](https://en.wikipedia.org/wiki/ISO_3166-1) of the
     * user, e.g. `US`.
     *
     * @param country the country value to set.
     * @return the ResponsesWebSearchApproximateLocation object itself.
     */
    @Generated
    public ResponsesWebSearchApproximateLocation setCountry(String country) {
        this.country = country;
        return this;
    }

    /**
     * Get the region property: Free text input for the region of the user, e.g. `California`.
     *
     * @return the region value.
     */
    @Generated
    public String getRegion() {
        return this.region;
    }

    /**
     * Set the region property: Free text input for the region of the user, e.g. `California`.
     *
     * @param region the region value to set.
     * @return the ResponsesWebSearchApproximateLocation object itself.
     */
    @Generated
    public ResponsesWebSearchApproximateLocation setRegion(String region) {
        this.region = region;
        return this;
    }

    /**
     * Get the city property: Free text input for the city of the user, e.g. `San Francisco`.
     *
     * @return the city value.
     */
    @Generated
    public String getCity() {
        return this.city;
    }

    /**
     * Set the city property: Free text input for the city of the user, e.g. `San Francisco`.
     *
     * @param city the city value to set.
     * @return the ResponsesWebSearchApproximateLocation object itself.
     */
    @Generated
    public ResponsesWebSearchApproximateLocation setCity(String city) {
        this.city = city;
        return this;
    }

    /**
     * Get the timezone property: The [IANA timezone](https://timeapi.io/documentation/iana-timezones) of the user, e.g.
     * `America/Los_Angeles`.
     *
     * @return the timezone value.
     */
    @Generated
    public String getTimezone() {
        return this.timezone;
    }

    /**
     * Set the timezone property: The [IANA timezone](https://timeapi.io/documentation/iana-timezones) of the user, e.g.
     * `America/Los_Angeles`.
     *
     * @param timezone the timezone value to set.
     * @return the ResponsesWebSearchApproximateLocation object itself.
     */
    @Generated
    public ResponsesWebSearchApproximateLocation setTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type);
        jsonWriter.writeStringField("country", this.country);
        jsonWriter.writeStringField("region", this.region);
        jsonWriter.writeStringField("city", this.city);
        jsonWriter.writeStringField("timezone", this.timezone);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesWebSearchApproximateLocation from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesWebSearchApproximateLocation if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesWebSearchApproximateLocation.
     */
    @Generated
    public static ResponsesWebSearchApproximateLocation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesWebSearchApproximateLocation deserializedResponsesWebSearchApproximateLocation
                = new ResponsesWebSearchApproximateLocation();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesWebSearchApproximateLocation.type = reader.getString();
                } else if ("country".equals(fieldName)) {
                    deserializedResponsesWebSearchApproximateLocation.country = reader.getString();
                } else if ("region".equals(fieldName)) {
                    deserializedResponsesWebSearchApproximateLocation.region = reader.getString();
                } else if ("city".equals(fieldName)) {
                    deserializedResponsesWebSearchApproximateLocation.city = reader.getString();
                } else if ("timezone".equals(fieldName)) {
                    deserializedResponsesWebSearchApproximateLocation.timezone = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesWebSearchApproximateLocation;
        });
    }
}

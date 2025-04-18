// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.
package com.azure.maps.weather.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.maps.weather.implementation.helpers.Utility;
import com.azure.maps.weather.implementation.models.GeoJsonGeometry;
import com.azure.maps.weather.implementation.models.LatLongPair;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Forecast window for the storm.
 */
@Fluent
public final class WeatherWindow implements JsonSerializable<WeatherWindow> {

    /*
     * Location of the point on the left side of the window at the time of the timeframe.
     */
    private LatLongPair topLeft;

    /*
     * Location of the point on the right side of the window at the end of the timeframe.
     */
    private LatLongPair bottomRight;

    /*
     * DateTime of the beginning of the window of movement, displayed in ISO8601 format.
     */
    private OffsetDateTime beginTimestamp;

    /*
     * DateTime of the end of the window of movement, displayed in ISO8601 format.
     */
    private OffsetDateTime endTimestamp;

    /*
     * Storm status at the beginning of the window.
     */
    private String beginStatus;

    /*
     * Storm status at the end of the window.
     */
    private String endStatus;

    /*
     * Displayed when windowGeometry=true in request. GeoJSON object containing coordinates describing the window of
     * movement during the specified timeframe.
     */
    private GeoJsonGeometry geometry;

    /**
     * Set default WeatherWindow constructor to private
     */
    private WeatherWindow() {
    }

    /**
     * Get the topLeft property: Location of the point on the left side of the window at the time of the timeframe.
     *
     * @return the topLeft value.
     */
    public GeoPosition getTopLeft() {
        return new GeoPosition(this.topLeft.getLongitude(), this.topLeft.getLatitude());
    }

    /**
     * Get the bottomRight property: Location of the point on the right side of the window at the end of the timeframe.
     *
     * @return the bottomRight value.
     */
    public GeoPosition getBottomRight() {
        return new GeoPosition(this.bottomRight.getLongitude(), this.bottomRight.getLatitude());
    }

    /**
     * Get the beginTimestamp property: DateTime of the beginning of the window of movement, displayed in ISO8601
     * format.
     *
     * @return the beginTimestamp value.
     */
    public OffsetDateTime getBeginTimestamp() {
        return this.beginTimestamp;
    }

    /**
     * Get the endTimestamp property: DateTime of the end of the window of movement, displayed in ISO8601 format.
     *
     * @return the endTimestamp value.
     */
    public OffsetDateTime getEndTimestamp() {
        return this.endTimestamp;
    }

    /**
     * Get the beginStatus property: Storm status at the beginning of the window.
     *
     * @return the beginStatus value.
     */
    public String getBeginStatus() {
        return this.beginStatus;
    }

    /**
     * Get the endStatus property: Storm status at the end of the window.
     *
     * @return the endStatus value.
     */
    public String getEndStatus() {
        return this.endStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("left", this.topLeft);
        jsonWriter.writeJsonField("right", this.bottomRight);
        jsonWriter.writeStringField("beginDateTime",
            this.beginTimestamp == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.beginTimestamp));
        jsonWriter.writeStringField("endDateTime",
            this.endTimestamp == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.endTimestamp));
        jsonWriter.writeStringField("beginStatus", this.beginStatus);
        jsonWriter.writeStringField("endStatus", this.endStatus);
        jsonWriter.writeJsonField("geometry", this.geometry);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of WeatherWindow from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of WeatherWindow if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the WeatherWindow.
     */
    public static WeatherWindow fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            WeatherWindow deserializedWeatherWindow = new WeatherWindow();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("left".equals(fieldName)) {
                    deserializedWeatherWindow.topLeft = LatLongPair.fromJson(reader);
                } else if ("right".equals(fieldName)) {
                    deserializedWeatherWindow.bottomRight = LatLongPair.fromJson(reader);
                } else if ("beginDateTime".equals(fieldName)) {
                    deserializedWeatherWindow.beginTimestamp = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("endDateTime".equals(fieldName)) {
                    deserializedWeatherWindow.endTimestamp = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("beginStatus".equals(fieldName)) {
                    deserializedWeatherWindow.beginStatus = reader.getString();
                } else if ("endStatus".equals(fieldName)) {
                    deserializedWeatherWindow.endStatus = reader.getString();
                } else if ("geometry".equals(fieldName)) {
                    deserializedWeatherWindow.geometry = GeoJsonGeometry.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedWeatherWindow;
        });
    }

    /**
     * Return GeoPolygon
     *
     * @return Returns a {@link GeoPolygon} for this weather window
     */
    public GeoPolygon getPolygon() {
        return Utility.toGeoPolygon(this.geometry);
    }
}

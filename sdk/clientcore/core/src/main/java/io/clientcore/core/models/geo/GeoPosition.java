// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * <p>Represents a geographic position in GeoJSON format.</p>
 *
 * <p>This class encapsulates a geographic position defined by longitude, latitude, and optionally altitude. It
 * provides methods to access these properties.</p>
 *
 * <p>This class is useful when you want to work with a geographic position in a geographic context. For example,
 * you can use it to represent a location on a map or a point in a geographic dataset.</p>
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class GeoPosition implements JsonSerializable<GeoPosition> {
    // GeoPosition is a commonly used model, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(GeoPosition.class);

    private final double longitude;
    private final double latitude;

    private final Double altitude;

    /**
     * Constructs a geo position.
     *
     * @param longitude Longitudinal position.
     * @param latitude Latitudinal position.
     */
    public GeoPosition(double longitude, double latitude) {
        this(longitude, latitude, null);
    }

    /**
     * Constructs a geo position.
     *
     * @param longitude Longitudinal position.
     * @param latitude Latitudinal position.
     * @param altitude Altitude position.
     */
    public GeoPosition(double longitude, double latitude, Double altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    /**
     * The longitudinal position of the geometric position.
     *
     * @return The longitudinal position.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * The latitudinal position of the geometric position.
     *
     * @return The latitudinal position.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * The altitude of the geometric position.
     *
     * @return The altitude.
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * Gets the number of coordinates used to compose the position.
     * <p>
     * This will return either 2 or 3 depending on whether {@link #getAltitude() altitude is set}.
     *
     * @return The number of coordinates used to compose the position.
     */
    public int count() {
        return (altitude == null) ? 2 : 3;
    }

    /**
     * Array accessor for the coordinates of this position.
     * <table>
     * <caption>Operation result mapping</caption>
     * <tr>
     * <th>Index</th>
     * <th>Result</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>{@link #getLongitude() Longitude}</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>{@link #getLatitude() Latitude}</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>{@link #getAltitude() Altitude} if non-null, otherwise {@link IndexOutOfBoundsException}</td>
     * </tr>
     * <tr>
     * <td>3+</td>
     * <td>{@link IndexOutOfBoundsException}</td>
     * </tr>
     * </table>
     *
     * @param index Coordinate index to access.
     * @return The coordinate value for the index.
     * @throws IndexOutOfBoundsException If {@code index} is equal to or greater than {@link #count()}.
     */
    double get(int index) {
        switch (index) {
            case 0:
                return longitude;

            case 1:
                return latitude;

            case 2:
                if (altitude == null) {
                    throw LOGGER.throwableAtError().addKeyValue("index", index).log(IndexOutOfBoundsException::new);
                }

                return altitude;

            default:
                throw LOGGER.throwableAtError().addKeyValue("index", index).log(IndexOutOfBoundsException::new);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude, altitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoPosition)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        GeoPosition other = (GeoPosition) obj;
        return Double.compare(longitude, other.longitude) == 0
            && Double.compare(latitude, other.latitude) == 0
            && Objects.equals(altitude, other.altitude);
    }

    @Override
    public String toString() {
        return (altitude != null)
            ? String.format("[%s, %s, %s]", longitude, latitude, altitude)
            : String.format("[%s, %s]", longitude, latitude);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartArray().writeDouble(longitude).writeDouble(latitude);

        if (altitude != null) {
            jsonWriter.writeDouble(altitude);
        }

        return jsonWriter.writeEndArray();
    }

    /**
     * Reads a JSON stream into a {@link GeoPosition}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoPosition} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IllegalStateException If the {@link GeoPosition} has less than two or more than three positions in the
     * array.
     * @throws IOException If a {@link GeoPosition} fails to be read from the {@code jsonReader}.
     */
    public static GeoPosition fromJson(JsonReader jsonReader) throws IOException {
        List<Number> coordinates = jsonReader.readArray(reader -> {
            if (reader.currentToken() == JsonToken.NUMBER) {
                return reader.getDouble();
            } else {
                return null;
            }
        });

        if (coordinates == null) {
            return null;
        }

        int coordinateCount = coordinates.size();
        if (coordinateCount < 2 || coordinateCount > 3) {
            throw LOGGER.throwableAtError()
                .log("Only 2 or 3 element coordinates supported.", IllegalStateException::new);
        }

        double longitude = coordinates.get(0).doubleValue();
        double latitude = coordinates.get(1).doubleValue();
        Double altitude = (coordinateCount == 3) ? coordinates.get(2).doubleValue() : null;

        return new GeoPosition(longitude, latitude, altitude);
    }
}

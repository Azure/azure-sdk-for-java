// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Represents a linear ring that is part of a {@link GeoPolygon}.</p>
 *
 * <p>This class encapsulates a list of {@link GeoPosition} instances that form a closed loop, which is a component
 * of a {@link GeoPolygon}. The first and last positions of the loop are the same, forming a closed ring.</p>
 *
 * <p>This class is useful when you want to work with a linear ring in a geographic context. For example, you can
 * use it to define the boundary of a geographic area in a {@link GeoPolygon}.</p>
 *
 * <p>Note: A linear ring requires at least 4 coordinates, and the first and last coordinates must be the same.</p>
 *
 * @see GeoPosition
 * @see GeoPolygon
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
public final class GeoLinearRing implements JsonSerializable<GeoLinearRing> {
    // GeoLinearRing is a commonly used model class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(GeoLinearRing.class);

    private final GeoArray<GeoPosition> coordinates;

    /**
     * Constructs a new linear ring with the passed coordinates.
     *
     * @param coordinates The coordinates of the linear ring.
     * @throws NullPointerException If {@code coordinates} is null.
     * @throws IllegalArgumentException If {@code coordinates} has less than 4 elements or the first and last elements
     * aren't equivalent.
     */
    public GeoLinearRing(List<GeoPosition> coordinates) {
        Objects.requireNonNull(coordinates, "'coordinates' cannot be null.");

        int size = coordinates.size();
        if (size < 4) {
            throw LOGGER.throwableAtError()
                .log("A linear ring requires at least 4 coordinates.", IllegalArgumentException::new);
        }

        if (!Objects.equals(coordinates.get(0), coordinates.get(size - 1))) {
            throw LOGGER.throwableAtError()
                .log("The first and last coordinates of a linear ring must be equivalent.",
                    IllegalArgumentException::new);
        }

        this.coordinates = new GeoArray<>(new ArrayList<>(coordinates));
    }

    /**
     * Unmodifiable representation of the {@link GeoPosition geometric positions} representing this linear ring.
     *
     * @return An unmodifiable representation of the {@link GeoPosition geometric positions} representing this linear
     * ring.
     */
    public List<GeoPosition> getCoordinates() {
        return coordinates;
    }

    @Override
    public int hashCode() {
        return coordinates.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoLinearRing)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoLinearRing other = (GeoLinearRing) obj;
        return Objects.equals(coordinates, other.coordinates);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeArray(getCoordinates(), JsonWriter::writeJson);
    }

    /**
     * Reads a JSON stream into a {@link GeoLinearRing}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link GeoLinearRing} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If a {@link GeoLinearRing} fails to be read from the {@code jsonReader}.
     */
    public static GeoLinearRing fromJson(JsonReader jsonReader) throws IOException {
        List<GeoPosition> coordinates = jsonReader.readArray(GeoPosition::fromJson);
        if (coordinates == null) {
            return null;
        }

        return new GeoLinearRing(coordinates);
    }
}

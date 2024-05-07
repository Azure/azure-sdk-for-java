// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.implementation.GeoObjectHelper;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an abstract geometric object in GeoJSON format.
 *
 * <p>This class encapsulates the common properties of a geometric object, including the bounding box and additional
 * custom properties. It provides methods to access these properties.</p>
 *
 * <p>This class also provides a {@link #toJson(JsonWriter)} method to serialize the geometric object to JSON,
 * and a {@link #fromJson(JsonReader)} method to deserialize a geometric object from JSON.</p>
 *
 * @see GeoBoundingBox
 * @see GeoPosition
 * @see GeoPoint
 * @see GeoLineString
 * @see GeoPolygon
 * @see GeoPointCollection
 * @see GeoLineStringCollection
 * @see GeoPolygonCollection
 * @see GeoCollection
 * @see JsonSerializable
 */
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Point", value = GeoPoint.class),
    @JsonSubTypes.Type(name = "LineString", value = GeoLineString.class),
    @JsonSubTypes.Type(name = "Polygon", value = GeoPolygon.class),
    @JsonSubTypes.Type(name = "MultiPoint", value = GeoPointCollection.class),
    @JsonSubTypes.Type(name = "MultiLineString", value = GeoLineStringCollection.class),
    @JsonSubTypes.Type(name = "MultiPolygon", value = GeoPolygonCollection.class),
    @JsonSubTypes.Type(name = "GeometryCollection", value = GeoCollection.class) })
@Immutable
public abstract class GeoObject implements JsonSerializable<GeoObject> {
    private final GeoBoundingBox boundingBox;
    private final Map<String, Object> customProperties;

    /**
     * Creates a {@link GeoObject} instance.
     *
     * @param boundingBox Optional bounding box of the {@link GeoObject}.
     * @param customProperties Optional additional properties to associate to the {@link GeoObject}.
     */
    protected GeoObject(GeoBoundingBox boundingBox, Map<String, Object> customProperties) {
        this.boundingBox = boundingBox;

        if (customProperties == null) {
            this.customProperties = null;
        } else {
            this.customProperties = Collections.unmodifiableMap(new HashMap<>(customProperties));
        }
    }

    static {
        GeoObjectHelper.setAccessor(GeoObject::getCustomProperties);
    }

    /**
     * Gets the GeoJSON type for this object.
     *
     * @return The GeoJSON type for this object.
     */
    @JsonProperty("type")
    public abstract GeoObjectType getType();

    /**
     * Bounding box for this {@link GeoObject}.
     *
     * @return The bounding box for this {@link GeoObject}.
     */
    public final GeoBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Additional properties about this {@link GeoObject}.
     *
     * @return An unmodifiable representation of the additional properties associated with this {@link GeoObject}.
     */
    public final Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, customProperties);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoObject)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        GeoObject other = (GeoObject) obj;

        return Objects.equals(boundingBox, other.boundingBox)
            && Objects.equals(customProperties, other.customProperties);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject().writeJsonField("bbox", getBoundingBox());

        Map<String, Object> customProperties = getCustomProperties();
        if (!CoreUtils.isNullOrEmpty(customProperties)) {
            jsonWriter.writeMap(customProperties, JsonWriter::writeUntyped);
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link GeoObject} from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of {@link GeoObject} if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties or the
     * polymorphic discriminator.
     * @throws IOException If an error occurs while reading the {@link GeoObject}.
     */
    public static GeoObject fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            JsonReader readerToUse = reader.bufferObject();

            readerToUse.nextToken(); // Prepare for reading
            while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = readerToUse.getFieldName();
                readerToUse.nextToken();
                if ("type".equals(fieldName)) {
                    discriminatorValue = readerToUse.getString();
                    break;
                } else {
                    readerToUse.skipChildren();
                }
            }

            if (discriminatorValue != null) {
                readerToUse = readerToUse.reset();
            }

            // Use the discriminator value to determine which subtype should be deserialized.
            if ("Point".equals(discriminatorValue)) {
                return GeoPoint.fromJson(readerToUse);
            } else if ("MultiPoint".equals(discriminatorValue)) {
                return GeoPointCollection.fromJson(readerToUse);
            } else if ("Polygon".equals(discriminatorValue)) {
                return GeoPolygon.fromJson(readerToUse);
            } else if ("MultiPolygon".equals(discriminatorValue)) {
                return GeoPolygonCollection.fromJson(readerToUse);
            } else if ("LineString".equals(discriminatorValue)) {
                return GeoLineString.fromJson(readerToUse);
            } else if ("MultiLineString".equals(discriminatorValue)) {
                return GeoLineStringCollection.fromJson(readerToUse);
            } else if ("GeometryCollection".equals(discriminatorValue)) {
                return GeoCollection.fromJson(readerToUse);
            } else {
                throw new IllegalStateException("Discriminator field 'type' didn't match one of the expected values "
                    + "'Point', 'MultiPoint', 'Polygon', 'MultiPolygon', 'LineString', 'MultiLineString', or "
                    + "'GeometryCollection'. It was: '" + discriminatorValue + "'.");
            }
        });
    }

    JsonWriter writeCustomProperties(JsonWriter jsonWriter) throws IOException {
        if (!CoreUtils.isNullOrEmpty(customProperties)) {
            for (Map.Entry<String, Object> entry : customProperties.entrySet()) {
                jsonWriter.writeUntypedField(String.valueOf(entry.getKey()), entry.getValue());
            }
        }

        return jsonWriter;
    }
}

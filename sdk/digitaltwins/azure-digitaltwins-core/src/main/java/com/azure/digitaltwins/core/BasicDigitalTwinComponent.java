// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.implementation.serializer.SerializationHelpers;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Properties on a component that adhere to a specific model.
 */
@Fluent
public final class BasicDigitalTwinComponent implements JsonSerializable<BasicDigitalTwinComponent> {

    /**
     * Information about the model a component conforms to. This field is present on every digital twin.
     */
    private final Map<String, DigitalTwinPropertyMetadata> metadata = new HashMap<>();

    /**
     * The time and date the component was last updated.
     */
    private OffsetDateTime lastUpdatedOn;

    /**
     * The additional contents of the model. This field will contain any contents of the digital twin that are not
     * already defined by the other strong types of this class.
     */
    private final Map<String, Object> contents = new HashMap<>();

    /**
     * Creates a new instance of {@link BasicDigitalTwinComponent}.
     */
    public BasicDigitalTwinComponent() {
    }

    /**
     * Gets the metadata about the model.
     *
     * @return The component metadata.
     */
    public Map<String, DigitalTwinPropertyMetadata> getMetadata() {
        return metadata;
    }

    /**
     * Adds property metadata.
     *
     * @param key The key that maps to the property metadata
     * @param metadata Property metadata.
     * @return The BasicDigitalTwinComponent object itself.
     */
    public BasicDigitalTwinComponent addMetadata(String key, DigitalTwinPropertyMetadata metadata) {
        this.metadata.put(key, metadata);
        return this;
    }

    /**
     * Gets the date and time when the twin was last updated.
     *
     * @return The date and time the twin was last updated.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    /**
     * Gets the custom contents
     *
     * @return The custom contents
     */
    public Map<String, Object> getContents() {
        return contents;
    }

    /**
     * Adds additional custom property to the component's contents.
     *
     * @param key The key of the additional property to be added to the component's contents.
     * @param value The value of the additional property to be added to the component's contents.
     * @return The BasicDigitalTwinComponent object itself.
     */
    public BasicDigitalTwinComponent addToContents(String key, Object value) {
        this.contents.put(key, value);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeMapField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA, metadata, JsonWriter::writeJson);

        for (Map.Entry<String, Object> entry : contents.entrySet()) {
            if (entry.getValue() instanceof String) {
                SerializationHelpers.serializeStringHelper(jsonWriter, entry.getKey(), (String) entry.getValue());
            } else {
                jsonWriter.writeUntypedField(entry.getKey(), entry.getValue());
            }
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of BasicDigitalTwinComponent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BasicDigitalTwinComponent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BasicDigitalTwinComponent.
     */
    public static BasicDigitalTwinComponent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BasicDigitalTwinComponent component = new BasicDigitalTwinComponent();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA.equals(fieldName)) {
                    if (reader.currentToken() == JsonToken.START_OBJECT) {
                        handleMetadata(reader.readMap(JsonReader::readUntyped),
                            lastUpdatedOn -> component.lastUpdatedOn = lastUpdatedOn, component::addMetadata);
                    }
                } else {
                    component.addToContents(fieldName, reader.readUntyped());
                }
            }

            return component;
        });
    }

    private static void handleMetadata(Map<String, Object> metadata, Consumer<OffsetDateTime> lastUpdatedOnConsumer,
        BiConsumer<String, DigitalTwinPropertyMetadata> metadataConsumer) throws IOException {
        String lastUpdatedOnString = (String) metadata.remove(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME);
        lastUpdatedOnConsumer.accept((lastUpdatedOnString == null) ? null : OffsetDateTime.parse(lastUpdatedOnString));

        for (Map.Entry<String, Object> metadataEntry : metadata.entrySet()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                jsonWriter.writeUntyped(metadataEntry.getValue()).flush();
            }

            try (JsonReader jsonReader = JsonProviders.createReader(outputStream.toByteArray())) {
                metadataConsumer.accept(metadataEntry.getKey(), DigitalTwinPropertyMetadata.fromJson(jsonReader));
            }
        }
    }
}

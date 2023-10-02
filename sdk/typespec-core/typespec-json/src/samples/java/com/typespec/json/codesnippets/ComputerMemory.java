// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.codesnippets;

import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.typespec.json.JsonToken;
import com.typespec.json.JsonWriter;

import java.io.IOException;

// BEGIN: com.azure.json.JsonSerializable.ComputerMemory

/**
 * Implementation of JsonSerializable where all properties are fluently set.
 */
public class ComputerMemory implements JsonSerializable<ComputerMemory> {
    private long memoryInBytes;
    private double clockSpeedInHertz;
    private String manufacturer;
    private boolean errorCorrecting;

    /**
     * Sets the memory capacity, in bytes, of the computer memory.
     *
     * @param memoryInBytes The memory capacity in bytes.
     * @return The update ComputerMemory
     */
    public ComputerMemory setMemoryInBytes(long memoryInBytes) {
        this.memoryInBytes = memoryInBytes;
        return this;
    }

    /**
     * Sets the clock speed, in hertz, of the computer memory.
     *
     * @param clockSpeedInHertz The clock speed in hertz.
     * @return The update ComputerMemory
     */
    public ComputerMemory setClockSpeedInHertz(double clockSpeedInHertz) {
        this.clockSpeedInHertz = clockSpeedInHertz;
        return this;
    }

    /**
     * Sets the manufacturer of the computer memory.
     *
     * @param manufacturer The manufacturer.
     * @return The update ComputerMemory
     */
    public ComputerMemory setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    /**
     * Sets whether the computer memory is error correcting.
     *
     * @param errorCorrecting Whether the computer memory is error correcting.
     * @return The update ComputerMemory
     */
    public ComputerMemory setErrorCorrecting(boolean errorCorrecting) {
        this.errorCorrecting = errorCorrecting;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeLongField("memoryInBytes", memoryInBytes)
            .writeDoubleField("clockSpeedInHertz", clockSpeedInHertz)
            // Writing fields with nullable types won't write the field if the value is null. If a nullable field needs
            // to always be written use 'writeNullableField(String, Object, WriteValueCallback<JsonWriter, Object>)'.
            // This will write 'fieldName: null' if the value is null.
            .writeStringField("manufacturer", manufacturer)
            .writeBooleanField("errorCorrecting", errorCorrecting)
            .writeEndObject();
    }

    /**
     * Reads an instance of ComputerMemory from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ComputerMemory if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ComputerMemory.
     */
    public static ComputerMemory fromJson(JsonReader jsonReader) throws IOException {
        // 'readObject' will initialize reading if the JsonReader hasn't begun JSON reading and validate that the
        // current state of reading is a JSON start object. If the state isn't JSON start object an exception will be
        // thrown.
        return jsonReader.readObject(reader -> {
            ComputerMemory deserializedValue = new ComputerMemory();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // In this case field names are case-sensitive but this could be replaced with 'equalsIgnoreCase' to
                // make them case-insensitive.
                if ("memoryInBytes".equals(fieldName)) {
                    deserializedValue.setMemoryInBytes(reader.getLong());
                } else if ("clockSpeedInHertz".equals(fieldName)) {
                    deserializedValue.setClockSpeedInHertz(reader.getDouble());
                } else if ("manufacturer".equals(fieldName)) {
                    deserializedValue.setManufacturer(reader.getString());
                } else if ("errorCorrecting".equals(fieldName)) {
                    deserializedValue.setErrorCorrecting(reader.getBoolean());
                } else {
                    // Fallthrough case of an unknown property. In this instance the value is skipped, if it's a JSON
                    // array or object the reader will progress until it terminated. This could also throw an exception
                    // if unknown properties should cause that or be read into an additional properties Map for further
                    // usage.
                    reader.skipChildren();
                }
            }

            return deserializedValue;
        });
    }
}
// END: com.azure.json.JsonSerializable.ComputerMemory

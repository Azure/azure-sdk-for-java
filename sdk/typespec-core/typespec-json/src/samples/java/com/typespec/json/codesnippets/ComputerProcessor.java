// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.codesnippets;

import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.typespec.json.JsonToken;
import com.typespec.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;

// BEGIN: com.typespec.json.JsonSerializable.ComputerProcessor

/**
 * Implementation of JsonSerializable where all properties are set in the constructor.
 */
public class ComputerProcessor implements JsonSerializable<ComputerProcessor> {
    private final int cores;
    private final int threads;
    private final String manufacturer;
    private final double clockSpeedInHertz;
    private final OffsetDateTime releaseDate;

    /**
     * Creates an instance of ComputerProcessor.
     *
     * @param cores The number of physical cores.
     * @param threads The number of virtual threads.
     * @param manufacturer The manufacturer of the processor.
     * @param clockSpeedInHertz The clock speed, in hertz, of the processor.
     * @param releaseDate The release date of the processor, if unreleased this is null.
     */
    public ComputerProcessor(int cores, int threads, String manufacturer, double clockSpeedInHertz,
        OffsetDateTime releaseDate) {
        // This constructor could be made package-private or private as 'fromJson' has access to internal APIs.
        this.cores = cores;
        this.threads = threads;
        this.manufacturer = manufacturer;
        this.clockSpeedInHertz = clockSpeedInHertz;
        this.releaseDate = releaseDate;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeIntField("cores", cores)
            .writeIntField("threads", threads)
            .writeStringField("manufacturer", manufacturer)
            .writeDoubleField("clockSpeedInHertz", clockSpeedInHertz)
            // 'writeNullableField' will always write a field, even if the value is null.
            .writeNullableField("releaseDate", releaseDate, (writer, value) -> writer.writeString(value.toString()))
            .writeEndObject()
            // In this case 'toJson' eagerly flushes the JsonWriter.
            // Flushing too often may result in performance penalties.
            .flush();
    }

    /**
     * Reads an instance of ComputerProcessor from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ComputerProcessor if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ComputerProcessor.
     * @throws IllegalStateException If any of the required properties to create ComputerProcessor aren't found.
     */
    public static ComputerProcessor fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // Local variables to keep track of what values have been found.
            // Some properties have a corresponding 'boolean found<Name>' to track if a JSON property with that name
            // was found. If the value wasn't found an exception will be thrown at the end of reading the object.
            int cores = 0;
            boolean foundCores = false;
            int threads = 0;
            boolean foundThreads = false;
            String manufacturer = null;
            boolean foundManufacturer = false;
            double clockSpeedInHertz = 0.0D;
            boolean foundClockSpeedInHertz = false;
            OffsetDateTime releaseDate = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // Example of case-insensitive names.
                if ("cores".equalsIgnoreCase(fieldName)) {
                    cores = reader.getInt();
                    foundCores = true;
                } else if ("threads".equalsIgnoreCase(fieldName)) {
                    threads = reader.getInt();
                    foundThreads = true;
                } else if ("manufacturer".equalsIgnoreCase(fieldName)) {
                    manufacturer = reader.getString();
                    foundManufacturer = true;
                } else if ("clockSpeedInHertz".equalsIgnoreCase(fieldName)) {
                    clockSpeedInHertz = reader.getDouble();
                    foundClockSpeedInHertz = true;
                } else if ("releaseDate".equalsIgnoreCase(fieldName)) {
                    // For nullable primitives 'getNullable' must be used as it will return null if the current token
                    // is JSON null or pass the reader to the non-null callback method for reading, in this case for
                    // OffsetDateTime it uses 'getString' to call 'OffsetDateTime.parse'.
                    releaseDate = reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            // Check that all required fields were found.
            if (foundCores && foundThreads && foundManufacturer && foundClockSpeedInHertz) {
                return new ComputerProcessor(cores, threads, manufacturer, clockSpeedInHertz, releaseDate);
            }

            // If required fields were missing throw an exception.
            throw new IOException("Missing one, or more, required fields. Required fields are 'cores', 'threads', "
                + "'manufacturer', and 'clockSpeedInHertz'.");
        });
    }
}
// END: com.typespec.json.JsonSerializable.ComputerProcessor

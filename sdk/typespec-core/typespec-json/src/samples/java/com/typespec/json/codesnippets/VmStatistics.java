// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json.codesnippets;

import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.typespec.json.JsonToken;
import com.typespec.json.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// BEGIN: com.azure.json.JsonSerializable.VmStatistics

/**
 * Implementation of JsonSerializable where some properties are set in the constructor and some properties are set using
 * fluent methods.
 */
public class VmStatistics implements JsonSerializable<VmStatistics> {
    private final String vmSize;
    private final ComputerProcessor processor;
    private final ComputerMemory memory;
    private final boolean acceleratedNetwork;
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance VmStatistics.
     *
     * @param vmSize The size, or name, of the VM type.
     * @param processor The processor of the VM.
     * @param memory The memory of the VM.
     * @param acceleratedNetwork Whether the VM has accelerated networking.
     */
    public VmStatistics(String vmSize, ComputerProcessor processor, ComputerMemory memory, boolean acceleratedNetwork) {
        this.vmSize = vmSize;
        this.processor = processor;
        this.memory = memory;
        this.acceleratedNetwork = acceleratedNetwork;
    }

    /**
     * Sets additional properties about the VM.
     *
     * @param additionalProperties Additional properties of the VM.
     * @return The update VmStatistics
     */
    public VmStatistics setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("VMSize", vmSize)
            .writeJsonField("Processor", processor)
            .writeJsonField("Memory", memory)
            .writeBooleanField("AcceleratedNetwork", acceleratedNetwork);

        // Include additional properties in JSON serialization.
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of VmStatistics from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of VmStatistics if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the VmStatistics.
     * @throws IllegalStateException If any of the required properties to create VmStatistics aren't found.
     */
    public static VmStatistics fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String vmSize = null;
            boolean foundVmSize = false;
            ComputerProcessor processor = null;
            boolean foundProcessor = false;
            ComputerMemory memory = null;
            boolean foundMemory = false;
            boolean acceleratedNetwork = false;
            boolean foundAcceleratedNetwork = false;
            Map<String, Object> additionalProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // Example of case-insensitive names and where serialization named don't match field names.
                if ("VMSize".equalsIgnoreCase(fieldName)) {
                    vmSize = reader.getString();
                    foundVmSize = true;
                } else if ("Processor".equalsIgnoreCase(fieldName)) {
                    // Pass the JsonReader to another JsonSerializable to read the inner object.
                    processor = ComputerProcessor.fromJson(reader);
                    foundProcessor = true;
                } else if ("Memory".equalsIgnoreCase(fieldName)) {
                    memory = ComputerMemory.fromJson(reader);
                    foundMemory = true;
                } else if ("AcceleratedNetwork".equalsIgnoreCase(fieldName)) {
                    acceleratedNetwork = reader.getBoolean();
                    foundAcceleratedNetwork = true;
                } else {
                    // Fallthrough case but the JSON property is maintained.
                    if (additionalProperties == null) {
                        // Maintain ordering of additional properties using a LinkedHashMap.
                        additionalProperties = new LinkedHashMap<>();
                    }

                    // Additional properties are unknown types, use 'readUntyped'.
                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }

            // Check that all required fields were found.
            if (foundVmSize && foundProcessor && foundMemory && foundAcceleratedNetwork) {
                return new VmStatistics(vmSize, processor, memory, acceleratedNetwork)
                    .setAdditionalProperties(additionalProperties);
            }

            // If required fields were missing throw an exception.
            throw new IOException("Missing one, or more, required fields. Required fields are 'VMSize', 'Processor',"
                + "'Memory', and 'AcceleratedNetwork'.");
        });
    }
}
// END: com.azure.json.JsonSerializable.VmStatistics

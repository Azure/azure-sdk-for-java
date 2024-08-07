// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The ContainerInfo model.
 */
@Fluent
public final class ContainerInfo implements JsonSerializable<ContainerInfo> {
    /*
     * The currentTimeStamp property.
     */
    private OffsetDateTime currentTimestamp;

    /*
     * The previousTimeStamp property.
     */
    private OffsetDateTime previousTimestamp;

    /*
     * The currentCpuStats property.
     */
    private ContainerCpuStatistics currentCpuStats;

    /*
     * The previousCpuStats property.
     */
    private ContainerCpuStatistics previousCpuStats;

    /*
     * The memoryStats property.
     */
    private ContainerMemoryStatistics memoryStats;

    /*
     * The name property.
     */
    private String name;

    /*
     * The id property.
     */
    private String id;

    /*
     * The eth0 property.
     */
    private ContainerNetworkInterfaceStatistics eth0;

    /**
     * Creates an instance of ContainerInfo class.
     */
    public ContainerInfo() {
    }

    /**
     * Get the currentTimestamp property: The currentTimeStamp property.
     * 
     * @return the currentTimestamp value.
     */
    public OffsetDateTime currentTimestamp() {
        return this.currentTimestamp;
    }

    /**
     * Set the currentTimestamp property: The currentTimeStamp property.
     * 
     * @param currentTimestamp the currentTimestamp value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withCurrentTimestamp(OffsetDateTime currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
        return this;
    }

    /**
     * Get the previousTimestamp property: The previousTimeStamp property.
     * 
     * @return the previousTimestamp value.
     */
    public OffsetDateTime previousTimestamp() {
        return this.previousTimestamp;
    }

    /**
     * Set the previousTimestamp property: The previousTimeStamp property.
     * 
     * @param previousTimestamp the previousTimestamp value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withPreviousTimestamp(OffsetDateTime previousTimestamp) {
        this.previousTimestamp = previousTimestamp;
        return this;
    }

    /**
     * Get the currentCpuStats property: The currentCpuStats property.
     * 
     * @return the currentCpuStats value.
     */
    public ContainerCpuStatistics currentCpuStats() {
        return this.currentCpuStats;
    }

    /**
     * Set the currentCpuStats property: The currentCpuStats property.
     * 
     * @param currentCpuStats the currentCpuStats value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withCurrentCpuStats(ContainerCpuStatistics currentCpuStats) {
        this.currentCpuStats = currentCpuStats;
        return this;
    }

    /**
     * Get the previousCpuStats property: The previousCpuStats property.
     * 
     * @return the previousCpuStats value.
     */
    public ContainerCpuStatistics previousCpuStats() {
        return this.previousCpuStats;
    }

    /**
     * Set the previousCpuStats property: The previousCpuStats property.
     * 
     * @param previousCpuStats the previousCpuStats value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withPreviousCpuStats(ContainerCpuStatistics previousCpuStats) {
        this.previousCpuStats = previousCpuStats;
        return this;
    }

    /**
     * Get the memoryStats property: The memoryStats property.
     * 
     * @return the memoryStats value.
     */
    public ContainerMemoryStatistics memoryStats() {
        return this.memoryStats;
    }

    /**
     * Set the memoryStats property: The memoryStats property.
     * 
     * @param memoryStats the memoryStats value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withMemoryStats(ContainerMemoryStatistics memoryStats) {
        this.memoryStats = memoryStats;
        return this;
    }

    /**
     * Get the name property: The name property.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name property: The name property.
     * 
     * @param name the name value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the id property: The id property.
     * 
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id property: The id property.
     * 
     * @param id the id value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the eth0 property: The eth0 property.
     * 
     * @return the eth0 value.
     */
    public ContainerNetworkInterfaceStatistics eth0() {
        return this.eth0;
    }

    /**
     * Set the eth0 property: The eth0 property.
     * 
     * @param eth0 the eth0 value to set.
     * @return the ContainerInfo object itself.
     */
    public ContainerInfo withEth0(ContainerNetworkInterfaceStatistics eth0) {
        this.eth0 = eth0;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (currentCpuStats() != null) {
            currentCpuStats().validate();
        }
        if (previousCpuStats() != null) {
            previousCpuStats().validate();
        }
        if (memoryStats() != null) {
            memoryStats().validate();
        }
        if (eth0() != null) {
            eth0().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("currentTimeStamp",
            this.currentTimestamp == null
                ? null
                : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.currentTimestamp));
        jsonWriter.writeStringField("previousTimeStamp",
            this.previousTimestamp == null
                ? null
                : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.previousTimestamp));
        jsonWriter.writeJsonField("currentCpuStats", this.currentCpuStats);
        jsonWriter.writeJsonField("previousCpuStats", this.previousCpuStats);
        jsonWriter.writeJsonField("memoryStats", this.memoryStats);
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeJsonField("eth0", this.eth0);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ContainerInfo from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ContainerInfo if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ContainerInfo.
     */
    public static ContainerInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ContainerInfo deserializedContainerInfo = new ContainerInfo();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("currentTimeStamp".equals(fieldName)) {
                    deserializedContainerInfo.currentTimestamp = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("previousTimeStamp".equals(fieldName)) {
                    deserializedContainerInfo.previousTimestamp = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("currentCpuStats".equals(fieldName)) {
                    deserializedContainerInfo.currentCpuStats = ContainerCpuStatistics.fromJson(reader);
                } else if ("previousCpuStats".equals(fieldName)) {
                    deserializedContainerInfo.previousCpuStats = ContainerCpuStatistics.fromJson(reader);
                } else if ("memoryStats".equals(fieldName)) {
                    deserializedContainerInfo.memoryStats = ContainerMemoryStatistics.fromJson(reader);
                } else if ("name".equals(fieldName)) {
                    deserializedContainerInfo.name = reader.getString();
                } else if ("id".equals(fieldName)) {
                    deserializedContainerInfo.id = reader.getString();
                } else if ("eth0".equals(fieldName)) {
                    deserializedContainerInfo.eth0 = ContainerNetworkInterfaceStatistics.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedContainerInfo;
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.connectedvmware.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Defines the network interface update.
 */
@Fluent
public final class NetworkInterfaceUpdate implements JsonSerializable<NetworkInterfaceUpdate> {
    /*
     * Gets or sets the name of the network interface.
     */
    private String name;

    /*
     * Gets or sets the ARM Id of the network resource to connect the virtual machine.
     */
    private String networkId;

    /*
     * NIC type
     */
    private NicType nicType;

    /*
     * Gets or sets the power on boot.
     */
    private PowerOnBootOption powerOnBoot;

    /*
     * Gets or sets the device key value.
     */
    private Integer deviceKey;

    /**
     * Creates an instance of NetworkInterfaceUpdate class.
     */
    public NetworkInterfaceUpdate() {
    }

    /**
     * Get the name property: Gets or sets the name of the network interface.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name property: Gets or sets the name of the network interface.
     * 
     * @param name the name value to set.
     * @return the NetworkInterfaceUpdate object itself.
     */
    public NetworkInterfaceUpdate withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the networkId property: Gets or sets the ARM Id of the network resource to connect the virtual machine.
     * 
     * @return the networkId value.
     */
    public String networkId() {
        return this.networkId;
    }

    /**
     * Set the networkId property: Gets or sets the ARM Id of the network resource to connect the virtual machine.
     * 
     * @param networkId the networkId value to set.
     * @return the NetworkInterfaceUpdate object itself.
     */
    public NetworkInterfaceUpdate withNetworkId(String networkId) {
        this.networkId = networkId;
        return this;
    }

    /**
     * Get the nicType property: NIC type.
     * 
     * @return the nicType value.
     */
    public NicType nicType() {
        return this.nicType;
    }

    /**
     * Set the nicType property: NIC type.
     * 
     * @param nicType the nicType value to set.
     * @return the NetworkInterfaceUpdate object itself.
     */
    public NetworkInterfaceUpdate withNicType(NicType nicType) {
        this.nicType = nicType;
        return this;
    }

    /**
     * Get the powerOnBoot property: Gets or sets the power on boot.
     * 
     * @return the powerOnBoot value.
     */
    public PowerOnBootOption powerOnBoot() {
        return this.powerOnBoot;
    }

    /**
     * Set the powerOnBoot property: Gets or sets the power on boot.
     * 
     * @param powerOnBoot the powerOnBoot value to set.
     * @return the NetworkInterfaceUpdate object itself.
     */
    public NetworkInterfaceUpdate withPowerOnBoot(PowerOnBootOption powerOnBoot) {
        this.powerOnBoot = powerOnBoot;
        return this;
    }

    /**
     * Get the deviceKey property: Gets or sets the device key value.
     * 
     * @return the deviceKey value.
     */
    public Integer deviceKey() {
        return this.deviceKey;
    }

    /**
     * Set the deviceKey property: Gets or sets the device key value.
     * 
     * @param deviceKey the deviceKey value to set.
     * @return the NetworkInterfaceUpdate object itself.
     */
    public NetworkInterfaceUpdate withDeviceKey(Integer deviceKey) {
        this.deviceKey = deviceKey;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeStringField("networkId", this.networkId);
        jsonWriter.writeStringField("nicType", this.nicType == null ? null : this.nicType.toString());
        jsonWriter.writeStringField("powerOnBoot", this.powerOnBoot == null ? null : this.powerOnBoot.toString());
        jsonWriter.writeNumberField("deviceKey", this.deviceKey);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of NetworkInterfaceUpdate from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of NetworkInterfaceUpdate if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the NetworkInterfaceUpdate.
     */
    public static NetworkInterfaceUpdate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NetworkInterfaceUpdate deserializedNetworkInterfaceUpdate = new NetworkInterfaceUpdate();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    deserializedNetworkInterfaceUpdate.name = reader.getString();
                } else if ("networkId".equals(fieldName)) {
                    deserializedNetworkInterfaceUpdate.networkId = reader.getString();
                } else if ("nicType".equals(fieldName)) {
                    deserializedNetworkInterfaceUpdate.nicType = NicType.fromString(reader.getString());
                } else if ("powerOnBoot".equals(fieldName)) {
                    deserializedNetworkInterfaceUpdate.powerOnBoot = PowerOnBootOption.fromString(reader.getString());
                } else if ("deviceKey".equals(fieldName)) {
                    deserializedNetworkInterfaceUpdate.deviceKey = reader.getNullable(JsonReader::getInt);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNetworkInterfaceUpdate;
        });
    }
}

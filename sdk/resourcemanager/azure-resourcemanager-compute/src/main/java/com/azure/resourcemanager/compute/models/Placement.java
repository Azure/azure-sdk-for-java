// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Describes the user-defined constraints for virtual machine hardware placement.
 */
@Fluent
public final class Placement implements JsonSerializable<Placement> {
    /*
     * Specifies the policy for virtual machine's placement in availability zone. Possible values are: **Any** - An
     * availability zone will be automatically picked by system as part of virtual machine creation.
     */
    private ZonePlacementPolicyType zonePlacementPolicy;

    /*
     * This property supplements the 'zonePlacementPolicy' property. If 'zonePlacementPolicy' is set to 'Any',
     * availability zone selected by the system must be present in the list of availability zones passed with
     * 'includeZones'. If 'includeZones' is not provided, all availability zones in region will be considered for
     * selection.
     */
    private List<String> includeZones;

    /*
     * This property supplements the 'zonePlacementPolicy' property. If 'zonePlacementPolicy' is set to 'Any',
     * availability zone selected by the system must not be present in the list of availability zones passed with
     * 'excludeZones'. If 'excludeZones' is not provided, all availability zones in region will be considered for
     * selection.
     */
    private List<String> excludeZones;

    /**
     * Creates an instance of Placement class.
     */
    public Placement() {
    }

    /**
     * Get the zonePlacementPolicy property: Specifies the policy for virtual machine's placement in availability zone.
     * Possible values are: **Any** - An availability zone will be automatically picked by system as part of virtual
     * machine creation.
     * 
     * @return the zonePlacementPolicy value.
     */
    public ZonePlacementPolicyType zonePlacementPolicy() {
        return this.zonePlacementPolicy;
    }

    /**
     * Set the zonePlacementPolicy property: Specifies the policy for virtual machine's placement in availability zone.
     * Possible values are: **Any** - An availability zone will be automatically picked by system as part of virtual
     * machine creation.
     * 
     * @param zonePlacementPolicy the zonePlacementPolicy value to set.
     * @return the Placement object itself.
     */
    public Placement withZonePlacementPolicy(ZonePlacementPolicyType zonePlacementPolicy) {
        this.zonePlacementPolicy = zonePlacementPolicy;
        return this;
    }

    /**
     * Get the includeZones property: This property supplements the 'zonePlacementPolicy' property. If
     * 'zonePlacementPolicy' is set to 'Any', availability zone selected by the system must be present in the list of
     * availability zones passed with 'includeZones'. If 'includeZones' is not provided, all availability zones in
     * region will be considered for selection.
     * 
     * @return the includeZones value.
     */
    public List<String> includeZones() {
        return this.includeZones;
    }

    /**
     * Set the includeZones property: This property supplements the 'zonePlacementPolicy' property. If
     * 'zonePlacementPolicy' is set to 'Any', availability zone selected by the system must be present in the list of
     * availability zones passed with 'includeZones'. If 'includeZones' is not provided, all availability zones in
     * region will be considered for selection.
     * 
     * @param includeZones the includeZones value to set.
     * @return the Placement object itself.
     */
    public Placement withIncludeZones(List<String> includeZones) {
        this.includeZones = includeZones;
        return this;
    }

    /**
     * Get the excludeZones property: This property supplements the 'zonePlacementPolicy' property. If
     * 'zonePlacementPolicy' is set to 'Any', availability zone selected by the system must not be present in the list
     * of availability zones passed with 'excludeZones'. If 'excludeZones' is not provided, all availability zones in
     * region will be considered for selection.
     * 
     * @return the excludeZones value.
     */
    public List<String> excludeZones() {
        return this.excludeZones;
    }

    /**
     * Set the excludeZones property: This property supplements the 'zonePlacementPolicy' property. If
     * 'zonePlacementPolicy' is set to 'Any', availability zone selected by the system must not be present in the list
     * of availability zones passed with 'excludeZones'. If 'excludeZones' is not provided, all availability zones in
     * region will be considered for selection.
     * 
     * @param excludeZones the excludeZones value to set.
     * @return the Placement object itself.
     */
    public Placement withExcludeZones(List<String> excludeZones) {
        this.excludeZones = excludeZones;
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
        jsonWriter.writeStringField("zonePlacementPolicy",
            this.zonePlacementPolicy == null ? null : this.zonePlacementPolicy.toString());
        jsonWriter.writeArrayField("includeZones", this.includeZones, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("excludeZones", this.excludeZones, (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of Placement from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of Placement if the JsonReader was pointing to an instance of it, or null if it was pointing
     * to JSON null.
     * @throws IOException If an error occurs while reading the Placement.
     */
    public static Placement fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Placement deserializedPlacement = new Placement();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("zonePlacementPolicy".equals(fieldName)) {
                    deserializedPlacement.zonePlacementPolicy = ZonePlacementPolicyType.fromString(reader.getString());
                } else if ("includeZones".equals(fieldName)) {
                    List<String> includeZones = reader.readArray(reader1 -> reader1.getString());
                    deserializedPlacement.includeZones = includeZones;
                } else if ("excludeZones".equals(fieldName)) {
                    List<String> excludeZones = reader.readArray(reader1 -> reader1.getString());
                    deserializedPlacement.excludeZones = excludeZones;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPlacement;
        });
    }
}

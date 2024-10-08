// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.privatedns.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.SubResource;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.privatedns.models.ProvisioningState;
import com.azure.resourcemanager.privatedns.models.ResolutionPolicy;
import com.azure.resourcemanager.privatedns.models.VirtualNetworkLinkState;
import java.io.IOException;

/**
 * Represents the properties of the Private DNS zone.
 */
@Fluent
public final class VirtualNetworkLinkProperties implements JsonSerializable<VirtualNetworkLinkProperties> {
    /*
     * The reference of the virtual network.
     */
    private SubResource virtualNetwork;

    /*
     * Is auto-registration of virtual machine records in the virtual network in the Private DNS zone enabled?
     */
    private Boolean registrationEnabled;

    /*
     * The resolution policy on the virtual network link. Only applicable for virtual network links to privatelink
     * zones, and for A,AAAA,CNAME queries. When set to 'NxDomainRedirect', Azure DNS resolver falls back to public
     * resolution if private dns query resolution results in non-existent domain response.
     */
    private ResolutionPolicy resolutionPolicy;

    /*
     * The status of the virtual network link to the Private DNS zone. Possible values are 'InProgress' and 'Done'. This
     * is a read-only property and any attempt to set this value will be ignored.
     */
    private VirtualNetworkLinkState virtualNetworkLinkState;

    /*
     * The provisioning state of the resource. This is a read-only property and any attempt to set this value will be
     * ignored.
     */
    private ProvisioningState provisioningState;

    /**
     * Creates an instance of VirtualNetworkLinkProperties class.
     */
    public VirtualNetworkLinkProperties() {
    }

    /**
     * Get the virtualNetwork property: The reference of the virtual network.
     * 
     * @return the virtualNetwork value.
     */
    public SubResource virtualNetwork() {
        return this.virtualNetwork;
    }

    /**
     * Set the virtualNetwork property: The reference of the virtual network.
     * 
     * @param virtualNetwork the virtualNetwork value to set.
     * @return the VirtualNetworkLinkProperties object itself.
     */
    public VirtualNetworkLinkProperties withVirtualNetwork(SubResource virtualNetwork) {
        this.virtualNetwork = virtualNetwork;
        return this;
    }

    /**
     * Get the registrationEnabled property: Is auto-registration of virtual machine records in the virtual network in
     * the Private DNS zone enabled?.
     * 
     * @return the registrationEnabled value.
     */
    public Boolean registrationEnabled() {
        return this.registrationEnabled;
    }

    /**
     * Set the registrationEnabled property: Is auto-registration of virtual machine records in the virtual network in
     * the Private DNS zone enabled?.
     * 
     * @param registrationEnabled the registrationEnabled value to set.
     * @return the VirtualNetworkLinkProperties object itself.
     */
    public VirtualNetworkLinkProperties withRegistrationEnabled(Boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
        return this;
    }

    /**
     * Get the resolutionPolicy property: The resolution policy on the virtual network link. Only applicable for virtual
     * network links to privatelink zones, and for A,AAAA,CNAME queries. When set to 'NxDomainRedirect', Azure DNS
     * resolver falls back to public resolution if private dns query resolution results in non-existent domain response.
     * 
     * @return the resolutionPolicy value.
     */
    public ResolutionPolicy resolutionPolicy() {
        return this.resolutionPolicy;
    }

    /**
     * Set the resolutionPolicy property: The resolution policy on the virtual network link. Only applicable for virtual
     * network links to privatelink zones, and for A,AAAA,CNAME queries. When set to 'NxDomainRedirect', Azure DNS
     * resolver falls back to public resolution if private dns query resolution results in non-existent domain response.
     * 
     * @param resolutionPolicy the resolutionPolicy value to set.
     * @return the VirtualNetworkLinkProperties object itself.
     */
    public VirtualNetworkLinkProperties withResolutionPolicy(ResolutionPolicy resolutionPolicy) {
        this.resolutionPolicy = resolutionPolicy;
        return this;
    }

    /**
     * Get the virtualNetworkLinkState property: The status of the virtual network link to the Private DNS zone.
     * Possible values are 'InProgress' and 'Done'. This is a read-only property and any attempt to set this value will
     * be ignored.
     * 
     * @return the virtualNetworkLinkState value.
     */
    public VirtualNetworkLinkState virtualNetworkLinkState() {
        return this.virtualNetworkLinkState;
    }

    /**
     * Get the provisioningState property: The provisioning state of the resource. This is a read-only property and any
     * attempt to set this value will be ignored.
     * 
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.provisioningState;
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
        jsonWriter.writeJsonField("virtualNetwork", this.virtualNetwork);
        jsonWriter.writeBooleanField("registrationEnabled", this.registrationEnabled);
        jsonWriter.writeStringField("resolutionPolicy",
            this.resolutionPolicy == null ? null : this.resolutionPolicy.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of VirtualNetworkLinkProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of VirtualNetworkLinkProperties if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the VirtualNetworkLinkProperties.
     */
    public static VirtualNetworkLinkProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            VirtualNetworkLinkProperties deserializedVirtualNetworkLinkProperties = new VirtualNetworkLinkProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("virtualNetwork".equals(fieldName)) {
                    deserializedVirtualNetworkLinkProperties.virtualNetwork = SubResource.fromJson(reader);
                } else if ("registrationEnabled".equals(fieldName)) {
                    deserializedVirtualNetworkLinkProperties.registrationEnabled
                        = reader.getNullable(JsonReader::getBoolean);
                } else if ("resolutionPolicy".equals(fieldName)) {
                    deserializedVirtualNetworkLinkProperties.resolutionPolicy
                        = ResolutionPolicy.fromString(reader.getString());
                } else if ("virtualNetworkLinkState".equals(fieldName)) {
                    deserializedVirtualNetworkLinkProperties.virtualNetworkLinkState
                        = VirtualNetworkLinkState.fromString(reader.getString());
                } else if ("provisioningState".equals(fieldName)) {
                    deserializedVirtualNetworkLinkProperties.provisioningState
                        = ProvisioningState.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedVirtualNetworkLinkProperties;
        });
    }
}

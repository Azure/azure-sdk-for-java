// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.labservices.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.labservices.fluent.models.LabPlanUpdateProperties;
import java.io.IOException;
import java.util.List;

/**
 * Contains lab configuration and default settings. This variant is used for PATCH.
 */
@Fluent
public final class LabPlanUpdate extends TrackedResourceUpdate {
    /*
     * Lab plan resource update properties
     */
    private LabPlanUpdateProperties innerProperties;

    /*
     * Managed Identity Information
     */
    private Identity identity;

    /**
     * Creates an instance of LabPlanUpdate class.
     */
    public LabPlanUpdate() {
    }

    /**
     * Get the innerProperties property: Lab plan resource update properties.
     * 
     * @return the innerProperties value.
     */
    private LabPlanUpdateProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the identity property: Managed Identity Information.
     * 
     * @return the identity value.
     */
    public Identity identity() {
        return this.identity;
    }

    /**
     * Set the identity property: Managed Identity Information.
     * 
     * @param identity the identity value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withIdentity(Identity identity) {
        this.identity = identity;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabPlanUpdate withTags(List<String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Get the defaultConnectionProfile property: The default lab connection profile. This can be changed on a lab
     * resource and only provides a default profile.
     * 
     * @return the defaultConnectionProfile value.
     */
    public ConnectionProfile defaultConnectionProfile() {
        return this.innerProperties() == null ? null : this.innerProperties().defaultConnectionProfile();
    }

    /**
     * Set the defaultConnectionProfile property: The default lab connection profile. This can be changed on a lab
     * resource and only provides a default profile.
     * 
     * @param defaultConnectionProfile the defaultConnectionProfile value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withDefaultConnectionProfile(ConnectionProfile defaultConnectionProfile) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withDefaultConnectionProfile(defaultConnectionProfile);
        return this;
    }

    /**
     * Get the defaultAutoShutdownProfile property: The default lab shutdown profile. This can be changed on a lab
     * resource and only provides a default profile.
     * 
     * @return the defaultAutoShutdownProfile value.
     */
    public AutoShutdownProfile defaultAutoShutdownProfile() {
        return this.innerProperties() == null ? null : this.innerProperties().defaultAutoShutdownProfile();
    }

    /**
     * Set the defaultAutoShutdownProfile property: The default lab shutdown profile. This can be changed on a lab
     * resource and only provides a default profile.
     * 
     * @param defaultAutoShutdownProfile the defaultAutoShutdownProfile value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withDefaultAutoShutdownProfile(AutoShutdownProfile defaultAutoShutdownProfile) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withDefaultAutoShutdownProfile(defaultAutoShutdownProfile);
        return this;
    }

    /**
     * Get the defaultNetworkProfile property: The lab plan network profile. To enforce lab network policies they must
     * be defined here and cannot be changed when there are existing labs associated with this lab plan.
     * 
     * @return the defaultNetworkProfile value.
     */
    public LabPlanNetworkProfile defaultNetworkProfile() {
        return this.innerProperties() == null ? null : this.innerProperties().defaultNetworkProfile();
    }

    /**
     * Set the defaultNetworkProfile property: The lab plan network profile. To enforce lab network policies they must
     * be defined here and cannot be changed when there are existing labs associated with this lab plan.
     * 
     * @param defaultNetworkProfile the defaultNetworkProfile value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withDefaultNetworkProfile(LabPlanNetworkProfile defaultNetworkProfile) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withDefaultNetworkProfile(defaultNetworkProfile);
        return this;
    }

    /**
     * Get the allowedRegions property: The allowed regions for the lab creator to use when creating labs using this lab
     * plan.
     * 
     * @return the allowedRegions value.
     */
    public List<String> allowedRegions() {
        return this.innerProperties() == null ? null : this.innerProperties().allowedRegions();
    }

    /**
     * Set the allowedRegions property: The allowed regions for the lab creator to use when creating labs using this lab
     * plan.
     * 
     * @param allowedRegions the allowedRegions value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withAllowedRegions(List<String> allowedRegions) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withAllowedRegions(allowedRegions);
        return this;
    }

    /**
     * Get the sharedGalleryId property: Resource ID of the Shared Image Gallery attached to this lab plan. When saving
     * a lab template virtual machine image it will be persisted in this gallery. Shared images from the gallery can be
     * made available to use when creating new labs.
     * 
     * @return the sharedGalleryId value.
     */
    public String sharedGalleryId() {
        return this.innerProperties() == null ? null : this.innerProperties().sharedGalleryId();
    }

    /**
     * Set the sharedGalleryId property: Resource ID of the Shared Image Gallery attached to this lab plan. When saving
     * a lab template virtual machine image it will be persisted in this gallery. Shared images from the gallery can be
     * made available to use when creating new labs.
     * 
     * @param sharedGalleryId the sharedGalleryId value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withSharedGalleryId(String sharedGalleryId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withSharedGalleryId(sharedGalleryId);
        return this;
    }

    /**
     * Get the supportInfo property: Support contact information and instructions for users of the lab plan. This
     * information is displayed to lab owners and virtual machine users for all labs in the lab plan.
     * 
     * @return the supportInfo value.
     */
    public SupportInfo supportInfo() {
        return this.innerProperties() == null ? null : this.innerProperties().supportInfo();
    }

    /**
     * Set the supportInfo property: Support contact information and instructions for users of the lab plan. This
     * information is displayed to lab owners and virtual machine users for all labs in the lab plan.
     * 
     * @param supportInfo the supportInfo value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withSupportInfo(SupportInfo supportInfo) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withSupportInfo(supportInfo);
        return this;
    }

    /**
     * Get the linkedLmsInstance property: Base Url of the lms instance this lab plan can link lab rosters against.
     * 
     * @return the linkedLmsInstance value.
     */
    public String linkedLmsInstance() {
        return this.innerProperties() == null ? null : this.innerProperties().linkedLmsInstance();
    }

    /**
     * Set the linkedLmsInstance property: Base Url of the lms instance this lab plan can link lab rosters against.
     * 
     * @param linkedLmsInstance the linkedLmsInstance value to set.
     * @return the LabPlanUpdate object itself.
     */
    public LabPlanUpdate withLinkedLmsInstance(String linkedLmsInstance) {
        if (this.innerProperties() == null) {
            this.innerProperties = new LabPlanUpdateProperties();
        }
        this.innerProperties().withLinkedLmsInstance(linkedLmsInstance);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
        if (identity() != null) {
            identity().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("properties", this.innerProperties);
        jsonWriter.writeJsonField("identity", this.identity);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of LabPlanUpdate from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of LabPlanUpdate if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the LabPlanUpdate.
     */
    public static LabPlanUpdate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LabPlanUpdate deserializedLabPlanUpdate = new LabPlanUpdate();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("tags".equals(fieldName)) {
                    List<String> tags = reader.readArray(reader1 -> reader1.getString());
                    deserializedLabPlanUpdate.withTags(tags);
                } else if ("properties".equals(fieldName)) {
                    deserializedLabPlanUpdate.innerProperties = LabPlanUpdateProperties.fromJson(reader);
                } else if ("identity".equals(fieldName)) {
                    deserializedLabPlanUpdate.identity = Identity.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedLabPlanUpdate;
        });
    }
}

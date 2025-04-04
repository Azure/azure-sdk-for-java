// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dynatrace.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.dynatrace.models.AutoUpdateSetting;
import com.azure.resourcemanager.dynatrace.models.AvailabilityState;
import com.azure.resourcemanager.dynatrace.models.LogModule;
import com.azure.resourcemanager.dynatrace.models.MonitoringType;
import com.azure.resourcemanager.dynatrace.models.UpdateStatus;
import java.io.IOException;

/**
 * Details of VM Resource having Dynatrace OneAgent installed.
 */
@Fluent
public final class VMInfoInner implements JsonSerializable<VMInfoInner> {
    /*
     * Azure VM resource ID
     */
    private String resourceId;

    /*
     * Version of the Dynatrace agent installed on the VM.
     */
    private String version;

    /*
     * The monitoring mode of OneAgent
     */
    private MonitoringType monitoringType;

    /*
     * Update settings of OneAgent.
     */
    private AutoUpdateSetting autoUpdateSetting;

    /*
     * The current update status of OneAgent.
     */
    private UpdateStatus updateStatus;

    /*
     * The availability state of OneAgent.
     */
    private AvailabilityState availabilityState;

    /*
     * Tells whether log modules are enabled or not
     */
    private LogModule logModule;

    /*
     * The name of the host group
     */
    private String hostGroup;

    /*
     * The name of the host
     */
    private String hostname;

    /**
     * Creates an instance of VMInfoInner class.
     */
    public VMInfoInner() {
    }

    /**
     * Get the resourceId property: Azure VM resource ID.
     * 
     * @return the resourceId value.
     */
    public String resourceId() {
        return this.resourceId;
    }

    /**
     * Set the resourceId property: Azure VM resource ID.
     * 
     * @param resourceId the resourceId value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Get the version property: Version of the Dynatrace agent installed on the VM.
     * 
     * @return the version value.
     */
    public String version() {
        return this.version;
    }

    /**
     * Set the version property: Version of the Dynatrace agent installed on the VM.
     * 
     * @param version the version value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get the monitoringType property: The monitoring mode of OneAgent.
     * 
     * @return the monitoringType value.
     */
    public MonitoringType monitoringType() {
        return this.monitoringType;
    }

    /**
     * Set the monitoringType property: The monitoring mode of OneAgent.
     * 
     * @param monitoringType the monitoringType value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withMonitoringType(MonitoringType monitoringType) {
        this.monitoringType = monitoringType;
        return this;
    }

    /**
     * Get the autoUpdateSetting property: Update settings of OneAgent.
     * 
     * @return the autoUpdateSetting value.
     */
    public AutoUpdateSetting autoUpdateSetting() {
        return this.autoUpdateSetting;
    }

    /**
     * Set the autoUpdateSetting property: Update settings of OneAgent.
     * 
     * @param autoUpdateSetting the autoUpdateSetting value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withAutoUpdateSetting(AutoUpdateSetting autoUpdateSetting) {
        this.autoUpdateSetting = autoUpdateSetting;
        return this;
    }

    /**
     * Get the updateStatus property: The current update status of OneAgent.
     * 
     * @return the updateStatus value.
     */
    public UpdateStatus updateStatus() {
        return this.updateStatus;
    }

    /**
     * Set the updateStatus property: The current update status of OneAgent.
     * 
     * @param updateStatus the updateStatus value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withUpdateStatus(UpdateStatus updateStatus) {
        this.updateStatus = updateStatus;
        return this;
    }

    /**
     * Get the availabilityState property: The availability state of OneAgent.
     * 
     * @return the availabilityState value.
     */
    public AvailabilityState availabilityState() {
        return this.availabilityState;
    }

    /**
     * Set the availabilityState property: The availability state of OneAgent.
     * 
     * @param availabilityState the availabilityState value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withAvailabilityState(AvailabilityState availabilityState) {
        this.availabilityState = availabilityState;
        return this;
    }

    /**
     * Get the logModule property: Tells whether log modules are enabled or not.
     * 
     * @return the logModule value.
     */
    public LogModule logModule() {
        return this.logModule;
    }

    /**
     * Set the logModule property: Tells whether log modules are enabled or not.
     * 
     * @param logModule the logModule value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withLogModule(LogModule logModule) {
        this.logModule = logModule;
        return this;
    }

    /**
     * Get the hostGroup property: The name of the host group.
     * 
     * @return the hostGroup value.
     */
    public String hostGroup() {
        return this.hostGroup;
    }

    /**
     * Set the hostGroup property: The name of the host group.
     * 
     * @param hostGroup the hostGroup value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
        return this;
    }

    /**
     * Get the hostname property: The name of the host.
     * 
     * @return the hostname value.
     */
    public String hostname() {
        return this.hostname;
    }

    /**
     * Set the hostname property: The name of the host.
     * 
     * @param hostname the hostname value to set.
     * @return the VMInfoInner object itself.
     */
    public VMInfoInner withHostname(String hostname) {
        this.hostname = hostname;
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
        jsonWriter.writeStringField("resourceId", this.resourceId);
        jsonWriter.writeStringField("version", this.version);
        jsonWriter.writeStringField("monitoringType",
            this.monitoringType == null ? null : this.monitoringType.toString());
        jsonWriter.writeStringField("autoUpdateSetting",
            this.autoUpdateSetting == null ? null : this.autoUpdateSetting.toString());
        jsonWriter.writeStringField("updateStatus", this.updateStatus == null ? null : this.updateStatus.toString());
        jsonWriter.writeStringField("availabilityState",
            this.availabilityState == null ? null : this.availabilityState.toString());
        jsonWriter.writeStringField("logModule", this.logModule == null ? null : this.logModule.toString());
        jsonWriter.writeStringField("hostGroup", this.hostGroup);
        jsonWriter.writeStringField("hostName", this.hostname);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of VMInfoInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of VMInfoInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the VMInfoInner.
     */
    public static VMInfoInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            VMInfoInner deserializedVMInfoInner = new VMInfoInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("resourceId".equals(fieldName)) {
                    deserializedVMInfoInner.resourceId = reader.getString();
                } else if ("version".equals(fieldName)) {
                    deserializedVMInfoInner.version = reader.getString();
                } else if ("monitoringType".equals(fieldName)) {
                    deserializedVMInfoInner.monitoringType = MonitoringType.fromString(reader.getString());
                } else if ("autoUpdateSetting".equals(fieldName)) {
                    deserializedVMInfoInner.autoUpdateSetting = AutoUpdateSetting.fromString(reader.getString());
                } else if ("updateStatus".equals(fieldName)) {
                    deserializedVMInfoInner.updateStatus = UpdateStatus.fromString(reader.getString());
                } else if ("availabilityState".equals(fieldName)) {
                    deserializedVMInfoInner.availabilityState = AvailabilityState.fromString(reader.getString());
                } else if ("logModule".equals(fieldName)) {
                    deserializedVMInfoInner.logModule = LogModule.fromString(reader.getString());
                } else if ("hostGroup".equals(fieldName)) {
                    deserializedVMInfoInner.hostGroup = reader.getString();
                } else if ("hostName".equals(fieldName)) {
                    deserializedVMInfoInner.hostname = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedVMInfoInner;
        });
    }
}

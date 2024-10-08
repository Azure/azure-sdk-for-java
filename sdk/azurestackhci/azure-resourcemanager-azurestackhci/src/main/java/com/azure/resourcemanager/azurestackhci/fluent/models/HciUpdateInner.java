// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.ProxyResource;
import com.azure.core.management.SystemData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.azurestackhci.models.AvailabilityType;
import com.azure.resourcemanager.azurestackhci.models.HealthState;
import com.azure.resourcemanager.azurestackhci.models.PackageVersionInfo;
import com.azure.resourcemanager.azurestackhci.models.PrecheckResult;
import com.azure.resourcemanager.azurestackhci.models.ProvisioningState;
import com.azure.resourcemanager.azurestackhci.models.RebootRequirement;
import com.azure.resourcemanager.azurestackhci.models.State;
import com.azure.resourcemanager.azurestackhci.models.UpdatePrerequisite;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Update details.
 */
@Fluent
public final class HciUpdateInner extends ProxyResource {
    /*
     * The geo-location where the resource lives
     */
    private String location;

    /*
     * Update properties
     */
    private UpdateProperties innerProperties;

    /*
     * Azure Resource Manager metadata containing createdBy and modifiedBy information.
     */
    private SystemData systemData;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * The type of the resource.
     */
    private String type;

    /**
     * Creates an instance of HciUpdateInner class.
     */
    public HciUpdateInner() {
    }

    /**
     * Get the location property: The geo-location where the resource lives.
     * 
     * @return the location value.
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location property: The geo-location where the resource lives.
     * 
     * @param location the location value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the innerProperties property: Update properties.
     * 
     * @return the innerProperties value.
     */
    private UpdateProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the systemData property: Azure Resource Manager metadata containing createdBy and modifiedBy information.
     * 
     * @return the systemData value.
     */
    public SystemData systemData() {
        return this.systemData;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * Get the name property: The name of the resource.
     * 
     * @return the name value.
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the provisioningState property: Provisioning state of the Updates proxy resource.
     * 
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the installedDate property: Date that the update was installed.
     * 
     * @return the installedDate value.
     */
    public OffsetDateTime installedDate() {
        return this.innerProperties() == null ? null : this.innerProperties().installedDate();
    }

    /**
     * Set the installedDate property: Date that the update was installed.
     * 
     * @param installedDate the installedDate value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withInstalledDate(OffsetDateTime installedDate) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withInstalledDate(installedDate);
        return this;
    }

    /**
     * Get the description property: Description of the update.
     * 
     * @return the description value.
     */
    public String description() {
        return this.innerProperties() == null ? null : this.innerProperties().description();
    }

    /**
     * Set the description property: Description of the update.
     * 
     * @param description the description value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withDescription(String description) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withDescription(description);
        return this;
    }

    /**
     * Get the minSbeVersionRequired property: Minimum Sbe Version of the update.
     * 
     * @return the minSbeVersionRequired value.
     */
    public String minSbeVersionRequired() {
        return this.innerProperties() == null ? null : this.innerProperties().minSbeVersionRequired();
    }

    /**
     * Set the minSbeVersionRequired property: Minimum Sbe Version of the update.
     * 
     * @param minSbeVersionRequired the minSbeVersionRequired value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withMinSbeVersionRequired(String minSbeVersionRequired) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withMinSbeVersionRequired(minSbeVersionRequired);
        return this;
    }

    /**
     * Get the state property: State of the update as it relates to this stamp.
     * 
     * @return the state value.
     */
    public State state() {
        return this.innerProperties() == null ? null : this.innerProperties().state();
    }

    /**
     * Set the state property: State of the update as it relates to this stamp.
     * 
     * @param state the state value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withState(State state) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withState(state);
        return this;
    }

    /**
     * Get the prerequisites property: If update State is HasPrerequisite, this property contains an array of objects
     * describing prerequisite updates before installing this update. Otherwise, it is empty.
     * 
     * @return the prerequisites value.
     */
    public List<UpdatePrerequisite> prerequisites() {
        return this.innerProperties() == null ? null : this.innerProperties().prerequisites();
    }

    /**
     * Set the prerequisites property: If update State is HasPrerequisite, this property contains an array of objects
     * describing prerequisite updates before installing this update. Otherwise, it is empty.
     * 
     * @param prerequisites the prerequisites value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withPrerequisites(List<UpdatePrerequisite> prerequisites) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withPrerequisites(prerequisites);
        return this;
    }

    /**
     * Get the componentVersions property: An array of component versions for a Solution Bundle update, and an empty
     * array otherwise.
     * 
     * @return the componentVersions value.
     */
    public List<PackageVersionInfo> componentVersions() {
        return this.innerProperties() == null ? null : this.innerProperties().componentVersions();
    }

    /**
     * Set the componentVersions property: An array of component versions for a Solution Bundle update, and an empty
     * array otherwise.
     * 
     * @param componentVersions the componentVersions value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withComponentVersions(List<PackageVersionInfo> componentVersions) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withComponentVersions(componentVersions);
        return this;
    }

    /**
     * Get the rebootRequired property: The rebootRequired property.
     * 
     * @return the rebootRequired value.
     */
    public RebootRequirement rebootRequired() {
        return this.innerProperties() == null ? null : this.innerProperties().rebootRequired();
    }

    /**
     * Set the rebootRequired property: The rebootRequired property.
     * 
     * @param rebootRequired the rebootRequired value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withRebootRequired(RebootRequirement rebootRequired) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withRebootRequired(rebootRequired);
        return this;
    }

    /**
     * Get the healthState property: Overall health state for update-specific health checks.
     * 
     * @return the healthState value.
     */
    public HealthState healthState() {
        return this.innerProperties() == null ? null : this.innerProperties().healthState();
    }

    /**
     * Set the healthState property: Overall health state for update-specific health checks.
     * 
     * @param healthState the healthState value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withHealthState(HealthState healthState) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withHealthState(healthState);
        return this;
    }

    /**
     * Get the healthCheckResult property: An array of PrecheckResult objects.
     * 
     * @return the healthCheckResult value.
     */
    public List<PrecheckResult> healthCheckResult() {
        return this.innerProperties() == null ? null : this.innerProperties().healthCheckResult();
    }

    /**
     * Set the healthCheckResult property: An array of PrecheckResult objects.
     * 
     * @param healthCheckResult the healthCheckResult value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withHealthCheckResult(List<PrecheckResult> healthCheckResult) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withHealthCheckResult(healthCheckResult);
        return this;
    }

    /**
     * Get the healthCheckDate property: Last time the package-specific checks were run.
     * 
     * @return the healthCheckDate value.
     */
    public OffsetDateTime healthCheckDate() {
        return this.innerProperties() == null ? null : this.innerProperties().healthCheckDate();
    }

    /**
     * Set the healthCheckDate property: Last time the package-specific checks were run.
     * 
     * @param healthCheckDate the healthCheckDate value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withHealthCheckDate(OffsetDateTime healthCheckDate) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withHealthCheckDate(healthCheckDate);
        return this;
    }

    /**
     * Get the packagePath property: Path where the update package is available.
     * 
     * @return the packagePath value.
     */
    public String packagePath() {
        return this.innerProperties() == null ? null : this.innerProperties().packagePath();
    }

    /**
     * Set the packagePath property: Path where the update package is available.
     * 
     * @param packagePath the packagePath value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withPackagePath(String packagePath) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withPackagePath(packagePath);
        return this;
    }

    /**
     * Get the packageSizeInMb property: Size of the package. This value is a combination of the size from update
     * metadata and size of the payload that results from the live scan operation for OS update content.
     * 
     * @return the packageSizeInMb value.
     */
    public Float packageSizeInMb() {
        return this.innerProperties() == null ? null : this.innerProperties().packageSizeInMb();
    }

    /**
     * Set the packageSizeInMb property: Size of the package. This value is a combination of the size from update
     * metadata and size of the payload that results from the live scan operation for OS update content.
     * 
     * @param packageSizeInMb the packageSizeInMb value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withPackageSizeInMb(Float packageSizeInMb) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withPackageSizeInMb(packageSizeInMb);
        return this;
    }

    /**
     * Get the displayName property: Display name of the Update.
     * 
     * @return the displayName value.
     */
    public String displayName() {
        return this.innerProperties() == null ? null : this.innerProperties().displayName();
    }

    /**
     * Set the displayName property: Display name of the Update.
     * 
     * @param displayName the displayName value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withDisplayName(String displayName) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withDisplayName(displayName);
        return this;
    }

    /**
     * Get the version property: Version of the update.
     * 
     * @return the version value.
     */
    public String version() {
        return this.innerProperties() == null ? null : this.innerProperties().version();
    }

    /**
     * Set the version property: Version of the update.
     * 
     * @param version the version value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withVersion(String version) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withVersion(version);
        return this;
    }

    /**
     * Get the publisher property: Publisher of the update package.
     * 
     * @return the publisher value.
     */
    public String publisher() {
        return this.innerProperties() == null ? null : this.innerProperties().publisher();
    }

    /**
     * Set the publisher property: Publisher of the update package.
     * 
     * @param publisher the publisher value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withPublisher(String publisher) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withPublisher(publisher);
        return this;
    }

    /**
     * Get the releaseLink property: Link to release notes for the update.
     * 
     * @return the releaseLink value.
     */
    public String releaseLink() {
        return this.innerProperties() == null ? null : this.innerProperties().releaseLink();
    }

    /**
     * Set the releaseLink property: Link to release notes for the update.
     * 
     * @param releaseLink the releaseLink value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withReleaseLink(String releaseLink) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withReleaseLink(releaseLink);
        return this;
    }

    /**
     * Get the availabilityType property: Indicates the way the update content can be downloaded.
     * 
     * @return the availabilityType value.
     */
    public AvailabilityType availabilityType() {
        return this.innerProperties() == null ? null : this.innerProperties().availabilityType();
    }

    /**
     * Set the availabilityType property: Indicates the way the update content can be downloaded.
     * 
     * @param availabilityType the availabilityType value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withAvailabilityType(AvailabilityType availabilityType) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withAvailabilityType(availabilityType);
        return this;
    }

    /**
     * Get the packageType property: Customer-visible type of the update.
     * 
     * @return the packageType value.
     */
    public String packageType() {
        return this.innerProperties() == null ? null : this.innerProperties().packageType();
    }

    /**
     * Set the packageType property: Customer-visible type of the update.
     * 
     * @param packageType the packageType value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withPackageType(String packageType) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withPackageType(packageType);
        return this;
    }

    /**
     * Get the additionalProperties property: Extensible KV pairs serialized as a string. This is currently used to
     * report the stamp OEM family and hardware model information when an update is flagged as Invalid for the stamp
     * based on OEM type.
     * 
     * @return the additionalProperties value.
     */
    public String additionalProperties() {
        return this.innerProperties() == null ? null : this.innerProperties().additionalProperties();
    }

    /**
     * Set the additionalProperties property: Extensible KV pairs serialized as a string. This is currently used to
     * report the stamp OEM family and hardware model information when an update is flagged as Invalid for the stamp
     * based on OEM type.
     * 
     * @param additionalProperties the additionalProperties value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withAdditionalProperties(String additionalProperties) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withAdditionalProperties(additionalProperties);
        return this;
    }

    /**
     * Get the progressPercentage property: Progress percentage of ongoing operation. Currently this property is only
     * valid when the update is in the Downloading state, where it maps to how much of the update content has been
     * downloaded.
     * 
     * @return the progressPercentage value.
     */
    public Float progressPercentage() {
        return this.innerProperties() == null ? null : this.innerProperties().progressPercentage();
    }

    /**
     * Set the progressPercentage property: Progress percentage of ongoing operation. Currently this property is only
     * valid when the update is in the Downloading state, where it maps to how much of the update content has been
     * downloaded.
     * 
     * @param progressPercentage the progressPercentage value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withProgressPercentage(Float progressPercentage) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withProgressPercentage(progressPercentage);
        return this;
    }

    /**
     * Get the notifyMessage property: Brief message with instructions for updates of AvailabilityType Notify.
     * 
     * @return the notifyMessage value.
     */
    public String notifyMessage() {
        return this.innerProperties() == null ? null : this.innerProperties().notifyMessage();
    }

    /**
     * Set the notifyMessage property: Brief message with instructions for updates of AvailabilityType Notify.
     * 
     * @param notifyMessage the notifyMessage value to set.
     * @return the HciUpdateInner object itself.
     */
    public HciUpdateInner withNotifyMessage(String notifyMessage) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateProperties();
        }
        this.innerProperties().withNotifyMessage(notifyMessage);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("location", this.location);
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of HciUpdateInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of HciUpdateInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the HciUpdateInner.
     */
    public static HciUpdateInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HciUpdateInner deserializedHciUpdateInner = new HciUpdateInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedHciUpdateInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedHciUpdateInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedHciUpdateInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedHciUpdateInner.location = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedHciUpdateInner.innerProperties = UpdateProperties.fromJson(reader);
                } else if ("systemData".equals(fieldName)) {
                    deserializedHciUpdateInner.systemData = SystemData.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedHciUpdateInner;
        });
    }
}

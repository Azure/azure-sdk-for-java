// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.containerservicefleet.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.ProxyResource;
import com.azure.core.management.SystemData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.containerservicefleet.models.ManagedClusterUpdate;
import com.azure.resourcemanager.containerservicefleet.models.UpdateRunProvisioningState;
import com.azure.resourcemanager.containerservicefleet.models.UpdateRunStatus;
import com.azure.resourcemanager.containerservicefleet.models.UpdateRunStrategy;
import java.io.IOException;

/**
 * A multi-stage process to perform update operations across members of a Fleet.
 */
@Fluent
public final class UpdateRunInner extends ProxyResource {
    /*
     * The resource-specific properties for this resource.
     */
    private UpdateRunProperties innerProperties;

    /*
     * If eTag is provided in the response body, it may also be provided as a header per the normal etag convention.
     * Entity tags are used for comparing two or more entities from the same requested resource. HTTP/1.1 uses entity
     * tags in the etag (section 14.19), If-Match (section 14.24), If-None-Match (section 14.26), and If-Range (section
     * 14.27) header fields.
     */
    private String etag;

    /*
     * Azure Resource Manager metadata containing createdBy and modifiedBy information.
     */
    private SystemData systemData;

    /*
     * The type of the resource.
     */
    private String type;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of UpdateRunInner class.
     */
    public UpdateRunInner() {
    }

    /**
     * Get the innerProperties property: The resource-specific properties for this resource.
     * 
     * @return the innerProperties value.
     */
    private UpdateRunProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the etag property: If eTag is provided in the response body, it may also be provided as a header per the
     * normal etag convention. Entity tags are used for comparing two or more entities from the same requested resource.
     * HTTP/1.1 uses entity tags in the etag (section 14.19), If-Match (section 14.24), If-None-Match (section 14.26),
     * and If-Range (section 14.27) header fields.
     * 
     * @return the etag value.
     */
    public String etag() {
        return this.etag;
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
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
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
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * Get the provisioningState property: The provisioning state of the UpdateRun resource.
     * 
     * @return the provisioningState value.
     */
    public UpdateRunProvisioningState provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the updateStrategyId property: The resource id of the FleetUpdateStrategy resource to reference.
     * 
     * When creating a new run, there are three ways to define a strategy for the run:
     * 1. Define a new strategy in place: Set the "strategy" field.
     * 2. Use an existing strategy: Set the "updateStrategyId" field. (since 2023-08-15-preview)
     * 3. Use the default strategy to update all the members one by one: Leave both "updateStrategyId" and "strategy"
     * unset. (since 2023-08-15-preview)
     * 
     * Setting both "updateStrategyId" and "strategy" is invalid.
     * 
     * UpdateRuns created by "updateStrategyId" snapshot the referenced UpdateStrategy at the time of creation and store
     * it in the "strategy" field.
     * Subsequent changes to the referenced FleetUpdateStrategy resource do not propagate.
     * UpdateRunStrategy changes can be made directly on the "strategy" field before launching the UpdateRun.
     * 
     * @return the updateStrategyId value.
     */
    public String updateStrategyId() {
        return this.innerProperties() == null ? null : this.innerProperties().updateStrategyId();
    }

    /**
     * Set the updateStrategyId property: The resource id of the FleetUpdateStrategy resource to reference.
     * 
     * When creating a new run, there are three ways to define a strategy for the run:
     * 1. Define a new strategy in place: Set the "strategy" field.
     * 2. Use an existing strategy: Set the "updateStrategyId" field. (since 2023-08-15-preview)
     * 3. Use the default strategy to update all the members one by one: Leave both "updateStrategyId" and "strategy"
     * unset. (since 2023-08-15-preview)
     * 
     * Setting both "updateStrategyId" and "strategy" is invalid.
     * 
     * UpdateRuns created by "updateStrategyId" snapshot the referenced UpdateStrategy at the time of creation and store
     * it in the "strategy" field.
     * Subsequent changes to the referenced FleetUpdateStrategy resource do not propagate.
     * UpdateRunStrategy changes can be made directly on the "strategy" field before launching the UpdateRun.
     * 
     * @param updateStrategyId the updateStrategyId value to set.
     * @return the UpdateRunInner object itself.
     */
    public UpdateRunInner withUpdateStrategyId(String updateStrategyId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateRunProperties();
        }
        this.innerProperties().withUpdateStrategyId(updateStrategyId);
        return this;
    }

    /**
     * Get the strategy property: The strategy defines the order in which the clusters will be updated.
     * If not set, all members will be updated sequentially. The UpdateRun status will show a single UpdateStage and a
     * single UpdateGroup targeting all members.
     * The strategy of the UpdateRun can be modified until the run is started.
     * 
     * @return the strategy value.
     */
    public UpdateRunStrategy strategy() {
        return this.innerProperties() == null ? null : this.innerProperties().strategy();
    }

    /**
     * Set the strategy property: The strategy defines the order in which the clusters will be updated.
     * If not set, all members will be updated sequentially. The UpdateRun status will show a single UpdateStage and a
     * single UpdateGroup targeting all members.
     * The strategy of the UpdateRun can be modified until the run is started.
     * 
     * @param strategy the strategy value to set.
     * @return the UpdateRunInner object itself.
     */
    public UpdateRunInner withStrategy(UpdateRunStrategy strategy) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateRunProperties();
        }
        this.innerProperties().withStrategy(strategy);
        return this;
    }

    /**
     * Get the managedClusterUpdate property: The update to be applied to all clusters in the UpdateRun. The
     * managedClusterUpdate can be modified until the run is started.
     * 
     * @return the managedClusterUpdate value.
     */
    public ManagedClusterUpdate managedClusterUpdate() {
        return this.innerProperties() == null ? null : this.innerProperties().managedClusterUpdate();
    }

    /**
     * Set the managedClusterUpdate property: The update to be applied to all clusters in the UpdateRun. The
     * managedClusterUpdate can be modified until the run is started.
     * 
     * @param managedClusterUpdate the managedClusterUpdate value to set.
     * @return the UpdateRunInner object itself.
     */
    public UpdateRunInner withManagedClusterUpdate(ManagedClusterUpdate managedClusterUpdate) {
        if (this.innerProperties() == null) {
            this.innerProperties = new UpdateRunProperties();
        }
        this.innerProperties().withManagedClusterUpdate(managedClusterUpdate);
        return this;
    }

    /**
     * Get the status property: The status of the UpdateRun.
     * 
     * @return the status value.
     */
    public UpdateRunStatus status() {
        return this.innerProperties() == null ? null : this.innerProperties().status();
    }

    /**
     * Get the autoUpgradeProfileId property: AutoUpgradeProfileId is the id of an auto upgrade profile resource.
     * 
     * @return the autoUpgradeProfileId value.
     */
    public String autoUpgradeProfileId() {
        return this.innerProperties() == null ? null : this.innerProperties().autoUpgradeProfileId();
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
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of UpdateRunInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of UpdateRunInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the UpdateRunInner.
     */
    public static UpdateRunInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UpdateRunInner deserializedUpdateRunInner = new UpdateRunInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedUpdateRunInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedUpdateRunInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedUpdateRunInner.type = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedUpdateRunInner.innerProperties = UpdateRunProperties.fromJson(reader);
                } else if ("eTag".equals(fieldName)) {
                    deserializedUpdateRunInner.etag = reader.getString();
                } else if ("systemData".equals(fieldName)) {
                    deserializedUpdateRunInner.systemData = SystemData.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedUpdateRunInner;
        });
    }
}

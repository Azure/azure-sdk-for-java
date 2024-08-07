// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Deployment resource properties payload.
 */
@Fluent
public final class DeploymentResourceProperties implements JsonSerializable<DeploymentResourceProperties> {
    /*
     * Uploaded source information of the deployment.
     */
    private UserSourceInfo source;

    /*
     * Deployment settings of the Deployment
     */
    private DeploymentSettings deploymentSettings;

    /*
     * Provisioning state of the Deployment
     */
    private DeploymentResourceProvisioningState provisioningState;

    /*
     * Status of the Deployment
     */
    private DeploymentResourceStatus status;

    /*
     * Indicates whether the Deployment is active
     */
    private Boolean active;

    /*
     * Collection of instances belong to the Deployment
     */
    private List<DeploymentInstance> instances;

    /**
     * Creates an instance of DeploymentResourceProperties class.
     */
    public DeploymentResourceProperties() {
    }

    /**
     * Get the source property: Uploaded source information of the deployment.
     * 
     * @return the source value.
     */
    public UserSourceInfo source() {
        return this.source;
    }

    /**
     * Set the source property: Uploaded source information of the deployment.
     * 
     * @param source the source value to set.
     * @return the DeploymentResourceProperties object itself.
     */
    public DeploymentResourceProperties withSource(UserSourceInfo source) {
        this.source = source;
        return this;
    }

    /**
     * Get the deploymentSettings property: Deployment settings of the Deployment.
     * 
     * @return the deploymentSettings value.
     */
    public DeploymentSettings deploymentSettings() {
        return this.deploymentSettings;
    }

    /**
     * Set the deploymentSettings property: Deployment settings of the Deployment.
     * 
     * @param deploymentSettings the deploymentSettings value to set.
     * @return the DeploymentResourceProperties object itself.
     */
    public DeploymentResourceProperties withDeploymentSettings(DeploymentSettings deploymentSettings) {
        this.deploymentSettings = deploymentSettings;
        return this;
    }

    /**
     * Get the provisioningState property: Provisioning state of the Deployment.
     * 
     * @return the provisioningState value.
     */
    public DeploymentResourceProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the status property: Status of the Deployment.
     * 
     * @return the status value.
     */
    public DeploymentResourceStatus status() {
        return this.status;
    }

    /**
     * Get the active property: Indicates whether the Deployment is active.
     * 
     * @return the active value.
     */
    public Boolean active() {
        return this.active;
    }

    /**
     * Set the active property: Indicates whether the Deployment is active.
     * 
     * @param active the active value to set.
     * @return the DeploymentResourceProperties object itself.
     */
    public DeploymentResourceProperties withActive(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * Get the instances property: Collection of instances belong to the Deployment.
     * 
     * @return the instances value.
     */
    public List<DeploymentInstance> instances() {
        return this.instances;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (source() != null) {
            source().validate();
        }
        if (deploymentSettings() != null) {
            deploymentSettings().validate();
        }
        if (instances() != null) {
            instances().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("source", this.source);
        jsonWriter.writeJsonField("deploymentSettings", this.deploymentSettings);
        jsonWriter.writeBooleanField("active", this.active);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DeploymentResourceProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DeploymentResourceProperties if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DeploymentResourceProperties.
     */
    public static DeploymentResourceProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DeploymentResourceProperties deserializedDeploymentResourceProperties = new DeploymentResourceProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("source".equals(fieldName)) {
                    deserializedDeploymentResourceProperties.source = UserSourceInfo.fromJson(reader);
                } else if ("deploymentSettings".equals(fieldName)) {
                    deserializedDeploymentResourceProperties.deploymentSettings = DeploymentSettings.fromJson(reader);
                } else if ("provisioningState".equals(fieldName)) {
                    deserializedDeploymentResourceProperties.provisioningState
                        = DeploymentResourceProvisioningState.fromString(reader.getString());
                } else if ("status".equals(fieldName)) {
                    deserializedDeploymentResourceProperties.status
                        = DeploymentResourceStatus.fromString(reader.getString());
                } else if ("active".equals(fieldName)) {
                    deserializedDeploymentResourceProperties.active = reader.getNullable(JsonReader::getBoolean);
                } else if ("instances".equals(fieldName)) {
                    List<DeploymentInstance> instances
                        = reader.readArray(reader1 -> DeploymentInstance.fromJson(reader1));
                    deserializedDeploymentResourceProperties.instances = instances;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDeploymentResourceProperties;
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.containerregistry.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.ProxyResource;
import com.azure.core.management.SystemData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.containerregistry.models.IdentityProperties;
import com.azure.resourcemanager.containerregistry.models.ProvisioningState;
import com.azure.resourcemanager.containerregistry.models.RunRequest;
import java.io.IOException;

/**
 * The task run that has the ARM resource and properties.
 * The task run will have the information of request and result of a run.
 */
@Fluent
public final class TaskRunInner extends ProxyResource {
    /*
     * Identity for the resource.
     */
    private IdentityProperties identity;

    /*
     * The properties associated with the task run, i.e., request and result of the run
     */
    private TaskRunPropertiesInner innerProperties;

    /*
     * The location of the resource
     */
    private String location;

    /*
     * Metadata pertaining to creation and last modification of the resource.
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
     * Creates an instance of TaskRunInner class.
     */
    public TaskRunInner() {
    }

    /**
     * Get the identity property: Identity for the resource.
     * 
     * @return the identity value.
     */
    public IdentityProperties identity() {
        return this.identity;
    }

    /**
     * Set the identity property: Identity for the resource.
     * 
     * @param identity the identity value to set.
     * @return the TaskRunInner object itself.
     */
    public TaskRunInner withIdentity(IdentityProperties identity) {
        this.identity = identity;
        return this;
    }

    /**
     * Get the innerProperties property: The properties associated with the task run, i.e., request and result of the
     * run.
     * 
     * @return the innerProperties value.
     */
    private TaskRunPropertiesInner innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the location property: The location of the resource.
     * 
     * @return the location value.
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location property: The location of the resource.
     * 
     * @param location the location value to set.
     * @return the TaskRunInner object itself.
     */
    public TaskRunInner withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the systemData property: Metadata pertaining to creation and last modification of the resource.
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
     * Get the provisioningState property: The provisioning state of this task run.
     * 
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the runRequest property: The request (parameters) for the run.
     * 
     * @return the runRequest value.
     */
    public RunRequest runRequest() {
        return this.innerProperties() == null ? null : this.innerProperties().runRequest();
    }

    /**
     * Set the runRequest property: The request (parameters) for the run.
     * 
     * @param runRequest the runRequest value to set.
     * @return the TaskRunInner object itself.
     */
    public TaskRunInner withRunRequest(RunRequest runRequest) {
        if (this.innerProperties() == null) {
            this.innerProperties = new TaskRunPropertiesInner();
        }
        this.innerProperties().withRunRequest(runRequest);
        return this;
    }

    /**
     * Get the runResult property: The result of this task run.
     * 
     * @return the runResult value.
     */
    public RunInner runResult() {
        return this.innerProperties() == null ? null : this.innerProperties().runResult();
    }

    /**
     * Get the forceUpdateTag property: How the run should be forced to rerun even if the run request configuration has
     * not changed.
     * 
     * @return the forceUpdateTag value.
     */
    public String forceUpdateTag() {
        return this.innerProperties() == null ? null : this.innerProperties().forceUpdateTag();
    }

    /**
     * Set the forceUpdateTag property: How the run should be forced to rerun even if the run request configuration has
     * not changed.
     * 
     * @param forceUpdateTag the forceUpdateTag value to set.
     * @return the TaskRunInner object itself.
     */
    public TaskRunInner withForceUpdateTag(String forceUpdateTag) {
        if (this.innerProperties() == null) {
            this.innerProperties = new TaskRunPropertiesInner();
        }
        this.innerProperties().withForceUpdateTag(forceUpdateTag);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (identity() != null) {
            identity().validate();
        }
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
        jsonWriter.writeJsonField("identity", this.identity);
        jsonWriter.writeJsonField("properties", this.innerProperties);
        jsonWriter.writeStringField("location", this.location);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TaskRunInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of TaskRunInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TaskRunInner.
     */
    public static TaskRunInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TaskRunInner deserializedTaskRunInner = new TaskRunInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedTaskRunInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedTaskRunInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedTaskRunInner.type = reader.getString();
                } else if ("identity".equals(fieldName)) {
                    deserializedTaskRunInner.identity = IdentityProperties.fromJson(reader);
                } else if ("properties".equals(fieldName)) {
                    deserializedTaskRunInner.innerProperties = TaskRunPropertiesInner.fromJson(reader);
                } else if ("location".equals(fieldName)) {
                    deserializedTaskRunInner.location = reader.getString();
                } else if ("systemData".equals(fieldName)) {
                    deserializedTaskRunInner.systemData = SystemData.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedTaskRunInner;
        });
    }
}

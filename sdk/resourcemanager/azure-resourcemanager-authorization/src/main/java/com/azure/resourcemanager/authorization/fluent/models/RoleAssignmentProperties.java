// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.authorization.models.PrincipalType;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Role assignment properties.
 */
@Fluent
public final class RoleAssignmentProperties implements JsonSerializable<RoleAssignmentProperties> {
    /*
     * The role assignment scope.
     */
    private String scope;

    /*
     * The role definition ID.
     */
    private String roleDefinitionId;

    /*
     * The principal ID.
     */
    private String principalId;

    /*
     * The principal type of the assigned principal ID.
     */
    private PrincipalType principalType;

    /*
     * Description of role assignment
     */
    private String description;

    /*
     * The conditions on the role assignment. This limits the resources it can be assigned to.
     * e.g.: @Resource[Microsoft.Storage/storageAccounts/blobServices/containers:ContainerName] StringEqualsIgnoreCase
     * 'foo_storage_container'
     */
    private String condition;

    /*
     * Version of the condition. Currently the only accepted value is '2.0'
     */
    private String conditionVersion;

    /*
     * Time it was created
     */
    private OffsetDateTime createdOn;

    /*
     * Time it was updated
     */
    private OffsetDateTime updatedOn;

    /*
     * Id of the user who created the assignment
     */
    private String createdBy;

    /*
     * Id of the user who updated the assignment
     */
    private String updatedBy;

    /*
     * Id of the delegated managed identity resource
     */
    private String delegatedManagedIdentityResourceId;

    /**
     * Creates an instance of RoleAssignmentProperties class.
     */
    public RoleAssignmentProperties() {
    }

    /**
     * Get the scope property: The role assignment scope.
     * 
     * @return the scope value.
     */
    public String scope() {
        return this.scope;
    }

    /**
     * Get the roleDefinitionId property: The role definition ID.
     * 
     * @return the roleDefinitionId value.
     */
    public String roleDefinitionId() {
        return this.roleDefinitionId;
    }

    /**
     * Set the roleDefinitionId property: The role definition ID.
     * 
     * @param roleDefinitionId the roleDefinitionId value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withRoleDefinitionId(String roleDefinitionId) {
        this.roleDefinitionId = roleDefinitionId;
        return this;
    }

    /**
     * Get the principalId property: The principal ID.
     * 
     * @return the principalId value.
     */
    public String principalId() {
        return this.principalId;
    }

    /**
     * Set the principalId property: The principal ID.
     * 
     * @param principalId the principalId value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withPrincipalId(String principalId) {
        this.principalId = principalId;
        return this;
    }

    /**
     * Get the principalType property: The principal type of the assigned principal ID.
     * 
     * @return the principalType value.
     */
    public PrincipalType principalType() {
        return this.principalType;
    }

    /**
     * Set the principalType property: The principal type of the assigned principal ID.
     * 
     * @param principalType the principalType value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withPrincipalType(PrincipalType principalType) {
        this.principalType = principalType;
        return this;
    }

    /**
     * Get the description property: Description of role assignment.
     * 
     * @return the description value.
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description property: Description of role assignment.
     * 
     * @param description the description value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the condition property: The conditions on the role assignment. This limits the resources it can be assigned
     * to. e.g.: &#064;Resource[Microsoft.Storage/storageAccounts/blobServices/containers:ContainerName]
     * StringEqualsIgnoreCase 'foo_storage_container'.
     * 
     * @return the condition value.
     */
    public String condition() {
        return this.condition;
    }

    /**
     * Set the condition property: The conditions on the role assignment. This limits the resources it can be assigned
     * to. e.g.: &#064;Resource[Microsoft.Storage/storageAccounts/blobServices/containers:ContainerName]
     * StringEqualsIgnoreCase 'foo_storage_container'.
     * 
     * @param condition the condition value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withCondition(String condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Get the conditionVersion property: Version of the condition. Currently the only accepted value is '2.0'.
     * 
     * @return the conditionVersion value.
     */
    public String conditionVersion() {
        return this.conditionVersion;
    }

    /**
     * Set the conditionVersion property: Version of the condition. Currently the only accepted value is '2.0'.
     * 
     * @param conditionVersion the conditionVersion value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withConditionVersion(String conditionVersion) {
        this.conditionVersion = conditionVersion;
        return this;
    }

    /**
     * Get the createdOn property: Time it was created.
     * 
     * @return the createdOn value.
     */
    public OffsetDateTime createdOn() {
        return this.createdOn;
    }

    /**
     * Get the updatedOn property: Time it was updated.
     * 
     * @return the updatedOn value.
     */
    public OffsetDateTime updatedOn() {
        return this.updatedOn;
    }

    /**
     * Get the createdBy property: Id of the user who created the assignment.
     * 
     * @return the createdBy value.
     */
    public String createdBy() {
        return this.createdBy;
    }

    /**
     * Get the updatedBy property: Id of the user who updated the assignment.
     * 
     * @return the updatedBy value.
     */
    public String updatedBy() {
        return this.updatedBy;
    }

    /**
     * Get the delegatedManagedIdentityResourceId property: Id of the delegated managed identity resource.
     * 
     * @return the delegatedManagedIdentityResourceId value.
     */
    public String delegatedManagedIdentityResourceId() {
        return this.delegatedManagedIdentityResourceId;
    }

    /**
     * Set the delegatedManagedIdentityResourceId property: Id of the delegated managed identity resource.
     * 
     * @param delegatedManagedIdentityResourceId the delegatedManagedIdentityResourceId value to set.
     * @return the RoleAssignmentProperties object itself.
     */
    public RoleAssignmentProperties withDelegatedManagedIdentityResourceId(String delegatedManagedIdentityResourceId) {
        this.delegatedManagedIdentityResourceId = delegatedManagedIdentityResourceId;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (roleDefinitionId() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property roleDefinitionId in model RoleAssignmentProperties"));
        }
        if (principalId() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property principalId in model RoleAssignmentProperties"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(RoleAssignmentProperties.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("roleDefinitionId", this.roleDefinitionId);
        jsonWriter.writeStringField("principalId", this.principalId);
        jsonWriter.writeStringField("principalType", this.principalType == null ? null : this.principalType.toString());
        jsonWriter.writeStringField("description", this.description);
        jsonWriter.writeStringField("condition", this.condition);
        jsonWriter.writeStringField("conditionVersion", this.conditionVersion);
        jsonWriter.writeStringField("delegatedManagedIdentityResourceId", this.delegatedManagedIdentityResourceId);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RoleAssignmentProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RoleAssignmentProperties if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the RoleAssignmentProperties.
     */
    public static RoleAssignmentProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RoleAssignmentProperties deserializedRoleAssignmentProperties = new RoleAssignmentProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("roleDefinitionId".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.roleDefinitionId = reader.getString();
                } else if ("principalId".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.principalId = reader.getString();
                } else if ("scope".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.scope = reader.getString();
                } else if ("principalType".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.principalType = PrincipalType.fromString(reader.getString());
                } else if ("description".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.description = reader.getString();
                } else if ("condition".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.condition = reader.getString();
                } else if ("conditionVersion".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.conditionVersion = reader.getString();
                } else if ("createdOn".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.createdOn = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("updatedOn".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.updatedOn = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("createdBy".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.createdBy = reader.getString();
                } else if ("updatedBy".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.updatedBy = reader.getString();
                } else if ("delegatedManagedIdentityResourceId".equals(fieldName)) {
                    deserializedRoleAssignmentProperties.delegatedManagedIdentityResourceId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRoleAssignmentProperties;
        });
    }
}

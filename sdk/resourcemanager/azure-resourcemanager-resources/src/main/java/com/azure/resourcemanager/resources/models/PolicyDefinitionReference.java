// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The policy definition reference.
 */
@Fluent
public final class PolicyDefinitionReference implements JsonSerializable<PolicyDefinitionReference> {
    /*
     * The ID of the policy definition or policy set definition.
     */
    private String policyDefinitionId;

    /*
     * The version of the policy definition to use.
     */
    private String definitionVersion;

    /*
     * The latest version of the policy definition available. This is only present if requested via the $expand query
     * parameter.
     */
    private String latestDefinitionVersion;

    /*
     * The effective version of the policy definition in use. This is only present if requested via the $expand query
     * parameter.
     */
    private String effectiveDefinitionVersion;

    /*
     * The parameter values for the referenced policy rule. The keys are the parameter names.
     */
    private Map<String, ParameterValuesValue> parameters;

    /*
     * A unique id (within the policy set definition) for this policy definition reference.
     */
    private String policyDefinitionReferenceId;

    /*
     * The name of the groups that this policy definition reference belongs to.
     */
    private List<String> groupNames;

    /**
     * Creates an instance of PolicyDefinitionReference class.
     */
    public PolicyDefinitionReference() {
    }

    /**
     * Get the policyDefinitionId property: The ID of the policy definition or policy set definition.
     * 
     * @return the policyDefinitionId value.
     */
    public String policyDefinitionId() {
        return this.policyDefinitionId;
    }

    /**
     * Set the policyDefinitionId property: The ID of the policy definition or policy set definition.
     * 
     * @param policyDefinitionId the policyDefinitionId value to set.
     * @return the PolicyDefinitionReference object itself.
     */
    public PolicyDefinitionReference withPolicyDefinitionId(String policyDefinitionId) {
        this.policyDefinitionId = policyDefinitionId;
        return this;
    }

    /**
     * Get the definitionVersion property: The version of the policy definition to use.
     * 
     * @return the definitionVersion value.
     */
    public String definitionVersion() {
        return this.definitionVersion;
    }

    /**
     * Set the definitionVersion property: The version of the policy definition to use.
     * 
     * @param definitionVersion the definitionVersion value to set.
     * @return the PolicyDefinitionReference object itself.
     */
    public PolicyDefinitionReference withDefinitionVersion(String definitionVersion) {
        this.definitionVersion = definitionVersion;
        return this;
    }

    /**
     * Get the latestDefinitionVersion property: The latest version of the policy definition available. This is only
     * present if requested via the $expand query parameter.
     * 
     * @return the latestDefinitionVersion value.
     */
    public String latestDefinitionVersion() {
        return this.latestDefinitionVersion;
    }

    /**
     * Get the effectiveDefinitionVersion property: The effective version of the policy definition in use. This is only
     * present if requested via the $expand query parameter.
     * 
     * @return the effectiveDefinitionVersion value.
     */
    public String effectiveDefinitionVersion() {
        return this.effectiveDefinitionVersion;
    }

    /**
     * Get the parameters property: The parameter values for the referenced policy rule. The keys are the parameter
     * names.
     * 
     * @return the parameters value.
     */
    public Map<String, ParameterValuesValue> parameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameter values for the referenced policy rule. The keys are the parameter
     * names.
     * 
     * @param parameters the parameters value to set.
     * @return the PolicyDefinitionReference object itself.
     */
    public PolicyDefinitionReference withParameters(Map<String, ParameterValuesValue> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Get the policyDefinitionReferenceId property: A unique id (within the policy set definition) for this policy
     * definition reference.
     * 
     * @return the policyDefinitionReferenceId value.
     */
    public String policyDefinitionReferenceId() {
        return this.policyDefinitionReferenceId;
    }

    /**
     * Set the policyDefinitionReferenceId property: A unique id (within the policy set definition) for this policy
     * definition reference.
     * 
     * @param policyDefinitionReferenceId the policyDefinitionReferenceId value to set.
     * @return the PolicyDefinitionReference object itself.
     */
    public PolicyDefinitionReference withPolicyDefinitionReferenceId(String policyDefinitionReferenceId) {
        this.policyDefinitionReferenceId = policyDefinitionReferenceId;
        return this;
    }

    /**
     * Get the groupNames property: The name of the groups that this policy definition reference belongs to.
     * 
     * @return the groupNames value.
     */
    public List<String> groupNames() {
        return this.groupNames;
    }

    /**
     * Set the groupNames property: The name of the groups that this policy definition reference belongs to.
     * 
     * @param groupNames the groupNames value to set.
     * @return the PolicyDefinitionReference object itself.
     */
    public PolicyDefinitionReference withGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (policyDefinitionId() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property policyDefinitionId in model PolicyDefinitionReference"));
        }
        if (parameters() != null) {
            parameters().values().forEach(e -> {
                if (e != null) {
                    e.validate();
                }
            });
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(PolicyDefinitionReference.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("policyDefinitionId", this.policyDefinitionId);
        jsonWriter.writeStringField("definitionVersion", this.definitionVersion);
        jsonWriter.writeMapField("parameters", this.parameters, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("policyDefinitionReferenceId", this.policyDefinitionReferenceId);
        jsonWriter.writeArrayField("groupNames", this.groupNames, (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PolicyDefinitionReference from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PolicyDefinitionReference if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the PolicyDefinitionReference.
     */
    public static PolicyDefinitionReference fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PolicyDefinitionReference deserializedPolicyDefinitionReference = new PolicyDefinitionReference();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("policyDefinitionId".equals(fieldName)) {
                    deserializedPolicyDefinitionReference.policyDefinitionId = reader.getString();
                } else if ("definitionVersion".equals(fieldName)) {
                    deserializedPolicyDefinitionReference.definitionVersion = reader.getString();
                } else if ("latestDefinitionVersion".equals(fieldName)) {
                    deserializedPolicyDefinitionReference.latestDefinitionVersion = reader.getString();
                } else if ("effectiveDefinitionVersion".equals(fieldName)) {
                    deserializedPolicyDefinitionReference.effectiveDefinitionVersion = reader.getString();
                } else if ("parameters".equals(fieldName)) {
                    Map<String, ParameterValuesValue> parameters
                        = reader.readMap(reader1 -> ParameterValuesValue.fromJson(reader1));
                    deserializedPolicyDefinitionReference.parameters = parameters;
                } else if ("policyDefinitionReferenceId".equals(fieldName)) {
                    deserializedPolicyDefinitionReference.policyDefinitionReferenceId = reader.getString();
                } else if ("groupNames".equals(fieldName)) {
                    List<String> groupNames = reader.readArray(reader1 -> reader1.getString());
                    deserializedPolicyDefinitionReference.groupNames = groupNames;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPolicyDefinitionReference;
        });
    }
}

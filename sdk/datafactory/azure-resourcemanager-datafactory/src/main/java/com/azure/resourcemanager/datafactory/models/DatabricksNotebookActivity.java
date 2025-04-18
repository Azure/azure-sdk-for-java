// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.datafactory.fluent.models.DatabricksNotebookActivityTypeProperties;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DatabricksNotebook activity.
 */
@Fluent
public final class DatabricksNotebookActivity extends ExecutionActivity {
    /*
     * Type of activity.
     */
    private String type = "DatabricksNotebook";

    /*
     * Databricks Notebook activity properties.
     */
    private DatabricksNotebookActivityTypeProperties innerTypeProperties
        = new DatabricksNotebookActivityTypeProperties();

    /**
     * Creates an instance of DatabricksNotebookActivity class.
     */
    public DatabricksNotebookActivity() {
    }

    /**
     * Get the type property: Type of activity.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the innerTypeProperties property: Databricks Notebook activity properties.
     * 
     * @return the innerTypeProperties value.
     */
    DatabricksNotebookActivityTypeProperties innerTypeProperties() {
        return this.innerTypeProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withLinkedServiceName(LinkedServiceReference linkedServiceName) {
        super.withLinkedServiceName(linkedServiceName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withPolicy(ActivityPolicy policy) {
        super.withPolicy(policy);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withName(String name) {
        super.withName(name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withState(ActivityState state) {
        super.withState(state);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withOnInactiveMarkAs(ActivityOnInactiveMarkAs onInactiveMarkAs) {
        super.withOnInactiveMarkAs(onInactiveMarkAs);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withDependsOn(List<ActivityDependency> dependsOn) {
        super.withDependsOn(dependsOn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatabricksNotebookActivity withUserProperties(List<UserProperty> userProperties) {
        super.withUserProperties(userProperties);
        return this;
    }

    /**
     * Get the notebookPath property: The absolute path of the notebook to be run in the Databricks Workspace. This path
     * must begin with a slash. Type: string (or Expression with resultType string).
     * 
     * @return the notebookPath value.
     */
    public Object notebookPath() {
        return this.innerTypeProperties() == null ? null : this.innerTypeProperties().notebookPath();
    }

    /**
     * Set the notebookPath property: The absolute path of the notebook to be run in the Databricks Workspace. This path
     * must begin with a slash. Type: string (or Expression with resultType string).
     * 
     * @param notebookPath the notebookPath value to set.
     * @return the DatabricksNotebookActivity object itself.
     */
    public DatabricksNotebookActivity withNotebookPath(Object notebookPath) {
        if (this.innerTypeProperties() == null) {
            this.innerTypeProperties = new DatabricksNotebookActivityTypeProperties();
        }
        this.innerTypeProperties().withNotebookPath(notebookPath);
        return this;
    }

    /**
     * Get the baseParameters property: Base parameters to be used for each run of this job.If the notebook takes a
     * parameter that is not specified, the default value from the notebook will be used.
     * 
     * @return the baseParameters value.
     */
    public Map<String, Object> baseParameters() {
        return this.innerTypeProperties() == null ? null : this.innerTypeProperties().baseParameters();
    }

    /**
     * Set the baseParameters property: Base parameters to be used for each run of this job.If the notebook takes a
     * parameter that is not specified, the default value from the notebook will be used.
     * 
     * @param baseParameters the baseParameters value to set.
     * @return the DatabricksNotebookActivity object itself.
     */
    public DatabricksNotebookActivity withBaseParameters(Map<String, Object> baseParameters) {
        if (this.innerTypeProperties() == null) {
            this.innerTypeProperties = new DatabricksNotebookActivityTypeProperties();
        }
        this.innerTypeProperties().withBaseParameters(baseParameters);
        return this;
    }

    /**
     * Get the libraries property: A list of libraries to be installed on the cluster that will execute the job.
     * 
     * @return the libraries value.
     */
    public List<Map<String, Object>> libraries() {
        return this.innerTypeProperties() == null ? null : this.innerTypeProperties().libraries();
    }

    /**
     * Set the libraries property: A list of libraries to be installed on the cluster that will execute the job.
     * 
     * @param libraries the libraries value to set.
     * @return the DatabricksNotebookActivity object itself.
     */
    public DatabricksNotebookActivity withLibraries(List<Map<String, Object>> libraries) {
        if (this.innerTypeProperties() == null) {
            this.innerTypeProperties = new DatabricksNotebookActivityTypeProperties();
        }
        this.innerTypeProperties().withLibraries(libraries);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (innerTypeProperties() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property innerTypeProperties in model DatabricksNotebookActivity"));
        } else {
            innerTypeProperties().validate();
        }
        if (name() == null) {
            throw LOGGER.atError()
                .log(
                    new IllegalArgumentException("Missing required property name in model DatabricksNotebookActivity"));
        }
        if (dependsOn() != null) {
            dependsOn().forEach(e -> e.validate());
        }
        if (userProperties() != null) {
            userProperties().forEach(e -> e.validate());
        }
        if (linkedServiceName() != null) {
            linkedServiceName().validate();
        }
        if (policy() != null) {
            policy().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(DatabricksNotebookActivity.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", name());
        jsonWriter.writeStringField("description", description());
        jsonWriter.writeStringField("state", state() == null ? null : state().toString());
        jsonWriter.writeStringField("onInactiveMarkAs",
            onInactiveMarkAs() == null ? null : onInactiveMarkAs().toString());
        jsonWriter.writeArrayField("dependsOn", dependsOn(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("userProperties", userProperties(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("linkedServiceName", linkedServiceName());
        jsonWriter.writeJsonField("policy", policy());
        jsonWriter.writeJsonField("typeProperties", this.innerTypeProperties);
        jsonWriter.writeStringField("type", this.type);
        if (additionalProperties() != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties().entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DatabricksNotebookActivity from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DatabricksNotebookActivity if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DatabricksNotebookActivity.
     */
    public static DatabricksNotebookActivity fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DatabricksNotebookActivity deserializedDatabricksNotebookActivity = new DatabricksNotebookActivity();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity.withName(reader.getString());
                } else if ("description".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity.withDescription(reader.getString());
                } else if ("state".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity.withState(ActivityState.fromString(reader.getString()));
                } else if ("onInactiveMarkAs".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity
                        .withOnInactiveMarkAs(ActivityOnInactiveMarkAs.fromString(reader.getString()));
                } else if ("dependsOn".equals(fieldName)) {
                    List<ActivityDependency> dependsOn
                        = reader.readArray(reader1 -> ActivityDependency.fromJson(reader1));
                    deserializedDatabricksNotebookActivity.withDependsOn(dependsOn);
                } else if ("userProperties".equals(fieldName)) {
                    List<UserProperty> userProperties = reader.readArray(reader1 -> UserProperty.fromJson(reader1));
                    deserializedDatabricksNotebookActivity.withUserProperties(userProperties);
                } else if ("linkedServiceName".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity
                        .withLinkedServiceName(LinkedServiceReference.fromJson(reader));
                } else if ("policy".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity.withPolicy(ActivityPolicy.fromJson(reader));
                } else if ("typeProperties".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity.innerTypeProperties
                        = DatabricksNotebookActivityTypeProperties.fromJson(reader);
                } else if ("type".equals(fieldName)) {
                    deserializedDatabricksNotebookActivity.type = reader.getString();
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedDatabricksNotebookActivity.withAdditionalProperties(additionalProperties);

            return deserializedDatabricksNotebookActivity;
        });
    }
}

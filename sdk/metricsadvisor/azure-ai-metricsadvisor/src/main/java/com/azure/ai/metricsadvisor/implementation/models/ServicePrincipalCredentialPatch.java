// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.ai.metricsadvisor.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ServicePrincipalCredentialPatch model.
 */
@Fluent
public final class ServicePrincipalCredentialPatch extends DataSourceCredentialPatch {
    /*
     * Type of data source credential
     */
    private DataSourceCredentialType dataSourceCredentialType = DataSourceCredentialType.SERVICE_PRINCIPAL;

    /*
     * The parameters property.
     */
    private ServicePrincipalParamPatch parameters;

    /**
     * Creates an instance of ServicePrincipalCredentialPatch class.
     */
    public ServicePrincipalCredentialPatch() {
    }

    /**
     * Get the dataSourceCredentialType property: Type of data source credential.
     * 
     * @return the dataSourceCredentialType value.
     */
    @Override
    public DataSourceCredentialType getDataSourceCredentialType() {
        return this.dataSourceCredentialType;
    }

    /**
     * Get the parameters property: The parameters property.
     * 
     * @return the parameters value.
     */
    public ServicePrincipalParamPatch getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameters property.
     * 
     * @param parameters the parameters value to set.
     * @return the ServicePrincipalCredentialPatch object itself.
     */
    public ServicePrincipalCredentialPatch setParameters(ServicePrincipalParamPatch parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServicePrincipalCredentialPatch setDataSourceCredentialName(String dataSourceCredentialName) {
        super.setDataSourceCredentialName(dataSourceCredentialName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServicePrincipalCredentialPatch setDataSourceCredentialDescription(String dataSourceCredentialDescription) {
        super.setDataSourceCredentialDescription(dataSourceCredentialDescription);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("dataSourceCredentialName", getDataSourceCredentialName());
        jsonWriter.writeStringField("dataSourceCredentialDescription", getDataSourceCredentialDescription());
        jsonWriter.writeStringField("dataSourceCredentialType",
            this.dataSourceCredentialType == null ? null : this.dataSourceCredentialType.toString());
        jsonWriter.writeJsonField("parameters", this.parameters);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ServicePrincipalCredentialPatch from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ServicePrincipalCredentialPatch if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ServicePrincipalCredentialPatch.
     */
    public static ServicePrincipalCredentialPatch fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ServicePrincipalCredentialPatch deserializedServicePrincipalCredentialPatch
                = new ServicePrincipalCredentialPatch();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("dataSourceCredentialName".equals(fieldName)) {
                    deserializedServicePrincipalCredentialPatch.setDataSourceCredentialName(reader.getString());
                } else if ("dataSourceCredentialDescription".equals(fieldName)) {
                    deserializedServicePrincipalCredentialPatch.setDataSourceCredentialDescription(reader.getString());
                } else if ("dataSourceCredentialType".equals(fieldName)) {
                    deserializedServicePrincipalCredentialPatch.dataSourceCredentialType
                        = DataSourceCredentialType.fromString(reader.getString());
                } else if ("parameters".equals(fieldName)) {
                    deserializedServicePrincipalCredentialPatch.parameters
                        = ServicePrincipalParamPatch.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedServicePrincipalCredentialPatch;
        });
    }
}

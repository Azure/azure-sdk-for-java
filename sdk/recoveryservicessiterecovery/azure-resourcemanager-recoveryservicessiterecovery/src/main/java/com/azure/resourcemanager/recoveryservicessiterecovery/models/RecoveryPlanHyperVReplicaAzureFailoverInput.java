// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicessiterecovery.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Recovery plan HVR Azure failover input.
 */
@Fluent
public final class RecoveryPlanHyperVReplicaAzureFailoverInput extends RecoveryPlanProviderSpecificFailoverInput {
    /*
     * The class type.
     */
    private String instanceType = "HyperVReplicaAzure";

    /*
     * The primary KEK certificate PFX.
     */
    private String primaryKekCertificatePfx;

    /*
     * The secondary KEK certificate PFX.
     */
    private String secondaryKekCertificatePfx;

    /*
     * The recovery point type.
     */
    private HyperVReplicaAzureRpRecoveryPointType recoveryPointType;

    /**
     * Creates an instance of RecoveryPlanHyperVReplicaAzureFailoverInput class.
     */
    public RecoveryPlanHyperVReplicaAzureFailoverInput() {
    }

    /**
     * Get the instanceType property: The class type.
     * 
     * @return the instanceType value.
     */
    @Override
    public String instanceType() {
        return this.instanceType;
    }

    /**
     * Get the primaryKekCertificatePfx property: The primary KEK certificate PFX.
     * 
     * @return the primaryKekCertificatePfx value.
     */
    public String primaryKekCertificatePfx() {
        return this.primaryKekCertificatePfx;
    }

    /**
     * Set the primaryKekCertificatePfx property: The primary KEK certificate PFX.
     * 
     * @param primaryKekCertificatePfx the primaryKekCertificatePfx value to set.
     * @return the RecoveryPlanHyperVReplicaAzureFailoverInput object itself.
     */
    public RecoveryPlanHyperVReplicaAzureFailoverInput withPrimaryKekCertificatePfx(String primaryKekCertificatePfx) {
        this.primaryKekCertificatePfx = primaryKekCertificatePfx;
        return this;
    }

    /**
     * Get the secondaryKekCertificatePfx property: The secondary KEK certificate PFX.
     * 
     * @return the secondaryKekCertificatePfx value.
     */
    public String secondaryKekCertificatePfx() {
        return this.secondaryKekCertificatePfx;
    }

    /**
     * Set the secondaryKekCertificatePfx property: The secondary KEK certificate PFX.
     * 
     * @param secondaryKekCertificatePfx the secondaryKekCertificatePfx value to set.
     * @return the RecoveryPlanHyperVReplicaAzureFailoverInput object itself.
     */
    public RecoveryPlanHyperVReplicaAzureFailoverInput
        withSecondaryKekCertificatePfx(String secondaryKekCertificatePfx) {
        this.secondaryKekCertificatePfx = secondaryKekCertificatePfx;
        return this;
    }

    /**
     * Get the recoveryPointType property: The recovery point type.
     * 
     * @return the recoveryPointType value.
     */
    public HyperVReplicaAzureRpRecoveryPointType recoveryPointType() {
        return this.recoveryPointType;
    }

    /**
     * Set the recoveryPointType property: The recovery point type.
     * 
     * @param recoveryPointType the recoveryPointType value to set.
     * @return the RecoveryPlanHyperVReplicaAzureFailoverInput object itself.
     */
    public RecoveryPlanHyperVReplicaAzureFailoverInput
        withRecoveryPointType(HyperVReplicaAzureRpRecoveryPointType recoveryPointType) {
        this.recoveryPointType = recoveryPointType;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("instanceType", this.instanceType);
        jsonWriter.writeStringField("primaryKekCertificatePfx", this.primaryKekCertificatePfx);
        jsonWriter.writeStringField("secondaryKekCertificatePfx", this.secondaryKekCertificatePfx);
        jsonWriter.writeStringField("recoveryPointType",
            this.recoveryPointType == null ? null : this.recoveryPointType.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RecoveryPlanHyperVReplicaAzureFailoverInput from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecoveryPlanHyperVReplicaAzureFailoverInput if the JsonReader was pointing to an instance
     * of it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecoveryPlanHyperVReplicaAzureFailoverInput.
     */
    public static RecoveryPlanHyperVReplicaAzureFailoverInput fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RecoveryPlanHyperVReplicaAzureFailoverInput deserializedRecoveryPlanHyperVReplicaAzureFailoverInput
                = new RecoveryPlanHyperVReplicaAzureFailoverInput();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("instanceType".equals(fieldName)) {
                    deserializedRecoveryPlanHyperVReplicaAzureFailoverInput.instanceType = reader.getString();
                } else if ("primaryKekCertificatePfx".equals(fieldName)) {
                    deserializedRecoveryPlanHyperVReplicaAzureFailoverInput.primaryKekCertificatePfx
                        = reader.getString();
                } else if ("secondaryKekCertificatePfx".equals(fieldName)) {
                    deserializedRecoveryPlanHyperVReplicaAzureFailoverInput.secondaryKekCertificatePfx
                        = reader.getString();
                } else if ("recoveryPointType".equals(fieldName)) {
                    deserializedRecoveryPlanHyperVReplicaAzureFailoverInput.recoveryPointType
                        = HyperVReplicaAzureRpRecoveryPointType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRecoveryPlanHyperVReplicaAzureFailoverInput;
        });
    }
}

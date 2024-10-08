// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Regression Training related configuration.
 */
@Fluent
public final class RegressionTrainingSettings extends TrainingSettings {
    /*
     * Allowed models for regression task.
     */
    private List<RegressionModels> allowedTrainingAlgorithms;

    /*
     * Blocked models for regression task.
     */
    private List<RegressionModels> blockedTrainingAlgorithms;

    /**
     * Creates an instance of RegressionTrainingSettings class.
     */
    public RegressionTrainingSettings() {
    }

    /**
     * Get the allowedTrainingAlgorithms property: Allowed models for regression task.
     * 
     * @return the allowedTrainingAlgorithms value.
     */
    public List<RegressionModels> allowedTrainingAlgorithms() {
        return this.allowedTrainingAlgorithms;
    }

    /**
     * Set the allowedTrainingAlgorithms property: Allowed models for regression task.
     * 
     * @param allowedTrainingAlgorithms the allowedTrainingAlgorithms value to set.
     * @return the RegressionTrainingSettings object itself.
     */
    public RegressionTrainingSettings withAllowedTrainingAlgorithms(List<RegressionModels> allowedTrainingAlgorithms) {
        this.allowedTrainingAlgorithms = allowedTrainingAlgorithms;
        return this;
    }

    /**
     * Get the blockedTrainingAlgorithms property: Blocked models for regression task.
     * 
     * @return the blockedTrainingAlgorithms value.
     */
    public List<RegressionModels> blockedTrainingAlgorithms() {
        return this.blockedTrainingAlgorithms;
    }

    /**
     * Set the blockedTrainingAlgorithms property: Blocked models for regression task.
     * 
     * @param blockedTrainingAlgorithms the blockedTrainingAlgorithms value to set.
     * @return the RegressionTrainingSettings object itself.
     */
    public RegressionTrainingSettings withBlockedTrainingAlgorithms(List<RegressionModels> blockedTrainingAlgorithms) {
        this.blockedTrainingAlgorithms = blockedTrainingAlgorithms;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withEnableOnnxCompatibleModels(Boolean enableOnnxCompatibleModels) {
        super.withEnableOnnxCompatibleModels(enableOnnxCompatibleModels);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withStackEnsembleSettings(StackEnsembleSettings stackEnsembleSettings) {
        super.withStackEnsembleSettings(stackEnsembleSettings);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withEnableStackEnsemble(Boolean enableStackEnsemble) {
        super.withEnableStackEnsemble(enableStackEnsemble);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withEnableVoteEnsemble(Boolean enableVoteEnsemble) {
        super.withEnableVoteEnsemble(enableVoteEnsemble);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withEnsembleModelDownloadTimeout(Duration ensembleModelDownloadTimeout) {
        super.withEnsembleModelDownloadTimeout(ensembleModelDownloadTimeout);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withEnableModelExplainability(Boolean enableModelExplainability) {
        super.withEnableModelExplainability(enableModelExplainability);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegressionTrainingSettings withEnableDnnTraining(Boolean enableDnnTraining) {
        super.withEnableDnnTraining(enableDnnTraining);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeBooleanField("enableOnnxCompatibleModels", enableOnnxCompatibleModels());
        jsonWriter.writeJsonField("stackEnsembleSettings", stackEnsembleSettings());
        jsonWriter.writeBooleanField("enableStackEnsemble", enableStackEnsemble());
        jsonWriter.writeBooleanField("enableVoteEnsemble", enableVoteEnsemble());
        jsonWriter.writeStringField("ensembleModelDownloadTimeout",
            CoreUtils.durationToStringWithDays(ensembleModelDownloadTimeout()));
        jsonWriter.writeBooleanField("enableModelExplainability", enableModelExplainability());
        jsonWriter.writeBooleanField("enableDnnTraining", enableDnnTraining());
        jsonWriter.writeArrayField("allowedTrainingAlgorithms", this.allowedTrainingAlgorithms,
            (writer, element) -> writer.writeString(element == null ? null : element.toString()));
        jsonWriter.writeArrayField("blockedTrainingAlgorithms", this.blockedTrainingAlgorithms,
            (writer, element) -> writer.writeString(element == null ? null : element.toString()));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RegressionTrainingSettings from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RegressionTrainingSettings if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RegressionTrainingSettings.
     */
    public static RegressionTrainingSettings fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RegressionTrainingSettings deserializedRegressionTrainingSettings = new RegressionTrainingSettings();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("enableOnnxCompatibleModels".equals(fieldName)) {
                    deserializedRegressionTrainingSettings
                        .withEnableOnnxCompatibleModels(reader.getNullable(JsonReader::getBoolean));
                } else if ("stackEnsembleSettings".equals(fieldName)) {
                    deserializedRegressionTrainingSettings
                        .withStackEnsembleSettings(StackEnsembleSettings.fromJson(reader));
                } else if ("enableStackEnsemble".equals(fieldName)) {
                    deserializedRegressionTrainingSettings
                        .withEnableStackEnsemble(reader.getNullable(JsonReader::getBoolean));
                } else if ("enableVoteEnsemble".equals(fieldName)) {
                    deserializedRegressionTrainingSettings
                        .withEnableVoteEnsemble(reader.getNullable(JsonReader::getBoolean));
                } else if ("ensembleModelDownloadTimeout".equals(fieldName)) {
                    deserializedRegressionTrainingSettings.withEnsembleModelDownloadTimeout(
                        reader.getNullable(nonNullReader -> Duration.parse(nonNullReader.getString())));
                } else if ("enableModelExplainability".equals(fieldName)) {
                    deserializedRegressionTrainingSettings
                        .withEnableModelExplainability(reader.getNullable(JsonReader::getBoolean));
                } else if ("enableDnnTraining".equals(fieldName)) {
                    deserializedRegressionTrainingSettings
                        .withEnableDnnTraining(reader.getNullable(JsonReader::getBoolean));
                } else if ("allowedTrainingAlgorithms".equals(fieldName)) {
                    List<RegressionModels> allowedTrainingAlgorithms
                        = reader.readArray(reader1 -> RegressionModels.fromString(reader1.getString()));
                    deserializedRegressionTrainingSettings.allowedTrainingAlgorithms = allowedTrainingAlgorithms;
                } else if ("blockedTrainingAlgorithms".equals(fieldName)) {
                    List<RegressionModels> blockedTrainingAlgorithms
                        = reader.readArray(reader1 -> RegressionModels.fromString(reader1.getString()));
                    deserializedRegressionTrainingSettings.blockedTrainingAlgorithms = blockedTrainingAlgorithms;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRegressionTrainingSettings;
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/** The set of tasks to execute on the input documents. Cannot specify the same task more than once. */
@Fluent
public final class JobManifestTasks implements JsonSerializable<JobManifestTasks> {
    /*
     * The entityRecognitionTasks property.
     */
    private List<EntitiesTask> entityRecognitionTasks;

    /*
     * The entityRecognitionPiiTasks property.
     */
    private List<PiiTask> entityRecognitionPiiTasks;

    /*
     * The keyPhraseExtractionTasks property.
     */
    private List<KeyPhrasesTask> keyPhraseExtractionTasks;

    /*
     * The entityLinkingTasks property.
     */
    private List<EntityLinkingTask> entityLinkingTasks;

    /*
     * The sentimentAnalysisTasks property.
     */
    private List<SentimentAnalysisTask> sentimentAnalysisTasks;

    /*
     * The extractiveSummarizationTasks property.
     */
    private List<ExtractiveSummarizationTask> extractiveSummarizationTasks;

    /*
     * The customEntityRecognitionTasks property.
     */
    private List<CustomEntitiesTask> customEntityRecognitionTasks;

    /*
     * The customSingleClassificationTasks property.
     */
    private List<CustomSingleClassificationTask> customSingleClassificationTasks;

    /*
     * The customMultiClassificationTasks property.
     */
    private List<CustomMultiClassificationTask> customMultiClassificationTasks;

    /**
     * Get the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @return the entityRecognitionTasks value.
     */
    public List<EntitiesTask> getEntityRecognitionTasks() {
        return this.entityRecognitionTasks;
    }

    /**
     * Set the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @param entityRecognitionTasks the entityRecognitionTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setEntityRecognitionTasks(List<EntitiesTask> entityRecognitionTasks) {
        this.entityRecognitionTasks = entityRecognitionTasks;
        return this;
    }

    /**
     * Get the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @return the entityRecognitionPiiTasks value.
     */
    public List<PiiTask> getEntityRecognitionPiiTasks() {
        return this.entityRecognitionPiiTasks;
    }

    /**
     * Set the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @param entityRecognitionPiiTasks the entityRecognitionPiiTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setEntityRecognitionPiiTasks(List<PiiTask> entityRecognitionPiiTasks) {
        this.entityRecognitionPiiTasks = entityRecognitionPiiTasks;
        return this;
    }

    /**
     * Get the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @return the keyPhraseExtractionTasks value.
     */
    public List<KeyPhrasesTask> getKeyPhraseExtractionTasks() {
        return this.keyPhraseExtractionTasks;
    }

    /**
     * Set the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @param keyPhraseExtractionTasks the keyPhraseExtractionTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setKeyPhraseExtractionTasks(List<KeyPhrasesTask> keyPhraseExtractionTasks) {
        this.keyPhraseExtractionTasks = keyPhraseExtractionTasks;
        return this;
    }

    /**
     * Get the entityLinkingTasks property: The entityLinkingTasks property.
     *
     * @return the entityLinkingTasks value.
     */
    public List<EntityLinkingTask> getEntityLinkingTasks() {
        return this.entityLinkingTasks;
    }

    /**
     * Set the entityLinkingTasks property: The entityLinkingTasks property.
     *
     * @param entityLinkingTasks the entityLinkingTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setEntityLinkingTasks(List<EntityLinkingTask> entityLinkingTasks) {
        this.entityLinkingTasks = entityLinkingTasks;
        return this;
    }

    /**
     * Get the sentimentAnalysisTasks property: The sentimentAnalysisTasks property.
     *
     * @return the sentimentAnalysisTasks value.
     */
    public List<SentimentAnalysisTask> getSentimentAnalysisTasks() {
        return this.sentimentAnalysisTasks;
    }

    /**
     * Set the sentimentAnalysisTasks property: The sentimentAnalysisTasks property.
     *
     * @param sentimentAnalysisTasks the sentimentAnalysisTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setSentimentAnalysisTasks(List<SentimentAnalysisTask> sentimentAnalysisTasks) {
        this.sentimentAnalysisTasks = sentimentAnalysisTasks;
        return this;
    }

    /**
     * Get the extractiveSummarizationTasks property: The extractiveSummarizationTasks property.
     *
     * @return the extractiveSummarizationTasks value.
     */
    public List<ExtractiveSummarizationTask> getExtractiveSummarizationTasks() {
        return this.extractiveSummarizationTasks;
    }

    /**
     * Set the extractiveSummarizationTasks property: The extractiveSummarizationTasks property.
     *
     * @param extractiveSummarizationTasks the extractiveSummarizationTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setExtractiveSummarizationTasks(
            List<ExtractiveSummarizationTask> extractiveSummarizationTasks) {
        this.extractiveSummarizationTasks = extractiveSummarizationTasks;
        return this;
    }

    /**
     * Get the customEntityRecognitionTasks property: The customEntityRecognitionTasks property.
     *
     * @return the customEntityRecognitionTasks value.
     */
    public List<CustomEntitiesTask> getCustomEntityRecognitionTasks() {
        return this.customEntityRecognitionTasks;
    }

    /**
     * Set the customEntityRecognitionTasks property: The customEntityRecognitionTasks property.
     *
     * @param customEntityRecognitionTasks the customEntityRecognitionTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setCustomEntityRecognitionTasks(List<CustomEntitiesTask> customEntityRecognitionTasks) {
        this.customEntityRecognitionTasks = customEntityRecognitionTasks;
        return this;
    }

    /**
     * Get the customSingleClassificationTasks property: The customSingleClassificationTasks property.
     *
     * @return the customSingleClassificationTasks value.
     */
    public List<CustomSingleClassificationTask> getCustomSingleClassificationTasks() {
        return this.customSingleClassificationTasks;
    }

    /**
     * Set the customSingleClassificationTasks property: The customSingleClassificationTasks property.
     *
     * @param customSingleClassificationTasks the customSingleClassificationTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setCustomSingleClassificationTasks(
            List<CustomSingleClassificationTask> customSingleClassificationTasks) {
        this.customSingleClassificationTasks = customSingleClassificationTasks;
        return this;
    }

    /**
     * Get the customMultiClassificationTasks property: The customMultiClassificationTasks property.
     *
     * @return the customMultiClassificationTasks value.
     */
    public List<CustomMultiClassificationTask> getCustomMultiClassificationTasks() {
        return this.customMultiClassificationTasks;
    }

    /**
     * Set the customMultiClassificationTasks property: The customMultiClassificationTasks property.
     *
     * @param customMultiClassificationTasks the customMultiClassificationTasks value to set.
     * @return the JobManifestTasks object itself.
     */
    public JobManifestTasks setCustomMultiClassificationTasks(
            List<CustomMultiClassificationTask> customMultiClassificationTasks) {
        this.customMultiClassificationTasks = customMultiClassificationTasks;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("entityRecognitionTasks", this.entityRecognitionTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("entityRecognitionPiiTasks", this.entityRecognitionPiiTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("keyPhraseExtractionTasks", this.keyPhraseExtractionTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("entityLinkingTasks", this.entityLinkingTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("sentimentAnalysisTasks", this.sentimentAnalysisTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("extractiveSummarizationTasks", this.extractiveSummarizationTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("customEntityRecognitionTasks", this.customEntityRecognitionTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("customSingleClassificationTasks", this.customSingleClassificationTasks, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("customMultiClassificationTasks", this.customMultiClassificationTasks, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of JobManifestTasks from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of JobManifestTasks if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the JobManifestTasks.
     */
    public static JobManifestTasks fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JobManifestTasks deserializedJobManifestTasks = new JobManifestTasks();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("entityRecognitionTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.entityRecognitionTasks =
                            reader.readArray(reader1 -> EntitiesTask.fromJson(reader1));
                } else if ("entityRecognitionPiiTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.entityRecognitionPiiTasks =
                            reader.readArray(reader1 -> PiiTask.fromJson(reader1));
                } else if ("keyPhraseExtractionTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.keyPhraseExtractionTasks =
                            reader.readArray(reader1 -> KeyPhrasesTask.fromJson(reader1));
                } else if ("entityLinkingTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.entityLinkingTasks =
                            reader.readArray(reader1 -> EntityLinkingTask.fromJson(reader1));
                } else if ("sentimentAnalysisTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.sentimentAnalysisTasks =
                            reader.readArray(reader1 -> SentimentAnalysisTask.fromJson(reader1));
                } else if ("extractiveSummarizationTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.extractiveSummarizationTasks =
                            reader.readArray(reader1 -> ExtractiveSummarizationTask.fromJson(reader1));
                } else if ("customEntityRecognitionTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.customEntityRecognitionTasks =
                            reader.readArray(reader1 -> CustomEntitiesTask.fromJson(reader1));
                } else if ("customSingleClassificationTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.customSingleClassificationTasks =
                            reader.readArray(reader1 -> CustomSingleClassificationTask.fromJson(reader1));
                } else if ("customMultiClassificationTasks".equals(fieldName)) {
                    deserializedJobManifestTasks.customMultiClassificationTasks =
                            reader.readArray(reader1 -> CustomMultiClassificationTask.fromJson(reader1));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedJobManifestTasks;
        });
    }
}

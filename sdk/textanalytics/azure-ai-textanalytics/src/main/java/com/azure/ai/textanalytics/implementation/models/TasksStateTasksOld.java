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

/** The TasksStateTasksOld model. */
@Fluent
public final class TasksStateTasksOld implements JsonSerializable<TasksStateTasksOld> {
    /*
     * The completed property.
     */
    private int completed;

    /*
     * The failed property.
     */
    private int failed;

    /*
     * The inProgress property.
     */
    private int inProgress;

    /*
     * The total property.
     */
    private int total;

    /*
     * The entityRecognitionTasks property.
     */
    private List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasks;

    /*
     * The entityRecognitionPiiTasks property.
     */
    private List<TasksStateTasksEntityRecognitionPiiTasksItem> entityRecognitionPiiTasks;

    /*
     * The keyPhraseExtractionTasks property.
     */
    private List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks;

    /*
     * The entityLinkingTasks property.
     */
    private List<TasksStateTasksEntityLinkingTasksItem> entityLinkingTasks;

    /*
     * The sentimentAnalysisTasks property.
     */
    private List<TasksStateTasksSentimentAnalysisTasksItem> sentimentAnalysisTasks;

    /*
     * The customEntityRecognitionTasks property.
     */
    private List<TasksStateTasksCustomEntityRecognitionTasksItem> customEntityRecognitionTasks;

    /*
     * The customSingleClassificationTasks property.
     */
    private List<TasksStateTasksCustomSingleClassificationTasksItem> customSingleClassificationTasks;

    /*
     * The customMultiClassificationTasks property.
     */
    private List<TasksStateTasksCustomMultiClassificationTasksItem> customMultiClassificationTasks;

    /**
     * Get the completed property: The completed property.
     *
     * @return the completed value.
     */
    public int getCompleted() {
        return this.completed;
    }

    /**
     * Set the completed property: The completed property.
     *
     * @param completed the completed value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setCompleted(int completed) {
        this.completed = completed;
        return this;
    }

    /**
     * Get the failed property: The failed property.
     *
     * @return the failed value.
     */
    public int getFailed() {
        return this.failed;
    }

    /**
     * Set the failed property: The failed property.
     *
     * @param failed the failed value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setFailed(int failed) {
        this.failed = failed;
        return this;
    }

    /**
     * Get the inProgress property: The inProgress property.
     *
     * @return the inProgress value.
     */
    public int getInProgress() {
        return this.inProgress;
    }

    /**
     * Set the inProgress property: The inProgress property.
     *
     * @param inProgress the inProgress value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setInProgress(int inProgress) {
        this.inProgress = inProgress;
        return this;
    }

    /**
     * Get the total property: The total property.
     *
     * @return the total value.
     */
    public int getTotal() {
        return this.total;
    }

    /**
     * Set the total property: The total property.
     *
     * @param total the total value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setTotal(int total) {
        this.total = total;
        return this;
    }

    /**
     * Get the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @return the entityRecognitionTasks value.
     */
    public List<TasksStateTasksEntityRecognitionTasksItem> getEntityRecognitionTasks() {
        return this.entityRecognitionTasks;
    }

    /**
     * Set the entityRecognitionTasks property: The entityRecognitionTasks property.
     *
     * @param entityRecognitionTasks the entityRecognitionTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setEntityRecognitionTasks(
        List<TasksStateTasksEntityRecognitionTasksItem> entityRecognitionTasks) {
        this.entityRecognitionTasks = entityRecognitionTasks;
        return this;
    }

    /**
     * Get the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @return the entityRecognitionPiiTasks value.
     */
    public List<TasksStateTasksEntityRecognitionPiiTasksItem> getEntityRecognitionPiiTasks() {
        return this.entityRecognitionPiiTasks;
    }

    /**
     * Set the entityRecognitionPiiTasks property: The entityRecognitionPiiTasks property.
     *
     * @param entityRecognitionPiiTasks the entityRecognitionPiiTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setEntityRecognitionPiiTasks(
        List<TasksStateTasksEntityRecognitionPiiTasksItem> entityRecognitionPiiTasks) {
        this.entityRecognitionPiiTasks = entityRecognitionPiiTasks;
        return this;
    }

    /**
     * Get the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @return the keyPhraseExtractionTasks value.
     */
    public List<TasksStateTasksKeyPhraseExtractionTasksItem> getKeyPhraseExtractionTasks() {
        return this.keyPhraseExtractionTasks;
    }

    /**
     * Set the keyPhraseExtractionTasks property: The keyPhraseExtractionTasks property.
     *
     * @param keyPhraseExtractionTasks the keyPhraseExtractionTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setKeyPhraseExtractionTasks(
        List<TasksStateTasksKeyPhraseExtractionTasksItem> keyPhraseExtractionTasks) {
        this.keyPhraseExtractionTasks = keyPhraseExtractionTasks;
        return this;
    }

    /**
     * Get the entityLinkingTasks property: The entityLinkingTasks property.
     *
     * @return the entityLinkingTasks value.
     */
    public List<TasksStateTasksEntityLinkingTasksItem> getEntityLinkingTasks() {
        return this.entityLinkingTasks;
    }

    /**
     * Set the entityLinkingTasks property: The entityLinkingTasks property.
     *
     * @param entityLinkingTasks the entityLinkingTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setEntityLinkingTasks(List<TasksStateTasksEntityLinkingTasksItem> entityLinkingTasks) {
        this.entityLinkingTasks = entityLinkingTasks;
        return this;
    }

    /**
     * Get the sentimentAnalysisTasks property: The sentimentAnalysisTasks property.
     *
     * @return the sentimentAnalysisTasks value.
     */
    public List<TasksStateTasksSentimentAnalysisTasksItem> getSentimentAnalysisTasks() {
        return this.sentimentAnalysisTasks;
    }

    /**
     * Set the sentimentAnalysisTasks property: The sentimentAnalysisTasks property.
     *
     * @param sentimentAnalysisTasks the sentimentAnalysisTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setSentimentAnalysisTasks(
        List<TasksStateTasksSentimentAnalysisTasksItem> sentimentAnalysisTasks) {
        this.sentimentAnalysisTasks = sentimentAnalysisTasks;
        return this;
    }

    /**
     * Get the customEntityRecognitionTasks property: The customEntityRecognitionTasks property.
     *
     * @return the customEntityRecognitionTasks value.
     */
    public List<TasksStateTasksCustomEntityRecognitionTasksItem> getCustomEntityRecognitionTasks() {
        return this.customEntityRecognitionTasks;
    }

    /**
     * Set the customEntityRecognitionTasks property: The customEntityRecognitionTasks property.
     *
     * @param customEntityRecognitionTasks the customEntityRecognitionTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setCustomEntityRecognitionTasks(
        List<TasksStateTasksCustomEntityRecognitionTasksItem> customEntityRecognitionTasks) {
        this.customEntityRecognitionTasks = customEntityRecognitionTasks;
        return this;
    }

    /**
     * Get the customSingleClassificationTasks property: The customSingleClassificationTasks property.
     *
     * @return the customSingleClassificationTasks value.
     */
    public List<TasksStateTasksCustomSingleClassificationTasksItem> getCustomSingleClassificationTasks() {
        return this.customSingleClassificationTasks;
    }

    /**
     * Set the customSingleClassificationTasks property: The customSingleClassificationTasks property.
     *
     * @param customSingleClassificationTasks the customSingleClassificationTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setCustomSingleClassificationTasks(
        List<TasksStateTasksCustomSingleClassificationTasksItem> customSingleClassificationTasks) {
        this.customSingleClassificationTasks = customSingleClassificationTasks;
        return this;
    }

    /**
     * Get the customMultiClassificationTasks property: The customMultiClassificationTasks property.
     *
     * @return the customMultiClassificationTasks value.
     */
    public List<TasksStateTasksCustomMultiClassificationTasksItem> getCustomMultiClassificationTasks() {
        return this.customMultiClassificationTasks;
    }

    /**
     * Set the customMultiClassificationTasks property: The customMultiClassificationTasks property.
     *
     * @param customMultiClassificationTasks the customMultiClassificationTasks value to set.
     * @return the TasksStateTasksOld object itself.
     */
    public TasksStateTasksOld setCustomMultiClassificationTasks(
        List<TasksStateTasksCustomMultiClassificationTasksItem> customMultiClassificationTasks) {
        this.customMultiClassificationTasks = customMultiClassificationTasks;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("completed", this.completed);
        jsonWriter.writeIntField("failed", this.failed);
        jsonWriter.writeIntField("inProgress", this.inProgress);
        jsonWriter.writeIntField("total", this.total);
        jsonWriter.writeArrayField("entityRecognitionTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("entityRecognitionPiiTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("keyPhraseExtractionTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("entityLinkingTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("sentimentAnalysisTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("customEntityRecognitionTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("customSingleClassificationTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("customMultiClassificationTasks", this.entityRecognitionTasks,
                (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TasksStateTasksOld from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TasksStateTasksOld if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the TasksStateTasksOld.
     */
    public static TasksStateTasksOld fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TasksStateTasksOld deserializedTasksStateTasksOld = new TasksStateTasksOld();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("completed".equals(fieldName)) {
                    deserializedTasksStateTasksOld.completed = reader.getInt();
                } else if ("failed".equals(fieldName)) {
                    deserializedTasksStateTasksOld.failed = reader.getInt();
                } else if ("inProgress".equals(fieldName)) {
                    deserializedTasksStateTasksOld.inProgress = reader.getInt();
                } else if ("total".equals(fieldName)) {
                    deserializedTasksStateTasksOld.total = reader.getInt();
                } else if ("entityRecognitionTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.entityRecognitionTasks = reader.readArray(
                            reader1 -> TasksStateTasksEntityRecognitionTasksItem.fromJson(reader1));
                } else if ("entityRecognitionPiiTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.entityRecognitionPiiTasks = reader.readArray(
                            reader1 -> TasksStateTasksEntityRecognitionPiiTasksItem.fromJson(reader1));
                } else if ("keyPhraseExtractionTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.keyPhraseExtractionTasks = reader.readArray(
                            reader1 -> TasksStateTasksKeyPhraseExtractionTasksItem.fromJson(reader1));
                } else if ("entityLinkingTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.entityLinkingTasks = reader.readArray(
                            reader1 -> TasksStateTasksEntityLinkingTasksItem.fromJson(reader1));
                } else if ("sentimentAnalysisTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.sentimentAnalysisTasks = reader.readArray(
                            reader1 -> TasksStateTasksSentimentAnalysisTasksItem.fromJson(reader1));
                } else if ("customEntityRecognitionTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.customEntityRecognitionTasks = reader.readArray(
                            reader1 -> TasksStateTasksCustomEntityRecognitionTasksItem.fromJson(reader1));
                } else if ("customSingleClassificationTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.customSingleClassificationTasks = reader.readArray(
                            reader1 -> TasksStateTasksCustomSingleClassificationTasksItem.fromJson(reader1));
                } else if ("customMultiClassificationTasks".equals(fieldName)) {
                    deserializedTasksStateTasksOld.customMultiClassificationTasks = reader.readArray(
                            reader1 -> TasksStateTasksCustomMultiClassificationTasksItem.fromJson(reader1));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedTasksStateTasksOld;
        });
    }
}

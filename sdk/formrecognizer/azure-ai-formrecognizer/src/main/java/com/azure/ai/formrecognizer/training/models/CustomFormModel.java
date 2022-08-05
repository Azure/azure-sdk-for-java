// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.ai.formrecognizer.implementation.CustomFormModelHelper;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * The CustomFormModel
 */
@Immutable
public final class CustomFormModel {

    /*
     * List of errors returned during the training operation.
     */
    private final List<FormRecognizerError> modelError;

    /*
     * Model identifier.
     */
    private final String modelId;

    /*
     * Status of the model.
     */
    private final CustomFormModelStatus modelStatus;

    /*
     * Date and time (UTC) when the training of the model was started.
     */
    private final OffsetDateTime trainingStartedOn;

    /*
     * Date and time (UTC) when the model training was completed.
     */
    private final OffsetDateTime trainingCompletedOn;

    /*
     * List of sub model that are part of this model, each of which can recognize and extract fields
     * from a different type of form.
     */
    private final List<CustomFormSubmodel> submodels;

    /*
     * List of the documents used to train the model.
     */
    private List<TrainingDocumentInfo> trainingDocuments;

    private String modelName;

    private CustomFormModelProperties customFormModelProperties;

    static {
        CustomFormModelHelper.setAccessor(new CustomFormModelHelper.CustomFormModelAccessor() {
            @Override
            public void setTrainingDocuments(CustomFormModel formModel, List<TrainingDocumentInfo> trainingDocuments) {
                formModel.setTrainingDocuments(trainingDocuments);
            }

            @Override
            public void setModelName(CustomFormModel formModel, String modelName) {
                formModel.setModelName(modelName);
            }

            @Override
            public void setCustomFormModelProperties(CustomFormModel formModel,
                                                     CustomFormModelProperties customFormModelProperties) {
                formModel.setCustomFormModelProperties(customFormModelProperties);
            }
        });
    }

    /**
     * Constructs a CustomFormModel object.
     *
     * @param modelId Model identifier.
     * @param modelStatus Status of the model.
     * @param trainingStartedOn Date and time (UTC) when the training of model was started.
     * @param trainingCompletedOn Date and time (UTC) when the model training was completed.
     * @param submodels List of sub model that are part of this model, each of which can recognize and extract fields
     * from a different type of form.
     * @param modelError List of errors returned during the training operation.
     * @param trainingDocuments List of the documents used to train the model.
     */
    public CustomFormModel(final String modelId, final CustomFormModelStatus modelStatus,
        final OffsetDateTime trainingStartedOn, final OffsetDateTime trainingCompletedOn,
        final List<CustomFormSubmodel> submodels, final List<FormRecognizerError> modelError,
        final List<TrainingDocumentInfo> trainingDocuments) {
        this.modelId = modelId;
        this.modelStatus = modelStatus;
        this.trainingStartedOn = trainingStartedOn;
        this.trainingCompletedOn = trainingCompletedOn;
        this.submodels = submodels == null ? null : Collections.unmodifiableList(submodels);
        this.modelError = modelError == null ? null : Collections.unmodifiableList(modelError);
        this.trainingDocuments = trainingDocuments == null ? null
            : Collections.unmodifiableList(trainingDocuments);
    }

    /**
     * Get the Model identifier.
     *
     * @return the {@code modelId} value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Get the status of the model.
     *
     * @return the {@code modelStatus} value.
     */
    public CustomFormModelStatus getModelStatus() {
        return this.modelStatus;
    }

    /**
     * Get the Date and time (UTC) when the training of the model was started.
     *
     * @return the {@code trainingStartedOn} value.
     */
    public OffsetDateTime getTrainingStartedOn() {
        return this.trainingStartedOn;
    }

    /**
     * Get the Date and time (UTC) when the model training was completed.
     *
     * @return the {@code trainingCompletedOn} value.
     */
    public OffsetDateTime getTrainingCompletedOn() {
        return this.trainingCompletedOn;
    }

    /**
     * Get the errors returned during the training operation.
     *
     * @return the unmodifiable list of model errors returned during the training operation.
     */
    public List<FormRecognizerError> getModelError() {
        return this.modelError;
    }

    /**
     * Get the list of sub model that are part of this model, each of which can recognize
     * and extract fields from a different type of form.
     *
     * @return the unmodifiable list of submodels that are a part of this model.
     */
    public List<CustomFormSubmodel> getSubmodels() {
        return this.submodels;
    }

    /**
     * Get the list of the documents used to train the model and any errors reported in each document.
     *
     * @return the unmodifiable list of documents used to train the model.
     */
    public List<TrainingDocumentInfo> getTrainingDocuments() {
        return this.trainingDocuments;
    }

    /**
     * Get model metadata properties.
     *
     * @return the custom model metadata properties.
     */
    public CustomFormModelProperties getCustomModelProperties() {
        return customFormModelProperties;
    }

    /**
     * Get the user defined model display name.
     *
     * @return the modelName value.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * The private setter to set the trainingDocuments property
     * via {@link CustomFormModelHelper.CustomFormModelAccessor}.
     *
     * @param trainingDocuments The list of the documents used to train the model and any
     *                         errors reported in each document.
     */
    private void setTrainingDocuments(List<TrainingDocumentInfo> trainingDocuments) {
        this.trainingDocuments = trainingDocuments;
    }

    /**
     * The private setter to set the modelName property
     * via {@link CustomFormModelHelper.CustomFormModelAccessor}.
     *
     * @param modelName The user defined model display name.
     */
    private void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * The private setter to set the customFormModelProperties property
     * via {@link CustomFormModelHelper.CustomFormModelAccessor}.
     *
     * @param customFormModelProperties The model metadata properties.
     */
    private void setCustomFormModelProperties(CustomFormModelProperties customFormModelProperties) {
        this.customFormModelProperties = customFormModelProperties;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training;

import com.azure.ai.formrecognizer.implementation.CustomFormModelHelper;
import com.azure.ai.formrecognizer.implementation.CustomFormModelInfoHelper;
import com.azure.ai.formrecognizer.implementation.CustomFormModelPropertiesHelper;
import com.azure.ai.formrecognizer.implementation.CustomFormSubmodelHelper;
import com.azure.ai.formrecognizer.implementation.TrainingDocumentInfoHelper;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.ModelInfo;
import com.azure.ai.formrecognizer.implementation.models.ModelStatus;
import com.azure.ai.formrecognizer.implementation.models.TrainResult;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelField;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.CustomFormModelProperties;
import com.azure.ai.formrecognizer.training.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.training.models.CustomFormSubmodel;
import com.azure.ai.formrecognizer.training.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.training.models.TrainingStatus;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.implementation.Utility.forEachWithIndex;

/**
 * Helper class to convert service level custom form related models to SDK exposed models.
 */
final class CustomModelTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(CustomModelTransforms.class);

    private CustomModelTransforms() {
    }

    /**
     * Helper method to convert the {@link Model model Response} from service to {@link CustomFormModel}.
     *
     * @param modelResponse The {@code Model model response} returned from the service.
     *
     * @return The {@link CustomFormModel}.
     */
    static CustomFormModel toCustomFormModel(Model modelResponse) {
        ModelInfo modelInfo = modelResponse.getModelInfo();
        if (modelInfo.getStatus() == ModelStatus.INVALID) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(String.format("Model Id %s returned with invalid status.",
                    modelInfo.getModelId())));
        }

        List<TrainingDocumentInfo> trainingDocumentInfoList = null;
        List<FormRecognizerError> modelErrors = null;
        final String modelId = modelInfo.getModelId().toString();

        // get document info for unlabeled and labeled models
        if (modelResponse.getTrainResult() != null) {
            trainingDocumentInfoList
                = getTrainingDocumentList(modelResponse.getTrainResult().getTrainingDocuments(), modelId);
            modelErrors = transformTrainingErrors(modelResponse.getTrainResult().getErrors());
        }

        List<CustomFormSubmodel> subModelList = null;
        if (modelResponse.getKeys() != null) {
            // unlabeled model, read from page results
            subModelList = getUnlabeledSubmodels(modelResponse.getKeys().getClusters(), modelId);
        } else if (modelResponse.getTrainResult() != null && modelResponse.getTrainResult().getFields() != null) {
            // labeled model
            String formType = "custom:";
            if (modelInfo.getModelName() != null) {
                formType = formType + modelInfo.getModelName();
            } else {
                formType = formType + modelInfo.getModelId();
            }
            subModelList = getLabeledSubmodels(modelResponse, modelId, formType);
        } else if (!CoreUtils.isNullOrEmpty(modelResponse.getComposedTrainResults())) {
            // composed model
            subModelList = getComposedSubmodels(modelResponse);
            trainingDocumentInfoList = new ArrayList<>();
            for (TrainResult composedTrainResultItem : modelResponse.getComposedTrainResults()) {
                final List<TrainingDocumentInfo> trainingDocumentSubModelList
                    = getTrainingDocumentList(composedTrainResultItem.getTrainingDocuments(),
                    composedTrainResultItem.getModelId().toString());
                trainingDocumentInfoList.addAll(trainingDocumentSubModelList);
            }
        }

        CustomFormModel customFormModel = new CustomFormModel(
            modelInfo.getModelId().toString(),
            CustomFormModelStatus.fromString(modelInfo.getStatus().toString()),
            modelInfo.getCreatedDateTime(),
            modelInfo.getLastUpdatedDateTime(),
            subModelList,
            modelErrors,
            trainingDocumentInfoList);

        CustomFormModelProperties customFormModelProperties = new CustomFormModelProperties();

        if (modelInfo.getAttributes() != null) {
            CustomFormModelPropertiesHelper.setIsComposed(customFormModelProperties,
                modelInfo.getAttributes().isComposed());

            CustomFormModelHelper.setCustomFormModelProperties(customFormModel, customFormModelProperties);
            if (modelInfo.getAttributes().isComposed()) {
                CustomFormModelHelper.setTrainingDocuments(customFormModel, trainingDocumentInfoList);
            }
        } else {
            // default to false
            CustomFormModelHelper.setCustomFormModelProperties(customFormModel, customFormModelProperties);
        }

        if (modelInfo.getModelName() != null) {
            CustomFormModelHelper.setModelName(customFormModel, modelInfo.getModelName());
        }
        return customFormModel;
    }

    /** Creates a training documents info list from service training documents **/
    private static List<TrainingDocumentInfo> getTrainingDocumentList(
        List<com.azure.ai.formrecognizer.implementation.models.TrainingDocumentInfo> trainingDocuments,
        String modelId) {
        return trainingDocuments.stream()
            .map(trainingDocumentItem ->
                new TrainingDocumentInfo(trainingDocumentItem.getDocumentName(),
                    TrainingStatus.fromString(trainingDocumentItem.getStatus().toString()),
                    trainingDocumentItem.getPages(),
                    transformTrainingErrors(trainingDocumentItem.getErrors())))
            .peek(trainingDocumentInfo ->
                TrainingDocumentInfoHelper.setModelId(trainingDocumentInfo, modelId))
            .collect(Collectors.toList());
    }

    /** Creates a submodel list from labeled models service data **/
    private static List<CustomFormSubmodel> getLabeledSubmodels(Model modelResponse, String modelId, String formType) {
        Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
        List<CustomFormSubmodel> subModelList = new ArrayList<>();
        modelResponse.getTrainResult().getFields()
            .forEach(formFieldsReport -> fieldMap.put(formFieldsReport.getFieldName(),
                new CustomFormModelField(null,
                    formFieldsReport.getFieldName(),
                    formFieldsReport.getAccuracy())));

        CustomFormSubmodel customFormSubmodel =
            new CustomFormSubmodel(
                modelResponse.getTrainResult().getAverageModelAccuracy(),
                fieldMap,
                formType);
        CustomFormSubmodelHelper.setModelId(customFormSubmodel, modelId);
        subModelList.add(customFormSubmodel);
        return subModelList;
    }

    /** Creates a submodel list from unlabeled models service data **/
    private static List<CustomFormSubmodel> getUnlabeledSubmodels(Map<String, List<String>> modelResponseClusters,
        String modelId) {
        List<CustomFormSubmodel> subModelList = new ArrayList<>();
        modelResponseClusters
            .forEach((clusterKey, clusterFields) -> {
                Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
                forEachWithIndex(clusterFields, (index, eachField) -> {
                    String fieldName = "field-" + index;
                    fieldMap.put(fieldName, new CustomFormModelField(eachField, fieldName, null));
                });
                CustomFormSubmodel customFormSubmodel = new CustomFormSubmodel(
                    null,
                    fieldMap,
                    "form-" + clusterKey);
                CustomFormSubmodelHelper.setModelId(customFormSubmodel, modelId);
                subModelList.add(customFormSubmodel);
            });
        return subModelList;
    }

    /** Creates a submodel list from composed models service data **/
    private static List<CustomFormSubmodel> getComposedSubmodels(Model modelResponse) {
        List<CustomFormSubmodel> subModelList = new ArrayList<>();
        for (TrainResult composedTrainResultItem : modelResponse.getComposedTrainResults()) {
            String formType = "custom:" + composedTrainResultItem.getModelId().toString();

            Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
            composedTrainResultItem.getFields()
                .forEach(formFieldsReport -> fieldMap.put(
                    formFieldsReport.getFieldName(),
                    new CustomFormModelField(
                        null,
                        formFieldsReport.getFieldName(),
                        formFieldsReport.getAccuracy())));

            CustomFormSubmodel customFormSubmodel =
                new CustomFormSubmodel(
                    composedTrainResultItem.getAverageModelAccuracy(),
                    fieldMap,
                    formType);
            CustomFormSubmodelHelper.setModelId(customFormSubmodel, composedTrainResultItem.getModelId().toString());
            subModelList.add(customFormSubmodel);
        }
        return subModelList;
    }

    /**
     * Transform a list of {@link ModelInfo} to a list of {@link CustomFormModelInfo}.
     *
     * @param modelInfoList A list of {@link ModelInfo}.
     *
     * @return A list of {@link CustomFormModelInfo}.
     */
    static List<CustomFormModelInfo> toCustomFormModelInfo(List<ModelInfo> modelInfoList) {
        return modelInfoList.stream()
            .map(modelInfo -> {
                CustomFormModelInfo customFormModelInfo = new CustomFormModelInfo(modelInfo.getModelId().toString(),
                    CustomFormModelStatus.fromString(modelInfo.getStatus().toString()),
                    modelInfo.getCreatedDateTime(),
                    modelInfo.getLastUpdatedDateTime());
                if (modelInfo.getAttributes() != null) {
                    CustomFormModelProperties customFormModelProperties = new CustomFormModelProperties();
                    CustomFormModelPropertiesHelper.setIsComposed(customFormModelProperties,
                        modelInfo.getAttributes().isComposed());
                    CustomFormModelInfoHelper.setCustomFormModelProperties(customFormModelInfo,
                        customFormModelProperties);
                }
                if (modelInfo.getModelName() != null) {
                    CustomFormModelInfoHelper.setModelName(customFormModelInfo,
                        modelInfo.getModelName());
                }
                return customFormModelInfo;
            }).collect(Collectors.toList());
    }

    /**
     * Helper method to convert the list of {@link com.azure.ai.formrecognizer.implementation.models.ErrorInformation}
     * to list of {@link FormRecognizerError}.
     *
     * @param trainingErrorList The list of {@link com.azure.ai.formrecognizer.implementation.models.ErrorInformation}.
     *
     * @return The list of {@link FormRecognizerError}
     */
    private static List<FormRecognizerError> transformTrainingErrors(
        List<com.azure.ai.formrecognizer.implementation.models.ErrorInformation> trainingErrorList) {
        if (CoreUtils.isNullOrEmpty(trainingErrorList)) {
            return Collections.emptyList();
        } else {
            return trainingErrorList.stream()
                .map(errorInformation -> new FormRecognizerError(errorInformation.getCode(),
                    errorInformation.getMessage()))
                .collect(Collectors.toList());
        }
    }
}


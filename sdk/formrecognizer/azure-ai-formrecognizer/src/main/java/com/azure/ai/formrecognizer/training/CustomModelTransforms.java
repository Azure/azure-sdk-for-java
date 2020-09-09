// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training;

import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.ModelInfo;
import com.azure.ai.formrecognizer.implementation.models.ModelStatus;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.CustomFormModelField;
import com.azure.ai.formrecognizer.training.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.training.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.training.models.CustomFormSubmodel;
import com.azure.ai.formrecognizer.training.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.training.models.TrainingStatus;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
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
    static final Duration DEFAULT_DURATION = Duration.ofSeconds(5);

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

        if (modelResponse.getTrainResult() != null) {
            trainingDocumentInfoList =
                modelResponse.getTrainResult().getTrainingDocuments().stream()
                    .map(trainingDocumentItem -> new TrainingDocumentInfo(
                        trainingDocumentItem.getDocumentName(),
                        TrainingStatus.fromString(trainingDocumentItem.getStatus().toString()),
                        trainingDocumentItem.getPages(),
                        transformTrainingErrors(trainingDocumentItem.getErrors())))
                    .collect(Collectors.toList());
            modelErrors = transformTrainingErrors(modelResponse.getTrainResult().getErrors());
        }

        List<CustomFormSubmodel> subModelList = new ArrayList<>();
        String formType = "form-";
        // unlabeled model
        if (modelResponse.getKeys() != null) {
            Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
            modelResponse.getKeys().getClusters().forEach((clusterKey, clusterFields) -> {
                forEachWithIndex(clusterFields, (index, eachField) -> {
                    String fieldName = "field-" + index;
                    fieldMap.put(fieldName, new CustomFormModelField(eachField, fieldName, null));
                });
                subModelList.add(new CustomFormSubmodel(
                    null,
                    fieldMap,
                    formType + clusterKey));
            });
        } else if (modelResponse.getTrainResult() != null && modelResponse.getTrainResult().getFields() != null) {
            // labeled model
            Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
            modelResponse.getTrainResult().getFields()
                .forEach(formFieldsReport -> fieldMap.put(formFieldsReport.getFieldName(),
                    new CustomFormModelField(null, formFieldsReport.getFieldName(),
                        formFieldsReport.getAccuracy())));
            subModelList.add(new CustomFormSubmodel(
                modelResponse.getTrainResult().getAverageModelAccuracy(),
                fieldMap,
                formType + modelInfo.getModelId()));
        }

        return new CustomFormModel(
            modelInfo.getModelId().toString(),
            CustomFormModelStatus.fromString(modelInfo.getStatus().toString()),
            modelInfo.getCreatedDateTime(),
            modelInfo.getLastUpdatedDateTime(),
            subModelList,
            modelErrors,
            trainingDocumentInfoList);
    }

    /**
     * Transform a list of {@link ModelInfo} to a list of {@link CustomFormModelInfo}.
     *
     * @param modelInfoList A list of {@link ModelInfo}.
     *
     * @return A list of {@link CustomFormModelInfo}.
     */
    static List<CustomFormModelInfo> toCustomFormModelInfo(List<ModelInfo> modelInfoList) {
        return modelInfoList.stream().map(modelInfo -> new CustomFormModelInfo(modelInfo.getModelId().toString(),
            CustomFormModelStatus.fromString(modelInfo.getStatus().toString()),
            modelInfo.getCreatedDateTime(), modelInfo.getLastUpdatedDateTime())).collect(Collectors.toList());

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


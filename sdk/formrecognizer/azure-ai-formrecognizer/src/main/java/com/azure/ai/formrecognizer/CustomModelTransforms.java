// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.ErrorInformation;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.ModelInfo;
import com.azure.ai.formrecognizer.implementation.models.ModelStatus;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelField;
import com.azure.ai.formrecognizer.models.CustomFormSubModel;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.ai.formrecognizer.models.ModelTrainingStatus;
import com.azure.ai.formrecognizer.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.models.TrainingStatus;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level custom form related models to SDK exposed models.
 */
final class CustomModelTransforms {
    public static final float DEFAULT_CONFIDENCE_VALUE = 1.0f;
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
                new IllegalArgumentException(String.format("Model Id %s returned with status: %s",
                    modelInfo.getModelId(), modelInfo.getStatus())));
        }

        List<TrainingDocumentInfo> trainingDocumentInfoList =
            modelResponse.getTrainResult().getTrainingDocuments().stream()
                .map(trainingDocumentItem -> new TrainingDocumentInfo(
                    trainingDocumentItem.getDocumentName(),
                    TrainingStatus.fromString(trainingDocumentItem.getStatus().toString()),
                    trainingDocumentItem.getPages(),
                    new IterableStream<FormRecognizerError>(transformTrainingErrors(trainingDocumentItem.getErrors()))))
                .collect(Collectors.toList());

        List<CustomFormSubModel> subModelList = new ArrayList<>();
        String formType = "form-";
        // unlabeled model
        if (modelResponse.getKeys() != null) {
            Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
            modelResponse.getKeys().getClusters().forEach((clusterKey, clusterFields) -> {
                for (int i = 0, clusterFieldsSize = clusterFields.size(); i < clusterFieldsSize; i++) {
                    String eachField = clusterFields.get(i);
                    String fieldLabel = "field-" + i;
                    fieldMap.put(fieldLabel, new CustomFormModelField(fieldLabel, eachField, null));
                }
                subModelList.add(new CustomFormSubModel(
                    DEFAULT_CONFIDENCE_VALUE,
                    fieldMap,
                    formType + clusterKey));
            });
        } else if (modelResponse.getTrainResult().getFields() != null) {
            // labeled model
            Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
            modelResponse.getTrainResult().getFields()
                .forEach(formFieldsReport -> fieldMap.put(formFieldsReport.getFieldName(),
                    new CustomFormModelField(null, formFieldsReport.getFieldName(),
                        formFieldsReport.getAccuracy())));
            subModelList.add(new CustomFormSubModel(
                modelResponse.getTrainResult().getAverageModelAccuracy(),
                fieldMap,
                formType + modelInfo.getModelId()));
        }

        return new CustomFormModel(
            modelInfo.getModelId().toString(),
            ModelTrainingStatus.fromString(modelInfo.getStatus().toString()),
            modelInfo.getCreatedDateTime(),
            modelInfo.getLastUpdatedDateTime(),
            new IterableStream<>(subModelList),
            new IterableStream<FormRecognizerError>(
                transformTrainingErrors(modelResponse.getTrainResult().getErrors())),
            new IterableStream<TrainingDocumentInfo>(trainingDocumentInfoList));
    }

    /**
     * Helper method to convert the list of {@link ErrorInformation} to list of {@link FormRecognizerError}.
     *
     * @param trainingErrorList The list of {@link ErrorInformation}.
     *
     * @return The list of {@link FormRecognizerError}
     */
    private static List<FormRecognizerError> transformTrainingErrors(List<ErrorInformation> trainingErrorList) {
        if (CoreUtils.isNullOrEmpty(trainingErrorList)) {
            return new ArrayList<>();
        } else {
            return trainingErrorList.stream().map(errorInformation ->
                new FormRecognizerError(errorInformation.getCode(),
                    errorInformation.getMessage())).collect(Collectors.toList());
        }
    }
}


// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.ErrorInformation;
import com.azure.ai.formrecognizer.implementation.models.Model;
import com.azure.ai.formrecognizer.implementation.models.ModelStatus;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelField;
import com.azure.ai.formrecognizer.models.CustomFormSubModel;
import com.azure.ai.formrecognizer.models.FormRecognizerError;
import com.azure.ai.formrecognizer.models.ModelTrainingStatus;
import com.azure.ai.formrecognizer.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.models.TrainingStatus;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helper class to convert service level custom form related models to SDK exposed models.
 */
final class CustomModelTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(CustomModelTransforms.class);

    private CustomModelTransforms() {
    }

    static CustomFormModel toCustomFormModel(Model modelResponse) {
        com.azure.ai.formrecognizer.implementation.models.ModelInfo modelInfo = modelResponse.getModelInfo();
        if (modelInfo.getStatus() == ModelStatus.INVALID) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid status Model Id."));
        }

        List<FormRecognizerError> trainResultErrors = new ArrayList<>();
        if (modelResponse.getTrainResult().getErrors() != null) {
            trainResultErrors = setTrainingErrors(modelResponse.getTrainResult().getErrors());
        }

        List<TrainingDocumentInfo> trainingDocumentInfoList = new ArrayList<>();
        modelResponse.getTrainResult().getTrainingDocuments().forEach(trainingDocumentItem -> {
            List<FormRecognizerError> documentErrors = new ArrayList<>();
            if (trainingDocumentItem.getErrors() != null) {
                documentErrors = setTrainingErrors(trainingDocumentItem.getErrors());
            }
            TrainingDocumentInfo trainingDocumentInfo = new TrainingDocumentInfo(trainingDocumentItem.getDocumentName(),
                TrainingStatus.fromString(trainingDocumentItem.getStatus().toString()), trainingDocumentItem.getPages(),
                documentErrors);
            trainingDocumentInfoList.add(trainingDocumentInfo);
        });

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
                CustomFormSubModel customFormSubModel = new CustomFormSubModel(null, fieldMap,
                    formType + clusterKey);
                subModelList.add(customFormSubModel);
            });
        } else if (modelResponse.getTrainResult().getFields() != null) {
            // labeled model
            Map<String, CustomFormModelField> fieldMap = new TreeMap<>();
            modelResponse.getTrainResult().getFields()
                .forEach(formFieldsReport -> fieldMap.put(formFieldsReport.getFieldName(),
                        new CustomFormModelField(null, formFieldsReport.getFieldName(), 
                        formFieldsReport.getAccuracy())));
            subModelList.add(new CustomFormSubModel(modelResponse.getTrainResult().getAverageModelAccuracy(),
                fieldMap, formType + modelInfo.getModelId()));
        }

        return new CustomFormModel(modelInfo.getModelId().toString(),
            ModelTrainingStatus.fromString(modelInfo.getStatus().toString()),
            modelInfo.getCreatedDateTime(), modelInfo.getLastUpdatedDateTime(),
            new IterableStream<>(subModelList),
            trainResultErrors, trainingDocumentInfoList);
    }

    private static List<FormRecognizerError> setTrainingErrors(List<ErrorInformation> trainingErrorList) {
        List<FormRecognizerError> formRecognizerErrorList = new ArrayList<>();
        trainingErrorList.forEach(errorInformation ->
            formRecognizerErrorList.add(new FormRecognizerError(errorInformation.getCode(),
                errorInformation.getMessage())));
        return formRecognizerErrorList;
    }
}


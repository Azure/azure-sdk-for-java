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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
final class CustomModelTransforms {
    private static final ClientLogger LOGGER = new ClientLogger(CustomModelTransforms.class);

    private CustomModelTransforms() {
    }

    static CustomFormModel toCustomFormModel(Model modelResponse) {
        com.azure.ai.formrecognizer.implementation.models.ModelInfo modelInfo = modelResponse.getModelInfo();
        if (modelInfo.getStatus().equals(ModelStatus.INVALID)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid status Model Id."));
        }
        List<FormRecognizerError> globalErrors = new ArrayList<>();

        if (modelResponse.getTrainResult().getErrors() != null) {
            globalErrors = setTrainingErrors(modelResponse.getTrainResult().getErrors());
        }

        String formType = null;
        Map<String, CustomFormModelField> fieldMap = new HashMap<>();
        // unsupervised model
        if (modelResponse.getKeys() != null) {
            for (Map.Entry<String, List<String>> entry : modelResponse.getKeys().getClusters().entrySet()) {
                String cluster = entry.getKey();
                formType = cluster;
                List<String> fields = entry.getValue();
                fields.forEach(eachField -> fieldMap.put(cluster, new CustomFormModelField(eachField, null)));
            }
        } else if (modelResponse.getTrainResult().getFields() != null) {
            // supervised model
            modelResponse.getTrainResult().getFields().forEach(formFieldsReport ->
                fieldMap.put(modelInfo.getModelId().toString(),
                    new CustomFormModelField(formFieldsReport.getFieldName(), formFieldsReport.getAccuracy())));
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

        CustomFormSubModel subModel =
            new CustomFormSubModel(modelResponse.getTrainResult().getAverageModelAccuracy(), fieldMap, formType);
        return new CustomFormModel(modelInfo.getModelId().toString(),
            ModelTrainingStatus.fromString(modelInfo.getStatus().toString()),
            modelInfo.getCreatedDateTime(), modelInfo.getLastUpdatedDateTime(),
            new IterableStream<>(Collections.singletonList(subModel)),
            globalErrors, trainingDocumentInfoList);
    }

    private static List<FormRecognizerError> setTrainingErrors(List<ErrorInformation> trainingErrorList) {
        List<FormRecognizerError> formRecognizerErrorList = new ArrayList<>();
        trainingErrorList.forEach(errorInformation ->
            formRecognizerErrorList.add(new FormRecognizerError(errorInformation.getCode(),
                errorInformation.getMessage())));
        return formRecognizerErrorList;
    }
}


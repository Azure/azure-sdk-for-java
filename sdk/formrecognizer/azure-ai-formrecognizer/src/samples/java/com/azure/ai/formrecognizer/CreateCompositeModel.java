package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.core.credential.AzureKeyCredential;

import java.util.Arrays;
import java.util.List;

public class CreateCompositeModel {
    public static void main(String[] args) {
        // Instantiate a source client which has the model that we want to copy.
        FormTrainingClient client = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        List<String> modelIdList = Arrays.asList("modelId1", "modelId2");
        final CustomFormModel customFormModel = client.beginCreateCompositeModel(modelIdList).getFinalResult();
        
        System.out.printf("Model Id: %s", customFormModel.getModelId());
        System.out.printf("Display name for model: %s", customFormModel.getDisplayName());
        System.out.printf("Is this a composite model: %s", customFormModel.getModelProperties().isCompositeModel());
        customFormModel.getSubmodels().forEach(customFormSubmodel -> {
            System.out.println("Submodels comprising this composite model");
            System.out.println(customFormSubmodel.getModelId());
        });

        System.out.println();
        customFormModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
            System.out.printf("Model ID for the document: %s%n", trainingDocumentInfo.getModelId());
            System.out.printf("Document name: %s%n", trainingDocumentInfo.getName());
            System.out.printf("Document status: %s%n", trainingDocumentInfo.getStatus());
            System.out.printf("Document page count: %d%n", trainingDocumentInfo.getPageCount());
            if (!trainingDocumentInfo.getErrors().isEmpty()) {
                System.out.println("Document Errors:");
                trainingDocumentInfo.getErrors().forEach(formRecognizerError ->
                    System.out.printf("Error code %s, Error message: %s%n", formRecognizerError.getErrorCode(),
                        formRecognizerError.getMessage()));
            }
        });
    }
}

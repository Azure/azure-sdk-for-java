// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.util.concurrent.atomic.AtomicReference;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 * <p>
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder().buildClient();
    private FormTrainingClient formTrainingClient = formRecognizerClient.getFormTrainingClient();

    /**
     * Code snippet for getting sync client using the AzureKeyCredential authentication.
     */
    public void useAzureKeyCredentialSyncClient() {
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
    }

    /**
     * Code snippet for rotating AzureKeyCredential of the client
     */
    public void rotatingAzureKeyCredential() {
        AzureKeyCredential credential = new AzureKeyCredential("{key}");
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .buildClient();

        credential.update("{new_key}");
    }

    public void recognizeCustomForm() {
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{custom_trained_model_id}";
        SyncPoller<OperationResult, IterableStream<RecognizedForm>> recognizeFormPoller =
            formRecognizerClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId);

        IterableStream<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

        recognizedForms.forEach(form -> {
            System.out.println("----------- Recognized Form -----------");
            System.out.printf("Form type: %s%n", form.getFormType());
            form.getFields().forEach((label, formField) -> {
                System.out.printf("Field %s has value %s with confidence score of %d.%n", label,
                    formField.getFieldValue(),
                    formField.getConfidence());
            });
            System.out.print("-----------------------------------");
        });
    }

    public void recognizeContent() {
        String analyzeFilePath = "{file_source_url}";
        SyncPoller<OperationResult, IterableStream<FormPage>> recognizeLayoutPoller =
            formRecognizerClient.beginRecognizeContentFromUrl(analyzeFilePath);

        IterableStream<FormPage> layoutPageResults = recognizeLayoutPoller.getFinalResult();

        layoutPageResults.forEach(formPage -> {
            // Table information
            System.out.println("----Recognizing content ----");
            System.out.printf("Has width: %d and height: %d, measured with unit: %s.%n", formPage.getWidth(),
                formPage.getHeight(),
                formPage.getUnit());
            formPage.getTables().forEach(formTable -> {
                System.out.printf("Table has %d rows and %d columns.%n", formTable.getRowCount(),
                    formTable.getColumnCount());
                formTable.getCells().forEach(formTableCell -> {
                    System.out.printf("Cell has text %s.%n", formTableCell.getText());
                });
                System.out.println();
            });
        });
    }

    public void recognizeReceipt() {
        String receiptSourceUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media"
            + "/contoso-allinone.jpg";
        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
            formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptSourceUrl);
        IterableStream<RecognizedReceipt> receiptPageResults = syncPoller.getFinalResult();

        receiptPageResults.forEach(recognizedReceipt -> {
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
            System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
            System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
            System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
            System.out.printf("Merchant Phone Number %s%n", usReceipt.getMerchantPhoneNumber().getName());
            System.out.printf("Merchant Phone Number Value: %s%n", usReceipt.getMerchantPhoneNumber().getFieldValue());
            System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
            System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
        });
    }

    public void trainModel() {
        String trainingSetSource = "{unlabeled_training_set_SAS_URL}";
        SyncPoller<OperationResult, CustomFormModel> trainingPoller =
            formTrainingClient.beginTraining(trainingSetSource, false);

        CustomFormModel customFormModel = trainingPoller.getFinalResult();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        System.out.printf("Model created on: %s%n", customFormModel.getCreatedOn());
        System.out.printf("Model last updated: %s%n%n", customFormModel.getLastUpdatedOn());

        System.out.println("Recognized Fields:");
        // looping through the sub-models, which contains the fields they were trained on
        // Since the given training documents are unlabeled, we still group them but they do not have a label.
        customFormModel.getSubModels().forEach(customFormSubModel -> {
            // Since the training data is unlabeled, we are unable to return the accuracy of this model
            customFormSubModel.getFieldMap().forEach((field, customFormModelField) ->
                System.out.printf("Field: %s Field Label: %s%n",
                    field, customFormModelField.getLabel()));
        });
    }

    public void manageModels() {
        AtomicReference<String> modelId = new AtomicReference<>();
        // First, we see how many custom models we have, and what our limit is
        AccountProperties accountProperties = formTrainingClient.getAccountProperties();
        System.out.printf("The account has %s custom models, and we can have at most %s custom models",
            accountProperties.getCount(), accountProperties.getLimit());

        // Next, we get a paged list of all of our custom models
        PagedIterable<CustomFormModelInfo> customModels = formTrainingClient.getModelInfos();
        System.out.println("We have following models in the account:");
        customModels.forEach(customFormModelInfo -> {
            System.out.printf("Model Id: %s%n", customFormModelInfo.getModelId());
            // get custom model info
            modelId.set(customFormModelInfo.getModelId());
            CustomFormModel customModel = formTrainingClient.getCustomModel(customFormModelInfo.getModelId());
            System.out.printf("Model Status: %s%n", customModel.getModelStatus());
            System.out.printf("Created on: %s%n", customModel.getCreatedOn());
            System.out.printf("Updated on: %s%n", customModel.getLastUpdatedOn());
            customModel.getSubModels().forEach(customFormSubModel -> {
                System.out.printf("Custom Model Form type: %s%n", customFormSubModel.getFormType());
                System.out.printf("Custom Model Accuracy: %d%n", customFormSubModel.getAccuracy());
                if (customFormSubModel.getFieldMap() != null) {
                    customFormSubModel.getFieldMap().forEach((fieldText, customFormModelField) -> {
                        System.out.printf("Field Text: %s%n", fieldText);
                        System.out.printf("Field Accuracy: %d%n", customFormModelField.getAccuracy());
                    });
                }
            });
        });
        // Delete Custom Model
        formTrainingClient.deleteModel(modelId.get());
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        try {
            formRecognizerClient.beginRecognizeContentFromUrl("invalidSourceUrl");
        } catch (ErrorResponseException e) {
            System.out.println(e.getMessage());
        }
    }
}

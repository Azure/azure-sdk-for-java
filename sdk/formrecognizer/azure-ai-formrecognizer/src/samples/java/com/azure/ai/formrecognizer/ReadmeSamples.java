// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.ErrorResponseException;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;
import java.util.Map;
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
    private FormTrainingClient formTrainingClient = new FormTrainingClientBuilder().buildClient();

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
     * Code snippet for getting async client using AAD authentication.
     */
    public void useAadAsyncClient() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
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
        String formUrl = "{file_url}";
        String modelId = "{custom_trained_model_id}";
        SyncPoller<OperationResult, List<RecognizedForm>> recognizeFormPoller =
            formRecognizerClient.beginRecognizeCustomFormsFromUrl(formUrl, modelId);

        List<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

        for (int i = 0; i < recognizedForms.size(); i++) {
            RecognizedForm form = recognizedForms.get(i);
            System.out.printf("----------- Recognized Form %s%n-----------", i);
            System.out.printf("Form type: %s%n", form.getFormType());
            form.getFields().forEach((label, formField) -> {
                System.out.printf("Field %s has value %s with confidence score of %d.%n", label,
                    formField.getFieldValue(),
                    formField.getConfidence());
            });
            System.out.print("-----------------------------------");
        }
    }

    public void recognizeContent() {
        String contentFileUrl = "{file_url}";
        SyncPoller<OperationResult, List<FormPage>> recognizeContentPoller =
            formRecognizerClient.beginRecognizeContentFromUrl(contentFileUrl);

        List<FormPage> contentPageResults = recognizeContentPoller.getFinalResult();

        for (int i = 0; i < contentPageResults.size(); i++) {
            FormPage formPage = contentPageResults.get(i);
            System.out.printf("----Recognizing content for page %s%n----", i);
            // Table information
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
        }
    }

    public void recognizeReceipt() {
        String receiptUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media"
            + "/contoso-allinone.jpg";
        SyncPoller<OperationResult, List<RecognizedReceipt>> syncPoller =
            formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl);
        List<RecognizedReceipt> receiptPageResults = syncPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            RecognizedReceipt recognizedReceipt = receiptPageResults.get(i);
            Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
            System.out.printf("----------- Recognized Receipt page %s -----------%n", i);
            FormField merchantNameField = recognizedFields.get("MerchantName");
            if (merchantNameField.getFieldValue().getType() == FieldValueType.STRING) {
                System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                    merchantNameField.getFieldValue().asString(),
                    merchantNameField.getConfidence());
            }
            FormField transactionDateField = recognizedFields.get("TransactionDate");
            if (transactionDateField.getFieldValue().getType() == FieldValueType.DATE) {
                System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                    transactionDateField.getFieldValue().asDate(),
                    transactionDateField.getConfidence());
            }
            FormField receiptItemsField = recognizedFields.get("Items");
            System.out.printf("Receipt Items: %n");
            if (receiptItemsField.getFieldValue().getType() == FieldValueType.LIST) {
                List<FormField> receiptItems = receiptItemsField.getFieldValue().asList();
                receiptItems.forEach(receiptItem -> {
                    if (receiptItem.getFieldValue().getType() == FieldValueType.MAP) {
                        receiptItem.getFieldValue().asMap().forEach((key, formField) -> {
                            if (key.equals("Name")) {
                                if (formField.getFieldValue().getType() == FieldValueType.STRING) {
                                    System.out.printf("Name: %s, confidence: %.2fs%n",
                                        formField.getFieldValue().asString(),
                                        formField.getConfidence());
                                }
                            }
                            if (key.equals("Quantity")) {
                                if (formField.getFieldValue().getType() == FieldValueType.INTEGER) {
                                    System.out.printf("Quantity: %s, confidence: %.2f%n",
                                        formField.getFieldValue().asInteger(), formField.getConfidence());
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    public void trainModel() {
        String trainingFilesUrl = "{training_set_SAS_URL}";
        SyncPoller<OperationResult, CustomFormModel> trainingPoller =
            formTrainingClient.beginTraining(trainingFilesUrl, false);

        CustomFormModel customFormModel = trainingPoller.getFinalResult();

        // Model Info
        System.out.printf("Model Id: %s%n", customFormModel.getModelId());
        System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
        System.out.printf("Model requested on: %s%n", customFormModel.getRequestedOn());
        System.out.printf("Model training completed on: %s%n%n", customFormModel.getCompletedOn());

        System.out.println("Recognized Fields:");
        // looping through the sub-models, which contains the fields they were trained on
        // Since the given training documents are unlabeled, we still group them but they do not have a label.
        customFormModel.getSubmodels().forEach(customFormSubmodel -> {
            // Since the training data is unlabeled, we are unable to return the accuracy of this model
            customFormSubmodel.getFieldMap().forEach((field, customFormModelField) ->
                System.out.printf("Field: %s Field Label: %s%n",
                    field, customFormModelField.getLabel()));
        });
    }

    public void manageModels() {
        AtomicReference<String> modelId = new AtomicReference<>();
        // First, we see how many custom models we have, and what our limit is
        AccountProperties accountProperties = formTrainingClient.getAccountProperties();
        System.out.printf("The account has %s custom models, and we can have at most %s custom models",
            accountProperties.getCustomModelCount(), accountProperties.getCustomModelLimit());

        // Next, we get a paged list of all of our custom models
        PagedIterable<CustomFormModelInfo> customModels = formTrainingClient.listCustomModels();
        System.out.println("We have following models in the account:");
        customModels.forEach(customFormModelInfo -> {
            System.out.printf("Model Id: %s%n", customFormModelInfo.getModelId());
            // get custom model info
            modelId.set(customFormModelInfo.getModelId());
            CustomFormModel customModel = formTrainingClient.getCustomModel(customFormModelInfo.getModelId());
            System.out.printf("Model Status: %s%n", customModel.getModelStatus());
            System.out.printf("Created on: %s%n", customModel.getRequestedOn());
            System.out.printf("Updated on: %s%n", customModel.getCompletedOn());
            customModel.getSubmodels().forEach(customFormSubmodel -> {
                System.out.printf("Custom Model Form type: %s%n", customFormSubmodel.getFormType());
                System.out.printf("Custom Model Accuracy: %d%n", customFormSubmodel.getAccuracy());
                if (customFormSubmodel.getFieldMap() != null) {
                    customFormSubmodel.getFieldMap().forEach((fieldText, customFormModelField) -> {
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

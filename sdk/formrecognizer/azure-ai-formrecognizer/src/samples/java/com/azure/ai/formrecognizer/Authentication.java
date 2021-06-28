// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.models.AccountProperties;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Samples for two supported methods of authentication in Form Recognizer and Form Training clients:
 * 1) Use a Form Recognizer API key with AzureKeyCredential from azure.core.credentials
 * 2) Use a token credential from azure-identity to authenticate with Azure Active Directory
 */
public class Authentication {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     *
     */
    public static void main(String[] args) {
        /*
          Set the environment variables with your own values before running the sample:
          AZURE_CLIENT_ID - the client ID of your active directory application.
          AZURE_TENANT_ID - the tenant ID of your active directory application.
          AZURE_CLIENT_SECRET - the secret of your active directory application.
         */

        // Form recognizer client: Key credential
        authenticationWithKeyCredentialFormRecognizerClient();
        // Form recognizer client: Azure Active Directory
        authenticationWithAzureActiveDirectoryFormRecognizerClient();
        // Form training client: Key credential
        authenticationWithKeyCredentialFormTrainingClient();
        // Form training client: Azure Active Directory
        authenticationWithAzureActiveDirectoryFormTrainingClient();
    }

    private static void authenticationWithKeyCredentialFormRecognizerClient() {
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        beginRecognizeCustomFormsFromUrl(formRecognizerClient);
    }

    private static void authenticationWithAzureActiveDirectoryFormRecognizerClient() {
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("{endpoint}")
            .buildClient();
        beginRecognizeCustomFormsFromUrl(formRecognizerClient);
    }

    private static void authenticationWithKeyCredentialFormTrainingClient() {
        FormTrainingClient formTrainingClient = new FormTrainingClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        getAccountProperties(formTrainingClient);
    }

    private static void authenticationWithAzureActiveDirectoryFormTrainingClient() {
        FormTrainingClient formTrainingClient = new FormTrainingClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("{endpoint}")
            .buildClient();
        getAccountProperties(formTrainingClient);
    }

    private static void beginRecognizeCustomFormsFromUrl(FormRecognizerClient formRecognizerClient) {
        String receiptUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/receipts/contoso-allinone.jpg";
        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> recognizeReceiptPoller =
            formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl);

        List<RecognizedForm> receiptPageResults = recognizeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            RecognizedForm recognizedForm = receiptPageResults.get(i);
            Map<String, FormField> recognizedFields = recognizedForm.getFields();
            System.out.printf("----------- Recognizing receipt info for page %d -----------%n", i);
            FormField merchantNameField = recognizedFields.get("MerchantName");
            if (merchantNameField != null) {
                if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
                    String merchantName = merchantNameField.getValue().asString();
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
            }

            FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
            if (merchantPhoneNumberField != null) {
                if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
                    String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
                    System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                        merchantAddress, merchantPhoneNumberField.getConfidence());
                }
            }

            FormField transactionDateField = recognizedFields.get("TransactionDate");
            if (transactionDateField != null) {
                if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
                    LocalDate transactionDate = transactionDateField.getValue().asDate();
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
            }

            FormField receiptItemsField = recognizedFields.get("Items");
            if (receiptItemsField != null) {
                System.out.printf("Receipt Items: %n");
                if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
                    List<FormField> receiptItems = receiptItemsField.getValue().asList();
                    receiptItems.stream()
                        .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                        .map(formField -> formField.getValue().asMap())
                        .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                            if ("Name".equals(key)) {
                                if (FieldValueType.STRING == formField.getValue().getValueType()) {
                                    String name = formField.getValue().asString();
                                    System.out.printf("Name: %s, confidence: %.2fs%n",
                                        name, formField.getConfidence());
                                }
                            }
                            if ("Quantity".equals(key)) {
                                if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                    Float quantity = formField.getValue().asFloat();
                                    System.out.printf("Quantity: %f, confidence: %.2f%n",
                                        quantity, formField.getConfidence());
                                }
                            }
                        }));
                }
            }
            System.out.print("-----------------------------------");
        }
    }

    private static void getAccountProperties(FormTrainingClient formTrainingClient) {
        AccountProperties accountProperties = formTrainingClient.getAccountProperties();
        System.out.printf("Max number of models that can be trained for this account: %s%n",
            accountProperties.getCustomModelLimit());
        System.out.printf("Current count of trained custom models: %d%n", accountProperties.getCustomModelCount());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Samples for two supported methods of authentication in Form Recognizer and Form Training clients:
 *     1) Use a Form Recognizer API key with AzureKeyCredential from azure.core.credentials
 *     2) Use a token credential from azure-identity to authenticate with Azure Active Directory
 */
public class Authentication {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(String[] args) {
        /*
        Set the environment variables with your own values before running the sample:

        1) AZURE_CLIENT_ID - the client ID of your active directory application.
        2) AZURE_TENANT_ID - the tenant ID of your active directory application.
        3) AZURE_CLIENT_SECRET - the secret of your active directory application.
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
        String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/receipts/contoso-allinone.jpg";

        SyncPoller<OperationResult, List<RecognizedReceipt>> recognizeReceiptPoller =
            formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl);

        List<RecognizedReceipt> receiptPageResults = recognizeReceiptPoller.getFinalResult();

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

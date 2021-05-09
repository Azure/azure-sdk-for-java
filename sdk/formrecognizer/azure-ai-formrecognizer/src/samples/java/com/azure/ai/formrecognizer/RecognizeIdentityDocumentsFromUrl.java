// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sample for recognizing commonly found ID document fields from a file source URL of an identity document.
 * See fields found on an identity document here: https://aka.ms/formrecognizer/iddocumentfields
 */
public class RecognizeIdentityDocumentsFromUrl {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String licenseDocumentUrl = "https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/formrecognizer/"
            + "azure-ai-formrecognizer/src/test/resources/sample_files/Test/license.jpg";
        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> analyzeIdentityDocumentPoller
            = client.beginRecognizeIdentityDocumentsFromUrl(licenseDocumentUrl);

        List<RecognizedForm> identityDocumentResults = analyzeIdentityDocumentPoller.getFinalResult();

        for (int i = 0; i < identityDocumentResults.size(); i++) {
            RecognizedForm recognizedForm = identityDocumentResults.get(i);
            Map<String, FormField> recognizedFields = recognizedForm.getFields();
            System.out.printf("----------- Recognized license info for page %d -----------%n", i);
            FormField addressField = recognizedFields.get("Address");
            if (addressField != null) {
                if (FieldValueType.STRING == addressField.getValue().getValueType()) {
                    String address = addressField.getValue().asString();
                    System.out.printf("Address: %s, confidence: %.2f%n",
                        address, addressField.getConfidence());
                }
            }

            FormField countryFormField = recognizedFields.get("Country");
            if (countryFormField != null) {
                if (FieldValueType.STRING == countryFormField.getValue().getValueType()) {
                    String country = countryFormField.getValue().asCountry();
                    System.out.printf("Country: %s, confidence: %.2f%n",
                        country, countryFormField.getConfidence());
                }
            }

            FormField dateOfBirthField = recognizedFields.get("DateOfBirth");
            if (dateOfBirthField != null) {
                if (FieldValueType.DATE == dateOfBirthField.getValue().getValueType()) {
                    LocalDate dateOfBirth = dateOfBirthField.getValue().asDate();
                    System.out.printf("Date of Birth: %s, confidence: %.2f%n",
                        dateOfBirth, dateOfBirthField.getConfidence());
                }
            }

            FormField dateOfExpirationField = recognizedFields.get("DateOfExpiration");
            if (dateOfExpirationField != null) {
                if (FieldValueType.DATE == dateOfExpirationField.getValue().getValueType()) {
                    LocalDate expirationDate = dateOfExpirationField.getValue().asDate();
                    System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                        expirationDate, dateOfExpirationField.getConfidence());
                }
            }

            FormField documentNumberField = recognizedFields.get("DocumentNumber");
            if (documentNumberField != null) {
                if (FieldValueType.STRING == documentNumberField.getValue().getValueType()) {
                    String documentNumber = documentNumberField.getValue().asString();
                    System.out.printf("Document number: %s, confidence: %.2f%n",
                        documentNumber, documentNumberField.getConfidence());
                }
            }

            FormField firstNameField = recognizedFields.get("FirstName");
            if (firstNameField != null) {
                if (FieldValueType.STRING == firstNameField.getValue().getValueType()) {
                    String firstName = firstNameField.getValue().asString();
                    System.out.printf("First Name: %s, confidence: %.2f%n",
                        firstName, documentNumberField.getConfidence());
                }
            }

            FormField lastNameField = recognizedFields.get("LastName");
            if (lastNameField != null) {
                if (FieldValueType.STRING == lastNameField.getValue().getValueType()) {
                    String lastName = lastNameField.getValue().asString();
                    System.out.printf("Last name: %s, confidence: %.2f%n",
                        lastName, lastNameField.getConfidence());
                }
            }

            FormField regionField = recognizedFields.get("Region");
            if (regionField != null) {
                if (FieldValueType.STRING == regionField.getValue().getValueType()) {
                    String region = regionField.getValue().asString();
                    System.out.printf("Region: %s, confidence: %.2f%n",
                        region, regionField.getConfidence());
                }
            }
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.Document;
import com.azure.ai.documentintelligence.models.DocumentField;
import com.azure.ai.documentintelligence.models.DocumentFieldType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Sample for analyzing commonly found ID document fields from a file source URL of an identity document.
 * See fields found on an identity document <a href=https://aka.ms/documentintelligence/iddocumentfields>href</a>
 */
public class AnalyzeIdentityDocumentsFromUrl {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String licenseDocumentUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/documentintelligence/"
                + "azure-ai-documentintelligence/src/samples/resources/sample-forms/IdentityDocuments/license.png";
        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeIdentityDocumentPoller
            = client.beginAnalyzeDocument("prebuilt-idDocument",
            null,
            null,
            null,
            null,
            null,
            null,
            null, new AnalyzeDocumentRequest().setUrlSource(licenseDocumentUrl));

        AnalyzeResult identityDocumentResults = analyzeIdentityDocumentPoller.getFinalResult();

        for (int i = 0; i < identityDocumentResults.getDocuments().size(); i++) {
            Document analyzedIDDocument = identityDocumentResults.getDocuments().get(i);
            Map<String, DocumentField> licenseFields = analyzedIDDocument.getFields();
            System.out.printf("----------- Analyzed license info for page %d -----------%n", i);
            DocumentField addressField = licenseFields.get("Address");
            if (addressField != null) {
                if (DocumentFieldType.STRING == addressField.getType()) {
                    String address = addressField.getValueString();
                    System.out.printf("Address: %s, confidence: %.2f%n",
                        address, addressField.getConfidence());
                }
            }

            DocumentField countryRegionDocumentField = licenseFields.get("CountryRegion");
            if (countryRegionDocumentField != null) {
                if (DocumentFieldType.STRING == countryRegionDocumentField.getType()) {
                    String countryRegion = countryRegionDocumentField.getValueCountryRegion();
                    System.out.printf("Country or region: %s, confidence: %.2f%n",
                        countryRegion, countryRegionDocumentField.getConfidence());
                }
            }

            DocumentField dateOfBirthField = licenseFields.get("DateOfBirth");
            if (dateOfBirthField != null) {
                if (DocumentFieldType.DATE == dateOfBirthField.getType()) {
                    LocalDate dateOfBirth = dateOfBirthField.getValueDate();
                    System.out.printf("Date of Birth: %s, confidence: %.2f%n",
                        dateOfBirth, dateOfBirthField.getConfidence());
                }
            }

            DocumentField dateOfExpirationField = licenseFields.get("DateOfExpiration");
            if (dateOfExpirationField != null) {
                if (DocumentFieldType.DATE == dateOfExpirationField.getType()) {
                    LocalDate expirationDate = dateOfExpirationField.getValueDate();
                    System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                        expirationDate, dateOfExpirationField.getConfidence());
                }
            }

            DocumentField documentNumberField = licenseFields.get("DocumentNumber");
            if (documentNumberField != null) {
                if (DocumentFieldType.STRING == documentNumberField.getType()) {
                    String documentNumber = documentNumberField.getValueString();
                    System.out.printf("Document number: %s, confidence: %.2f%n",
                        documentNumber, documentNumberField.getConfidence());
                }
            }

            DocumentField firstNameField = licenseFields.get("FirstName");
            if (firstNameField != null) {
                if (DocumentFieldType.STRING == firstNameField.getType()) {
                    String firstName = firstNameField.getValueString();
                    System.out.printf("First Name: %s, confidence: %.2f%n",
                        firstName, documentNumberField.getConfidence());
                }
            }

            DocumentField lastNameField = licenseFields.get("LastName");
            if (lastNameField != null) {
                if (DocumentFieldType.STRING == lastNameField.getType()) {
                    String lastName = lastNameField.getValueString();
                    System.out.printf("Last name: %s, confidence: %.2f%n",
                        lastName, lastNameField.getConfidence());
                }
            }

            DocumentField regionField = licenseFields.get("Region");
            if (regionField != null) {
                if (DocumentFieldType.STRING == regionField.getType()) {
                    String region = regionField.getValueString();
                    System.out.printf("Region: %s, confidence: %.2f%n",
                        region, regionField.getConfidence());
                }
            }
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

/**
 * Sample for analyzing commonly found License document fields from a local file input stream.
 * See fields found on an identity document <a href=https://aka.ms/formrecognizer/iddocumentfields>here</a>
 */
public class AnalyzeIdentityDocuments {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IOException from reading file.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File licenseDocumentFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/identityDocuments/license.png");
        Path filePath = licenseDocumentFile.toPath();
        BinaryData fileData = BinaryData.fromFile(filePath, (int) licenseDocumentFile.length());

        SyncPoller<OperationResult, AnalyzeResult> analyzeIdentityDocumentPoller =
            client.beginAnalyzeDocument("prebuilt-idDocument", fileData);

        AnalyzeResult identityDocumentResults = analyzeIdentityDocumentPoller.getFinalResult();

        for (int i = 0; i < identityDocumentResults.getDocuments().size(); i++) {
            AnalyzedDocument analyzedIDDocument = identityDocumentResults.getDocuments().get(i);
            Map<String, DocumentField> licenseFields = analyzedIDDocument.getFields();
            System.out.printf("----------- Analyzed license info for page %d -----------%n", i);
            DocumentField addressField = licenseFields.get("Address");
            if (addressField != null) {
                if (DocumentFieldType.STRING == addressField.getType()) {
                    String address = addressField.getValueAsString();
                    System.out.printf("Address: %s, confidence: %.2f%n",
                        address, addressField.getConfidence());
                }
            }

            DocumentField countryRegionDocumentField = licenseFields.get("CountryRegion");
            if (countryRegionDocumentField != null) {
                if (DocumentFieldType.STRING == countryRegionDocumentField.getType()) {
                    String countryRegion = countryRegionDocumentField.getValueAsCountry();
                    System.out.printf("Country or region: %s, confidence: %.2f%n",
                        countryRegion, countryRegionDocumentField.getConfidence());
                }
            }

            DocumentField dateOfBirthField = licenseFields.get("DateOfBirth");
            if (dateOfBirthField != null) {
                if (DocumentFieldType.DATE == dateOfBirthField.getType()) {
                    LocalDate dateOfBirth = dateOfBirthField.getValueAsDate();
                    System.out.printf("Date of Birth: %s, confidence: %.2f%n",
                        dateOfBirth, dateOfBirthField.getConfidence());
                }
            }

            DocumentField dateOfExpirationField = licenseFields.get("DateOfExpiration");
            if (dateOfExpirationField != null) {
                if (DocumentFieldType.DATE == dateOfExpirationField.getType()) {
                    LocalDate expirationDate = dateOfExpirationField.getValueAsDate();
                    System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                        expirationDate, dateOfExpirationField.getConfidence());
                }
            }

            DocumentField documentNumberField = licenseFields.get("DocumentNumber");
            if (documentNumberField != null) {
                if (DocumentFieldType.STRING == documentNumberField.getType()) {
                    String documentNumber = documentNumberField.getValueAsString();
                    System.out.printf("Document number: %s, confidence: %.2f%n",
                        documentNumber, documentNumberField.getConfidence());
                }
            }

            DocumentField firstNameField = licenseFields.get("FirstName");
            if (firstNameField != null) {
                if (DocumentFieldType.STRING == firstNameField.getType()) {
                    String firstName = firstNameField.getValueAsString();
                    System.out.printf("First Name: %s, confidence: %.2f%n",
                        firstName, documentNumberField.getConfidence());
                }
            }

            DocumentField lastNameField = licenseFields.get("LastName");
            if (lastNameField != null) {
                if (DocumentFieldType.STRING == lastNameField.getType()) {
                    String lastName = lastNameField.getValueAsString();
                    System.out.printf("Last name: %s, confidence: %.2f%n",
                        lastName, lastNameField.getConfidence());
                }
            }

            DocumentField regionField = licenseFields.get("Region");
            if (regionField != null) {
                if (DocumentFieldType.STRING == regionField.getType()) {
                    String region = regionField.getValueAsString();
                    System.out.printf("Region: %s, confidence: %.2f%n",
                        region, regionField.getConfidence());
                }
            }
        }
    }
}

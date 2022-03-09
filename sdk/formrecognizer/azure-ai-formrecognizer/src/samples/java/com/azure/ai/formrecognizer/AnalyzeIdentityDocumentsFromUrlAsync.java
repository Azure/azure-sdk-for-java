// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for analyzing commonly found identity document fields from a file source URL.
 * See fields found on a license here:
 * https://aka.ms/formrecognizer/iddocumentfields
 */
public class AnalyzeIdentityDocumentsFromUrlAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisAsyncClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String licenseDocumentUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer/"
                + "azure-ai-formrecognizer/src/samples/resources/sample-forms/IdentityDocuments/license.jpg";
        PollerFlux<DocumentOperationResult, AnalyzeResult> analyzeIdentityDocumentPoller =
            client.beginAnalyzeDocumentFromUrl("prebuilt-idDocument", licenseDocumentUrl);

        Mono<AnalyzeResult> identityDocumentPollerResult = analyzeIdentityDocumentPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        identityDocumentPollerResult.subscribe(identityDocumentResults -> {
            for (int i = 0; i < identityDocumentResults.getDocuments().size(); i++) {
                AnalyzedDocument analyzedIDDocument = identityDocumentResults.getDocuments().get(i);
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
                            firstName, firstNameField.getConfidence());
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
        });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

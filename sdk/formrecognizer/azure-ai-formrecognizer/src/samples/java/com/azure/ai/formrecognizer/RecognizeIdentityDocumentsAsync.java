// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;

/**
 * Async sample for recognizing commonly found license fields from a local file input stream of an license identity
 * document. See fields found on an license here: https://aka.ms/formrecognizer/iddocumentfields
 */
public class RecognizeIdentityDocumentsAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File licenseDocumentFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/identityDocuments/license.jpg");
        byte[] fileContent = Files.readAllBytes(licenseDocumentFile.toPath());

        PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> recognizeIdentityDocumentPoller
            = client.beginRecognizeIdentityDocuments(
            toFluxByteBuffer(new ByteArrayInputStream(fileContent)),
            fileContent.length);

        Mono<List<RecognizedForm>> identityDocumentPollerResult = recognizeIdentityDocumentPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        identityDocumentPollerResult.subscribe(idDocumentResults -> {
            for (int i = 0; i < idDocumentResults.size(); i++) {
                RecognizedForm recognizedForm = idDocumentResults.get(i);
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

                FormField countryRegionFormField = recognizedFields.get("CountryRegion");
                if (countryRegionFormField != null) {
                    if (FieldValueType.STRING == countryRegionFormField.getValue().getValueType()) {
                        String countryRegion = countryRegionFormField.getValue().asCountryRegion();
                        System.out.printf("Country or region: %s, confidence: %.2f%n",
                            countryRegion, countryRegionFormField.getConfidence());
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
                            firstName, firstNameField.getConfidence());
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

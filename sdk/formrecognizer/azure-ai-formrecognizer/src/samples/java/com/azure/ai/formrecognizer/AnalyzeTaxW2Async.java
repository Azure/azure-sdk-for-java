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

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Async Sample for analyzing commonly found W-2 fields from a local file input stream of a tax W-2 document.
 * See fields found on a US Tax W2 document here:
 * https://aka.ms/formrecognizer/taxusw2fieldschema
 */
public class AnalyzeTaxW2Async {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisAsyncClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String w2Url =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/resources/sample-forms/w2/Sample-W2.jpg";

        PollerFlux<DocumentOperationResult, AnalyzeResult> analyzeW2Poller =
            client.beginAnalyzeDocumentFromUrl("prebuilt-tax.us.w2", w2Url);

        Mono<AnalyzeResult> w2Mono = analyzeW2Poller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        w2Mono.subscribe(analyzeTaxResult -> {

            for (int i = 0; i < analyzeTaxResult.getDocuments().size(); i++) {
                AnalyzedDocument analyzedTaxDocument = analyzeTaxResult.getDocuments().get(i);
                Map<String, DocumentField> taxFields = analyzedTaxDocument.getFields();
                System.out.printf("----------- Analyzing Document  %d -----------%n", i);
                DocumentField w2FormVariantField = taxFields.get("W2FormVariant");
                if (w2FormVariantField != null) {
                    if (DocumentFieldType.STRING == w2FormVariantField.getType()) {
                        String merchantName = w2FormVariantField.getValueString();
                        System.out.printf("Form variant: %s, confidence: %.2f%n",
                            merchantName, w2FormVariantField.getConfidence());
                    }
                }

                DocumentField employeeField = taxFields.get("Employee");
                if (employeeField != null) {
                    System.out.println("Employee Data: ");
                    if (DocumentFieldType.MAP == employeeField.getType()) {
                        Map<String, DocumentField> employeeDataFieldMap = employeeField.getValueMap();
                        DocumentField employeeName = employeeDataFieldMap.get("Name");
                        if (employeeName != null) {
                            if (DocumentFieldType.STRING == employeeName.getType()) {
                                String merchantAddress = employeeName.getValueString();
                                System.out.printf("Employee Name: %s, confidence: %.2f%n",
                                    merchantAddress, employeeName.getConfidence());
                            }
                        }
                        DocumentField employeeAddrField = employeeDataFieldMap.get("Address");
                        if (employeeAddrField != null) {
                            if (DocumentFieldType.STRING == employeeAddrField.getType()) {
                                String employeeAddress = employeeAddrField.getValueString();
                                System.out.printf("Employee Address: %s, confidence: %.2f%n",
                                    employeeAddress, employeeAddrField.getConfidence());
                            }
                        }
                    }
                }

                DocumentField employerField = taxFields.get("Employer");
                if (employerField != null) {
                    System.out.println("Employer Data: ");
                    if (DocumentFieldType.MAP == employerField.getType()) {
                        Map<String, DocumentField> employerDataFieldMap = employerField.getValueMap();
                        DocumentField employerNameField = employerDataFieldMap.get("Name");
                        if (employerNameField != null) {
                            if (DocumentFieldType.STRING == employerNameField.getType()) {
                                String employerName = employerNameField.getValueString();
                                System.out.printf("Employee Name: %s, confidence: %.2f%n",
                                    employerName, employerNameField.getConfidence());
                            }
                        }

                        DocumentField employerIDNumberField = employerDataFieldMap.get("IdNumber");
                        if (employerIDNumberField != null) {
                            if (DocumentFieldType.STRING == employerIDNumberField.getType()) {
                                String employerIdNumber = employerIDNumberField.getValueString();
                                System.out.printf("Employee ID Number: %s, confidence: %.2f%n",
                                    employerIdNumber, employerIDNumberField.getConfidence());
                            }
                        }
                    }
                }

                DocumentField localTaxInfosField = taxFields.get("LocalTaxInfos");
                if (localTaxInfosField != null) {
                    System.out.println("Local Tax Info data:");
                    if (DocumentFieldType.LIST == localTaxInfosField.getType()) {
                        Map<String, DocumentField> localTaxInfoDataFields = localTaxInfosField.getValueMap();
                        DocumentField localWagesTips = localTaxInfoDataFields.get("LocalWagesTipsEtc");
                        if (DocumentFieldType.FLOAT == localTaxInfosField.getType()) {
                            System.out.printf("Local Wages Tips Value: %.2f, confidence: %.2f%n",
                                localWagesTips.getValueFloat(), localTaxInfosField.getConfidence());
                        }
                    }
                }

                DocumentField taxYearField = taxFields.get("TaxYear");
                if (taxYearField != null) {
                    if (DocumentFieldType.STRING == taxYearField.getType()) {
                        String taxYear = taxYearField.getValueString();
                        System.out.printf("Tax year: %s, confidence: %.2f%n",
                            taxYear, taxYearField.getConfidence());
                    }
                }

                DocumentField taxDateField = taxFields.get("TaxDate");
                if (employeeField != null) {
                    if (DocumentFieldType.DATE == taxDateField.getType()) {
                        LocalDate taxDate = taxDateField.getValueDate();
                        System.out.printf("Tax Date: %s, confidence: %.2f%n",
                            taxDate, taxDateField.getConfidence());
                    }
                }

                DocumentField socialSecurityTaxField = taxFields.get("SocialSecurityTaxWithheld");
                if (localTaxInfosField != null) {
                    if (DocumentFieldType.FLOAT == socialSecurityTaxField.getType()) {
                        Float socialSecurityTax = socialSecurityTaxField.getValueFloat();
                        System.out.printf("Social Security Tax withheld: %.2f, confidence: %.2f%n",
                            socialSecurityTax, socialSecurityTaxField.getConfidence());
                    }
                }
            }
        });
    }
}

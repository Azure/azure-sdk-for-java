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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Map;

/**
 * Sample for analyzing commonly found W-2 fields from a local file input stream of a tax W-2 document.
 * See fields found on a US Tax W2 document <a href=https://aka.ms/documentintelligence/taxusw2fieldschema>here</a>
 */
public class AnalyzeTaxW2 {
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

        File invoice = new File("./documentintelligence/azure-ai-documentintelligence/src/samples/resources/Sample-W2.jpg");

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeW2Poller =
            client.beginAnalyzeDocument("prebuilt-tax.us.w2", null,
                null,
                null,
                null,
                null,
                null,
                null,
                new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(invoice.toPath())));

        AnalyzeResult analyzeTaxResult = analyzeW2Poller.getFinalResult();

        for (int i = 0; i < analyzeTaxResult.getDocuments().size(); i++) {
            Document analyzedTaxDocument = analyzeTaxResult.getDocuments().get(i);
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
                if (DocumentFieldType.OBJECT == employeeField.getType()) {
                    Map<String, DocumentField> employeeDataFieldMap = employeeField.getValueObject();
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
                if (DocumentFieldType.OBJECT == employerField.getType()) {
                    Map<String, DocumentField> employerDataFieldMap = employerField.getValueObject();
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
                if (DocumentFieldType.ARRAY == localTaxInfosField.getType()) {
                    Map<String, DocumentField> localTaxInfoDataFields = localTaxInfosField.getValueObject();
                    DocumentField localWagesTips = localTaxInfoDataFields.get("LocalWagesTipsEtc");
                    if (DocumentFieldType.NUMBER == localTaxInfosField.getType()) {
                        System.out.printf("Local Wages Tips Value: %.2f, confidence: %.2f%n",
                            localWagesTips.getValueNumber(), localTaxInfosField.getConfidence());
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
                if (DocumentFieldType.NUMBER == socialSecurityTaxField.getType()) {
                    Double socialSecurityTax = socialSecurityTaxField.getValueNumber();
                    System.out.printf("Social Security Tax withheld: %.2f, confidence: %.2f%n",
                        socialSecurityTax, socialSecurityTaxField.getConfidence());
                }
            }
        }
    }
}

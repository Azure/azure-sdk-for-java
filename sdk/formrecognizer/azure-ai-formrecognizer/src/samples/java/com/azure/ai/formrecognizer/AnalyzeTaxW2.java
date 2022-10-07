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
 * Sample for analyzing commonly found W-2 fields from a local file input stream of a tax W-2 document.
 * See fields found on a US Tax W2 document <a href=https://aka.ms/formrecognizer/taxusw2fieldschema>here</a>
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
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File invoice = new File("./formrecognizer/azure-ai-formrecognizer/src/samples/resources/Sample-W2.jpg");
        Path filePath = invoice.toPath();
        BinaryData invoiceData = BinaryData.fromFile(filePath);

        SyncPoller<OperationResult, AnalyzeResult> analyzeW2Poller =
            client.beginAnalyzeDocument("prebuilt-tax.us.w2", invoiceData);

        AnalyzeResult analyzeTaxResult = analyzeW2Poller.getFinalResult();

        for (int i = 0; i < analyzeTaxResult.getDocuments().size(); i++) {
            AnalyzedDocument analyzedTaxDocument = analyzeTaxResult.getDocuments().get(i);
            Map<String, DocumentField> taxFields = analyzedTaxDocument.getFields();
            System.out.printf("----------- Analyzing Document  %d -----------%n", i);
            DocumentField w2FormVariantField = taxFields.get("W2FormVariant");
            if (w2FormVariantField != null) {
                if (DocumentFieldType.STRING == w2FormVariantField.getType()) {
                    String merchantName = w2FormVariantField.getValueAsString();
                    System.out.printf("Form variant: %s, confidence: %.2f%n",
                        merchantName, w2FormVariantField.getConfidence());
                }
            }

            DocumentField employeeField = taxFields.get("Employee");
            if (employeeField != null) {
                System.out.println("Employee Data: ");
                if (DocumentFieldType.MAP == employeeField.getType()) {
                    Map<String, DocumentField> employeeDataFieldMap = employeeField.getValueAsMap();
                    DocumentField employeeName = employeeDataFieldMap.get("Name");
                    if (employeeName != null) {
                        if (DocumentFieldType.STRING == employeeName.getType()) {
                            String merchantAddress = employeeName.getValueAsString();
                            System.out.printf("Employee Name: %s, confidence: %.2f%n",
                                merchantAddress, employeeName.getConfidence());
                        }
                    }
                    DocumentField employeeAddrField = employeeDataFieldMap.get("Address");
                    if (employeeAddrField != null) {
                        if (DocumentFieldType.STRING == employeeAddrField.getType()) {
                            String employeeAddress = employeeAddrField.getValueAsString();
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
                    Map<String, DocumentField> employerDataFieldMap = employerField.getValueAsMap();
                    DocumentField employerNameField = employerDataFieldMap.get("Name");
                    if (employerNameField != null) {
                        if (DocumentFieldType.STRING == employerNameField.getType()) {
                            String employerName = employerNameField.getValueAsString();
                            System.out.printf("Employee Name: %s, confidence: %.2f%n",
                                employerName, employerNameField.getConfidence());
                        }
                    }

                    DocumentField employerIDNumberField = employerDataFieldMap.get("IdNumber");
                    if (employerIDNumberField != null) {
                        if (DocumentFieldType.STRING == employerIDNumberField.getType()) {
                            String employerIdNumber = employerIDNumberField.getValueAsString();
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
                    Map<String, DocumentField> localTaxInfoDataFields = localTaxInfosField.getValueAsMap();
                    DocumentField localWagesTips = localTaxInfoDataFields.get("LocalWagesTipsEtc");
                    if (DocumentFieldType.DOUBLE == localTaxInfosField.getType()) {
                        System.out.printf("Local Wages Tips Value: %.2f, confidence: %.2f%n",
                            localWagesTips.getValueAsDouble(), localTaxInfosField.getConfidence());
                    }
                }
            }

            DocumentField taxYearField = taxFields.get("TaxYear");
            if (taxYearField != null) {
                if (DocumentFieldType.STRING == taxYearField.getType()) {
                    String taxYear = taxYearField.getValueAsString();
                    System.out.printf("Tax year: %s, confidence: %.2f%n",
                        taxYear, taxYearField.getConfidence());
                }
            }

            DocumentField taxDateField = taxFields.get("TaxDate");
            if (employeeField != null) {
                if (DocumentFieldType.DATE == taxDateField.getType()) {
                    LocalDate taxDate = taxDateField.getValueAsDate();
                    System.out.printf("Tax Date: %s, confidence: %.2f%n",
                        taxDate, taxDateField.getConfidence());
                }
            }

            DocumentField socialSecurityTaxField = taxFields.get("SocialSecurityTaxWithheld");
            if (localTaxInfosField != null) {
                if (DocumentFieldType.DOUBLE == socialSecurityTaxField.getType()) {
                    Double socialSecurityTax = socialSecurityTaxField.getValueAsDouble();
                    System.out.printf("Social Security Tax withheld: %.2f, confidence: %.2f%n",
                        socialSecurityTax, socialSecurityTaxField.getConfidence());
                }
            }
        }
    }
}

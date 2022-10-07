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
import java.util.List;
import java.util.Map;

/**
 * Sample for analyzing commonly found invoice fields from a local file input stream of an invoice document.
 * See fields found on an invoice <a href=https://aka.ms/formrecognizer/invoicefields>here</a>
 */
public class AnalyzeInvoices {
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

        File invoice = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
                                    + "sample-forms/invoices/sample_invoice.jpg");
        Path filePath = invoice.toPath();
        BinaryData invoiceData = BinaryData.fromFile(filePath);

        SyncPoller<OperationResult, AnalyzeResult> analyzeInvoicesPoller =
            client.beginAnalyzeDocument("prebuilt-invoice", invoiceData);

        AnalyzeResult analyzeInvoiceResult = analyzeInvoicesPoller.getFinalResult();

        for (int i = 0; i < analyzeInvoiceResult.getDocuments().size(); i++) {
            AnalyzedDocument analyzedInvoice = analyzeInvoiceResult.getDocuments().get(i);
            Map<String, DocumentField> invoiceFields = analyzedInvoice.getFields();
            System.out.printf("----------- Analyzing invoice  %d -----------%n", i);
            DocumentField vendorNameField = invoiceFields.get("VendorName");
            if (vendorNameField != null) {
                if (DocumentFieldType.STRING == vendorNameField.getType()) {
                    String merchantName = vendorNameField.getValueAsString();
                    System.out.printf("Vendor Name: %s, confidence: %.2f%n",
                        merchantName, vendorNameField.getConfidence());
                }
            }

            DocumentField vendorAddressField = invoiceFields.get("VendorAddress");
            if (vendorAddressField != null) {
                if (DocumentFieldType.STRING == vendorAddressField.getType()) {
                    String merchantAddress = vendorAddressField.getValueAsString();
                    System.out.printf("Vendor address: %s, confidence: %.2f%n",
                        merchantAddress, vendorAddressField.getConfidence());
                }
            }

            DocumentField customerNameField = invoiceFields.get("CustomerName");
            if (customerNameField != null) {
                if (DocumentFieldType.STRING == customerNameField.getType()) {
                    String merchantAddress = customerNameField.getValueAsString();
                    System.out.printf("Customer Name: %s, confidence: %.2f%n",
                        merchantAddress, customerNameField.getConfidence());
                }
            }

            DocumentField customerAddressRecipientField = invoiceFields.get("CustomerAddressRecipient");
            if (customerAddressRecipientField != null) {
                if (DocumentFieldType.STRING == customerAddressRecipientField.getType()) {
                    String customerAddr = customerAddressRecipientField.getValueAsString();
                    System.out.printf("Customer Address Recipient: %s, confidence: %.2f%n",
                        customerAddr, customerAddressRecipientField.getConfidence());
                }
            }

            DocumentField invoiceIdField = invoiceFields.get("InvoiceId");
            if (invoiceIdField != null) {
                if (DocumentFieldType.STRING == invoiceIdField.getType()) {
                    String invoiceId = invoiceIdField.getValueAsString();
                    System.out.printf("Invoice ID: %s, confidence: %.2f%n",
                        invoiceId, invoiceIdField.getConfidence());
                }
            }

            DocumentField invoiceDateField = invoiceFields.get("InvoiceDate");
            if (customerNameField != null) {
                if (DocumentFieldType.DATE == invoiceDateField.getType()) {
                    LocalDate invoiceDate = invoiceDateField.getValueAsDate();
                    System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                        invoiceDate, invoiceDateField.getConfidence());
                }
            }

            DocumentField invoiceTotalField = invoiceFields.get("InvoiceTotal");
            if (customerAddressRecipientField != null) {
                if (DocumentFieldType.DOUBLE == invoiceTotalField.getType()) {
                    Double invoiceTotal = invoiceTotalField.getValueAsDouble();
                    System.out.printf("Invoice Total: %.2f, confidence: %.2f%n",
                        invoiceTotal, invoiceTotalField.getConfidence());
                }
            }

            DocumentField invoiceItemsField = invoiceFields.get("Items");
            if (invoiceItemsField != null) {
                System.out.printf("Invoice Items: %n");
                if (DocumentFieldType.LIST == invoiceItemsField.getType()) {
                    List<DocumentField> invoiceItems = invoiceItemsField.getValueAsList();
                    invoiceItems.stream()
                        .filter(invoiceItem -> DocumentFieldType.MAP == invoiceItem.getType())
                        .map(documentField -> documentField.getValueAsMap())
                        .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                            // See a full list of fields found on an invoice here:
                            // https://aka.ms/formrecognizer/invoicefields
                            if ("Description".equals(key)) {
                                if (DocumentFieldType.STRING == documentField.getType()) {
                                    String name = documentField.getValueAsString();
                                    System.out.printf("Description: %s, confidence: %.2fs%n",
                                        name, documentField.getConfidence());
                                }
                            }
                            if ("Quantity".equals(key)) {
                                if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                    Double quantity = documentField.getValueAsDouble();
                                    System.out.printf("Quantity: %f, confidence: %.2f%n",
                                        quantity, documentField.getConfidence());
                                }
                            }
                            if ("UnitPrice".equals(key)) {
                                if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                    Double unitPrice = documentField.getValueAsDouble();
                                    System.out.printf("Unit Price: %f, confidence: %.2f%n",
                                        unitPrice, documentField.getConfidence());
                                }
                            }
                            if ("ProductCode".equals(key)) {
                                if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                    Double productCode = documentField.getValueAsDouble();
                                    System.out.printf("Product Code: %f, confidence: %.2f%n",
                                        productCode, documentField.getConfidence());
                                }
                            }
                        }));
                }
            }
        }
    }
}

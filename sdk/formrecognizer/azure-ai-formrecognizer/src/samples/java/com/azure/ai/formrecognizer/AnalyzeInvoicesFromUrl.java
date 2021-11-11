// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sample for analyzing commonly found invoice fields from a file source URL of an invoice document.
 * See fields found on an invoice here:
 * https://aka.ms/formrecognizer/invoicefields
 */
public class AnalyzeInvoicesFromUrl {

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

        String invoiceUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-python/main/sdk/formrecognizer/"
                + "azure-ai-formrecognizer/samples/sample_forms/forms/sample_invoice.jpg";

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeInvoicesPoller
            = client.beginAnalyzeDocumentFromUrl("prebuilt-invoice", invoiceUrl);

        AnalyzeResult analyzeInvoiceResult = analyzeInvoicesPoller.getFinalResult();

        for (int i = 0; i < analyzeInvoiceResult.getDocuments().size(); i++) {
            AnalyzedDocument analyzedInvoice = analyzeInvoiceResult.getDocuments().get(i);
            Map<String, DocumentField> invoiceFields = analyzedInvoice.getFields();
            System.out.printf("----------- Analyzing invoice  %d -----------%n", i);
            DocumentField vendorNameField = invoiceFields.get("VendorName");
            if (vendorNameField != null) {
                if (DocumentFieldType.STRING == vendorNameField.getType()) {
                    String merchantName = vendorNameField.getValueString();
                    System.out.printf("Vendor Name: %s, confidence: %.2f%n",
                        merchantName, vendorNameField.getConfidence());
                }
            }

            DocumentField vendorAddressField = invoiceFields.get("VendorAddress");
            if (vendorAddressField != null) {
                if (DocumentFieldType.STRING == vendorAddressField.getType()) {
                    String merchantAddress = vendorAddressField.getValueString();
                    System.out.printf("Vendor address: %s, confidence: %.2f%n",
                        merchantAddress, vendorAddressField.getConfidence());
                }
            }

            DocumentField customerNameField = invoiceFields.get("CustomerName");
            if (customerNameField != null) {
                if (DocumentFieldType.STRING == customerNameField.getType()) {
                    String merchantAddress = customerNameField.getValueString();
                    System.out.printf("Customer Name: %s, confidence: %.2f%n",
                        merchantAddress, customerNameField.getConfidence());
                }
            }

            DocumentField customerAddressRecipientField = invoiceFields.get("CustomerAddressRecipient");
            if (customerAddressRecipientField != null) {
                if (DocumentFieldType.STRING == customerAddressRecipientField.getType()) {
                    String customerAddr = customerAddressRecipientField.getValueString();
                    System.out.printf("Customer Address Recipient: %s, confidence: %.2f%n",
                        customerAddr, customerAddressRecipientField.getConfidence());
                }
            }

            DocumentField invoiceIdField = invoiceFields.get("InvoiceId");
            if (invoiceIdField != null) {
                if (DocumentFieldType.STRING == invoiceIdField.getType()) {
                    String invoiceId = invoiceIdField.getValueString();
                    System.out.printf("Invoice ID: %s, confidence: %.2f%n",
                        invoiceId, invoiceIdField.getConfidence());
                }
            }

            DocumentField invoiceDateField = invoiceFields.get("InvoiceDate");
            if (customerNameField != null) {
                if (DocumentFieldType.DATE == invoiceDateField.getType()) {
                    LocalDate invoiceDate = invoiceDateField.getValueDate();
                    System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                        invoiceDate, invoiceDateField.getConfidence());
                }
            }

            DocumentField invoiceTotalField = invoiceFields.get("InvoiceTotal");
            if (customerAddressRecipientField != null) {
                if (DocumentFieldType.FLOAT == invoiceTotalField.getType()) {
                    Float invoiceTotal = invoiceTotalField.getValueFloat();
                    System.out.printf("Invoice Total: %.2f, confidence: %.2f%n",
                        invoiceTotal, invoiceTotalField.getConfidence());
                }
            }

            DocumentField invoiceItemsField = invoiceFields.get("Items");
            if (invoiceItemsField != null) {
                System.out.printf("Invoice Items: %n");
                if (DocumentFieldType.LIST == invoiceItemsField.getType()) {
                    List<DocumentField> invoiceItems = invoiceItemsField.getValueList();
                    invoiceItems.stream()
                        .filter(invoiceItem -> DocumentFieldType.MAP == invoiceItem.getType())
                        .map(formField -> formField.getValueMap())
                        .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                            // See a full list of fields found on an invoice here:
                            // https://aka.ms/formrecognizer/invoicefields
                            if ("Description".equals(key)) {
                                if (DocumentFieldType.STRING == formField.getType()) {
                                    String name = formField.getValueString();
                                    System.out.printf("Description: %s, confidence: %.2fs%n",
                                        name, formField.getConfidence());
                                }
                            }
                            if ("Quantity".equals(key)) {
                                if (DocumentFieldType.FLOAT == formField.getType()) {
                                    Float quantity = formField.getValueFloat();
                                    System.out.printf("Quantity: %f, confidence: %.2f%n",
                                        quantity, formField.getConfidence());
                                }
                            }
                            if ("UnitPrice".equals(key)) {
                                if (DocumentFieldType.FLOAT == formField.getType()) {
                                    Float unitPrice = formField.getValueFloat();
                                    System.out.printf("Unit Price: %f, confidence: %.2f%n",
                                        unitPrice, formField.getConfidence());
                                }
                            }
                            if ("ProductCode".equals(key)) {
                                if (DocumentFieldType.FLOAT == formField.getType()) {
                                    Float productCode = formField.getValueFloat();
                                    System.out.printf("Product Code: %f, confidence: %.2f%n",
                                        productCode, formField.getConfidence());
                                }
                            }
                        }));
                }
            }
        }
    }
}

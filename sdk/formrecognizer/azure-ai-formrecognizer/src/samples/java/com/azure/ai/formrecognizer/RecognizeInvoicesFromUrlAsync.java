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

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for recognizing commonly found invoice fields from a file source URL of an invoice document.
 * For a suggested approach to
 * extracting information from a general recognized form, see StronglyTypedRecognizedForm.java.
 * See fields found on a invoice here:
 * https://aka.ms/formrecognizer/invoicefields
 */
public class RecognizeInvoicesFromUrlAsync {
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

        String invoiceUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-python/main/sdk/formrecognizer/"
                + "azure-ai-formrecognizer/samples/sample_forms/forms/sample_invoice.jpg";
        PollerFlux<FormRecognizerOperationResult, List<RecognizedForm>> recognizeInvoicesPoller
            = client.beginRecognizeInvoicesFromUrl(invoiceUrl);

        Mono<List<RecognizedForm>> invoicePageResultMono =
            recognizeInvoicesPoller
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

        invoicePageResultMono.subscribe(recognizedInvoices -> {
            for (int i = 0; i < recognizedInvoices.size(); i++) {
                RecognizedForm recognizedInvoice = recognizedInvoices.get(i);
                Map<String, FormField> recognizedFields = recognizedInvoice.getFields();
                System.out.printf("----------- Recognized invoice info for page %d -----------%n", i);
                FormField vendorNameField = recognizedFields.get("VendorName");
                if (vendorNameField != null) {
                    if (FieldValueType.STRING == vendorNameField.getValue().getValueType()) {
                        String merchantName = vendorNameField.getValue().asString();
                        System.out.printf("Vendor Name: %s, confidence: %.2f%n",
                            merchantName, vendorNameField.getConfidence());
                    }
                }

                FormField vendorAddressField = recognizedFields.get("VendorAddress");
                if (vendorAddressField != null) {
                    if (FieldValueType.STRING == vendorAddressField.getValue().getValueType()) {
                        String merchantAddress = vendorAddressField.getValue().asString();
                        System.out.printf("Vendor address: %s, confidence: %.2f%n",
                            merchantAddress, vendorAddressField.getConfidence());
                    }
                }

                FormField customerNameField = recognizedFields.get("CustomerName");
                if (customerNameField != null) {
                    if (FieldValueType.STRING == customerNameField.getValue().getValueType()) {
                        String merchantAddress = customerNameField.getValue().asString();
                        System.out.printf("Customer Name: %s, confidence: %.2f%n",
                            merchantAddress, customerNameField.getConfidence());
                    }
                }

                FormField customerAddressRecipientField = recognizedFields.get("CustomerAddressRecipient");
                if (customerAddressRecipientField != null) {
                    if (FieldValueType.STRING == customerAddressRecipientField.getValue().getValueType()) {
                        String customerAddr = customerAddressRecipientField.getValue().asString();
                        System.out.printf("Customer Address Recipient: %s, confidence: %.2f%n",
                            customerAddr, customerAddressRecipientField.getConfidence());
                    }
                }

                FormField invoiceIdField = recognizedFields.get("InvoiceId");
                if (invoiceIdField != null) {
                    if (FieldValueType.STRING == invoiceIdField.getValue().getValueType()) {
                        String invoiceId = invoiceIdField.getValue().asString();
                        System.out.printf("Invoice Id: %s, confidence: %.2f%n",
                            invoiceId, invoiceIdField.getConfidence());
                    }
                }

                FormField invoiceDateField = recognizedFields.get("InvoiceDate");
                if (customerNameField != null) {
                    if (FieldValueType.DATE == invoiceDateField.getValue().getValueType()) {
                        LocalDate invoiceDate = invoiceDateField.getValue().asDate();
                        System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                            invoiceDate, invoiceDateField.getConfidence());
                    }
                }

                FormField invoiceTotalField = recognizedFields.get("InvoiceTotal");
                if (customerAddressRecipientField != null) {
                    if (FieldValueType.FLOAT == invoiceTotalField.getValue().getValueType()) {
                        Float invoiceTotal = invoiceTotalField.getValue().asFloat();
                        System.out.printf("Invoice Total: %.2f, confidence: %.2f%n",
                            invoiceTotal, invoiceTotalField.getConfidence());
                    }
                }

                FormField invoiceItemsField = recognizedFields.get("Items");
                if (invoiceItemsField != null) {
                    System.out.printf("Invoice Items: %n");
                    if (FieldValueType.LIST == invoiceItemsField.getValue().getValueType()) {
                        List<FormField> invoiceItems = invoiceItemsField.getValue().asList();
                        invoiceItems.stream()
                            .filter(invoiceItem -> FieldValueType.MAP == invoiceItem.getValue().getValueType())
                            .map(formField -> formField.getValue().asMap())
                            .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                // See a full list of fields found on an invoice here:
                                // https://aka.ms/formrecognizer/invoicefields
                                if ("Description".equals(key)) {
                                    if (FieldValueType.STRING == formField.getValue().getValueType()) {
                                        String name = formField.getValue().asString();
                                        System.out.printf("Description: %s, confidence: %.2fs%n",
                                            name, formField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                        Float quantity = formField.getValue().asFloat();
                                        System.out.printf("Quantity: %f, confidence: %.2f%n",
                                            quantity, formField.getConfidence());
                                    }
                                }
                                if ("UnitPrice".equals(key)) {
                                    if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                        Float unitPrice = formField.getValue().asFloat();
                                        System.out.printf("Unit Price: %f, confidence: %.2f%n",
                                            unitPrice, formField.getConfidence());
                                    }
                                }
                                if ("ProductCode".equals(key)) {
                                    if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                        Float productCode = formField.getValue().asFloat();
                                        System.out.printf("Product Code: %f, confidence: %.2f%n",
                                            productCode, formField.getConfidence());
                                    }
                                }
                            }));
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

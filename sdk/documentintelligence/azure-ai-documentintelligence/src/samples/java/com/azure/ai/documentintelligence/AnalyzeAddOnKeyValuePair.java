// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.BoundingRegion;
import com.azure.ai.documentintelligence.models.DocumentAnalysisFeature;
import com.azure.ai.documentintelligence.models.DocumentKeyValueElement;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * This sample demonstrates how to extract all identified barcodes using the add-on 'KeyValuePair' capability.
 * Add-on capabilities are available within all models except for the Business card model.
 * This sample uses Layout model to demonstrate.
 */
public class AnalyzeAddOnKeyValuePair {
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

        File invoiceDocument = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
                + "sample-forms/invoices/Invoice_1.pdf");

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutResultPoller =
                client.beginAnalyzeDocument("prebuilt-layout", null,
                        null,
                        null,
                        Arrays.asList(DocumentAnalysisFeature.KEY_VALUE_PAIRS),
                        null,
                        null,
                        null,
                        new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(invoiceDocument.toPath())));

        AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

        analyzeLayoutResult.getKeyValuePairs().forEach(
                keyValuePair -> {
                    DocumentKeyValueElement key = keyValuePair.getKey();
                    DocumentKeyValueElement value = keyValuePair.getValue();
                    if (key != null) {
                        System.out.println("- Key: " + key.getContent());
                        List<BoundingRegion> boundingRegions = key.getBoundingRegions();
                        boundingRegions.forEach(boundingRegion -> {
                            System.out.printf("  Bounding regions page: %s, polygon: %s%n",
                                    boundingRegion.getPageNumber(), boundingRegion.getPolygon());
                        });
                    }
                    if (value != null) {
                        System.out.println("- Value: " + value.getContent());
                        List<BoundingRegion> boundingRegions = value.getBoundingRegions();
                        boundingRegions.forEach(boundingRegion -> {
                            System.out.printf("  Bounding regions page: %s, polygon: %s%n",
                                    boundingRegion.getPageNumber(), boundingRegion.getPolygon());
                        });
                    }
                }
        );
    }
}

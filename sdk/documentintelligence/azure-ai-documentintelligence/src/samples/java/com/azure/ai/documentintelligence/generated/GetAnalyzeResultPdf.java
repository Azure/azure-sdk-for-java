// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence.generated;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeOutputOption;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.StringIndexType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class GetAnalyzeResultPdf {
    public static void main(String[] args) throws IOException {
        DocumentIntelligenceClient documentIntelligenceClient
            = new DocumentIntelligenceClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myendpoint.cognitiveservices.azure.com")
            .buildClient();
        // BEGIN:com.azure.ai.documentintelligence.generated.analyzedocument.analyzedocumenttopdf
        String modelID = "prebuilt-read";
        byte[] fileBytes = Files.readAllBytes(Paths.get("layout-pageobject.pdf"));

        SyncPoller<AnalyzeResultOperation, AnalyzeResult> syncPoller = documentIntelligenceClient.beginAnalyzeDocument(
            modelID, "1-2,4", "en-US", StringIndexType.TEXT_ELEMENTS, null, null, null, Arrays.asList(AnalyzeOutputOption.PDF),
            new AnalyzeDocumentRequest().setBase64Source(fileBytes));

        AnalyzeResult analyzeResult = syncPoller.getFinalResult();
        String resultId = syncPoller.poll().getValue().getOperationId();

        // Gets the above analyzeResult in PDF output format
        BinaryData pdf = documentIntelligenceClient.getAnalyzeResultPdf(modelID, resultId);
        // END:com.azure.ai.documentintelligence.generated.analyzedocument.analyzedocumenttopdf
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeBatchDocumentsRequest;
import com.azure.ai.documentintelligence.models.AnalyzeBatchResult;
import com.azure.ai.documentintelligence.models.AnalyzeBatchResultOperation;
import com.azure.ai.documentintelligence.models.AzureBlobContentSource;
import com.azure.ai.documentintelligence.models.StringIndexType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

public class BatchAnalysis {
    public static void main(String[] args) {
        DocumentIntelligenceClient documentIntelligenceClient = new DocumentIntelligenceClientBuilder()
            .endpoint("<your-endpoint>")
            .credential(new AzureKeyCredential("<your-key>"))
            .buildClient();

        DocumentIntelligenceAdministrationClient documentAdminClient = new DocumentIntelligenceAdministrationClientBuilder()
            .endpoint("<your-endpoint>")
            .credential(new AzureKeyCredential("<your-key>"))
            .buildClient();

        String modelId = "customModel";

        // poller way of getting the batch result
        SyncPoller<AnalyzeBatchResultOperation, AnalyzeBatchResult> syncPoller
            = documentIntelligenceClient.beginAnalyzeBatchDocuments(modelId, "1-5", "en-US",
            StringIndexType.TEXT_ELEMENTS, null, null, null, null,
            new AnalyzeBatchDocumentsRequest(
                "https://myStorageAccount.blob.core.windows.net/myOutputContainer?mySasToken")
                .setAzureBlobSource(new AzureBlobContentSource(
                    "https://myStorageAccount.blob.core.windows.net/myContainer?mySasToken")
                    .setPrefix("trainingDocs/"))
                .setResultPrefix("trainingDocsResult/")
                .setOverwriteExisting(true));

        AnalyzeBatchResult finalBatchResult = syncPoller.getFinalResult();

        // This is a super long LRO, since the polling takes too long, customers can choose to fire and forget,
        // and later retrieve the results using the operation ID
        String operationId = syncPoller.poll().getValue().getOperationId();

        // Retrieve the same result as finalBatchResult
        AnalyzeBatchResult retrievedBatchResult = documentAdminClient.getAnalyzeBatchResult(modelId, operationId).getResult();
    }
}

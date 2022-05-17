// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf;

import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs document analysis using a prebuilt model operation.
 */
public class DocumentModelAnalysisTest extends ServiceTest<PerfStressOptions> {

    private static final String URL_TEST_FILE_FORMAT = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/"
        + "master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources/sample_files/Test/";
    private static final String RECEIPT_CONTOSO_PNG = "contoso-receipt.png";

    /**
     * The DocumentModelAnalysisTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public DocumentModelAnalysisTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        AnalyzeResult analyzedResult =
            documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt",
                    URL_TEST_FILE_FORMAT + RECEIPT_CONTOSO_PNG)
                .getFinalResult();
        assert analyzedResult.getPages() != null;
        assert analyzedResult.getModelId() == "prebuilt-receipt";
    }

    @Override
    public Mono<Void> runAsync() {
        return documentAnalysisAsyncClient
            .beginAnalyzeDocumentFromUrl("prebuilt-receipt",
                URL_TEST_FILE_FORMAT + RECEIPT_CONTOSO_PNG)
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    return Mono.empty();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf;

import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.perf.core.ServiceTest;
import com.azure.core.util.polling.SyncPoller;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs building a custom document model for analysis.
 */
public class BuildDocumentModelTest extends ServiceTest<PerfStressOptions> {

    private static final String FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL =
        GLOBAL_CONFIGURATION.get("FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL");

    /**
     * The BuildDocumentModelTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public BuildDocumentModelTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        SyncPoller<DocumentOperationResult, DocumentModelInfo>
            syncPoller = documentModelAdministrationAsyncClient
            .beginBuildModel(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL,
                DocumentBuildMode.TEMPLATE,
                new BuildModelOptions().setDescription("perf-training-model"))
            .getSyncPoller();
        modelId = syncPoller.getFinalResult().getModelId();
        assert modelId != null;
    }

    @Override
    public Mono<Void> runAsync() {
        return documentModelAdministrationAsyncClient
            .beginBuildModel(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL,
                DocumentBuildMode.TEMPLATE,
                new BuildModelOptions().setDescription("perf-training-model"))
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    // training completed successfully, retrieving final result.
                    pollResponse.getFinalResult().subscribe(response -> modelId = response.getModelId());
                    return Mono.empty();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });
    }
}

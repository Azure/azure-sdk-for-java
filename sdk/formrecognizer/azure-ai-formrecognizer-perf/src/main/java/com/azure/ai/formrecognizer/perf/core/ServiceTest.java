// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf.core;

import com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.SyncPoller;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

/**
 * Base class for Azure Formrecognizer performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
        + "or system properties.%n";

    protected final DocumentAnalysisClient documentAnalysisClient;
    protected final DocumentAnalysisAsyncClient documentAnalysisAsyncClient;
    protected final DocumentModelAdministrationClient documentModelAdministrationClient;
    private final DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient;

    protected String modelId;

    /**
     * The base class for Azure Formrecognizer tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     *
     * @throws RuntimeException if "AZURE_FORMRECOGNIZER_API_KEY" or "AZURE_FORMRECOGNIZER_ENDPOINT" is null or empty.
     */
    public ServiceTest(TOptions options) {
        super(options);

        String formrecognizerEndpoint = Configuration.getGlobalConfiguration().get("AZURE_FORMRECOGNIZER_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(formrecognizerEndpoint)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_FORMRECOGNIZER_ENDPOINT"));
        }

        String formrecognizerApiKey = Configuration.getGlobalConfiguration().get("AZURE_FORMRECOGNIZER_API_KEY");
        if (CoreUtils.isNullOrEmpty(formrecognizerApiKey)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_FORMRECOGNIZER_API_KEY"));
        }

        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(formrecognizerEndpoint)
            .credential(new AzureKeyCredential(formrecognizerApiKey));

        this.documentModelAdministrationClient = builder.buildClient();
        this.documentAnalysisAsyncClient = builder.buildAsyncClient().getDocumentAnalysisAsyncClient();
        this.documentAnalysisClient = documentModelAdministrationClient.getDocumentAnalysisClient();
        this.documentModelAdministrationAsyncClient = builder.buildAsyncClient();
    }

    /**
     * Runs the setup required prior to running the performance test.
     * @throws RuntimeException if "FORMRECOGNIZER_TRAINING_CONTAINER_SAS_URL" is null or empty.
     */
    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.defer(() -> {
            String trainingDocumentsUrl = Configuration.getGlobalConfiguration()
                .get("FORMRECOGNIZER_TRAINING_CONTAINER_SAS_URL");
            if (CoreUtils.isNullOrEmpty(trainingDocumentsUrl)) {
                return Mono.error(new RuntimeException(
                    String.format(CONFIGURATION_ERROR, "FORMRECOGNIZER_TRAINING_CONTAINER_SAS_URL")));
            }
            SyncPoller<DocumentOperationResult, DocumentModel>
                syncPoller = documentModelAdministrationAsyncClient
                .beginBuildModel(trainingDocumentsUrl,
                    null,
                    new BuildModelOptions().setDescription("perf-model"))
                .getSyncPoller();
            modelId = syncPoller.getFinalResult().getModelId();
            return Mono.empty();
        }).then();
    }

    /**
     * Runs the cleanup logic after an individual thread finishes in the performance test.
     */
    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.defer(() -> documentModelAdministrationAsyncClient.deleteModel(modelId));
    }
}

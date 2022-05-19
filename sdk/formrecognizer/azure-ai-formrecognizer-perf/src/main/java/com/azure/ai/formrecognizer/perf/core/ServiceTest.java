// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf.core;

import com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

/**
 * Base class for Azure FormRecognizer performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
        + "or system properties.%n";
    protected final DocumentAnalysisClient documentAnalysisClient;
    protected final DocumentAnalysisAsyncClient documentAnalysisAsyncClient;
    protected final DocumentModelAdministrationClient documentModelAdministrationClient;
    protected final DocumentModelAdministrationAsyncClient documentModelAdministrationAsyncClient;
    protected static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();
    private static final String AZURE_FORM_RECOGNIZER_ENDPOINT =
        GLOBAL_CONFIGURATION.get("AZURE_FORM_RECOGNIZER_ENDPOINT");
    private static final String AZURE_FORM_RECOGNIZER_API_KEY =
        GLOBAL_CONFIGURATION.get("AZURE_FORM_RECOGNIZER_API_KEY");
    protected String modelId;

    /**
     * The base class for Azure Formrecognizer tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     * @throws RuntimeException if "AZURE_FORM_RECOGNIZER_API_KEY" or "AZURE_FORM_RECOGNIZER_ENDPOINT" is null or empty.
     */
    public ServiceTest(TOptions options) {
        super(options);

        if (CoreUtils.isNullOrEmpty(AZURE_FORM_RECOGNIZER_ENDPOINT)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_FORM_RECOGNIZER_ENDPOINT"));
        }

        if (CoreUtils.isNullOrEmpty(AZURE_FORM_RECOGNIZER_API_KEY)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_FORM_RECOGNIZER_API_KEY"));
        }

        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(AZURE_FORM_RECOGNIZER_ENDPOINT)
            .credential(new AzureKeyCredential(AZURE_FORM_RECOGNIZER_API_KEY));

        this.documentModelAdministrationClient = builder.buildClient();
        this.documentAnalysisAsyncClient = builder.buildAsyncClient().getDocumentAnalysisAsyncClient();
        this.documentAnalysisClient = documentModelAdministrationClient.getDocumentAnalysisClient();
        this.documentModelAdministrationAsyncClient = builder.buildAsyncClient();
    }

    /**
     * Runs the setup required prior to running the performance test.
     */
    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.empty();
    }

    /**
     * Runs the cleanup logic after an individual thread finishes in the performance test.
     */
    @Override
    public Mono<Void> globalCleanupAsync() {
        if (modelId != null) {
            return Mono.defer(() -> documentModelAdministrationAsyncClient.deleteModel(modelId));
        } else {
            return Mono.empty();
        }
    }
}

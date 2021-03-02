// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.formrecognizer.perf.core;

import com.azure.ai.formrecognizer.FormRecognizerAsyncClient;
import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.CustomFormModel;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.SyncPoller;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Base class for Azure Formrecognizer performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
        + "or system properties.%n";

    protected final FormRecognizerClient formrecognizerClient;
    protected final FormRecognizerAsyncClient formrecognizerAsyncClient;
    protected final FormTrainingClient formTrainingClient;
    private final FormTrainingAsyncClient formTrainingAsyncClient;

    protected String modelId;

    /**
     * The base class for Azure Formrecognizer tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     */
    public ServiceTest(TOptions options) {
        super(options);

        String formrecognizerEndpoint = Configuration.getGlobalConfiguration().get("AZURE_FORMRECOGNIZER_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(formrecognizerEndpoint)) {
            System.out.printf(CONFIGURATION_ERROR, "AZURE_FORMRECOGNIZER_ENDPOINT");
            System.exit(1);
        }

        String formrecognizerApiKey = Configuration.getGlobalConfiguration().get("AZURE_FORMRECOGNIZER_API_KEY");
        if (CoreUtils.isNullOrEmpty(formrecognizerApiKey)) {
            System.out.printf(CONFIGURATION_ERROR, "AZURE_FORMRECOGNIZER_API_KEY");
            System.exit(1);
        }

        FormTrainingClientBuilder builder = new FormTrainingClientBuilder()
            .endpoint(formrecognizerEndpoint)
            .credential(new AzureKeyCredential(formrecognizerApiKey))
            .httpClient(new NettyAsyncHttpClientBuilder()
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .build());

        this.formTrainingClient = builder.buildClient();
        this.formrecognizerAsyncClient = builder.buildAsyncClient().getFormRecognizerAsyncClient();
        this.formrecognizerClient = formTrainingClient.getFormRecognizerClient();
        this.formTrainingAsyncClient  = builder.buildAsyncClient();
    }

    /**
     * Runs the setup required prior to running the performance test.
     */
    @Override
    public Mono<Void> globalSetupAsync() {
        String trainingDocumentsUrl = Configuration.getGlobalConfiguration()
            .get("FORMRECOGNIZER_TRAINING_CONTAINER_SAS_URL");

        if (CoreUtils.isNullOrEmpty(trainingDocumentsUrl)) {
            throw new RuntimeException("'FORMRECOGNIZER_TRAINING_CONTAINER_SAS_URL' is required.");
        }

        SyncPoller<FormRecognizerOperationResult, CustomFormModel>
            syncPoller = formTrainingClient
            .beginTraining(trainingDocumentsUrl, true, new TrainingOptions().setModelName("labeled-perf-model"),
                Context.NONE);
        modelId = syncPoller.getFinalResult().getModelId();
        return Mono.just(modelId).then();
    }

    /**
     * Runs the cleanup logic after an individual thread finishes in the performance test.
     */
    @Override
    public Mono<Void> globalCleanupAsync() {
        return formTrainingAsyncClient.deleteModel(modelId);
    }
}

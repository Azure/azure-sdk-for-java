// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.perf.core;

import com.azure.ai.formrecognizer.FormRecognizerAsyncClient;
import com.azure.ai.formrecognizer.FormRecognizerClient;
import com.azure.ai.formrecognizer.FormRecognizerClientBuilder;
import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
                                                          + "or system properties.%n";
    private static final String AZURE_FORM_RECOGNIZER_API_KEY = "AZURE_FORM_RECOGNIZER_API_KEY";
    private static final String AZURE_FORM_RECOGNIZER_ENDPOINT = "AZURE_FORM_RECOGNIZER_ENDPOINT";
    private static final String LOCAL_HOST = "localhost";

    protected final FormRecognizerAsyncClient formRecognizerAsyncClient;
    protected final FormRecognizerClient formRecognizerClient;
    protected final FormTrainingAsyncClient formTrainingAsyncClient;
    protected final FormTrainingClient formTrainingClient;

    public ServiceTest(TOptions options) {
        super(options);

        final String formRecognizerEndpoint = Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
        if (CoreUtils.isNullOrEmpty(formRecognizerEndpoint)) {
            System.out.printf(CONFIGURATION_ERROR, AZURE_FORM_RECOGNIZER_ENDPOINT);
            System.exit(1);
        }

        final String formRecognizerKeyCredential = Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_API_KEY);
        if (CoreUtils.isNullOrEmpty(formRecognizerKeyCredential)) {
            System.out.printf(CONFIGURATION_ERROR, AZURE_FORM_RECOGNIZER_API_KEY);
            System.exit(1);
        }

        FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder()
            .endpoint(formRecognizerEndpoint)
            .credential(new AzureKeyCredential(formRecognizerKeyCredential))
            .httpClient(new NettyAsyncHttpClientBuilder()
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(LOCAL_HOST, 8888)))
                .build());
        this.formRecognizerAsyncClient = builder.buildAsyncClient();
        this.formRecognizerClient = builder.buildClient();


        FormTrainingClientBuilder trainingClientBuilder = new FormTrainingClientBuilder()
            .endpoint(formRecognizerEndpoint)
            .credential(new AzureKeyCredential(formRecognizerKeyCredential))
            .httpClient(new NettyAsyncHttpClientBuilder()
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(LOCAL_HOST, 8888)))
                .build());
        this.formTrainingAsyncClient = trainingClientBuilder.buildAsyncClient();
        this.formTrainingClient = trainingClientBuilder.buildClient();
    }

    // Required resource setup goes here.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then();
    }


}

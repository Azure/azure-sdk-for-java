// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.perf.test.core.PerfStressTest;

/**
 * The Text Analytics service base class to setup the performance tests.
 * @param <TOptions> The options type.
 */
public abstract class ServiceTest<TOptions> extends PerfStressTest<TextAnalyticsStressOptions> {
    protected final TextAnalyticsAsyncClient textAnalyticsAsyncClient;
    protected final TextAnalyticsClient textAnalyticsClient;

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public ServiceTest(TextAnalyticsStressOptions options) {
        super(options);
        textAnalyticsAsyncClient = new TextAnalyticsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TA_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TA_ACCESS_KEY")))
            .httpClient(new LocalHttpClient(options.getDocumentCount()))
            .buildAsyncClient();

        textAnalyticsClient = new TextAnalyticsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_TA_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_TA_ACCESS_KEY")))
            .httpClient(new LocalHttpClient(options.getDocumentCount()))
            .buildClient();
    }

}

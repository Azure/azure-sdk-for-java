// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf.core;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Base class for Azure Formrecognizer performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
                                                          + "or system properties.%n";

    protected final TextAnalyticsClient textAnalyticsClient;
    protected final TextAnalyticsAsyncClient textAnalyticsAsyncClient;

    /**
     * The base class for Azure Text Analytics tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     *
     * @throws RuntimeException if "AZURE_TEXT_ANALYTICS_ENDPOINT" or "AZURE_TEXT_ANALYTICS_API_KEY" is null or empty.
     */
    public ServiceTest(TOptions options) {
        super(options);

        String textAnalyticsEndpoint = Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(textAnalyticsEndpoint)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_TEXT_ANALYTICS_ENDPOINT"));
        }

        String textAnalyticsApiKey = Configuration.getGlobalConfiguration().get("AZURE_TEXT_ANALYTICS_API_KEY");
        if (CoreUtils.isNullOrEmpty(textAnalyticsApiKey)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_TEXT_ANALYTICS_API_KEY"));
        }

        TextAnalyticsClientBuilder builder = new TextAnalyticsClientBuilder()
                                                 .endpoint(textAnalyticsEndpoint)
                                                 .credential(new AzureKeyCredential(textAnalyticsApiKey));

        this.textAnalyticsClient = builder.buildClient();
        this.textAnalyticsAsyncClient  = builder.buildAsyncClient();
    }
}

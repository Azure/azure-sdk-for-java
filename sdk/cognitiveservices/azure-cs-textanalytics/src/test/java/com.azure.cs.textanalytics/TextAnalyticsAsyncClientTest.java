// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;

public class TextAnalyticsAsyncClientTest extends TextAnalyticsClientTestBase{
    private final ClientLogger logger = new ClientLogger(TextAnalyticsAsyncClientTest.class);

    private TextAnalyticsAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndPoint())
            .pipeline(httpPipeline)
            .buildAsyncClient());
    }

    @Test
    public void detectLanguage() {

    }
}

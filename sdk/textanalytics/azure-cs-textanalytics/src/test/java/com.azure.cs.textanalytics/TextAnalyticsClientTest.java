// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextAnalyticsClientTest extends TextAnalyticsClientTestBase {

    private final ClientLogger logger = new ClientLogger(TextAnalyticsClientTest.class);

    private TextAnalyticsClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        client = clientSetup(httpPipeline -> new TextAnalyticsClientBuilder()
            .endpoint(getEndPoint())
            .pipeline(httpPipeline)
            .buildClient());
    }

    @Test
    public void detectLanguage() {

    }

    @Override
    public void detectLanguagesBatchInput() {

    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.ai.textanalytics.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs custom model recognition operations.
 */
public class DetectLanguageTest extends ServiceTest<PerfStressOptions> {
    List<String> documents = new ArrayList<>();

    /**
     * The DetectLanguageTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public DetectLanguageTest(PerfStressOptions options) {
        super(options);
        final int documentSize = options.getCount();
        for (int i = 0; i < documentSize; i++) {
            documents.add("Detta är ett dokument skrivet på engelska.");
        }
    }

    @Override
    public void run() {
        textAnalyticsClient.detectLanguageBatch(documents, "en", null);
    }

    @Override
    public Mono<Void> runAsync() {
        return textAnalyticsAsyncClient.detectLanguageBatch(documents, "en", null).then();
    }
}

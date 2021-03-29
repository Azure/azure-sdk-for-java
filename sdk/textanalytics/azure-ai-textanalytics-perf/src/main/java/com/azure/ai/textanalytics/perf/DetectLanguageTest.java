// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs custom model recognition operations.
 */
public class DetectLanguageTest extends ServiceTest<PerfStressOptions> {
    /**
     * The CustomModelRecognitionTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public DetectLanguageTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        final DetectedLanguage detectedLanguage = textAnalyticsClient.detectLanguage(
            "Detta är ett dokument skrivet på engelska.");
        assert "swedish".equals(detectedLanguage.getName());
    }

    @Override
    public Mono<Void> runAsync() {
        return textAnalyticsAsyncClient.detectLanguage("Detta är ett dokument skrivet på engelska.")
                   .flatMap(
                       result -> "swedish".equals(result.getName())
                                     ? Mono.empty()
                                     : Mono.error(new RuntimeException("Expected detected language.")));
    }
}

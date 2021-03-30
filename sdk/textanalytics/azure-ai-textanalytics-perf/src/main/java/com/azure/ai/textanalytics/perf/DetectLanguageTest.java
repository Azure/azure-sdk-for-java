// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.ai.textanalytics.models.TextAnalyticsRequestOptions;
import com.azure.ai.textanalytics.util.DetectLanguageResultCollection;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Performance test for detect language API.
 */
public class DetectLanguageTest extends ServiceTest<TextAnalyticsStressOptions> {

    /**
     * Creates an instance of performance test.
     * @param options the options configured for the test.
     */
    public DetectLanguageTest(TextAnalyticsStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        DetectLanguageResultCollection result = textAnalyticsClient.detectLanguageBatch(getDocuments(), "US",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true));
        long count = result.stream().count();
        assert count == options.getDocumentCount();
    }

    @Override
    public Mono<Void> runAsync() {
        return textAnalyticsAsyncClient.detectLanguageBatch(getDocuments(), "US",
            new TextAnalyticsRequestOptions().setIncludeStatistics(true))
            .map(result -> result.stream().count())
            .handle((val, sink) -> {
                if (val == options.getDocumentCount()) {
                    sink.next(val);
                } else {
                    sink.error(new IllegalStateException("The result count doesn't match the input doc count"));
                }
            })
            .then();
    }

    private Iterable<String> getDocuments() {
        List<String> documents = new ArrayList<>();
        IntStream.range(0, options.getDocumentCount())
            .forEach(i -> documents.add("The quick brown fox jumps over the lazy dog"));
        return documents;
    }
}

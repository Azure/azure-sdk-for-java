package com.azure.ai.textanalytics.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class TextAnalyticsStressOptions extends PerfStressOptions {

    @Parameter(names = {"-dc", "--docCount"}, description = "Number of documents to send in request")
    private int docCount = 10;

    /**
     * Get the configured document count option for performance test.
     * @return The document count.
     */
    public int getDocumentCount() {
        return docCount;
    }
}

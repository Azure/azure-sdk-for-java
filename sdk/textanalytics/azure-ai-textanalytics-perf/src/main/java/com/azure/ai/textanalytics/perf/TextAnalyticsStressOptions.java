// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Extended test options for Text Analytics perf tests.
 */
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

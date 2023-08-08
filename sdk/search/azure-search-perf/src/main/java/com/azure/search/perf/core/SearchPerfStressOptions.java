// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

/**
 * Azure Search specific performance test configuration options.
 */
public class SearchPerfStressOptions extends PerfStressOptions {
    @Parameter(names = {"-ds", "--documentSize"}, description = "Size of Search documents (SMALL, LARGE)")
    private String documentSize = DocumentSize.SMALL.name();

    /**
     * Get the configured document size option for performance test.
     *
     * @return The document size.
     */
    public String getDocumentSize() {
        return documentSize;
    }
}

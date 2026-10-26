// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Sample06_GetAnalyzerPreviewAsyncTest extends Sample06_GetAnalyzerAsyncTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testGetPrebuiltAnalyzerAsync() throws IOException {
        super.testGetPrebuiltAnalyzerAsync();
    }

    @Override
    @Test
    public void testGetPrebuiltInvoiceAsync() throws IOException {
        super.testGetPrebuiltInvoiceAsync();
    }

    @Override
    @Test
    public void testGetCustomAnalyzerAsync() throws IOException {
        super.testGetCustomAnalyzerAsync();
    }
}

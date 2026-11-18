// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Sample06_GetAnalyzerPreviewTest extends Sample06_GetAnalyzerTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testGetPrebuiltAnalyzer() throws IOException {
        super.testGetPrebuiltAnalyzer();
    }

    @Override
    @Test
    public void testGetPrebuiltInvoice() throws IOException {
        super.testGetPrebuiltInvoice();
    }

    @Override
    @Test
    public void testGetCustomAnalyzer() throws IOException {
        super.testGetCustomAnalyzer();
    }
}

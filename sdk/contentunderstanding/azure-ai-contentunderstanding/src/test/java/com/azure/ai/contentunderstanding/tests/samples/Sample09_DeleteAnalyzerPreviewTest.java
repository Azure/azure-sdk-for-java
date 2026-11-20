// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

public class Sample09_DeleteAnalyzerPreviewTest extends Sample09_DeleteAnalyzerTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testDeleteAnalyzer() {
        super.testDeleteAnalyzer();
    }

    @Override
    @Test
    public void testDeleteNonexistentAnalyzer() {
        super.testDeleteNonexistentAnalyzer();
    }
}

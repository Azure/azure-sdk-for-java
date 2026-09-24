// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Sample05_CreateClassifierPreviewTest extends Sample05_CreateClassifierTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testCreateClassifier() throws IOException {
        super.testCreateClassifier();
    }

    @Override
    @Test
    public void testAnalyzeCategory() throws IOException {
        super.testAnalyzeCategory();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

public class Sample02_AnalyzeUrlPreviewTest extends Sample02_AnalyzeUrlTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testAnalyzeUrl() {
        super.testAnalyzeUrl();
    }

    @Override
    @Test
    public void testAnalyzeVideoUrl() {
        super.testAnalyzeVideoUrl();
    }

    @Override
    @Test
    public void testAnalyzeAudioUrl() {
        super.testAnalyzeAudioUrl();
    }

    @Override
    @Test
    public void testAnalyzeImageUrl() {
        super.testAnalyzeImageUrl();
    }

    @Override
    @Test
    public void testAnalyzeUrlWithPageContentRanges() {
        super.testAnalyzeUrlWithPageContentRanges();
    }

    @Override
    @Test
    public void testAnalyzeVideoUrlWithTimeContentRanges() {
        super.testAnalyzeVideoUrlWithTimeContentRanges();
    }

    @Override
    @Test
    public void testAnalyzeAudioUrlWithTimeContentRanges() {
        super.testAnalyzeAudioUrlWithTimeContentRanges();
    }
}

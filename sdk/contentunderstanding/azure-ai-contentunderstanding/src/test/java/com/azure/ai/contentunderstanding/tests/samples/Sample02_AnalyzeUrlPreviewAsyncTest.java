// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

public class Sample02_AnalyzeUrlPreviewAsyncTest extends Sample02_AnalyzeUrlAsyncTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testAnalyzeUrlAsync() {
        super.testAnalyzeUrlAsync();
    }

    @Override
    @Test
    public void testAnalyzeVideoUrlAsync() {
        super.testAnalyzeVideoUrlAsync();
    }

    @Override
    @Test
    public void testAnalyzeAudioUrlAsync() {
        super.testAnalyzeAudioUrlAsync();
    }

    @Override
    @Test
    public void testAnalyzeImageUrlAsync() {
        super.testAnalyzeImageUrlAsync();
    }

    @Override
    @Test
    public void testAnalyzeUrlWithPageContentRangesAsync() {
        super.testAnalyzeUrlWithPageContentRangesAsync();
    }

    @Override
    @Test
    public void testAnalyzeVideoUrlWithTimeContentRangesAsync() {
        super.testAnalyzeVideoUrlWithTimeContentRangesAsync();
    }

    @Override
    @Test
    public void testAnalyzeAudioUrlWithTimeContentRangesAsync() {
        super.testAnalyzeAudioUrlWithTimeContentRangesAsync();
    }
}

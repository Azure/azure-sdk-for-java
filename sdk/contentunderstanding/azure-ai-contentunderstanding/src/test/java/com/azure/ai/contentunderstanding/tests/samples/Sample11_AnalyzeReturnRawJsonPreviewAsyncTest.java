// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Sample11_AnalyzeReturnRawJsonPreviewAsyncTest extends Sample11_AnalyzeReturnRawJsonAsyncTest {
    @Override
    protected ContentUnderstandingServiceVersion getServiceVersion() {
        return ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW;
    }

    @Override
    @Test
    public void testAnalyzeReturnRawJsonAsync() throws IOException {
        super.testAnalyzeReturnRawJsonAsync();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import org.junit.jupiter.api.Test;

public class ContentUnderstandingCommonApiGaTest extends ContentUnderstandingCommonApiTestBase {
    @Test
    public void listAnalyzersSupportsConfiguredServiceVersion() {
        verifyListAnalyzersSupportsConfiguredServiceVersion();
    }

    @Test
    public void asyncListAnalyzersSupportsConfiguredServiceVersion() {
        verifyAsyncListAnalyzersSupportsConfiguredServiceVersion();
    }

    @Test
    public void analyzerManagementSupportsConfiguredServiceVersion() {
        verifyAnalyzerManagementSupportsConfiguredServiceVersion();
    }

    @Test
    public void asyncAnalyzerManagementSupportsConfiguredServiceVersion() {
        verifyAsyncAnalyzerManagementSupportsConfiguredServiceVersion();
    }

    @Test
    public void analysisLifecycleSupportsConfiguredServiceVersion() throws Exception {
        verifyAnalysisLifecycleSupportsConfiguredServiceVersion();
    }

    @Test
    public void asyncAnalysisLifecycleSupportsConfiguredServiceVersion() throws Exception {
        verifyAsyncAnalysisLifecycleSupportsConfiguredServiceVersion();
    }

    @Test
    public void defaultsSupportConfiguredServiceVersion() {
        verifyDefaultsSupportConfiguredServiceVersion();
    }

    @Test
    public void asyncDefaultsSupportConfiguredServiceVersion() {
        verifyAsyncDefaultsSupportConfiguredServiceVersion();
    }

    @Test
    public void resultFileSupportsConfiguredServiceVersion() {
        verifyResultFileSupportsConfiguredServiceVersion();
    }

    @Test
    public void asyncResultFileSupportsConfiguredServiceVersion() {
        verifyAsyncResultFileSupportsConfiguredServiceVersion();
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.batch.recording;

import com.microsoft.azure.management.resources.fluentcore.utils.DelayProvider;

public class TestDelayProvider extends DelayProvider {
    private final boolean isRecordMode;
    public TestDelayProvider(boolean isRecordMode) {
        this.isRecordMode = isRecordMode;
    }
    @Override
    public void sleep(int milliseconds) {
        if (isRecordMode) {
            super.sleep(milliseconds);
        }
    }

}

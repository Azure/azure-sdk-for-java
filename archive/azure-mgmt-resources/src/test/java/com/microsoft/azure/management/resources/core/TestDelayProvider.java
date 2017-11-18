/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.microsoft.azure.management.resources.fluentcore.utils.DelayProvider;

public class TestDelayProvider extends DelayProvider {
    private boolean isRecordMode;
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

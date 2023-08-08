// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

public class LiveOnlyExtension implements IAnnotationDrivenExtension<LiveOnly> {

    @Override
    public void visitFeatureAnnotation(LiveOnly annotation, FeatureInfo feature) {
        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        if (testMode != TestMode.LIVE) {
            feature.skip(String.format("Test ignored in %s mode", testMode));
        }
    }

    @Override
    public void visitSpecAnnotation(LiveOnly annotation, SpecInfo spec) {
        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        if (testMode != TestMode.LIVE) {
            spec.skip(String.format("Test ignored in %s mode", testMode));
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.storage;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

public class LiveOnlyExtension implements IAnnotationDrivenExtension<LiveOnly> {

    @Override
    public void visitFeatureAnnotation(LiveOnly annotation, FeatureInfo feature) {
        String testMode = (String) APISpec.getTestMode();
        if (!"LIVE".equalsIgnoreCase(testMode)) {
            feature.skip(String.format("Test ignored in %s mode", testMode));
        }
    }

    @Override
    public void visitSpecAnnotation(LiveOnly annotation, SpecInfo spec) {
        String testMode = (String) APISpec.getTestMode();
        if (!"LIVE".equalsIgnoreCase(testMode)) {
            spec.skip(String.format("Test ignored in %s mode", testMode));
        }
    }
}

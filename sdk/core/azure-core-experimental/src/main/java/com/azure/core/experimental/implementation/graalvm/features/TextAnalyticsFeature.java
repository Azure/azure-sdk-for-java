// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.experimental.implementation.graalvm.features;

import com.azure.core.experimental.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.core.experimental.graalvm.GraalVMFeatureUtils.interfaces;
import static com.azure.core.experimental.graalvm.GraalVMFeatureUtils.setsOf;

@AutomaticFeature
public class TextAnalyticsFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.ai.textanalytics";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(
            interfaces("com.azure.ai.textanalytics.implementation.TextAnalyticsClientImpl$TextAnalyticsClientService")
        );
    }
}

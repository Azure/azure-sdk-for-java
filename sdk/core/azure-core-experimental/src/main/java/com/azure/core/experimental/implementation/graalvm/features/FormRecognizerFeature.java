// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.experimental.implementation.graalvm.features;

import com.azure.core.experimental.implementation.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.core.experimental.implementation.graalvm.GraalVMFeatureUtils.interfaces;
import static com.azure.core.experimental.implementation.graalvm.GraalVMFeatureUtils.setsOf;

@AutomaticFeature
public class FormRecognizerFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.ai.formrecognizer";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(
            interfaces("com.azure.ai.formrecognizer.implementation.FormRecognizerClientImpl$FormRecognizerClientService")
        );
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.experimental.implementation.graalvm.features;

import com.azure.core.experimental.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

@AutomaticFeature
public class AzureCoreFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.core";
    }
}

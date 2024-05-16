// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.support.implementation.features;

import com.azure.aot.graalvm.support.implementation.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

@AutomaticFeature
public class CosmosFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.cosmos";
    }
}

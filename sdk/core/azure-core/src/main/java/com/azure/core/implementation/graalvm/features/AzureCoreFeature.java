// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.graalvm.implementation.features.core;

import com.azure.graalvm.ClassReflectionAttributes;
import com.azure.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.graalvm.GraalVMFeatureUtils.*;
import static com.azure.graalvm.ClassReflectionAttributes.createWithAllDeclared;

@AutomaticFeature
public class AzureCoreFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.core";
    }

    @Override
    public Set<ClassReflectionAttributes> getReflectionClasses() {
        return setOf(
                createWithAllDeclared("com.azure.core.util.DateTimeRfc1123")
        );
    }
}

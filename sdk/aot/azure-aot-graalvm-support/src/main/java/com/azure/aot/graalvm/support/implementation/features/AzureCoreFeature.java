// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.aot.graalvm.support.implementation.features;

import com.azure.aot.graalvm.support.implementation.ClassReflectionAttributes;
import com.azure.aot.graalvm.support.implementation.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.aot.graalvm.support.implementation.ClassReflectionAttributes.createWithAllDeclared;
import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.setOf;

@AutomaticFeature
public class AzureCoreFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.core";
    }

    @Override
    public Set<ClassReflectionAttributes> getReflectionClasses() {
        return setOf(createWithAllDeclared("com.azure.core.util.DateTimeRfc1123"),
            createWithAllDeclared("com.azure.core.http.rest.StreamResponse"));
    }
}

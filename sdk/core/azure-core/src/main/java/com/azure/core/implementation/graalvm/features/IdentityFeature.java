// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.graalvm.features;

import com.azure.core.implementation.graalvm.ClassReflectionAttributes;
import com.azure.core.implementation.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.core.implementation.graalvm.ClassReflectionAttributes.createWithAllDeclared;
import static com.azure.core.implementation.graalvm.GraalVMFeatureUtils.setOf;

@AutomaticFeature
public class IdentityFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.identity";
    }

    @Override
    public Set<ClassReflectionAttributes> getReflectionClasses() {
        return setOf(
            createWithAllDeclared("com.microsoft.aad.msal4j.AadInstanceDiscoveryResponse"),
            createWithAllDeclared("com.microsoft.aad.msal4j.InstanceDiscoveryMetadataEntry"),

            // this is due to Msal4j library dependency
            createWithAllDeclared("java.util.HashSet")
        );
    }
}

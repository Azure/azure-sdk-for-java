// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.aot.graalvm.support.implementation.features;

import com.azure.aot.graalvm.support.implementation.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.interfaces;
import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.setsOf;

@AutomaticFeature
public class KeyvaultCertificatesFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.security.keyvault.certificates";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(interfaces("com.azure.security.keyvault.certificates.implementation.CertificateService"),
            interfaces("com.microsoft.aad.msal4jextensions.persistence.mac.ISecurityLibrary"));
    }
}

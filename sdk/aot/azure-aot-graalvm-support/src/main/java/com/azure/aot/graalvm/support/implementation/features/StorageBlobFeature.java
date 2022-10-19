// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.aot.graalvm.support.implementation.features;

import com.azure.aot.graalvm.support.implementation.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.interfaces;
import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.setsOf;

@AutomaticFeature
public class StorageBlobFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.storage.blob";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(
            interfaces("com.azure.storage.blob.implementation.AppendBlobsImpl$AppendBlobsService"),
            interfaces("com.azure.storage.blob.implementation.BlobsImpl$BlobsService"),
            interfaces("com.azure.storage.blob.implementation.BlockBlobsImpl$BlockBlobsService"),
            interfaces("com.azure.storage.blob.implementation.ContainersImpl$ContainersService"),
            interfaces("com.azure.storage.blob.implementation.PageBlobsImpl$PageBlobsService"),
            interfaces("com.azure.storage.blob.implementation.ServicesImpl$ServicesService")
        );
    }
}

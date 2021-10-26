package com.azure.graalvm.implementation.features.core;

import com.azure.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.graalvm.GraalVMFeatureUtils.*;

@AutomaticFeature
public class StorageFileShareFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.storage.file.share";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(
            interfaces("com.azure.storage.file.share.implementation.DirectoriesImpl$DirectoriesService"),
            interfaces("com.azure.storage.file.share.implementation.FilesImpl$FilesService"),
            interfaces("com.azure.storage.file.share.implementation.ServicesImpl$ServicesService"),
            interfaces("com.azure.storage.file.share.implementation.SharesImpl$SharesService")
        );
    }
}

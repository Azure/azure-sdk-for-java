package com.azure.graalvm.implementation.features.core;

import com.azure.graalvm.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.graalvm.GraalVMFeatureUtils.interfaces;
import static com.azure.graalvm.GraalVMFeatureUtils.setsOf;

@AutomaticFeature
public class StorageQueueFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.storage.queue";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(
                interfaces("com.azure.storage.queue.implementation.MessageIdsImpl$MessageIdsService"),
                interfaces("com.azure.storage.queue.implementation.MessagesImpl$MessagesService"),
                interfaces("com.azure.storage.queue.implementation.QueuesImpl$QueuesService"),
                interfaces("com.azure.storage.queue.implementation.ServicesImpl$ServicesService")
        );
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;

/**
 * Customization class for Queue Storage.
 */
public class QueueStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {
        PackageCustomization impl = customization.getPackage("com.azure.storage.queue.implementation");

        ClassCustomization queuesImpl = impl.getClass("QueuesImpl");
        MethodCustomization create = queuesImpl.getMethod("create");
        create.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        create.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization delete = queuesImpl.getMethod("delete");
        delete.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        delete.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization getProperties = queuesImpl.getMethod("getProperties");
        getProperties.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getProperties.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization setMetadata = queuesImpl.getMethod("setMetadata");
        setMetadata.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setMetadata.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization getAccessPolicy = queuesImpl.getMethod("getAccessPolicy");
        getAccessPolicy.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getAccessPolicy.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization setAccessPolicy = queuesImpl.getMethod("setAccessPolicy");
        setAccessPolicy.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setAccessPolicy.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        ClassCustomization messageIdsImpl = impl.getClass("MessageIdsImpl");
        MethodCustomization update = messageIdsImpl.getMethod("update");
        update.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        update.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization delete1 = messageIdsImpl.getMethod("delete");
        delete1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        delete1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        ClassCustomization messagesImpl = impl.getClass("MessagesImpl");
        MethodCustomization dequeue = messagesImpl.getMethod("dequeue");
        dequeue.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        dequeue.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization clear = messagesImpl.getMethod("clear");
        clear.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        clear.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization enqueue = messagesImpl.getMethod("enqueue");
        enqueue.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        enqueue.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization peek = messagesImpl.getMethod("peek");
        peek.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        peek.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        ClassCustomization servicesImpl = impl.getClass("ServicesImpl");
        MethodCustomization setProperties1 = servicesImpl.getMethod("setProperties");
        setProperties1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setProperties1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization getProperties1 = servicesImpl.getMethod("getProperties");
        getProperties1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getProperties1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization getStatistics = servicesImpl.getMethod("getStatistics");
        getStatistics.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getStatistics.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization listQueuesSegment = servicesImpl.getMethod("listQueuesSegment");
        listQueuesSegment.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listQueuesSegment.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        MethodCustomization listQueuesSegmentNext = servicesImpl.getMethod("listQueuesSegmentNext");
        listQueuesSegmentNext.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listQueuesSegmentNext.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.queue.models");
        ClassCustomization queueServiceProperties = models.getClass("QueueServiceProperties");
        PropertyCustomization hourMetrics = queueServiceProperties.getProperty("hourMetrics");
        hourMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        hourMetrics.addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        PropertyCustomization minuteMetrics = queueServiceProperties.getProperty("minuteMetrics");
        minuteMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        minuteMetrics.addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");
    }
}

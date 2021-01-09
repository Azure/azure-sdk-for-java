package com.azure.storage.queue.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

/**
 * Customization class for Queue Storage.
 */
public class QueueStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {
        PackageCustomization impl = customization.getPackage("com.azure.storage.queue.implementation");

        ClassCustomization queuesImpl = impl.getClass("QueuesImpl");
        queuesImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        queuesImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        queuesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        queuesImpl.getMethod("setMetadata").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        queuesImpl.getMethod("getAccessPolicy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        queuesImpl.getMethod("setAccessPolicy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        ClassCustomization messageIdsImpl = impl.getClass("MessageIdsImpl");
        messageIdsImpl.getMethod("update").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        messageIdsImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        ClassCustomization messagesImpl = impl.getClass("MessagesImpl");
        messagesImpl.getMethod("dequeue").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        messagesImpl.getMethod("clear").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        messagesImpl.getMethod("enqueue").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        messagesImpl.getMethod("peek").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        ClassCustomization servicesImpl = impl.getClass("ServicesImpl");
        servicesImpl.getMethod("setProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        servicesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        servicesImpl.getMethod("getStatistics").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        servicesImpl.getMethod("listQueuesSegment").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
        servicesImpl.getMethod("listQueuesSegmentNext").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.queue.models");
        models.getClass("StorageErrorCode").rename("QueueErrorCode");
//        queueServiceProperties.getProperty("hourMetrics").

    }
}

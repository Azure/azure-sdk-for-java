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
        // Update unexpected response exception type.
        PackageCustomization impl = customization.getPackage("com.azure.storage.queue.implementation");

        ClassCustomization queuesImpl = impl.getClass("QueuesImpl");
        modifyUnexpectedResponseExceptionType(queuesImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(queuesImpl.getMethod("delete"));
        modifyUnexpectedResponseExceptionType(queuesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(queuesImpl.getMethod("setMetadata"));
        modifyUnexpectedResponseExceptionType(queuesImpl.getMethod("getAccessPolicy"));
        modifyUnexpectedResponseExceptionType(queuesImpl.getMethod("setAccessPolicy"));

        ClassCustomization messageIdsImpl = impl.getClass("MessageIdsImpl");
        modifyUnexpectedResponseExceptionType(messageIdsImpl.getMethod("update"));
        modifyUnexpectedResponseExceptionType(messageIdsImpl.getMethod("delete"));

        ClassCustomization messagesImpl = impl.getClass("MessagesImpl");
        modifyUnexpectedResponseExceptionType(messagesImpl.getMethod("dequeue"));
        modifyUnexpectedResponseExceptionType(messagesImpl.getMethod("clear"));
        modifyUnexpectedResponseExceptionType(messagesImpl.getMethod("enqueue"));
        modifyUnexpectedResponseExceptionType(messagesImpl.getMethod("peek"));

        ClassCustomization servicesImpl = impl.getClass("ServicesImpl");
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("setProperties"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getStatistics"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("listQueuesSegment"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("listQueuesSegmentNext"));

        // Update incorrect JsonProperty of Metrics
        PackageCustomization models = customization.getPackage("com.azure.storage.queue.models");
        ClassCustomization queueServiceProperties = models.getClass("QueueServiceProperties");
        PropertyCustomization hourMetrics = queueServiceProperties.getProperty("hourMetrics");
        hourMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        hourMetrics.addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        PropertyCustomization minuteMetrics = queueServiceProperties.getProperty("minuteMetrics");
        minuteMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        minuteMetrics.addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");
    }

    private void modifyUnexpectedResponseExceptionType(MethodCustomization method) {
        method.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        method.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.queue.models.QueueStorageException.class)");
    }
}

package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.TopicInfo;

public class Util {
    public static Iterable<QueueInfo> iterateQueues(ServiceBusContract service) throws ServiceException {
        // TODO: iterate over link rel=next pagination
        return service.listQueues().getItems();
    }

    public static Iterable<TopicInfo> iterateTopics(ServiceBusContract service) throws ServiceException {
        // TODO: iterate over link rel=next pagination
        return service.listTopics().getItems();
    }
}

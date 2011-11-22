package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.common.ServiceException;
import com.microsoft.windowsazure.services.serviceBus.models.QueueInfo;
import com.microsoft.windowsazure.services.serviceBus.models.Topic;

public class Util {
    public static Iterable<QueueInfo> iterateQueues(ServiceBusContract service)
            throws ServiceException {
        // TODO: iterate over link rel=next pagination
        return service.listQueues().getItems();
    }

    public static Iterable<Topic> iterateTopics(ServiceBusContract service)
            throws ServiceException {
        // TODO: iterate over link rel=next pagination
        return service.listTopics().getItems();
    }
}

package com.microsoft.windowsazure.services.serviceBus;

import com.microsoft.windowsazure.ServiceException;

public class Util {
    public static Iterable<Queue> iterateQueues(ServiceBusContract service)
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

package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.configuration.builder.Builder;
import com.microsoft.windowsazure.services.queue.implementation.QueueRestProxy;
import com.microsoft.windowsazure.services.queue.implementation.QueueExceptionProcessor;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyLiteFilter;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(QueueContract.class, QueueExceptionProcessor.class);
        registry.add(QueueRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
    }
}

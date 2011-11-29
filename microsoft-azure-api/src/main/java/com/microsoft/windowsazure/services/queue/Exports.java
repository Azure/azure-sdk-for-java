package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.queue.implementation.QueueExceptionProcessor;
import com.microsoft.windowsazure.services.queue.implementation.QueueRestProxy;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyFilter;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyLiteFilter;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {
        registry.add(QueueContract.class, QueueExceptionProcessor.class);
        registry.add(QueueExceptionProcessor.class);
        registry.add(QueueRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
        registry.add(SharedKeyFilter.class);
    }
}

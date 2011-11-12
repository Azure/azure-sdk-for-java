package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.configuration.builder.Builder;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter;
import com.microsoft.windowsazure.services.queue.implementation.QueueServiceForJersey;
import com.microsoft.windowsazure.services.queue.implementation.QueueServiceImpl;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(QueueServiceContract.class, QueueServiceImpl.class);
        registry.add(QueueServiceForJersey.class);
        registry.add(SharedKeyLiteFilter.class);
    }
}

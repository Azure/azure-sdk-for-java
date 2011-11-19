package com.microsoft.windowsazure.services.queue.implementation;

import javax.inject.Named;

import com.microsoft.windowsazure.services.queue.QueueConfiguration;

public class SharedKeyFilter extends com.microsoft.windowsazure.services.blob.implementation.SharedKeyFilter {
    public SharedKeyFilter(@Named(QueueConfiguration.ACCOUNT_NAME) String accountName,
            @Named(QueueConfiguration.ACCOUNT_KEY) String accountKey) {
        super(accountName, accountKey);
    }
}

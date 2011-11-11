package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.services.serviceBus.implementation.Entry;
import com.microsoft.azure.services.serviceBus.implementation.EntryModel;
import com.microsoft.azure.services.serviceBus.implementation.QueueDescription;
import com.microsoft.azure.services.serviceBus.implementation.SubscriptionDescription;

public class Subscription extends EntryModel<SubscriptionDescription> {

    public Subscription(Entry entry, SubscriptionDescription model) {
        super(entry, model);
        // TODO Auto-generated constructor stub
    }

}

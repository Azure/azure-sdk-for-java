// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;


class DefaultEventProcessorFactory<T extends IEventProcessor> implements IEventProcessorFactory<T> {
    private Class<T> eventProcessorClass = null;

    void setEventProcessorClass(Class<T> eventProcessorClass) {
        this.eventProcessorClass = eventProcessorClass;
    }

    @Override
    public T createEventProcessor(PartitionContext context) throws Exception {
        return this.eventProcessorClass.newInstance();
    }
}

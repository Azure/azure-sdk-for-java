/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;


class DefaultEventProcessorFactory<T extends IEventProcessor> implements IEventProcessorFactory<T>
{
    private Class<T> eventProcessorClass = null;

    void setEventProcessorClass(Class<T> eventProcessorClass)
    {
        this.eventProcessorClass = eventProcessorClass;
    }

    @Override
    public T createEventProcessor(PartitionContext context) throws Exception
    {
        return this.eventProcessorClass.newInstance();
    }
}

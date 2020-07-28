/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.test.support.AzureMessageConverterTest;

public class EventHubMessageConverterTest extends AzureMessageConverterTest<EventData> {

    @Override
    protected EventData getInstance() {
        return new EventData(this.payload.getBytes());
    }

    @Override
    public AzureMessageConverter<EventData> getConverter() {
        return new EventHubMessageConverter();
    }

    @Override
    protected Class<EventData> getTargetClass() {
        return EventData.class;
    }
}

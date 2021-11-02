// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.service.servicebus.properties.ServiceBusProducerDescriptor;

public class ProducerProperties extends CommonProperties implements ServiceBusProducerDescriptor {

    private String name;
    private ServiceBusEntityType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServiceBusEntityType getType() {
        return type;
    }

    public void setType(ServiceBusEntityType type) {
        this.type = type;
    }
}

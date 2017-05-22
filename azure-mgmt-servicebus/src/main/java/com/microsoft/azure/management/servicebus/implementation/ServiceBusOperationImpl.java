/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.servicebus.OperationDisplay;
import com.microsoft.azure.management.servicebus.ServiceBusOperation;

/**
 * The implementation of ServiceBusOperation.
 */
@LangDefinition
class ServiceBusOperationImpl extends WrapperImpl<OperationInner> implements ServiceBusOperation {
    protected ServiceBusOperationImpl(OperationInner innerObject) {
        super(innerObject);
    }

    @Override
    public String name() {
        return inner().name();
    }

    @Override
    public OperationDisplay displayInformation() {
        return inner().display();
    }
}

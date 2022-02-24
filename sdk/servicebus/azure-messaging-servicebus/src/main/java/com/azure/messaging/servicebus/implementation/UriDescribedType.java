// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.net.URI;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.URI_SYMBOL;

/**
 * URI described type.
 */
public class UriDescribedType extends ServiceBusDescribedType {

    /**
     * Set described to describe data in described type.
     *
     * @param described  real value in the described type.
     */
    public UriDescribedType(Object described) {
        super(URI_SYMBOL, ((URI) described).toString());
    }

    @Override
    public int size() {
        return URI_SYMBOL.length() + ((String) this.getDescribed()).length();
    }
}

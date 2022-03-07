// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.URI_SYMBOL;

/**
 * URI described type.
 */
public class UriDescribedType extends ServiceBusDescribedType {

    /**
     * Set described to describe data in described type.
     *
     * @param uri set as described in DescribedType.
     */
    public UriDescribedType(URI uri) {
        super(URI_SYMBOL, uri.toString());
    }

    @Override
    public int size() {
        return URI_SYMBOL.length() + ((String) this.getDescribed()).getBytes(StandardCharsets.UTF_8).length;
    }
}

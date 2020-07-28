/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core.api;

import org.springframework.messaging.Message;
import rx.Observable;

/**
 * Operations for sending {@link Message} to a destination in a reactive way.
 *
 * @author Warren Zhu
 */
public interface RxSendOperation {

    /**
     * Send a {@link Message} to the given destination with a given partition supplier.
     */
    <T> Observable<Void> sendRx(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination.
     */
    default <T> Observable<Void> sendRx(String destination, Message<T> message) {
        return sendRx(destination, message, null);
    }
}

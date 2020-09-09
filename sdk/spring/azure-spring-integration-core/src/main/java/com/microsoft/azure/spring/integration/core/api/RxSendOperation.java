// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
     * @param destination destination
     * @param message message
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return observable instance
     */
    <T> Observable<Void> sendRx(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination.
     * @param destination destination
     * @param message message
     * @param <T> payload type in message
     * @return observable instance
     */
    default <T> Observable<Void> sendRx(String destination, Message<T> message) {
        return sendRx(destination, message, null);
    }
}

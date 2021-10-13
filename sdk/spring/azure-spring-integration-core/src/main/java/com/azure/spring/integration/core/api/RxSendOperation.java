// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

import org.springframework.messaging.Message;
import rx.Observable;

/**
 * Operations for sending {@link Message} to a destination in a reactive way.
 *
 * @author Warren Zhu
 *
 * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link com.azure.spring.integration.core.api.reactor.SendOperation}. From version 4.0.0, the reactor API support will
 * be moved to com.azure.spring.messaging.core.SendOperation.
 */
@Deprecated
public interface RxSendOperation {

    /**
     * Send a {@link Message} to the given destination with a given partition supplier.
     * @param destination destination
     * @param message message
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return observable instance
     *
     * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link com.azure.spring.integration.core.api.reactor.SendOperation}. From version 4.0.0, the reactor API support will
     * be moved to com.azure.spring.messaging.core.SendOperation.
     */
    @Deprecated
    <T> Observable<Void> sendRx(String destination, Message<T> message, PartitionSupplier partitionSupplier);

    /**
     * Send a {@link Message} to the given destination.
     * @param destination destination
     * @param message message
     * @param <T> payload type in message
     * @return observable instance
     *
     * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
     * {@link com.azure.spring.integration.core.api.reactor.SendOperation}. From version 4.0.0, the reactor API support will
     * be moved to com.azure.spring.messaging.core.SendOperation.
     */
    @Deprecated
    default <T> Observable<Void> sendRx(String destination, Message<T> message) {
        return sendRx(destination, message, null);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.implementation.DispositionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * A TEMPORARY interface describing the contract to work with one (V1, V2) or multiple sessions (V1).
 * Note: Once the library is completely on V2 stack (i.e, when side-by-side V1 support is no longer needed),
 * we will delete the V1 type {@link ServiceBusSessionManager} and this interface, so excuse the 'I' in the name
 * of this "TO-BE-DELETED" interface .
 */
interface IServiceBusSessionManager extends AutoCloseable {
    String getIdentifier();
    String getLinkName(String sessionId);
    Flux<ServiceBusMessageContext> receive();
    Mono<Boolean> updateDisposition(String lockToken, String sessionId, DispositionStatus dispositionStatus,
        Map<String, Object> propertiesToModify, String deadLetterReason, String deadLetterDescription,
        ServiceBusTransactionContext transactionContext);
    void close();
}

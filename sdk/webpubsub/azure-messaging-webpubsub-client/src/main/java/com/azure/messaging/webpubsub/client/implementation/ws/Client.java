// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.ws;

import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface Client {

    Session connectToServer(ClientEndpointConfiguration cec, String path,
                            AtomicReference<ClientLogger> loggerReference,
                            Consumer<Object> messageHandler,
                            Consumer<Session> openHandler,
                            Consumer<CloseReason> closeHandler);
}

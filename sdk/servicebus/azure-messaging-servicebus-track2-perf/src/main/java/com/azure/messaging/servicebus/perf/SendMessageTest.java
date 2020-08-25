// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * Performance test.
 */
public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes(Charset.defaultCharset()));;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEK_LOCK);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync();
    }

    @Override
    public void run() {
        sender.sendMessage(message);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessage(message);
    }
}

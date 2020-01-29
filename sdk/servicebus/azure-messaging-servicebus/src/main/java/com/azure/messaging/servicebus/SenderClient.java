// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;



import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.SendOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.SBConnectionProcessor;
import reactor.core.publisher.Flux;

import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class SenderClient implements Closeable {

    //private final ClientLogger logger = new ClientLogger(SenderClient.class);
    //private final AtomicBoolean isDisposed = new AtomicBoolean();

    //private SenderAsyncClient asyncSender ;

    SenderClient(){
        //asyncSender =  new SenderAsyncClient(null, null, null, null,
            //null, null, false);
    }

    public void send(EventData message) {
        Objects.requireNonNull(message, "'event' cannot be null.");
        //asyncSender.send(message);
    }

    public void send(EventData event, SendOptions options) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        send(Flux.just(event).toIterable(), options);
    }


    public void send(Iterable<EventData> events) {
        Objects.requireNonNull(events, "'events' cannot be null.");
        // TODO : Not implemented yet.
        //throw new NotImplementedException();
    }

    public void send(Iterable<EventData> events, SendOptions options) {
        Objects.requireNonNull(events, "'events' cannot be null.");
        // TODO : Not implemented yet.
        //throw new NotImplementedException();

    }

    public void scheduleMessage(EventData message, ZonedDateTime scheduledEnqueueTime) {
        // TODO : Not implemented yet.
        //throw new NotImplementedException();
    }

    public void scheduleMessage(Iterable<EventData> message, ZonedDateTime scheduledEnqueueTime) {
        // TODO : Not implemented yet.
        //throw new NotImplementedException();
    }

    public void cancelScheduledMessage(long sequenceNumber) {
        // TODO : Not implemented yet.
        //throw new NotImplementedException();
    }

    public void cancelScheduledMessage(Iterable<Long> sequenceNumbers) {
        // TODO : Not implemented yet.
        //throw new NotImplementedException();
    }

    @Override
    public void close() throws IOException {
        /*if (!isDisposed.getAndSet(true)) {
            // TODO
           // asyncSender.close();
        }*/
    }

}

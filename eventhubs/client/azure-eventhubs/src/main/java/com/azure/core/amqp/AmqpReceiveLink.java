package com.azure.core.amqp;

import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

public interface AmqpReceiveLink extends AmqpLink {
    Flux<Message> receive();
}

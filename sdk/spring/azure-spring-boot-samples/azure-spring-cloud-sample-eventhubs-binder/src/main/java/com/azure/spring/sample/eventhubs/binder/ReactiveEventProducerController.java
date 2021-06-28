// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;

/**
 * @author Warren Zhu
 */
@RestController
@Profile("manual")
public class ReactiveEventProducerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveEventProducerController.class);

    @Autowired
    private Sinks.Many<Message<String>> many;

    @PostMapping("/messages/reactive")
    public ResponseEntity<String> reactiveSendMessage(@RequestParam String message) {
        LOGGER.info("Reactive method to send message: {} to destination.", message);
        many.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/")
    public String welcome() {
        return "welcome";
    }

}

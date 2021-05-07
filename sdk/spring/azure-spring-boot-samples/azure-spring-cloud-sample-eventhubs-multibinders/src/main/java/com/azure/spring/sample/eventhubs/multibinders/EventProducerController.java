// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.multibinders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;

import javax.annotation.Resource;


@Profile("manual")
@RestController
public class EventProducerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubMultiBindersApplication.class);

    @Resource(name = "many1")
    private Sinks.Many<Message<String>> many1;

    @Resource(name = "many2")
    private Sinks.Many<Message<String>> many2;

    @PostMapping("/messages1")
    public ResponseEntity<String> sendMessage1(@RequestParam String message) {
        LOGGER.info("Going to add message {} to sendMessage1", message);
        many1.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/messages2")
    public ResponseEntity<String> sendMessage2(@RequestParam String message) {
        LOGGER.info("Going to add message {} to sendMessage2.", message);
        many2.emitNext(MessageBuilder.withPayload(message).build(), Sinks.EmitFailureHandler.FAIL_FAST);
        return ResponseEntity.ok(message);
    }
}

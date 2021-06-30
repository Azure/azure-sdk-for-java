// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.binder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("manual")
public class ImperativeEventProducerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImperativeEventProducerController.class);

    //TODO Add output destination
    private String bindingName = "<output-destination>";

    @Autowired
    private StreamBridge streamBridge;

    @PostMapping("/messages/imperative/staticalDestination")
    public ResponseEntity<String> sendMessageToStaticalDestination(@RequestParam String message) {
        LOGGER.info("Imperative method to send message: {} to static destination.", message);
        streamBridge.send("supply-out-0", message);
        LOGGER.info("Sent {}.", message);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/messages/imperative/dynamicDestination")
    public ResponseEntity<String> syncSendMessageToDynamicDestination(@RequestParam String message) {
        LOGGER.info("Imperative method to send message: {} to dynamic destination.", message);
        streamBridge.send(bindingName, message);
        LOGGER.info("Sent {}.", message);
        return ResponseEntity.ok(message);
    }

}

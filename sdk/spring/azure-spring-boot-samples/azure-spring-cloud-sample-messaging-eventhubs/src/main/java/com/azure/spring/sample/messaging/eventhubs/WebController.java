// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.messaging.eventhubs;

import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.messaging.annotation.AzureMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);
    private static final String EVENT_HUB_NAME = "event-hub-name";
    private static final String CONSUMER_GROUP = "$Default";

    @Autowired
    EventHubOperation eventHubOperation;

    @PostMapping("/messages")
    public User send(@RequestBody User user) {
        this.eventHubOperation.sendAsync(EVENT_HUB_NAME, MessageBuilder.withPayload(user).build()).block();
        return user;
    }

    @AzureMessageListener(destination = EVENT_HUB_NAME, group = CONSUMER_GROUP)
    public void handleMessage(User user) {
        LOGGER.info("New message received: '{}'", user);
    }
}

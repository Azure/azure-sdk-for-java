// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.jms;
/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the Key Vault in README.md
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class TopicReceiveController {

    private static final String TOPIC_NAME = "<ServiceBusTopicName>";

    private static final String SUBSCRIPTION_NAME = "<ServiceBusSubscriptionName>";

    private final Logger logger = LoggerFactory.getLogger(TopicReceiveController.class);

    @JmsListener(destination = TOPIC_NAME, containerFactory = "topicJmsListenerContainerFactory",
        subscription = SUBSCRIPTION_NAME)
    public void receiveMessage(User user) {
        logger.info("Received message: {}", user.getName());
    }
}

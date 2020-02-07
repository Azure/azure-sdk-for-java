/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package sample.jms.topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopicSendController {

    private static final String TOPIC_NAME = "tpc001";

    private static final Logger logger = LoggerFactory.getLogger(TopicSendController.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostMapping("/topic")
    public String postMessage(@RequestParam String message) {

        logger.info("Sending message");

        jmsTemplate.convertAndSend(TOPIC_NAME, new User(message));

        return message;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.cloudfoundry.servicebus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class ServiceBusRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusRestController.class);

    private static final String CR = "</BR>";

    private static final String QUEUE_NAME = "que001";

    @Autowired
    private JmsTemplate jmsTemplate;

    @JmsListener(destination = QUEUE_NAME, containerFactory = "jmsListenerContainerFactory")
    public void receiveMessage(User user) {
        LOGGER.info("Received message from queue: {}", user.getName());
    }

    @RequestMapping(value = "/sb", method = RequestMethod.GET)
    @ResponseBody
    public String processMessages(HttpServletResponse response) {
        final StringBuilder result = new StringBuilder();
        result.append("starting..." + CR);
        try {
            result.append("sending queue message" + CR);
            sendQueueMessage();
            result.append("receiving queue message" + CR);
            Thread.sleep(2000);

            result.append("done!" + CR);

        } catch (InterruptedException e) {
            LOGGER.error("Error processing messages", e);
        }

        return result.toString();
    }

    private void sendQueueMessage() {
        final String name = "queue message";
        LOGGER.debug("Sending message: " + name);
        jmsTemplate.convertAndSend(new User(name));
    }

}

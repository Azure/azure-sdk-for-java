// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus.jms;

import com.azure.spring.test.AppRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import static com.azure.spring.test.EnvironmentVariable.SPRING_JMS_PREMIUM_SERVICEBUS_CONNECTION_STRING;
import static com.azure.spring.test.EnvironmentVariable.SPRING_JMS_STANDARD_SERVICEBUS_CONNECTION_STRING;

class ServiceBusJmsIT {

    @Autowired
    private JmsTemplate jmsTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"standard", "premium"})
    void integrationTest(String testType) throws InterruptedException {
        AppRunner apprunner = new AppRunner(DumbApp.class);
        if (testType.equals("standard")) {
            apprunner.property("spring.jms.servicebus.connection-string", SPRING_JMS_STANDARD_SERVICEBUS_CONNECTION_STRING);
        } else {
            apprunner.property("spring.jms.servicebus.connection-string", SPRING_JMS_PREMIUM_SERVICEBUS_CONNECTION_STRING);
        }
        apprunner.property("spring.jms.servicebus.pricing-tier", testType);
        apprunner.start();

        final String name = "Tester";
        jmsTemplate.convertAndSend(Receiver.QUEUE_NAME, new User(name));
        String msg = Receiver.EXCHANGER.exchange(name);
        Assertions.assertEquals(name, msg);
    }

    @SpringBootApplication
    @EnableJms
    public static class DumbApp {

    }
}

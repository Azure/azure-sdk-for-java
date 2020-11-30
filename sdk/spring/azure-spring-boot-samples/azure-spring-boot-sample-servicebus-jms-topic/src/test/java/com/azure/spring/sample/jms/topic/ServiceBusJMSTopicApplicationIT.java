// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.jms.topic;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceBusJMSTopicApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ServiceBusJMSTopicApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Rule
    public OutputCaptureRule outputCaptureRule = new OutputCaptureRule();

    @Test
    public void testTopicSendAndReceiveMessage() throws Exception {
        final String message = UUID.randomUUID().toString();

        mvc.perform(post("/topic?message=" + message)).andExpect(status().isOk())
                .andExpect(content().string(message));

        final String messageReceivedLog = String.format("Received message from topic: %s", message);

        boolean messageReceived = false;

        for (int i = 0; i < 100; i++) {
            final String output = outputCaptureRule.toString();
            if (!messageReceived && output.contains(messageReceivedLog)) {
                messageReceived = true;
            }

            if (messageReceived) {
                break;
            }

            Thread.sleep(1000);
        }

        assertThat(messageReceived).isTrue();
    }

}

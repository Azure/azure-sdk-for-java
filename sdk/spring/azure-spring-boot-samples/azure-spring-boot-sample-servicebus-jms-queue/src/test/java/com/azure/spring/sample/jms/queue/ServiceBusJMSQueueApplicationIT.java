// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.jms.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ServiceBusJMSQueueApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith({ OutputCaptureExtension.class, MockitoExtension.class })
public class ServiceBusJMSQueueApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testQueueSendAndReceiveMessage(CapturedOutput capture) throws Exception {
        final String message = UUID.randomUUID().toString();

        mvc.perform(post("/queue?message=" + message)).andExpect(status().isOk())
                .andExpect(content().string(message));

        final String messageReceivedLog = String.format("Received message from queue: %s", message);

        boolean messageReceived = false;

        for (int i = 0; i < 100; i++) {
            final String output = capture.toString();
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.eventhubs.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = EventHubKafkaBinderApplication.class)
@AutoConfigureMockMvc
@ExtendWith({ OutputCaptureExtension.class, MockitoExtension.class})
public class EventHubKafkaBinderApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testSendAndReceiveMessage(CapturedOutput capturedOutput) throws Exception {
        Thread.sleep(10000);
        String message = UUID.randomUUID().toString();
        mvc.perform(post("/messages?message=" + message)).andExpect(status().isOk())
            .andExpect(content().string(message));
        String messageReceivedLog = String.format("New message received: '%s'", message);

        boolean messageReceived = false;
        for (int i = 0; i < 100; i++) {
            String output = capturedOutput.toString();
            if (output.contains(messageReceivedLog)) {
                messageReceived = true;
                break;
            }

            Thread.sleep(1000);
        }
        assertThat(messageReceived).isTrue();
    }
}

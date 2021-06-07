// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus;

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

@SpringBootTest(classes = ServiceBusIntegrationApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith({ OutputCaptureExtension.class, MockitoExtension.class })
public class ServiceBusIntegrationApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testQueueSendAndReceiveMessage(CapturedOutput capture) throws Exception {
        testSendAndReceiveMessage("queues", capture);
    }

    @Test
    public void testTopicSendAndReceiveMessage(CapturedOutput capture) throws Exception {
        testSendAndReceiveMessage("topics", capture);
    }

    private void testSendAndReceiveMessage(String url, CapturedOutput capture) throws Exception {
        String message = UUID.randomUUID().toString();

        String urlTemplate = String.format("/%s?message=%s", url, message);

        mvc.perform(post(urlTemplate)).andExpect(status().isOk())
           .andExpect(content().string(message));

        String messageReceivedLog = String.format("New message received: '%s'", message);
        String messageCheckpointedLog = String.format("Message '%s' successfully checkpointed", message);
        boolean messageReceived = false;
        boolean messageCheckpointed = false;
        for (int i = 0; i < 100; i++) {
            String output = capture.toString();
            if (!messageReceived && output.contains(messageReceivedLog)) {
                messageReceived = true;
            }

            if (!messageCheckpointed && output.contains(messageCheckpointedLog)) {
                messageCheckpointed = true;
            }

            if (messageReceived && messageCheckpointed) {
                break;
            }

            Thread.sleep(1000);
        }
        assertThat(messageReceived).isTrue();
        assertThat(messageCheckpointed).isTrue();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

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
@SpringBootTest(classes = ServiceBusOperationApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ServiceBusOperationApplicationIT {

    @Rule
    public OutputCaptureRule capture = new OutputCaptureRule();
    @Autowired
    private MockMvc mvc;

    @Test
    public void testQueueSendAndReceiveMessage() throws Exception {
        testSendAndReceiveMessage("queues");
    }

    @Test
    public void testTopicSendAndReceiveMessage() throws Exception {
        testSendAndReceiveMessage("topics");
    }

    private void testSendAndReceiveMessage(String url) throws Exception {
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

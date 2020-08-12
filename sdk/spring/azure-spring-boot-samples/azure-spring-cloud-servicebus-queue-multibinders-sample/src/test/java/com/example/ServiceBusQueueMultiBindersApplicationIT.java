/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceBusQueueMultiBindersApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ServiceBusQueueMultiBindersApplicationIT {

    @Autowired
    private MockMvc mvc;

    @Rule
    public OutputCapture capture = new OutputCapture();

    @Test
    public void testSendAndReceiveMessage() throws Exception {
        String message = UUID.randomUUID().toString();

        mvc.perform(post("/messages?message=" + message)).andExpect(status().isOk())
                .andExpect(content().string(message));

        String messageReceivedLog = String.format("[1] New message received: '%s'", message);
        String messageCheckpointedLog = String.format("[1] Message '%s' successfully checkpointed", message);
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

    @Test
    public void testSendAndReceiveMessage_2() throws Exception {
        String message = UUID.randomUUID().toString();

        mvc.perform(post("/messages1?message=" + message)).andExpect(status().isOk())
                .andExpect(content().string(message));

        String messageReceivedLog = String.format("[2] New message received: '%s'", message);
        String messageCheckpointedLog = String.format("[2] Message '%s' successfully checkpointed", message);
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

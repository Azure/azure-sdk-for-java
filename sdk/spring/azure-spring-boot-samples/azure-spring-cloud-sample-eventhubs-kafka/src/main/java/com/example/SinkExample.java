// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

/**
 * @author Warren Zhu
 */
@EnableBinding(Sink.class)
public class SinkExample {

    @StreamListener(Sink.INPUT)
    public void handleMessage(String message) {
        System.out.println(String.format("New message received: '%s'", message));
    }
}

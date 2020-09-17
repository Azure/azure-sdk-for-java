// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author Yi Liu, 2020-4-30.
 */
public interface CustomProcessor {

    String INPUT = "input";

    String OUTPUT = "output";

    String INPUT1 = "input1";

    String OUTPUT1 = "output1";

    @Input(CustomProcessor.INPUT)
    SubscribableChannel input();

    @Output(CustomProcessor.OUTPUT)
    MessageChannel output();

    @Input(CustomProcessor.INPUT1)
    SubscribableChannel input1();

    @Output(CustomProcessor.OUTPUT1)
    MessageChannel output1();

}

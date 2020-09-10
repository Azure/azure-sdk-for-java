// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@EnableBinding(CustomProcessor.class)
@RestController
public class SourceExample {

    @Autowired
    private CustomProcessor pipe;

    @PostMapping("/messages")
    public String postMessage(@RequestParam String message) {
        this.pipe.output().send(new GenericMessage<>(message));
        return message;
    }

    @PostMapping("/messages1")
    public String postMessage1(@RequestParam String message) {
        this.pipe.output1().send(new GenericMessage<>(message));
        return message;
    }

}

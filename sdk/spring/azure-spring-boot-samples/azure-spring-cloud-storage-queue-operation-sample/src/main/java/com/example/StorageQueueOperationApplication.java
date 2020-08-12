/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import com.microsoft.azure.spring.integration.storage.queue.StorageQueueOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * {@link StorageQueueOperation} code sample.
 *
 * @author Miao Cao
 */
@SpringBootApplication
public class StorageQueueOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageQueueOperationApplication.class, args);
    }
}

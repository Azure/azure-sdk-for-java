// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import java.nio.file.FileSystems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Warren Zhu
 */
@SpringBootApplication
public class StorageApplication {

    public static void main(String[] args) {
        System.out.println("Running in " + FileSystems.getDefault().getPath("").toAbsolutePath().toString());
        System.getenv().forEach((key, val) -> {
            System.err.println(key + ":" + val);
        });
        SpringApplication.run(StorageApplication.class, args);
    }
}

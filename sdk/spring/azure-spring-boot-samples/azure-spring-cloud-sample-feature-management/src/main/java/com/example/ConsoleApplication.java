// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class ConsoleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ConsoleApplication.class);

    @Autowired
    private FeatureManager featureManager;

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("EXECUTING : command line runner");

        if (featureManager.isEnabledAsync("beta").block()) {
            LOGGER.info("RUNNING : beta");
        } else {
            LOGGER.info("RUNNING : application");
        }
    }


}

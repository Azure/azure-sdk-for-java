/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package sample.keyvault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleApplication implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleApplication.class);

    @Value("${yourSecretPropertyName}")
    private String mySecretProperty;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... varl) throws Exception {
        LOGGER.info("property yourSecretPropertyName in Azure Key Vault: {}", mySecretProperty);

        System.out.println("property yourSecretPropertyName in Azure Key Vault: " + mySecretProperty);
    }

}

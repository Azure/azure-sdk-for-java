// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleApplication.class);

    @Value("${keyVaultSecretName}")
    private String keyVaultSecretName;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String[] args) {
        LOGGER.info("property keyVaultSecretName in Azure Key Vault: {}", keyVaultSecretName);

        System.out.println("property keyVaultSecretName in Azure Key Vault: " + keyVaultSecretName);
    }

}

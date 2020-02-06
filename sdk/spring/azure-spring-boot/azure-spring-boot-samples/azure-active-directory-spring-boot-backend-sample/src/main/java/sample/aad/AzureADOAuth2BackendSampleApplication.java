/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.aad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AzureADOAuth2BackendSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzureADOAuth2BackendSampleApplication.class, args);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.jms.ConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceBusJmsAutoConfigurationTest {

    static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    protected ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
        .withConfiguration(AutoConfigurations.of(ServiceBusJmsAutoConfiguration.class));


    @Test
    void testAzureServiceBusJMSPropertiesConnectionStringValidation() {
        this.contextRunner.withPropertyValues("spring.jms.servicebus.pricing-tier=basic")
                          .run(context -> {
                              assertThat(context).hasSingleBean(ConnectionFactory.class);
                          });
    }


}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress;

import com.azure.messaging.servicebus.stress.scenarios.ServiceBusScenario;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * Runner for the Service Bus stress tests.
 */
@SpringBootApplication
public class ServiceBusScenarioRunner implements ApplicationRunner {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ScenarioOptions options;

    public static void main(String[] args) {
        SpringApplication.run(ServiceBusScenarioRunner.class, args);
    }

    /**
     * Run test scenario class.
     *
     * @param args the application arguments. it should contain "--TEST_CLASS='your scenarios class name'".
     */
    @Override
    public void run(ApplicationArguments args) {
        String scenarioName = Objects.requireNonNull(options.getTestClass(),
            "The test class should be provided, please add --TEST_CLASS=<your test class> as start argument");
        ServiceBusScenario scenario = (ServiceBusScenario) applicationContext.getBean(scenarioName);
        scenario.run();
    }
}

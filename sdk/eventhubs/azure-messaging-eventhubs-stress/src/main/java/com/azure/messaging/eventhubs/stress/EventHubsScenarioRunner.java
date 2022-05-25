// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress;

import com.azure.messaging.eventhubs.stress.scenarios.EventHubsScenario;
import com.azure.messaging.eventhubs.stress.util.Constants;
import com.azure.messaging.eventhubs.stress.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Runner for the Event Hubs stress tests.
 */
@SpringBootApplication
public class EventHubsScenarioRunner implements ApplicationRunner {

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ScenarioOptions options;

    public static void main(String[] args) {
        SpringApplication.run(EventHubsScenarioRunner.class, args);
    }

    /**
     * Run test scenario class.
     *
     * @param args the application arguments. it should contain "--TEST_CLASS='your scenarios class name'".
     */
    @Override
    public void run(ApplicationArguments args) {
        String scenarioName = options.get(Constants.TEST_CLASS);
        EventHubsScenario scenario = (EventHubsScenario) applicationContext.getBean(scenarioName);
        scenario.run();
    }
}

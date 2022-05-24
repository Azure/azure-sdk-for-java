package com.azure.messaging.eventhubs.stress.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ScenarioOptions {
    @Autowired
    private Environment env;

    @Autowired
    private ApplicationArguments args;

    public String get(String name) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        } else {
            return env.getProperty(name);
        }
    }

    public String get(String name, String defaultValue) {
        if (args.containsOption(name)) {
            return args.getOptionValues(name).get(0);
        } else if (System.getenv().containsKey(name)) {
            return env.getProperty(name);
        } else {
            return defaultValue;
        }
    }
}

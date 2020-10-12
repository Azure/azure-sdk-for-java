package com.azure.learn.appconfig;

import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.learn.appconfig.models.ConfigurationSetting;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ConditionalAccessSample {
    static final String FONT_COLOR = "FontColor";
    static final String GREETING_TEXT = "GreetingText";
    public static void main(String[] args) throws InterruptedException {
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(System.getenv("API_LEARN_ENDPOINT"))
            .buildClient();
    
    
        Map<String, ConfigurationSetting> configCache = new HashMap<>();
    
        initializeCache(configurationClient, configCache);
        printGreeting(configCache);
    
        for (int i = 0; i < 5; i++) {
            updateCache(configurationClient, configCache);
            printGreeting(configCache);
            TimeUnit.SECONDS.sleep(2);
        }
    }
    
    private static void updateCache(ConfigurationClient configurationClient, Map<String, ConfigurationSetting> configCache) {
        ConfigurationSetting newFontColor = configurationClient.getConfigurationSettingWithResponse(configCache.get(FONT_COLOR), true, Context.NONE).getValue();

        if (newFontColor != null) {
            configCache.putIfAbsent("FontColor", newFontColor);
        }

        ConfigurationSetting newGreetingText = configurationClient.getConfigurationSettingWithResponse(configCache.get(GREETING_TEXT), true, Context.NONE).getValue();
        if (newGreetingText != null) {
            configCache.putIfAbsent("GreetingText", newGreetingText);
        }
    }
    
    private static void printGreeting(Map<String, ConfigurationSetting> configCache) {
        System.out.println(configCache.get("FontColor").getValue() + configCache.get("GreetingText").getValue());
    }
    
    private static void initializeCache(ConfigurationClient configurationClient, Map<String, ConfigurationSetting> configCache) {
        configCache.putIfAbsent("FontColor", configurationClient.getConfigurationSetting("FontColor"));
        configCache.putIfAbsent("GreetingText", configurationClient.getConfigurationSetting("GreetingText"));
    }
}

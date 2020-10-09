package com.azure.learn.appconfig;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.learn.appconfig.models.ConfigurationSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConditionalAccessSample {

    public static final String FONT_COLOR = "FontColor";
    public static final String GREETING_TEXT = "GreetingText";

    public static void main(String[] args) throws InterruptedException {
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(System.getenv("APP_CONFIG_URL"))
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
        ConfigurationSetting newFontColor = configurationClient.getConfigurationSetting(configCache.get(FONT_COLOR), true);
        if (newFontColor != null) {
            configCache.putIfAbsent(FONT_COLOR, newFontColor);
        }

        ConfigurationSetting newGreetingText = configurationClient.getConfigurationSetting(configCache.get(GREETING_TEXT), true);
        if (newGreetingText != null) {
            configCache.putIfAbsent(GREETING_TEXT, newGreetingText);
        }
    }

    private static void printGreeting(Map<String, ConfigurationSetting> configCache) {
        System.out.println(configCache.get(FONT_COLOR).getValue() + configCache.get(GREETING_TEXT).getValue());
    }

    private static void initializeCache(ConfigurationClient configurationClient, Map<String, ConfigurationSetting> configCache) {
        configCache.putIfAbsent("FontColor", configurationClient.getConfigurationSetting("FontColor"));
        configCache.putIfAbsent("GreetingText", configurationClient.getConfigurationSetting("GreetingText"));
    }
}

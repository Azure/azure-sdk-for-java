// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = {"app", "service-bus-jms"})
public class AppConfigurationIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationIT.class);
    private final String sampleKey = "sample-key";
    private final String sampleLabel = "sample-label";
    private final String sampleValue = "sample-value";

    @Autowired
    private ConfigurationClient client;

    @Test
    public void testAppConfigurationOperation() {
        LOGGER.info("AppConfigurationIT begin.");
        client.addConfigurationSetting(sampleKey, sampleLabel, sampleValue);
        ConfigurationSetting configurationSetting = client.getConfigurationSetting(sampleKey, sampleLabel);
        Assertions.assertEquals(configurationSetting.getValue(),sampleValue);
        LOGGER.info("AppConfigurationIT end.");
    }

}

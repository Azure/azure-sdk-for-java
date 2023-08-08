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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("appconfiguration")
public class AppConfigurationIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationIT.class);
    private static final String SAMPLE_KEY = "sample-key";
    private static final String SAMPLE_LABEL = "sample-label";
    private static final String SAMPLE_VALUE = "sample-value";

    @Autowired
    private ConfigurationClient client;

    @Test
    public void testAppConfigurationOperation() {
        LOGGER.info("AppConfigurationIT begin.");
        client.addConfigurationSetting(SAMPLE_KEY, SAMPLE_LABEL, SAMPLE_VALUE);
        ConfigurationSetting configurationSetting = client.getConfigurationSetting(SAMPLE_KEY, SAMPLE_LABEL);
        Assertions.assertEquals(SAMPLE_VALUE, configurationSetting.getValue());
        LOGGER.info("AppConfigurationIT end.");
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AadCredentialTest extends TestBase {

    private ConfigurationAsyncClient client;


    @BeforeAll
    public void setup() {
        client = new ConfigurationClientBuilder()
            .buildAsyncClient();
    }
    @Test
    public void addKey(){
        ConfigurationSetting setting = client.addConfigurationSetting("shawn", null, "fang").block();
        System.out.println(String.format("Setting: key = %s", setting.getKey()));
    }
}

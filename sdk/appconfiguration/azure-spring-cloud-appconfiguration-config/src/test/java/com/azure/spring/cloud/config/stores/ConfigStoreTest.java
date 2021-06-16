// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import com.azure.spring.cloud.config.properties.ConfigStore;

public class ConfigStoreTest {

    @Test(expected = IllegalArgumentException.class)
    public void invalidLabel() {
        ConfigStore configStore = new ConfigStore();
        configStore.setLabel("*");
        configStore.validateAndInit();
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void invalidEndpoint() {
        ConfigStore configStore = new ConfigStore();
        configStore.setConnectionString("Endpoint=a^a;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==");
        configStore.validateAndInit();
        fail();
    }

    @Test
    public void getLabelsTest() {
        ConfigStore configStore = new ConfigStore();
        assertEquals(configStore.getLabels(new ArrayList<String>())[0], "\0");

        configStore.setLabel("dev");
        assertEquals(configStore.getLabels(new ArrayList<String>())[0], "dev");

        configStore.setLabel("dev,test");
        assertEquals(configStore.getLabels(new ArrayList<String>())[0], "test");
        assertEquals(configStore.getLabels(new ArrayList<String>())[1], "dev");

        configStore.setLabel(",");
        assertEquals(configStore.getLabels(new ArrayList<String>())[0], "\0");
    }

}

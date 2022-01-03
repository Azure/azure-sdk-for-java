// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;

public class ConfigStoreTest {

    @Test
    public void invalidLabel() {
        ConfigStore configStore = new ConfigStore();
        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/")
            .setLabelFilter("*");
        List<AppConfigurationStoreSelects> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);

        assertThrows(IllegalArgumentException.class, () -> configStore.validateAndInit());
    }

    @Test
    public void invalidKey() {
        ConfigStore configStore = new ConfigStore();
        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/*");
        List<AppConfigurationStoreSelects> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        
        assertThrows(IllegalArgumentException.class, () -> configStore.validateAndInit());
    }

    @Test
    public void invalidEndpoint() {
        ConfigStore configStore = new ConfigStore();
        configStore.validateAndInit();
        configStore.setConnectionString("Endpoint=a^a;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==");

        assertThrows(IllegalStateException.class, () -> configStore.validateAndInit());
    }

    @Test
    public void getLabelsTest() {
        ConfigStore configStore = new ConfigStore();
        configStore.validateAndInit();

        assertEquals("\0", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);

        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/")
            .setLabelFilter("dev");
        List<AppConfigurationStoreSelects> selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        assertEquals("dev", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);

        selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/").setLabelFilter("dev,test");
        selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        assertEquals("test", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);
        assertEquals("dev", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[1]);

        selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/").setLabelFilter(",");
        selects = new ArrayList<>();
        selects.add(selectedKeys);
        configStore.setSelects(selects);
        assertEquals("\0", configStore.getSelects().get(0).getLabelFilter(new ArrayList<>())[0]);
    }

}

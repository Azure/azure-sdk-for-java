// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AppConfigurationStoreSelectsTest {

    @Test
    public void labelOverProfiles() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector().setLabelFilter("v1");

        List<String> profiles = new ArrayList<>();
        profiles.add("dev");

        String[] results = selects.getLabelFilter(profiles);
        assertEquals(1, results.length);
        assertEquals("v1", results[0]);
        selects.validateAndInit();
    }

    @Test
    public void useProfiles() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector();

        List<String> profiles = new ArrayList<>();
        profiles.add("dev");

        String[] results = selects.getLabelFilter(profiles);
        assertEquals(1, results.length);
        assertEquals("dev", results[0]);
        selects.validateAndInit();
    }

    @Test
    public void defaultCase() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector();

        String[] results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(1, results.length);
        assertEquals("\0", results[0]);
        selects.validateAndInit();
    }

    @Test
    public void emptyCases() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector().setLabelFilter(" ");

        String[] results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(1, results.length);
        assertEquals("\0", results[0]);
        selects.validateAndInit();

        selects.setLabelFilter("");

        results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(1, results.length);
        assertEquals("\0", results[0]);
        selects.validateAndInit();
    }

    @Test
    public void multipleLabels() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector().setLabelFilter("dev,test");

        String[] results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(2, results.length);
        assertEquals("test", results[0]);
        assertEquals("dev", results[1]);
        selects.validateAndInit();

        selects.setLabelFilter("dev,,test");

        results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(3, results.length);
        assertEquals("test", results[0]);
        assertEquals("\0", results[1]);
        assertEquals("dev", results[2]);
        selects.validateAndInit();

        selects.setLabelFilter("dev,\0,test");

        results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(3, results.length);
        assertEquals("test", results[0]);
        assertEquals("\0", results[1]);
        assertEquals("dev", results[2]);
        selects.validateAndInit();
    }

    @Test
    public void workaroundForEmptyLabelConfig() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector().setLabelFilter("v1,");

        String[] results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(2, results.length);
        assertEquals("\0", results[0]);
        assertEquals("v1", results[1]);
        selects.validateAndInit();
    }

    @Test
    public void invalidCharacters() {
        AppConfigurationKeyValueSelector selects = new AppConfigurationKeyValueSelector().setLabelFilter("v1*");

        String[] results = selects.getLabelFilter(new ArrayList<>());
        assertEquals(1, results.length);
        assertEquals("v1*", results[0]);
        assertThrows(IllegalArgumentException.class, selects::validateAndInit);

        AppConfigurationKeyValueSelector selects2 = new AppConfigurationKeyValueSelector().setLabelFilter("v1")
            .setKeyFilter("/application/*");

        results = selects2.getLabelFilter(new ArrayList<>());
        assertEquals(1, results.length);
        assertEquals("v1", results[0]);
        assertThrows(IllegalArgumentException.class, selects2::validateAndInit);
    }

}

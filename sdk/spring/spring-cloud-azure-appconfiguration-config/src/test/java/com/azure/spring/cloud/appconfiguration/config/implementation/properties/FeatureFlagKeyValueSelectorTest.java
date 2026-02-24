// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FeatureFlagKeyValueSelectorTest {

    @Test
    public void validateAndInitTest() {
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        selector.validateAndInit();

        selector.setLabelFilter("de*");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> selector.validateAndInit());
        assertEquals("LabelFilter must not contain asterisk(*)", e.getMessage());

        selector.setLabelFilter("dev");
        selector.validateAndInit();
    }

    @Test
    public void getLabelFilterTest() {
        // Default is Empty Label
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        selector.validateAndInit();

        List<String> profiles = new ArrayList<>();

        String[] labels = selector.getLabelFilter(profiles);

        assertEquals(1, labels.length);
        assertTrue(EMPTY_LABEL.equalsIgnoreCase(labels[0]));

        // Uses the profile
        profiles.add("dev");
        labels = selector.getLabelFilter(profiles);

        assertEquals(1, labels.length);
        assertEquals("dev", labels[0]);

        // Label should override profile
        selector.setLabelFilter("test");
        labels = selector.getLabelFilter(profiles);

        assertEquals(1, labels.length);
        assertEquals("test", labels[0]);

        // Multiple Labels, List is reversed as high number will have priority
        selector.setLabelFilter("test1, test2");
        labels = selector.getLabelFilter(profiles);

        assertEquals(2, labels.length);
        assertEquals("test2", labels[0]);
        assertEquals("test1", labels[1]);

        // Multiple Labels, Ending with a comma results in a null label
        selector.setLabelFilter("test1,");
        labels = selector.getLabelFilter(profiles);

        assertEquals(2, labels.length);
        assertEquals(EMPTY_LABEL, labels[0]);
        assertEquals("test1", labels[1]);
    }

    @Test
    public void getTagsFilterDefaultTest() {
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        // When no tags filter is set, should return null
        assertNull(selector.getTagsFilter());
    }

    @Test
    public void getTagsFilterCustomTest() {
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        List<String> tags = Arrays.asList("env=prod", "team=backend");
        selector.setTagsFilter(tags);

        List<String> result = selector.getTagsFilter();
        assertEquals(2, result.size());
        assertEquals("env=prod", result.get(0));
        assertEquals("team=backend", result.get(1));
    }

    @Test
    public void setTagsFilterReturnsSelectorTest() {
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        FeatureFlagKeyValueSelector returned = selector.setTagsFilter(Arrays.asList("env=dev"));
        assertSame(selector, returned);
    }

    @Test
    public void setTagsFilterEmptyListTest() {
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        selector.setTagsFilter(new ArrayList<>());
        List<String> result = selector.getTagsFilter();
        assertTrue(result.isEmpty());
    }

    @Test
    public void setTagsFilterSingleTagTest() {
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        selector.setTagsFilter(Arrays.asList("environment=production"));

        List<String> result = selector.getTagsFilter();
        assertEquals(1, result.size());
        assertEquals("environment=production", result.get(0));
    }

}

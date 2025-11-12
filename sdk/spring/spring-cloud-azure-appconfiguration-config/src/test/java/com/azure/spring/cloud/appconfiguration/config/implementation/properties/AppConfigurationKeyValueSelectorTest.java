// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;

public class AppConfigurationKeyValueSelectorTest {

    private AppConfigurationKeyValueSelector selector;

    @BeforeEach
    public void setup() {
        selector = new AppConfigurationKeyValueSelector();
    }

    @Test
    public void getKeyFilterDefaultTest() {
        // When no key filter is set, should return default
        String keyFilter = selector.getKeyFilter();
        assertEquals("/application/", keyFilter);
    }

    @Test
    public void getKeyFilterCustomTest() {
        // When custom key filter is set
        selector.setKeyFilter("/custom/");
        String keyFilter = selector.getKeyFilter();
        assertEquals("/custom/", keyFilter);
    }

    @Test
    public void getKeyFilterEmptyTest() {
        // When empty key filter is set, should return default
        selector.setKeyFilter("");
        String keyFilter = selector.getKeyFilter();
        assertEquals("/application/", keyFilter);
    }

    @Test
    public void getLabelFilterWithProfilesTest() {
        // Test with profiles when labelFilter is null
        List<String> profiles = new ArrayList<>(Arrays.asList("dev", "prod", "staging"));
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should be reversed: staging, prod, dev
        assertArrayEquals(new String[]{"staging", "prod", "dev"}, result);
    }

    @Test
    public void getLabelFilterWithEmptyProfilesTest() {
        // Test with empty profiles when labelFilter is null
        List<String> profiles = new ArrayList<>();
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should return empty label array
        assertArrayEquals(new String[]{EMPTY_LABEL}, result);
    }

    @Test
    public void getLabelFilterWithImmutableProfilesTest() {
        // Test with immutable profiles list (simulates the UnsupportedOperationException scenario)
        List<String> profiles = List.of("dev", "prod"); // Immutable list
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should handle immutable list and return reversed: prod, dev
        assertArrayEquals(new String[]{"prod", "dev"}, result);
    }

    @Test
    public void getLabelFilterWithSnapshotTest() {
        // Test when snapshot is set
        selector.setSnapshotName("test-snapshot");
        List<String> profiles = Arrays.asList("dev", "prod");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should return empty array when snapshot is set
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void getLabelFilterWithCustomLabelFilterTest() {
        // Test with custom label filter
        selector.setLabelFilter("dev,prod,staging");
        List<String> profiles = Arrays.asList("ignored");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should be reversed and trimmed
        assertArrayEquals(new String[]{"staging", "prod", "dev"}, result);
    }

    @Test
    public void getLabelFilterWithLabelFilterTrailingCommaTest() {
        // Test with trailing comma in label filter
        selector.setLabelFilter("dev,prod,");
        List<String> profiles = Arrays.asList("ignored");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should include empty label at the end after reversing
        assertArrayEquals(new String[]{EMPTY_LABEL, "prod", "dev"}, result);
    }

    @Test
    public void getLabelFilterWithSpacesTest() {
        // Test label filter with spaces (should be trimmed)
        selector.setLabelFilter(" dev , prod , staging ");
        List<String> profiles = Arrays.asList("ignored");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should be reversed and trimmed
        assertArrayEquals(new String[]{"staging", "prod", "dev"}, result);
    }

    @Test
    public void getLabelFilterWithDuplicatesTest() {
        // Test label filter with duplicates (should be distinct)
        selector.setLabelFilter("dev,prod,dev,staging,prod");
        List<String> profiles = Arrays.asList("ignored");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should be reversed, trimmed, and distinct
        assertArrayEquals(new String[]{"staging", "prod", "dev"}, result);
    }

    @Test
    public void getLabelFilterWithEmptyLabelsTest() {
        // Test label filter with empty labels
        selector.setLabelFilter("dev,,prod,");
        List<String> profiles = Arrays.asList("ignored");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Should match the expected order and duplicates, including EMPTY_LABEL
        assertArrayEquals(new String[]{EMPTY_LABEL, "prod", EMPTY_LABEL, "dev"}, result);
    }

    @Test
    public void validateAndInitValidConfigurationTest() {
        // Test valid configuration
        selector.setKeyFilter("/valid/");
        selector.setLabelFilter("dev,prod");
        
        // Should not throw any exception
        selector.validateAndInit();
    }

    @Test
    public void validateAndInitInvalidKeyFilterTest() {
        // Test invalid key filter with asterisk
        selector.setKeyFilter("/invalid*filter/");
        
        assertThrows(IllegalArgumentException.class, () -> {
            selector.validateAndInit();
        });
    }

    @Test
    public void validateAndInitInvalidLabelFilterTest() {
        // Test invalid label filter with asterisk
        selector.setLabelFilter("dev*,prod");
        
        assertThrows(IllegalArgumentException.class, () -> {
            selector.validateAndInit();
    });
    }

    @Test
    public void validateAndInitSnapshotWithKeyFilterTest() {
        // Test snapshot with key filter (should fail)
        selector.setKeyFilter("/test/");
        selector.setSnapshotName("test-snapshot");
        
        assertThrows(IllegalArgumentException.class, () -> {
            selector.validateAndInit();
        });
    }

    @Test
    public void validateAndInitSnapshotWithLabelFilterTest() {
        // Test snapshot with label filter (should fail)
        selector.setLabelFilter("dev");
        selector.setSnapshotName("test-snapshot");
        
        assertThrows(IllegalArgumentException.class, () -> {
            selector.validateAndInit();
        });
    }

    @Test
    public void validateAndInitValidSnapshotTest() {
        // Test valid snapshot configuration (no key or label filters)
        selector.setSnapshotName("test-snapshot");
        
        // Should not throw any exception
        selector.validateAndInit();
    }

    @Test
    public void mapLabelNullTest() {
        // Test the private mapLabel method indirectly through getLabelFilter
        selector.setLabelFilter("dev,,prod");
        List<String> profiles = Arrays.asList("ignored");
        
        String[] result = selector.getLabelFilter(profiles);
        
        // Empty label should be converted to EMPTY_LABEL constant
        assertTrue(Arrays.asList(result).contains(EMPTY_LABEL));
    }

    @Test
    public void complexScenarioTest() {
        // Test a complex real-world scenario
        List<String> profiles = new ArrayList<>(Arrays.asList("test", "dev", "prod"));
        
        // First call with profiles
        String[] profileResult = selector.getLabelFilter(profiles);
        assertArrayEquals(new String[]{"prod", "dev", "test"}, profileResult);
        
        // Then set a custom label filter
        selector.setLabelFilter("custom1, custom2 ,custom3,");
        String[] customResult = selector.getLabelFilter(profiles);
        assertArrayEquals(new String[]{EMPTY_LABEL, "custom3", "custom2", "custom1"}, customResult);
        
        // Finally set a snapshot (should override everything)
        selector.setSnapshotName("final-snapshot");
        String[] snapshotResult = selector.getLabelFilter(profiles);
        assertArrayEquals(new String[0], snapshotResult);
    }

    @Test
    public void profilesListModificationSafetyTest() {
        // Test that the original profiles list is not modified
        List<String> originalProfiles = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> profilesCopy = new ArrayList<>(originalProfiles);
        
        selector.getLabelFilter(originalProfiles);
        
        // Original list should remain unchanged
        assertEquals(profilesCopy, originalProfiles);
    }
}
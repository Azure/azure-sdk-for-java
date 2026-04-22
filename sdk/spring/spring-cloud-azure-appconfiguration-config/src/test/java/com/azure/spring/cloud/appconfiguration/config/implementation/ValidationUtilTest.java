// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ValidationUtilTest {

    @Test
    public void testValidateTagsFilterNullList() {
        assertDoesNotThrow(() -> ValidationUtil.validateTagsFilter(null));
    }

    @Test
    public void testValidateTagsFilterEmptyList() {
        List<String> tagsFilter = Collections.emptyList();
        assertDoesNotThrow(() -> ValidationUtil.validateTagsFilter(tagsFilter));
    }

    @Test
    public void testValidateTagsFilterValidSingleTag() {
        List<String> tagsFilter = Collections.singletonList("env=prod");
        assertDoesNotThrow(() -> ValidationUtil.validateTagsFilter(tagsFilter));
    }

    @Test
    public void testValidateTagsFilterValidMultipleTags() {
        List<String> tagsFilter = Arrays.asList("env=prod", "team=backend", "region=us-east");
        assertDoesNotThrow(() -> ValidationUtil.validateTagsFilter(tagsFilter));
    }

    @Test
    public void testValidateTagsFilterValidTagWithSpecialCharactersInValue() {
        List<String> tagsFilter = Collections.singletonList("version=1.0.0-beta");
        assertDoesNotThrow(() -> ValidationUtil.validateTagsFilter(tagsFilter));
    }

    @Test
    public void testValidateTagsFilterValidTagWithEqualsInValue() {
        List<String> tagsFilter = Collections.singletonList("formula=x=y+z");
        assertDoesNotThrow(() -> ValidationUtil.validateTagsFilter(tagsFilter));
    }

    @Test
    public void testValidateTagsFilterNullEntry() {
        List<String> tagsFilter = Arrays.asList("env=prod", null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag filter entries must not be null or empty", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterEmptyEntry() {
        List<String> tagsFilter = Arrays.asList("env=prod", "");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag filter entries must not be null or empty", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterWhitespaceOnlyEntry() {
        List<String> tagsFilter = Arrays.asList("env=prod", "   ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag filter entries must not be null or empty", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterMissingEqualsSeparator() {
        List<String> tagsFilter = Collections.singletonList("envprod");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag filter entries must be in tagName=tagValue format", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterEmptyTagName() {
        List<String> tagsFilter = Collections.singletonList("=prod");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag name must not be empty in tag filter: =prod", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterWhitespaceOnlyTagName() {
        List<String> tagsFilter = Collections.singletonList("  =prod");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag name must not be empty in tag filter:   =prod", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterEmptyTagValue() {
        List<String> tagsFilter = Collections.singletonList("env=");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag value must not be empty in tag filter: env=", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterWhitespaceOnlyTagValue() {
        List<String> tagsFilter = Collections.singletonList("env=   ");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag value must not be empty in tag filter: env=   ", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterMultipleInvalidEntriesFailsOnFirst() {
        List<String> tagsFilter = Arrays.asList("=invalid", "alsoInvalid", "env=valid");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag name must not be empty in tag filter: =invalid", exception.getMessage());
    }

    @Test
    public void testValidateTagsFilterMixedValidAndInvalidEntries() {
        List<String> tagsFilter = Arrays.asList("env=prod", "team=backend", "invalid");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ValidationUtil.validateTagsFilter(tagsFilter));
        assertEquals("Tag filter entries must be in tagName=tagValue format", exception.getMessage());
    }
}

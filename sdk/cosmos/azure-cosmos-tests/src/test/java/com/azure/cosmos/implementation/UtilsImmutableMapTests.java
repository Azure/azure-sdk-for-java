/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.cosmos.implementation;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Utils#immutableMapOf(Object, Object)} and the matching detector
 * {@link Utils#isImmutableMap(Map)}. These live next to the factory so that any future
 * change to the factory's runtime class shape is caught by the same regression suite.
 */
public class UtilsImmutableMapTests {

    @Test(groups = { "unit" })
    public void immutableMapOf_isDetectedAsImmutable() {
        Map<String, String> m = Utils.immutableMapOf("k", "v");
        assertThat(Utils.isImmutableMap(m)).isTrue();
    }

    @Test(groups = { "unit" })
    public void emptyMap_isDetectedAsImmutable() {
        assertThat(Utils.isImmutableMap(Collections.emptyMap())).isTrue();
    }

    @Test(groups = { "unit" })
    public void hashMap_isNotDetectedAsImmutable() {
        assertThat(Utils.isImmutableMap(new HashMap<>())).isFalse();
        Map<String, String> populated = new HashMap<>();
        populated.put("k", "v");
        assertThat(Utils.isImmutableMap(populated)).isFalse();
    }

    @Test(groups = { "unit" })
    public void linkedHashMap_isNotDetectedAsImmutable() {
        assertThat(Utils.isImmutableMap(new LinkedHashMap<>())).isFalse();
    }

    @Test(groups = { "unit" })
    public void nullMap_isNotDetectedAsImmutable() {
        assertThat(Utils.isImmutableMap(null)).isFalse();
    }
}

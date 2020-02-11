// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RangeTest {

    @Test(groups = { "unit" })
    public void getEmptyRange() {
        Range<String> range = Range.getEmptyRange("xyz");
        assertThat(range.getMin()).isEqualTo("xyz");
        assertThat(range.getMax()).isEqualTo("xyz");
        assertThat(range.isMaxInclusive()).isEqualTo(false);
        assertThat(range.isMinInclusive()).isEqualTo(true);
    }

    @Test(groups = { "unit" })
    public void getPointRange() {
        Range<String> range = Range.getPointRange("xyz");
        assertThat(range.getMin()).isEqualTo("xyz");
        assertThat(range.getMax()).isEqualTo("xyz");
        assertThat(range.isMinInclusive()).isEqualTo(true);
        assertThat(range.isMaxInclusive()).isEqualTo(true);
    }

    @Test(groups = { "unit" })
    public void constructor1() {
        Range<String> range = new Range<>("abc", "xyz", true, true);
        assertThat(range.getMin()).isEqualTo("abc");
        assertThat(range.getMax()).isEqualTo("xyz");
        assertThat(range.isMinInclusive()).isEqualTo(true);
        assertThat(range.isMaxInclusive()).isEqualTo(true);
    }

    @Test(groups = { "unit" })
    public void constructor2() {
        Range<String> range = new Range<>("abc", "xyz", true, false);
        assertThat(range.getMin()).isEqualTo("abc");
        assertThat(range.getMax()).isEqualTo("xyz");
        assertThat(range.isMinInclusive()).isEqualTo(true);
        assertThat(range.isMaxInclusive()).isEqualTo(false);
        assertThat(range.toJson()).isEqualTo("{\"min\":\"abc\",\"max\":\"xyz\",\"isMinInclusive\":true,\"isMaxInclusive\":false}");
    }

    @Test(groups = { "unit" })
    public void parseJson() {
        Range<String> range = new Range<>("{'min':'abc','max':'xyz','isMinInclusive':true,'isMaxInclusive':false}");
        assertThat(range.getMin()).isEqualTo("abc");
        assertThat(range.getMax()).isEqualTo("xyz");
        assertThat(range.isMinInclusive()).isEqualTo(true);
        assertThat(range.isMaxInclusive()).isEqualTo(false);
    }
}

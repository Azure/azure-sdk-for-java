// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.NameFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class NameFormatterTests {

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   DTSomeRandomValueInfo",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsClass(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsClass(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsClass(input), expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   someRandomValue",
        "SomeRandomValue    |   someRandomValue",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsParameter(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsParameter(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsParameter(input), expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   someRandomValue",
        "SomeRandomValue    |   someRandomValue",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsProperty(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsProperty(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsProperty(input), expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   DTSomeRandomValueKind",
        "SomeRandomValue    |   DTSomeRandomValueKind",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsEnum(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsEnum(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsEnum(input), expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   someRandomValueKind",
        "SomeRandomValue    |   someRandomValueKind",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsEnumProperty(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsEnumProperty(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsEnumProperty(input), expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   SOMERANDOMVALUE",
        "SomeRandomValue    |   SOMERANDOMVALUE",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsEnumValue(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsEnumValue(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsEnumValue(input), expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "someRandomValue    |   someRandomValue",
        "SomeRandomValue    |   someRandomValue",
        "some RandomValue   |   ",
        "                   |   "
    }, delimiter = '|')
    public void formatNameAsField(String input, String expected) {
        if (input == null || input.length() < 2 || input.contains(" ")) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> NameFormatter.formatNameAsField(input));
            return;
        }

        Assertions.assertEquals(NameFormatter.formatNameAsField(input), expected);
    }
}

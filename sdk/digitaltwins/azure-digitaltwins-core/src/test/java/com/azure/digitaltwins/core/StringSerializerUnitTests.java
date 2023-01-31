// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsSerializerAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

public class StringSerializerUnitTests {
    private static final SerializerAdapter ADAPTER = new DigitalTwinsSerializerAdapter();

    @ParameterizedTest
    @CsvSource(value = {
        "1234                       | \"1234\"",
        "false                      | \"false\"",
        "true                       | \"true\"",
        "1234 room                  | \"1234 room\"",
        "{ \"a\" : 2 }              | { \"a\" : 2 }",
        "{ \"a\" : false }          | { \"a\" : false }",
        "{ \"a\" : \"false\" }      | { \"a\" : \"false\" }",
        "{ \"a\" : \"some text\" }  | { \"a\" : \"some text\" }",
        "[ 3, 2 ]                   | [ 3, 2 ]"
    }, delimiter = '|')
    public void serializeStringTokens(String input, String expected) throws IOException {
        Assertions.assertEquals(expected, ADAPTER.serialize(input, SerializerEncoding.JSON));
    }
}

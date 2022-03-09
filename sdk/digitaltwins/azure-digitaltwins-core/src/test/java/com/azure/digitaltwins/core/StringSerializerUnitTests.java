// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsStringSerializer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.StringWriter;

public class StringSerializerUnitTests {

    private static DigitalTwinsStringSerializer serializer = new DigitalTwinsStringSerializer(String.class, new ObjectMapper());

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
        String result  = serializeTheToken(input);
        Assertions.assertEquals(expected, result);
    }

    private String serializeTheToken(String token) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);

        serializer.serialize(token, generator, null);
        generator.flush();
        generator.close();

        return writer.toString();
    }
}

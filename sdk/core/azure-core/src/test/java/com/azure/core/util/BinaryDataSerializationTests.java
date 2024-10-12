// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BinaryDataSerializationTests {

    private static final TypeReference<List<BinaryData>> TYPE_REFERENCE_LIST_BINARY_DATA = new TypeReference<List<BinaryData>>() {
    };

    private static final String jsonString = "[1,\"hello\"]";

    @Test
    public void testBinaryDataInCollectionDeserialization() {
        BinaryData data = BinaryData.fromBytes(jsonString.getBytes(StandardCharsets.UTF_8));

        List<BinaryData> list = data.toObject(TYPE_REFERENCE_LIST_BINARY_DATA);

        Assertions.assertArrayEquals("1".getBytes(StandardCharsets.UTF_8), list.get(0).toBytes());
        Assertions.assertArrayEquals("\"hello\"".getBytes(StandardCharsets.UTF_8), list.get(1).toBytes());
        // array lengths differ, Actual   :5, it is "hello" without quote marks
    }

    @Test
    public void testBinaryDataInCollectionSerialization() {
        List<BinaryData> list = new ArrayList<>();
        list.add(BinaryData.fromObject(1));
        list.add(BinaryData.fromObject("hello"));

        Assertions.assertArrayEquals("1".getBytes(StandardCharsets.UTF_8), list.get(0).toBytes());

        BinaryData data = BinaryData.fromObject(list);

        String json = data.toString();
        Assertions.assertEquals(jsonString, json);
    }

    @Test
    public void testBinaryDataInCollectionSerialization2() {
        List<BinaryData> list = new ArrayList<>();
        list.add(BinaryData.fromString("1"));
        list.add(BinaryData.fromString("\"hello\""));

        Assertions.assertArrayEquals("1".getBytes(StandardCharsets.UTF_8), list.get(0).toBytes());

        BinaryData data = BinaryData.fromObject(list);

        String json = data.toString();
        Assertions.assertEquals(jsonString, json);
        // Actual   :["1","\"hello\""]

        // Actual   :["MQ==","ImhlbGxvIg=="], if BinaryData.fromBytes
    }
}

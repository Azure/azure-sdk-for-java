// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageBodyTests {

    @Test
    public void nullBinaryDataTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromBinaryData(null),
            "MessageBody created with null binary data.");
    }

    @Test
    public void nullSequenceTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromSequenceData(null),
            "MessageBody created with null sequence data.");
    }

    @Test
    public void nullValueDataTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromValueData(null),
            "MessageBody created with null value data.");
    }

    @Test
    public void multipleDataSectionsTest() {
        ArrayList<byte[]> dataList = new ArrayList<>();
        dataList.add(new byte[0]);
        dataList.add(new byte[0]);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromBinaryData(dataList),
            "MessageBody created with null binary data.");
    }

    @Test
    public void zeroDataSectionsTest() {
        ArrayList<byte[]> dataList = new ArrayList<>();
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromBinaryData(dataList),
            "MessageBody created with null binary data.");
    }

    @Test
    public void multipleSequenceSectionsTest() {
        ArrayList<List<Object>> sequenceList = new ArrayList<>();
        ArrayList<Object> sequence1 = new ArrayList<>();
        sequence1.add("hello");
        ArrayList<Object> sequence2 = new ArrayList<>();
        sequence2.add("howdy");
        sequenceList.add(sequence1);
        sequenceList.add(sequence2);
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromSequenceData(sequenceList),
            "MessageBody created with null binary data.");
    }

    @Test
    public void zeroSequenceSectionsTest() {
        ArrayList<List<Object>> sequenceList = new ArrayList<>();
        Assertions.assertThrows(IllegalArgumentException.class, () -> MessageBody.fromSequenceData(sequenceList),
            "MessageBody created with null binary data.");
    }

    @Test
    public void valueMessageBodyTest() {
        String value = "ValueBody";
        MessageBody body = MessageBody.fromValueData(value);
        Assertions.assertEquals(MessageBodyType.VALUE, body.getBodyType(), "Message body type didn't match.");
        Assertions.assertNull(body.getBinaryData(), "MessageBody of value type has binary data.");
        Assertions.assertNull(body.getSequenceData(), "MessageBody of value type has sequence data.");
        Assertions.assertEquals(value, body.getValueData(), "Message body value didn't match");
    }

    @Test
    public void sequenceMessageBodyTest() {
        String str1 = "hello";
        String str2 = "howdy";
        ArrayList<List<Object>> sequenceList = new ArrayList<>();
        ArrayList<Object> sequence1 = new ArrayList<>();
        sequence1.add(str1);
        sequence1.add(str2);
        sequenceList.add(sequence1);
        MessageBody body = MessageBody.fromSequenceData(sequenceList);
        Assertions.assertEquals(MessageBodyType.SEQUENCE, body.getBodyType(), "Message body type didn't match.");
        Assertions.assertNull(body.getBinaryData(), "MessageBody of sequence type has binary data.");
        Assertions.assertNull(body.getValueData(), "MessageBody of sequence type has value data.");
        List<List<Object>> outputSequenceList = body.getSequenceData();
        Assertions.assertEquals(1, outputSequenceList.size(), "Message body sequence didn't match");
        List<Object> outputInnerSequence = outputSequenceList.get(0);
        Assertions.assertEquals(2, outputInnerSequence.size(), "Message body sequence didn't match");
        Assertions.assertEquals(str1, outputInnerSequence.get(0), "Message body sequence didn't match");
        Assertions.assertEquals(str2, outputInnerSequence.get(1), "Message body sequence didn't match");
    }

    @Test
    public void binaryMessageBodyTest() {
        byte[] binaryData = new byte[1024];
        Arrays.fill(binaryData, (byte) 32);
        ArrayList<byte[]> binaryDataList = new ArrayList<>();
        binaryDataList.add(binaryData);
        MessageBody body = MessageBody.fromBinaryData(binaryDataList);
        Assertions.assertEquals(MessageBodyType.BINARY, body.getBodyType(), "Message body type didn't match.");
        Assertions.assertNull(body.getValueData(), "MessageBody of binary type has value data.");
        Assertions.assertNull(body.getSequenceData(), "MessageBody of binary type has sequence data.");
        List<byte[]> outputataList = body.getBinaryData();
        Assertions.assertEquals(1, outputataList.size(), "Message body binary data didn't match");
        byte[] outputBinaryData = outputataList.get(0);
        Assertions.assertEquals(binaryData, outputBinaryData, "Message body sequence didn't match");
    }
}

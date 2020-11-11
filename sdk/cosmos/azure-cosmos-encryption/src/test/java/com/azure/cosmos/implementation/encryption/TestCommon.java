// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.guava25.collect.Lists;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.Bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestCommon {

    static <T> T fromStream(byte[] content, Class<T> classType) {
        return Utils.parse(content, classType);
    }

    static byte[] GenerateRandomByteArray() {
        Random random = new Random();
        byte[] b = new byte[10];
        random.nextBytes(b);
        return b;
    }

    public static byte[] EncryptData(byte[] plainText) {
        return Bytes.toArray(Bytes.asList(plainText).stream().map(b -> (byte) b+1).collect(Collectors.toList()));

//        return plainText.Select(b = > (byte) (b + 1)).ToArray();
    }


    public static byte[] DecryptData(byte[] cipherText) {
        return Bytes.toArray(Bytes.asList(cipherText).stream().map(b -> (byte) b-1).collect(Collectors.toList()));
    }
//
//    internal
    public static <T> byte[] ToStream(T input)
    {
        return EncryptionUtils.serializeJsonToByteArray(Utils.getSimpleObjectMapper(), input);
    }
//
//    internal
//    static T FromStream<T>(
//    Stream stream)
//
//    {
//        using(StreamReader sr = new StreamReader(stream))
//        using(JsonReader reader = new JsonTextReader(sr))
//        {
//            JsonSerializer serializer = new JsonSerializer();
//            return serializer.Deserialize < T > (reader);
//        }
//    }
//
//    private static JObject ParseStream(Stream stream) {
//        return JObject.Load(new JsonTextReader(new StreamReader(stream)));
//    }

    public static class TestDoc {
        public static List<String> PathsToEncrypt = ImmutableList.of("/SensitiveStr", "/SensitiveInt");

        @JsonProperty("id")
        public String Id;

        public String PK;

        public String NonSensitive;

        public String SensitiveStr;

        public int SensitiveInt;

        public TestDoc() {
        }

        public TestDoc(TestDoc other) {
            this.Id = other.Id;
            this.PK = other.PK;
            this.NonSensitive = other.NonSensitive;
            this.SensitiveStr = other.SensitiveStr;
            this.SensitiveInt = other.SensitiveInt;
        }


        public static TestDoc Create() {
            return Create(null);
        }

         public static TestDoc Create(String partitionKey) {

            TestDoc testDoc = new TestDoc();
            testDoc.Id = UUID.randomUUID().toString();
            testDoc.PK = partitionKey != null ? partitionKey : UUID.randomUUID().toString();
            testDoc.SensitiveInt = new Random().nextInt();
            testDoc.SensitiveStr = UUID.randomUUID().toString();
            testDoc.NonSensitive = UUID.randomUUID().toString();

            return testDoc;

//        {
//            Id = UUID..ToString(),
//            PK = partitionKey ?? Guid.NewGuid().ToString(),
//            NonSensitive = Guid.NewGuid().ToString(),
//            SensitiveStr = Guid.NewGuid().ToString(),
//            SensitiveInt = new Random().Next()
//        };
        }

    public byte[] ToStream()
    {
        return TestCommon.ToStream(this);
    }
    }
}

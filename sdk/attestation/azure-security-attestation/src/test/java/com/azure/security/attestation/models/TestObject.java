// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class TestObject implements JsonSerializable<TestObject> {
    private String alg;
    private int integer;
    private long expiration;
    private long issuedOn;
    private long notBefore;
    private int[] integerArray;
    String issuer;

    TestObject() {
    }

    public TestObject setAlg(String v) {
        alg = v;
        return this;
    }

    public String getAlg() {
        return alg;
    }

    public TestObject setInteger(int v) {
        integer = v;
        return this;
    }

    public int getInteger() {
        return integer;
    }

    public TestObject setExpiresOn(OffsetDateTime expirationTime) {
        this.expiration = expirationTime.toEpochSecond();
        return this;
    }

    public OffsetDateTime getExpiresOn() {
        return OffsetDateTime.ofInstant(Instant.EPOCH.plusSeconds(expiration), ZoneOffset.UTC);
    }

    public TestObject setIssuedOn(OffsetDateTime issuedOn) {
        this.issuedOn = issuedOn.toEpochSecond();
        return this;
    }

    public OffsetDateTime getIssuedOn() {
        return OffsetDateTime.ofInstant(Instant.EPOCH.plusSeconds(issuedOn), ZoneOffset.UTC);
    }

    public TestObject setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore.toEpochSecond();
        return this;
    }

    public OffsetDateTime getNotBefore() {
        return OffsetDateTime.ofInstant(Instant.EPOCH.plusSeconds(notBefore), ZoneOffset.UTC);
    }

    public TestObject setIntegerArray(int[] v) {
        integerArray = v.clone();
        return this;
    }

    public int[] getIntegerArray() {
        return integerArray;
    }

    public TestObject setIssuer(String iss) {
        issuer = iss;
        return this;
    }

    public String getIssuer() {
        return issuer;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("alg", alg)
            .writeIntField("int", integer)
            .writeLongField("exp", expiration)
            .writeLongField("iat", issuedOn)
            .writeLongField("nbf", notBefore);

        if (integerArray != null) {
            jsonWriter.writeStartArray("intArray");

            for (int value : integerArray) {
                jsonWriter.writeInt(value);
            }

            jsonWriter.writeEndArray();
        }

        return jsonWriter.writeStringField("iss", issuer).writeEndObject();
    }

    public static TestObject fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TestObject testObject = new TestObject();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("alg".equals(fieldName)) {
                    testObject.alg = reader.getString();
                } else if ("int".equals(fieldName)) {
                    testObject.integer = reader.getInt();
                } else if ("exp".equals(fieldName)) {
                    testObject.expiration = reader.getLong();
                } else if ("iat".equals(fieldName)) {
                    testObject.issuedOn = reader.getLong();
                } else if ("nbf".equals(fieldName)) {
                    testObject.notBefore = reader.getLong();
                } else if ("intArray".equals(fieldName)) {
                    List<Integer> intList = reader.readArray(JsonReader::getInt);
                    if (intList != null) {
                        testObject.integerArray = new int[intList.size()];
                        for (int i = 0; i < testObject.integerArray.length; i++) {
                            testObject.integerArray[i] = intList.get(i);
                        }

                    }
                } else if ("iss".equals(fieldName)) {
                    testObject.issuer = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return testObject;
        });
    }
}

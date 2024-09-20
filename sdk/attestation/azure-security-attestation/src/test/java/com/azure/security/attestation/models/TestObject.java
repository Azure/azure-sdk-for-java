// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

final class TestObject {
    @JsonProperty(value = "alg")
    private String alg;

    public TestObject setAlg(String v) {
        alg = v;
        return this;
    }

    public String getAlg() {
        return alg;
    }

    @JsonProperty(value = "int")
    private int integer;

    public TestObject setInteger(int v) {
        integer = v;
        return this;
    }

    public int getInteger() {
        return integer;
    }

    @JsonProperty(value = "exp")
    private long expiration;

    public TestObject setExpiresOn(OffsetDateTime expirationTime) {
        this.expiration = expirationTime.toEpochSecond();
        return this;
    }

    public OffsetDateTime getExpiresOn() {
        return OffsetDateTime.ofInstant(Instant.EPOCH.plusSeconds(expiration), ZoneOffset.UTC);
    }

    @JsonProperty(value = "iat")
    private long issuedOn;

    public TestObject setIssuedOn(OffsetDateTime issuedOn) {
        this.issuedOn = issuedOn.toEpochSecond();
        return this;
    }

    public OffsetDateTime getIssuedOn() {
        return OffsetDateTime.ofInstant(Instant.EPOCH.plusSeconds(issuedOn), ZoneOffset.UTC);
    }

    @JsonProperty(value = "nbf")
    private long notBefore;

    public TestObject setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore.toEpochSecond();
        return this;
    }

    public OffsetDateTime getNotBefore() {
        return OffsetDateTime.ofInstant(Instant.EPOCH.plusSeconds(notBefore), ZoneOffset.UTC);
    }

    @JsonProperty(value = "intArray")
    private int[] integerArray;

    public TestObject setIntegerArray(int[] v) {
        integerArray = v.clone();
        return this;
    }

    public int[] getIntegerArray() {
        return integerArray;
    }

    @JsonProperty(value = "iss")
    String issuer;

    public TestObject setIssuer(String iss) {
        issuer = iss;
        return this;
    }

    public String getIssuer() {
        return issuer;
    }

    TestObject() {

    }
}

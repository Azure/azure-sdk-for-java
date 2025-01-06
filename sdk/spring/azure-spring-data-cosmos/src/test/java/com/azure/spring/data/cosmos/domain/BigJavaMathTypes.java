// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Container(ru = TestConstants.DEFAULT_MINIMUM_RU)
public class BigJavaMathTypes {
    @Id
    @PartitionKey
    private String id;
    private java.math.BigDecimal bigDecimal;
    private java.math.BigInteger bigInteger;

    public BigJavaMathTypes(String id, java.math.BigDecimal bigDecimal, java.math.BigInteger bigInteger) {
        this.id = id;
        this.bigDecimal = bigDecimal;
        this.bigInteger = bigInteger;
    }

    public BigJavaMathTypes() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public java.math.BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(java.math.BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public java.math.BigInteger getBigInteger() {
        return bigInteger;
    }

    public void setBigInteger(java.math.BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BigJavaMathTypes that = (BigJavaMathTypes) o;
        return Objects.equals(id, that.id) && Objects.equals(bigDecimal, that.bigDecimal) && Objects.equals(bigInteger, that.bigInteger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bigDecimal, bigInteger);
    }

    @Override
    public String toString() {
        return "BigJavaMathTypes{" +
            "id='" + id + '\'' +
            ", bigDecimal=" + bigDecimal +
            ", bigInteger=" + bigInteger +
            '}';
    }
}

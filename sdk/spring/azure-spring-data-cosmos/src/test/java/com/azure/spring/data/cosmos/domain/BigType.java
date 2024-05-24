// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.BigInteger;

@Container(ru = TestConstants.DEFAULT_MINIMUM_RU)
public class BigType {

    @Id
    String id;
    @PartitionKey
    String name;
    BigDecimal bigDecimal;
    BigInteger bigInteger;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBigDecimal() {
        return this.bigDecimal;
    }

    public BigInteger getBigInteger() {
        return this.bigInteger;
    }

    public BigType(String id, String bigTypeName, BigDecimal bigDecimal, BigInteger bigInteger) {
        this.id = id;
        this.name = bigTypeName;
        this.bigDecimal = bigDecimal;
        this.bigInteger = bigInteger;
    }

    public BigType() {

    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;

import java.math.BigDecimal;
import java.util.Objects;

@Container(ru = TestConstants.DEFAULT_MINIMUM_RU)
@CosmosIndexingPolicy()
public class ObjectWithBigDecimal {
    private String id;
    private BigDecimal amount;

    public ObjectWithBigDecimal(String id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
    }

    public ObjectWithBigDecimal() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ObjectWithBigDecimal object = (ObjectWithBigDecimal) o;
        return Objects.equals(id, object.id)
            && Objects.equals(amount, object.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount);
    }

    @Override
    public String toString() {
        return "Person{"
            + "id='"
            + id
            + '\''
            + ", amount='"
            + amount
            + '\''
            + '}';
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;

@Container
public class LongIdDomainPartition {

    @Id
    private Long number;

    @PartitionKey
    private String name;

    public LongIdDomainPartition(Long number, String name) {
        this.number = number;
        this.name = name;
    }

    public LongIdDomainPartition() {
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LongIdDomainPartition that = (LongIdDomainPartition) o;
        return Objects.equals(number, that.number)
            && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, name);
    }

    @Override
    public String toString() {
        return "LongIdDomain{"
            + "number="
            + number
            + ", name='"
            + name
            + '\''
            + '}';
    }
}

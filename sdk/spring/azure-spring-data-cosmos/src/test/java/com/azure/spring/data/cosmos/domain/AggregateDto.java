// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

/**
 * Simple DTO for testing non-entity deserialization of GROUP BY aggregate query results.
 * Not annotated with @Container — intentionally a plain POJO (GitHub #43912).
 */
public class AggregateDto {
    private int count;
    private String label;

    public AggregateDto() {
    }

    public AggregateDto(int count, String label) {
        this.count = count;
        this.label = label;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

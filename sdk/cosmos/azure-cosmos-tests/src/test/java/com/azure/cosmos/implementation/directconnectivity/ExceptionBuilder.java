// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExceptionBuilder {
    private Integer status;
    private List<Map.Entry<String, String>> headerEntries;
    private String message;

    public static ExceptionBuilder create() {
        return new ExceptionBuilder();
    }

    public ExceptionBuilder() {
        headerEntries = new ArrayList<>();
    }

    public ExceptionBuilder withHeader(String key, String value) {
        headerEntries.add(new AbstractMap.SimpleEntry<>(key, value));
        return this;
    }

    public ExceptionBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    public ExceptionBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public GoneException asGoneException() {
        assert status == null;
        GoneException dce = new GoneException();
        dce.getResponseHeaders().putAll(headerEntries.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue())));
        return dce;
    }

    public InvalidPartitionException asInvalidPartitionException() {
        assert status == null;
        InvalidPartitionException dce = new InvalidPartitionException();
        dce.getResponseHeaders().putAll(headerEntries.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue())));
        return dce;
    }

    public PartitionKeyRangeGoneException asPartitionKeyRangeGoneException() {
        assert status == null;
        PartitionKeyRangeGoneException dce = new PartitionKeyRangeGoneException();
        dce.getResponseHeaders().putAll(headerEntries.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue())));
        return dce;
    }


    public PartitionKeyRangeIsSplittingException asPartitionKeyRangeIsSplittingException() {
        assert status == null;
        PartitionKeyRangeIsSplittingException dce = new PartitionKeyRangeIsSplittingException();
        dce.getResponseHeaders().putAll(headerEntries.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue())));
        return dce;
    }

    public PartitionIsMigratingException asPartitionIsMigratingException() {
        assert status == null;
        PartitionIsMigratingException dce = new PartitionIsMigratingException();
        dce.getResponseHeaders().putAll(headerEntries.stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue())));
        return dce;
    }
}

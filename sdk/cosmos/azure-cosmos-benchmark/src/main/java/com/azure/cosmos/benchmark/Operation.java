// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.util.Arrays;

/**
 * Benchmark operation types. Defines the workload an {@link AsyncBenchmark}
 * or {@link SyncBenchmark} will execute.
 */
public enum Operation {
    ReadThroughput,
    WriteThroughput,
    ReadLatency,
    WriteLatency,
    QueryInClauseParallel,
    QueryCross,
    QuerySingle,
    QuerySingleMany,
    QueryParallel,
    QueryOrderby,
    QueryAggregate,
    QueryAggregateTopOrderby,
    QueryTopOrderby,
    Mixed,
    ReadMyWrites,
    CtlWorkload,
    ReadAllItemsOfLogicalPartition,
    LinkedInCtlWorkload,
    ReadManyLatency,
    ReadManyThroughput;

    /**
     * Case-insensitive lookup by name.
     * @return the matching Operation, or null if not found
     */
    public static Operation fromString(String code) {
        if (code == null) return null;
        for (Operation op : Operation.values()) {
            if (op.name().equalsIgnoreCase(code)) {
                return op;
            }
        }
        return null;
    }

    /**
     * JCommander converter for CLI parsing.
     */
    public static class OperationTypeConverter implements IStringConverter<Operation> {
        @Override
        public Operation convert(String value) {
            Operation ret = fromString(value);
            if (ret == null) {
                throw new ParameterException("Value " + value + " cannot be converted to Operation. "
                    + "Available values are: " + Arrays.toString(Operation.values()));
            }
            return ret;
        }
    }
}

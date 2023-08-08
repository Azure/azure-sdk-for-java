// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.aggregation;

import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.Utils;

import java.io.IOException;

public class AverageAggregator implements Aggregator {
    private AverageInfo averageInfo;

    public AverageAggregator() {
        this.averageInfo = new AverageInfo();
    }

    @Override
    public void aggregate(Object item) {
        AverageInfo averageInfo;
        try {
            averageInfo = Utils.getSimpleObjectMapper().readValue(item.toString(), AverageInfo.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize aggregate result");
        }
        this.averageInfo.add(averageInfo);
    }

    @Override
    public Object getResult() {
        return this.averageInfo.getAverage();
    }

    private static class AverageInfo {
        public Double sum;
        public long count;

        public void add(AverageInfo other) {
            if (other == null) {
                throw new IllegalArgumentException("other");
            }
            if (other.sum == null) {
                return;
            }
            if (this.sum == null) {
                this.sum = 0.0;
            }

            this.sum += other.sum;
            this.count += other.count;
        }

        Object getAverage() {
            if (this.sum == null || this.count <= 0) {
                return Undefined.value();
            }
            return this.sum / this.count;
        }
    }
}

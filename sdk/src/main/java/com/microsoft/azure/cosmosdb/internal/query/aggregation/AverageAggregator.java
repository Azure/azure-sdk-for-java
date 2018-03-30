/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.query.aggregation;

import java.io.IOException;

import com.microsoft.azure.cosmosdb.Undefined;
import com.microsoft.azure.cosmosdb.internal.Utils;

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
                return Undefined.Value();
            }
            return this.sum / this.count;
        }
    }
}

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
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos database.
 */
public class CosmosDatabaseRequestOptions extends CosmosRequestOptions{
    private Integer offerThroughput;

    /**
     * Gets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @return the throughput value.
     */
    public Integer getOfferThroughput() {
        return offerThroughput;
    }

    /**
     * Sets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @param offerThroughput the throughput value.
     * @return the current request options
     */
    public CosmosDatabaseRequestOptions offerThroughput(Integer offerThroughput) {
        this.offerThroughput = offerThroughput;
        return this;
    }

    @Override
    protected RequestOptions toRequestOptions() {
        super.toRequestOptions();
        requestOptions.setOfferThroughput(offerThroughput);
        return requestOptions;
    }
}
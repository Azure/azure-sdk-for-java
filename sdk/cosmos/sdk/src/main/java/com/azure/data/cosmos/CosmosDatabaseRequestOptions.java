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
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;

/**
 * Encapsulates options that can be specified for a request issued to cosmos database.
 */
public class CosmosDatabaseRequestOptions{
    private Integer offerThroughput;
    private AccessCondition accessCondition;

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition accessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosDatabaseRequestOptions accessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    /**
     * Gets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @return the throughput value.
     */
    Integer offerThroughput() {
        return offerThroughput;
    }

    /**
     * Sets the throughput in the form of Request Units per second when creating a cosmos database.
     *
     * @param offerThroughput the throughput value.
     * @return the current request options
     */
    CosmosDatabaseRequestOptions offerThroughput(Integer offerThroughput) {
        this.offerThroughput = offerThroughput;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions options = new RequestOptions();
        options.setAccessCondition(accessCondition);
        options.setOfferThroughput(offerThroughput);
        return options;
    }
}
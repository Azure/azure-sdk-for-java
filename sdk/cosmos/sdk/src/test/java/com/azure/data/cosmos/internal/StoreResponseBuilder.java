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

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoreResponseBuilder {
    private int status;
    private List<Map.Entry<String, String>> headerEntries;
    private String content;

    public static StoreResponseBuilder create() {
        return new StoreResponseBuilder();
    }

    public StoreResponseBuilder() {
        headerEntries = new ArrayList<>();
    }

    public StoreResponseBuilder withHeader(String key, String value) {
        headerEntries.add(new AbstractMap.SimpleEntry(key, value));
        return this;
    }

    public StoreResponseBuilder withLSN(long lsn) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.LSN, Long.toString(lsn)));
        return this;
    }

    public StoreResponseBuilder withRequestCharge(BigDecimal requestCharge) {
        withRequestCharge(requestCharge.doubleValue());
        return this;
    }

    public StoreResponseBuilder withRequestCharge(double requestCharge) {
        headerEntries.add(new AbstractMap.SimpleEntry(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge)));
        return this;
    }

    public StoreResponseBuilder withLocalLSN(long localLsn) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(localLsn)));
        return this;
    }

    public StoreResponseBuilder withPartitionKeyRangeId(String partitionKeyRangeId) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId));
        return this;
    }

    public StoreResponseBuilder withItemLocalLSN(long itemLocalLsn) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.ITEM_LOCAL_LSN, Long.toString(itemLocalLsn)));
        return this;
    }

    public StoreResponseBuilder withQuorumAckecdLsn(long quorumAckecdLsn) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, Long.toString(quorumAckecdLsn)));
        return this;
    }

    public StoreResponseBuilder withQuorumAckecdLocalLsn(long quorumAckecdLocalLsn) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(quorumAckecdLocalLsn)));
        return this;
    }

    public StoreResponseBuilder withGlobalCommittedLsn(long globalCommittedLsn) {
        headerEntries.add(new AbstractMap.SimpleEntry(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn)));
        return this;
    }

    public StoreResponseBuilder withSessionToken(String sessionToken) {
        headerEntries.add(new AbstractMap.SimpleEntry(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken));
        return this;
    }

    public StoreResponseBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    public StoreResponseBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public StoreResponse build() {
        return new StoreResponse(status, headerEntries, content);
    }
}
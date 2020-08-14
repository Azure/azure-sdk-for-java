// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;

public class StoreResponseBuilder {
    private int status;
    private String content;
    private Map<String, String> headerEntries;

    public static StoreResponseBuilder create() {
        return new StoreResponseBuilder();
    }

    public StoreResponseBuilder() {

        headerEntries = new HashMap<>();
    }

    public StoreResponseBuilder withHeader(String key, String value) {
        headerEntries.put(key, value);
        return this;
    }

    public StoreResponseBuilder withLSN(long lsn) {
        headerEntries.put(WFConstants.BackendHeaders.LSN, Long.toString(lsn));
        return this;
    }

    public StoreResponseBuilder withRequestCharge(BigDecimal requestCharge) {
        withRequestCharge(requestCharge.doubleValue());
        return this;
    }

    public StoreResponseBuilder withRequestCharge(double requestCharge) {
        headerEntries.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, Double.toString(requestCharge));
        return this;
    }

    public StoreResponseBuilder withLocalLSN(long localLsn) {
        headerEntries.put(WFConstants.BackendHeaders.LOCAL_LSN, Long.toString(localLsn));
        return this;
    }

    public StoreResponseBuilder withPartitionKeyRangeId(String partitionKeyRangeId) {
        headerEntries.put(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
        return this;
    }

    public StoreResponseBuilder withItemLocalLSN(long itemLocalLsn) {
        headerEntries.put(WFConstants.BackendHeaders.ITEM_LOCAL_LSN, Long.toString(itemLocalLsn));
        return this;
    }

    public StoreResponseBuilder withQuorumAckecdLsn(long quorumAckecdLsn) {
        headerEntries.put(WFConstants.BackendHeaders.QUORUM_ACKED_LSN, Long.toString(quorumAckecdLsn));
        return this;
    }

    public StoreResponseBuilder withQuorumAckecdLocalLsn(long quorumAckecdLocalLsn) {
        headerEntries.put(WFConstants.BackendHeaders.QUORUM_ACKED_LOCAL_LSN, Long.toString(quorumAckecdLocalLsn));
        return this;
    }

    public StoreResponseBuilder withGlobalCommittedLsn(long globalCommittedLsn) {
        headerEntries.put(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, Long.toString(globalCommittedLsn));
        return this;
    }

    public StoreResponseBuilder withSessionToken(String sessionToken) {
        headerEntries.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
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
        return new StoreResponse(status, headerEntries, getUTF8BytesOrNull(content));
    }
}

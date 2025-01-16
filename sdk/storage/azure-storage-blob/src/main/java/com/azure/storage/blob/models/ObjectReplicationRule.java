// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * A type that contains information about an object replication rule on a source blob.
 */
public class ObjectReplicationRule {

    private final String ruleId;
    private final ObjectReplicationStatus status;

    /**
     * Constructs a new ObjectReplicationRule object.
     * @param ruleId The rule id.
     * @param status The {@link ObjectReplicationStatus}
     */
    public ObjectReplicationRule(String ruleId, ObjectReplicationStatus status) {
        this.ruleId = ruleId;
        this.status = status;
    }

    /**
     * @return The rule id.
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * @return The {@link ObjectReplicationStatus}
     */
    public ObjectReplicationStatus getStatus() {
        return status;
    }
}

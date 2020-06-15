// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.List;
import java.util.Optional;

/**
 * A type that contains information about an object replication rule on a source blob.
 */
public class ObjectReplicationRule {

    private final String ruleId;
    private final ObjectReplicationStatus status;

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

    static ObjectReplicationRule getObjectReplicationRule(String ruleId,
        List<ObjectReplicationRule> objectReplicationRules) {
        Optional<ObjectReplicationRule> r = objectReplicationRules.stream()
            .filter(rule -> ruleId.equals(rule.getRuleId()))
            .findFirst();
        return r.orElse(null);
    }
}

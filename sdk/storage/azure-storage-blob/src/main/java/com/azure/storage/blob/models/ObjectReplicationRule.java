// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.List;

/**
 * A type that contains information about an object replication rule on a source blob.
 */
public class ObjectReplicationRule {

    private final String ruleId;
    private final ObjectReplicationStatus status;


    ObjectReplicationRule(String ruleId, ObjectReplicationStatus status) {
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

    static int getIndexOfObjectReplicationRule(String ruleId,
        List<ObjectReplicationRule> objectReplicationRules) {
        for (int i = 0; i < objectReplicationRules.size(); i++) {
            if (ruleId.equals(objectReplicationRules.get(i).getRuleId())) {
                return i;
            }
        }
        return -1;
    }
}

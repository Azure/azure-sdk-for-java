// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.ArrayList;
import java.util.List;

/**
 * A type that contains information about an object replication policy on a source blob.
 */
public class ObjectReplicationPolicy {

    private final String policyId;
    private final List<ObjectReplicationRule> objectReplicationRules;

    ObjectReplicationPolicy(String policyId) {
        this.policyId = policyId;
        this.objectReplicationRules = new ArrayList<>();
    }

    void putRule(ObjectReplicationRule rule) {
        this.objectReplicationRules.add(rule);
    }

    /**
     * @return The policy id.
     */
    public String getPolicyId() {
        return policyId;
    }

    /**
     * @return A {@code List} of rules associated with this policy to the status of the replication associated with that
     * rule.
     */
    public List<ObjectReplicationRule> getRules() {
        return this.objectReplicationRules;
    }

    static int getIndexOfObjectReplicationPolicy(String policyId,
        List<ObjectReplicationPolicy> objectReplicationPolicies) {
        for (int i = 0; i < objectReplicationPolicies.size(); i++) {
            if (policyId.equals(objectReplicationPolicies.get(i).getPolicyId())) {
                return i;
            }
        }
        return -1;
    }
}

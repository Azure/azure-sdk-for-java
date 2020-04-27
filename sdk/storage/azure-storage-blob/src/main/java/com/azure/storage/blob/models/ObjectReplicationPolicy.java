// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.HashMap;
import java.util.Map;

/**
 * A type that contains information about an object replication policy on a source blob.
 */
public class ObjectReplicationPolicy {

    private final String policyId;
    private final Map<String, String> ruleStatuses;

    ObjectReplicationPolicy(String policyId) {
        this.policyId = policyId;
        this.ruleStatuses = new HashMap<>();
    }

    void putRuleAndStatus(String rule, String status) {
        this.ruleStatuses.put(rule, status);
    }

    /**
     * @return The policy id.
     */
    public String getPolicyId() {
        return policyId;
    }

    /**
     * @return A {@code Map} of rules associated with this policy to the status of the replication associated with that
     * rule.
     */
    public Map<String, String> getRules() {
        return this.ruleStatuses;
    }
}

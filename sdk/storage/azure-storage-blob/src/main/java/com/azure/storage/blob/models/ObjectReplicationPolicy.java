// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A type that contains information about an object replication policy on a source blob.
 */
public class ObjectReplicationPolicy {

    private String policyId;
    private Map<String, String> ruleStatuses;

    ObjectReplicationPolicy(String policyId) {
        this.policyId = policyId;
        this.ruleStatuses = new HashMap<>();
    }

    void putRuleAndStatus(String rule, String status) {
        this.ruleStatuses.put(rule, status);
    }

    public Map<String, String> getRules() {
        return this.ruleStatuses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectReplicationPolicy that = (ObjectReplicationPolicy) o;
        return policyId.equals(that.policyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyId);
    }
}

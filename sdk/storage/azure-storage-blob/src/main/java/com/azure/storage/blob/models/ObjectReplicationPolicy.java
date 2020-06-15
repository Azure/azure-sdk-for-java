// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A type that contains information about an object replication policy on a source blob.
 */
public class ObjectReplicationPolicy {

    private final String policyId;
    private final List<ObjectReplicationRule> objectReplicationRules;

    public ObjectReplicationPolicy(String policyId, List<ObjectReplicationRule> rules) {
        this.policyId = policyId;
        this.objectReplicationRules = Collections.unmodifiableList(rules);
    }

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
        return Collections.unmodifiableList(this.objectReplicationRules);
    }

    static ObjectReplicationPolicy getObjectReplicationPolicy(String policyId,
        List<ObjectReplicationPolicy> objectReplicationPolicies) {
        Optional<ObjectReplicationPolicy> p = objectReplicationPolicies.stream()
            .filter(policy -> policyId.equals(policy.getPolicyId()))
            .findFirst();
        return p.orElse(null);
    }
}

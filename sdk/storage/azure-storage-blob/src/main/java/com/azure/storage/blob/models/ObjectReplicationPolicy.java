// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A type that contains information about an object replication policy on a source blob.
 */
@Immutable
public class ObjectReplicationPolicy {

    private final String policyId;
    private final List<ObjectReplicationRule> objectReplicationRules;

    /**
     * Constructs a new ObjectReplicationPolicy object.
     * @param policyId The policy id
     * @param rules A {@code List} of rules associated with this policy to the status of the replication associated
     * with that rule.
     */
    public ObjectReplicationPolicy(String policyId, List<ObjectReplicationRule> rules) {
        this.policyId = policyId;
        this.objectReplicationRules = Collections.unmodifiableList(new ArrayList<>(rules));
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
}

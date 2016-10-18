/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.PolicyAssignment;
import com.microsoft.azure.management.resources.PolicyDefinition;
import com.microsoft.azure.management.resources.PolicyType;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

/**
 * Implementation for {@link PolicyAssignment}.
 */
final class PolicyAssignmentImpl extends
        IndexableWrapperImpl<PolicyAssignmentInner>
        implements
        PolicyAssignment {

    PolicyAssignmentImpl(PolicyAssignmentInner innerModel) {
        super(innerModel);
    }

    @Override
    public String displayName() {
        return inner().displayName();
    }

    @Override
    public String policyDefinitionId() {
        return inner().policyDefinitionId();
    }

    @Override
    public String scope() {
        return inner().scope();
    }
}

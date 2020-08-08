// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerservice.fluent.inner.ManagedClusterUpgradeProfileInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** The result of checking for the Kubernetes cluster's upgrade profile. */
@Fluent()
public interface KubernetesClusterUpgradeProfile extends HasInner<ManagedClusterUpgradeProfileInner> {
    /** @return the ID of the Kubernetes cluster upgrade profile */
    String id();

    /** @return the name of the Kubernetes cluster upgrade profile */
    String name();

    /** @return the type of the Kubernetes cluster upgrade profile. */
    String type();
}

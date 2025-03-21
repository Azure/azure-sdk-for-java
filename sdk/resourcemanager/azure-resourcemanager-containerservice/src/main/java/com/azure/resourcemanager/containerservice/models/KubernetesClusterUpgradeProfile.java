// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerservice.fluent.models.ManagedClusterUpgradeProfileInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** The result of checking for the Kubernetes cluster's upgrade profile. */
@Fluent()
public interface KubernetesClusterUpgradeProfile extends HasInnerModel<ManagedClusterUpgradeProfileInner> {
    /**
     * Gets the ID of the Kubernetes cluster upgrade profile.
     *
     * @return the ID of the Kubernetes cluster upgrade profile
     */
    String id();

    /**
     * Gets the name of the Kubernetes cluster upgrade profile.
     *
     * @return the name of the Kubernetes cluster upgrade profile
     */
    String name();

    /**
     * Gets the type of the Kubernetes cluster upgrade profile.
     *
     * @return the type of the Kubernetes cluster upgrade profile.
     */
    String type();
}
